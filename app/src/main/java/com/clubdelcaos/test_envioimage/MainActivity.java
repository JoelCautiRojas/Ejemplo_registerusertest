package com.clubdelcaos.test_envioimage;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import cz.msebera.android.httpclient.Header;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    ProgressDialog barprog;
    LinearLayout l;
    EditText etnombres, etapellido1, etapellido2, etemail, etpassword, etalias, etfecha, etpregunta, etrespuesta, etruc, etdni, ettelefono;
    ImageButton btnfotografia;
    Button enviar;

    File fotografia_archivo;
    String fotografia_ruta;
    Bitmap fotografia_bitmap;
    Uri path;

    String APP_DIRECTORY    = "MyAppFerreteria/";
    String MEDIA_DIRECTORY  = APP_DIRECTORY + "MyPicture";
    String BASE_URL         = "http://104.236.60.119/admincomunityapp/appregisteruser";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        l = (LinearLayout) findViewById(R.id.milayout);
        etnombres = (EditText) findViewById(R.id.nombres);
        etapellido1 = (EditText) findViewById(R.id.apellido1);
        etapellido2 = (EditText) findViewById(R.id.apellido2);
        ettelefono = (EditText) findViewById(R.id.telefono);
        etemail = (EditText) findViewById(R.id.email);
        etpassword = (EditText) findViewById(R.id.password);
        etalias = (EditText) findViewById(R.id.alias);
        etfecha = (EditText) findViewById(R.id.fecha);
        etpregunta = (EditText) findViewById(R.id.pregunta);
        etrespuesta = (EditText) findViewById(R.id.respuesta);
        etruc = (EditText) findViewById(R.id.ruc);
        etdni = (EditText) findViewById(R.id.dni);
        btnfotografia = (ImageButton) findViewById(R.id.fotografia);
        enviar = (Button) findViewById(R.id.enviar);
        enviar.setEnabled(false);
        btnfotografia.setEnabled(false);
        if(verificacionPermisos())
        {
            iniciarActivity();
        }
        else
        {
            solicitarPermisos();
        }
    }

    private boolean verificacionPermisos() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            return true;
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        return false;
    }

    private void solicitarPermisos() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, CAMERA)
                || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, READ_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, WRITE_EXTERNAL_STORAGE))
        {
            Snackbar.make(l,"Te has olvidado de los permisos.",Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                            CAMERA,
                            READ_EXTERNAL_STORAGE,
                            WRITE_EXTERNAL_STORAGE
                    },100);
                }
            }).show();
        }
        else
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    CAMERA,
                    READ_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_STORAGE
            },100);
        }
    }

    private void iniciarActivity() {
        enviar.setEnabled(true);
        btnfotografia.setEnabled(true);
        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombres = etnombres.getText().toString();
                String apellido1 = etapellido1.getText().toString();
                String apellido2 = etapellido2.getText().toString();
                String telefono = ettelefono.getText().toString();
                String email = etemail.getText().toString();
                String password = etpassword.getText().toString();
                String alias = etalias.getText().toString();
                String fecha = etfecha.getText().toString();
                String pregunta = etpregunta.getText().toString();
                String respuesta = etrespuesta.getText().toString();
                String ruc = etruc.getText().toString();
                String dni = etdni.getText().toString();

                barprog = new ProgressDialog(MainActivity.this);
                barprog.setCancelable(false);
                barprog.setMessage("Cargando...");
                barprog.setMax(100);
                barprog.setProgress(0);
                barprog.show();
                //--------------------------------------------------------------------
                AsyncHttpClient cliente = new AsyncHttpClient();
                RequestParams datos = new RequestParams();
                datos.put("nombres",nombres);
                datos.put("apellido1",apellido1);
                datos.put("apellido2",apellido2);
                datos.put("telefono",telefono);
                datos.put("email",email);
                datos.put("password",password);
                datos.put("alias",alias);
                datos.put("fecha_nacimiento",fecha);
                datos.put("id_pregunta",pregunta);
                datos.put("respuesta",respuesta);
                datos.put("ruc",ruc);
                datos.put("dni",dni);
                try {
                    datos.put("fotografia",fotografia_archivo);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"ERROR, no se encuentra el archivo de fotografia.",Toast.LENGTH_LONG).show();
                }
                cliente.post(getApplicationContext(), BASE_URL, datos, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        barprog.dismiss();
                        String cadena = new String(responseBody);
                        Toast.makeText(getApplicationContext(),"Status: "+statusCode+" Respuesta JSON: "+cadena,Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        barprog.dismiss();
                        Toast.makeText(getApplicationContext(),"ERROR sin conexion al servidor",Toast.LENGTH_LONG).show();
                    }
                });
                //----------------------------------------------------------------------
            }
        });
        btnfotografia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capturarImagen();
            }
        });
    }

    private void capturarImagen()
    {
        final CharSequence[] option = {"Tomar Foto","Elegir de Galeria","Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Elige una opcion");
        builder.setItems(option, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                if(option[which]=="Tomar Foto")
                {
                    File carpeta =  new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
                    boolean isDirectoryCreated = carpeta.exists();
                    if(!isDirectoryCreated)
                    {
                        isDirectoryCreated = carpeta.mkdirs();
                    }
                    if(isDirectoryCreated)
                    {
                        Long timestamp = System.currentTimeMillis() / 1000;
                        String imageName = timestamp.toString()+".jpg";
                        fotografia_ruta = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY + File.separator + imageName;
                        fotografia_archivo = new File(fotografia_ruta);
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fotografia_archivo));
                        startActivityForResult(intent,100);
                    }
                }
                else if(option[which]=="Elegir de Galeria")
                {
                    Intent intent =  new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent,"Selecciona app de imagen"),200);
                }
                else
                {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 100:
                    MediaScannerConnection.scanFile(this, new String[]{fotografia_ruta}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {

                        }
                    });
                    ContentResolver cr = this.getContentResolver();
                    try {
                        try {
                            fotografia_bitmap = MediaStore.Images.Media.getBitmap(cr, Uri.fromFile(fotografia_archivo));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int rotate = 0;
                        ExifInterface exif = new ExifInterface(fotografia_archivo.getAbsolutePath());
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        switch (orientation) {
                            case ExifInterface.ORIENTATION_ROTATE_90:
                                rotate = 90;
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_180:
                                rotate = 180;
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_270:
                                rotate = 270;
                                break;
                        }
                        Matrix matriz = new Matrix();
                        matriz.postRotate(rotate);
                        fotografia_bitmap = Bitmap.createBitmap(fotografia_bitmap, 0, 0, fotografia_bitmap.getWidth(), fotografia_bitmap.getHeight(), matriz, true);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    btnfotografia.setImageBitmap(fotografia_bitmap);
                    break;
                case 200:
                    path = data.getData();
                    String pathSegment[] = path.getLastPathSegment().split(":");
                    String id = pathSegment[0];
                    final String[] imageColumns = {MediaStore.Images.Media.DATA};
                    final String imageOrderBy = null;
                    String state = Environment.getExternalStorageState();
                    if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
                        path = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
                    } else {
                        path = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    }
                    Cursor imageCursor = getContentResolver().query(path, imageColumns, MediaStore.Images.Media._ID + "=" + id, null, imageOrderBy);
                    if (imageCursor.moveToFirst()) {
                        fotografia_ruta = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    }
                    fotografia_archivo = new File(fotografia_ruta);
                    ContentResolver crv = this.getContentResolver();
                    fotografia_bitmap = null;
                    try {
                        fotografia_bitmap = android.provider.MediaStore.Images.Media.getBitmap(crv, Uri.fromFile(fotografia_archivo));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    btnfotografia.setImageBitmap(fotografia_bitmap);
                    break;
            }
        }
    }


    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults)
    {
        switch(requestCode)
        {
            case 100:
                if(grantResults.length == 0
                        || grantResults[0] == PackageManager.PERMISSION_DENIED
                        || grantResults[1] == PackageManager.PERMISSION_DENIED
                        || grantResults[2] == PackageManager.PERMISSION_DENIED)
                {
                    // Este codigo solo se activa si se ah rechazado alguna peticion
                    // Codigo Opcional para abrir la configuracion de la aplicacion
                    AlertDialog.Builder ventana = new AlertDialog.Builder(MainActivity.this);
                    ventana.setTitle("Permisos Negados");
                    ventana.setMessage("Necesitas otorgar los Permisos");
                    ventana.setPositiveButton("Aceptar",new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent configuracion = new Intent();
                            configuracion.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri direccion = Uri.fromParts("package",getPackageName(),null);
                            configuracion.setData(direccion);
                            startActivity(configuracion);
                        }
                    });
                    ventana.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    ventana.show();
                }
                else
                {
                    iniciarActivity();
                }
                break;
        }
    }
}
