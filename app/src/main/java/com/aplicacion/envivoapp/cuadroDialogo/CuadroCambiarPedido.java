package com.aplicacion.envivoapp.cuadroDialogo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activityParaClientes.DataCliente;
import com.aplicacion.envivoapp.activityParaClientes.ListarStreamingsVendedor;
import com.aplicacion.envivoapp.activityParaClientes.MensajeriaCliente;
import com.aplicacion.envivoapp.activitysParaVendedores.PedidoVendedor;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.facebook.gamingservices.GamingGroupIntegration;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.internal.Util;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CuadroCambiarPedido extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    String currentPhotoPath;
    private EncriptacionDatos encrypt= new EncriptacionDatos();
    private final int PICKER = 1; //variable para seleccionador de imagenes
    static final int REQUEST_IMAGE_CAPTURE = 2; //variable para foto tomada con la camara
    static final int REQUEST_TAKE_PHOTO = 1;
    private Uri fotoTomada=null;

    private Bitmap bitmapImagenPedido = null;
    private FirebaseStorage  storage;
    private ImageView imagenPedido;
    private  String idPedido;
    private Pedido pedido;
    private EditText nombreProducto;
    private EditText cantidadProducto;
    private EditText codigoProducto ;
    private EditText precioProducto;
    private EditText descripcionProducto;
    private Button cambiarPedido;
    private Button cancelarProducto;
    private Button tomarFoto;
    private Button seleccionarImagen ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.cuadro_aceptar_pedido_mensaje_cliente);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        storage = FirebaseStorage.getInstance();

        //incializamos las variables
         nombreProducto = findViewById(R.id.txtNombreProductoCuadroAceptar);
         cantidadProducto = findViewById(R.id.txtCantidadPedidoCuadroAceptar);
         codigoProducto = findViewById(R.id.txtCodigoPedidoCuadroAceptar);
         precioProducto = findViewById(R.id.txtPrecioCuadroAceptarPedido);
         descripcionProducto = findViewById(R.id.txtDescripcionCuadroPedido);
         cambiarPedido = findViewById(R.id.btnAceptarPedidoCuadroAceptarPedido);
         cancelarProducto = findViewById(R.id.btnCancelarCuadroAceptarPedido);
         tomarFoto = findViewById(R.id.btnTomarFoto);
         seleccionarImagen  = findViewById(R.id.btnSeleccionarImagen);
        imagenPedido = findViewById(R.id.imagenPedidoCuadroAceptarPedidoMensajeCliente);

        imagenPedido.setVisibility(View.GONE);

        Bundle vendedor = getIntent().getExtras();
        idPedido = vendedor.getString("idPedido"); //recogemos los datos del vendedor


        databaseReference.child("Pedido").child(idPedido).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                if (snapshot.exists()){
                    pedido = snapshot.getValue(Pedido.class);
                    try {
                        nombreProducto.setText(encrypt.desencriptar(pedido.getNombreProducto()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cantidadProducto.setText(pedido.getCantidadProducto()+"");
                    try {
                        codigoProducto.setText(encrypt.desencriptar(pedido.getCodigoProducto()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    precioProducto.setText(pedido.getPrecioProducto()+"");
                    try {
                        descripcionProducto.setText(encrypt.desencriptar(pedido.getDescripcionProducto()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (pedido.getImagen()!=null){
                        try {
                            pedido.setImagen(encrypt.desencriptar(pedido.getImagen()));
                            imagenPedido.setVisibility(View.VISIBLE);
                            storage.getReference().child(pedido.getImagen()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.with(CuadroCambiarPedido.this).load(uri).into(imagenPedido);
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        });


        seleccionarImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmapImagenPedido = null;
                selectorImagenes();
            }
        });

        tomarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmapImagenPedido = null;
                fotoTomada = null;
                dispatchTakePictureIntent();
            }
        });
        Utilidades util = new Utilidades();
        Dialog dialogCargando = util.dialogCargar(CuadroCambiarPedido.this);

        cambiarPedido.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                //activamos el cuadro de dialogo
                dialogCargando.show();
                Map<String,Object> pedidoActualizacion= new HashMap<>();
                try {
                    pedidoActualizacion.put("nombreProducto",encrypt.encriptar(nombreProducto.getText().toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pedidoActualizacion.put("cantidadProducto",Integer.parseInt(cantidadProducto.getText().toString()));
                try {
                    pedidoActualizacion.put("codigoProducto",encrypt.encriptar(codigoProducto.getText().toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pedidoActualizacion.put("precioProducto",Double.parseDouble(precioProducto.getText().toString()));
                try {
                    pedidoActualizacion.put("descripcionProducto",encrypt.encriptar(descripcionProducto.getText().toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //creamos el mensaje
                Mensaje  mensaje = new Mensaje();
                String idMensaje = databaseReference.push().getKey();
                mensaje.setIdMensaje(idMensaje);
                mensaje.setIdStreaming(pedido.getIdStreaming());
                mensaje.setIdvendedor(pedido.getIdVendedor());
                mensaje.setIdcliente(pedido.getIdCliente());
                mensaje.setPedidoAceptado(true);
                mensaje.setPedidoCancelado(false);
                mensaje.setEsVededor(true);
                mensaje.setPedidoCancelado(false);
                mensaje.setTexto("Su pedido ha sido cambiado porfavor revise su carrito de compra");

                //creamos el date en base la hora actual
                LocalDateTime tiempoActual = LocalDateTime.now();//obtenemos la fecha actual
                Date fecha = new Date();
                fecha.setDate(tiempoActual.getDayOfMonth());
                fecha.setMonth(tiempoActual.getMonth().getValue());
                fecha.setYear(tiempoActual.getYear());
                fecha.setHours(tiempoActual.getHour());
                fecha.setMinutes(tiempoActual.getMinute());
                fecha.setSeconds(tiempoActual.getSecond());

                mensaje.setFecha(fecha);


                if (bitmapImagenPedido!=null){
                    StorageReference storageRef = storage.getReference().child(idMensaje);//creamos la referencia para subir datos
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmapImagenPedido.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] data = baos.toByteArray();

                    bitmapImagenPedido = null;

                    UploadTask uploadTask = storageRef.putBytes(data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(getApplicationContext(),"Error al subir imagen",Toast.LENGTH_LONG).show();
                            dialogCargando.dismiss();
                            Log.e("ImagenError","Error al cargar la imagen",exception);
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            try {
                                pedidoActualizacion.put("imagen",encrypt.encriptar(storageRef.getPath()));
                                databaseReference.child("Mensaje").child(idMensaje).setValue(mensaje);
                                databaseReference.child("Pedido").child(pedido.getIdPedido()).updateChildren(pedidoActualizacion).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            dialogCargando.dismiss();
                                            iniciarActividadPedido();
                                            Toast.makeText(CuadroCambiarPedido.this,"Pedido actualizado con éxito",Toast.LENGTH_LONG).show();
                                        }else{
                                            dialogCargando.dismiss();
                                            Toast.makeText(CuadroCambiarPedido.this,"No se pudo actualizar el pedido",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }else{
                    databaseReference.child("Mensaje").child(idMensaje).setValue(mensaje);
                    databaseReference.child("Pedido").child(pedido.getIdPedido()).updateChildren(pedidoActualizacion).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                dialogCargando.dismiss();
                                iniciarActividadPedido();
                                Toast.makeText(CuadroCambiarPedido.this,"Pedido actualizado con éxito",Toast.LENGTH_LONG).show();
                            }else{
                                dialogCargando.dismiss();
                                Toast.makeText(CuadroCambiarPedido.this,"No se pudo actualizar el pedido",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

            }
        });

        cancelarProducto.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                iniciarActividadPedido();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICKER:
                if (resultCode == RESULT_OK) {
                    if (data!=null) {
                        Uri pathArchivo = data.getData(); //Obtenemos el uri de la imagen seleccionada
                        if (pathArchivo != null) {
                            if (!pathArchivo.equals("")) {
                                try {
                                    imagenPedido.setVisibility(View.VISIBLE);
                                    Bitmap bitImagen = getBitmapFromUri(pathArchivo);
                                    bitmapImagenPedido = bitImagen;
                                    Picasso.with(getApplicationContext()).load(pathArchivo).into(imagenPedido);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }else{
                        if (fotoTomada!=null) {
                            try {
                                Bitmap bitImagen = getBitmapFromUri(fotoTomada);
                                imagenPedido.setVisibility(View.VISIBLE);
                                bitmapImagenPedido = bitImagen;
                                Picasso.with(getApplicationContext()).load(fotoTomada).into(imagenPedido);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
                break;
        }


    }

    private  void selectorImagenes(){//inicia el cuadro de dialogo para seleccionar una imagen
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {

            startActivityForResult(Intent.createChooser(intent,"Seleccione la captura de pantalla"),1);
        }catch (android.content.ActivityNotFoundException ex){
            Toast.makeText(this,"Por favor instale un administrados de archivos",Toast.LENGTH_SHORT).show();
        }

    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException { //obtenemos un bitmap de la ruta real del archivo
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private void iniciarActividadPedido(){
        Intent pedidoVendedorIntent = new Intent(CuadroCambiarPedido.this, PedidoVendedor.class);
        CuadroCambiarPedido.this.startActivity(pedidoVendedorIntent);
        CuadroCambiarPedido.this.finish();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                        "com.aplicacion.envivoapp",
                        photoFile);
                fotoTomada = photoURI;
                Picasso.with(getApplicationContext()).load(photoURI).into(imagenPedido);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File nuevaCarpeta = new File(getExternalFilesDir(null), "/Pictures");
        if (!nuevaCarpeta.exists()) {
            nuevaCarpeta.mkdirs();
        }

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                nuevaCarpeta      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }




}
