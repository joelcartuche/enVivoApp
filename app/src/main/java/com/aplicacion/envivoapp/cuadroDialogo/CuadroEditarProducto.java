package com.aplicacion.envivoapp.cuadroDialogo;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activitysParaVendedores.fragmentos.FragmentoPedidoVendedor;
import com.aplicacion.envivoapp.activitysParaVendedores.fragmentos.GestionProducto.FragmentoListarProductos;
import com.aplicacion.envivoapp.activitysParaVendedores.fragmentos.GestionProducto.FragmentoListarProductosSinImagne;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Notificacion;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Producto;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class CuadroEditarProducto extends Fragment {
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    String currentPhotoPath;

    private EncriptacionDatos encrypt= new EncriptacionDatos();
    private final int PICKER = 1; //variable para seleccionador de imagenes
    static final int REQUEST_IMAGE_CAPTURE = 2; //variable para foto tomada con la camara
    static final int REQUEST_TAKE_PHOTO = 1;
    private Uri fotoTomada=null;

    private Bitmap bitmapimagenProducto = null;
    private FirebaseStorage storage;
    private ImageView imagenProducto;
    private  String idProducto;
    private Producto producto;
    private EditText nombreProducto;
    private EditText cantidadProducto;
    private EditText codigoProducto ;
    private EditText precioProducto;
    private EditText descripcionProducto;
    private Button btnActualizarProducto;
    private Button btnCancelarProducto;
    private Button btnTomarFoto;
    private Button btnSeleccionarImagen ;
    private String actividadDeLaQueViene;
    private Cliente clienteGlobal;
    private Vendedor vendedorGlobal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.cuadro_editar_producto, container, false);
        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        storage = FirebaseStorage.getInstance();

        //incializamos las variables
        nombreProducto = root.findViewById(R.id.txtNombreProductoCuadroEditarProducto);
        cantidadProducto = root.findViewById(R.id.txtCantidadPedidoCuadroEditarProducto);
        codigoProducto = root.findViewById(R.id.txtCodigoPedidoCuadroEditarProducto);
        precioProducto = root.findViewById(R.id.txtPrecioCuadroEditarProducto);
        descripcionProducto = root.findViewById(R.id.txtDescripcionCuadroProducto);
        btnActualizarProducto = root.findViewById(R.id.btnGuardarCuadroEditarProducto);
        btnCancelarProducto = root.findViewById(R.id.btnCancelarCuadroEditarProducto);
        btnTomarFoto = root.findViewById(R.id.btnTomarFotoCuadroEditarProducto);
        btnSeleccionarImagen  = root.findViewById(R.id.btnSeleccionarImagenCuadroEditarProducto);
        imagenProducto = root.findViewById(R.id.imgPedidoCuadroEditarProducto);


        imagenProducto.setVisibility(View.GONE);

        idProducto = ((MyFirebaseApp) getActivity().getApplicationContext()).getIdProducto();
        vendedorGlobal =  ((MyFirebaseApp) getActivity().getApplicationContext()).getVendedor();
        actividadDeLaQueViene = ((MyFirebaseApp) getActivity().getApplicationContext()).getActividadDeLaQueViene();

        databaseReference.child("Producto").child(idProducto).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                if (snapshot.exists()){
                    producto = snapshot.getValue(Producto.class);
                    try {
                        nombreProducto.setText(encrypt.desencriptar(producto.getNombreProducto()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cantidadProducto.setText(producto.getCantidadProducto()+"");
                    try {
                        codigoProducto.setText(encrypt.desencriptar(producto.getCodigoProducto()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    precioProducto.setText(producto.getPrecioProducto()+"");
                    try {
                        descripcionProducto.setText(encrypt.desencriptar(producto.getDescripcionProducto()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (producto.getImagen()!=null){
                        try {
                            producto.setImagen(encrypt.desencriptar(producto.getImagen()));
                            imagenProducto.setVisibility(View.VISIBLE);
                            storage.getReference().child(producto.getImagen()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.with(getContext()).load(uri).into(imagenProducto);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    imagenProducto.setVisibility(View.GONE);
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
/*
                    databaseReference.child("Vendedor").child(producto.getIdVendedor()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                clienteGlobal = snapshot.getValue(Cliente.class);
                            }
                        }
                    });

 */

                }
            }
        });


        btnSeleccionarImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmapimagenProducto = null;
                selectorImagenes();
            }
        });

        btnTomarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmapimagenProducto = null;
                fotoTomada = null;
                dispatchTakePictureIntent();
            }
        });
        Utilidades util = new Utilidades();
        Dialog dialogCargando = util.dialogCargar(getContext());

        btnActualizarProducto.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                //activamos el cuadro de dialogo
                dialogCargando.show();
                //cargamos los datos que van a ser actualizados en un map
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

                if (bitmapimagenProducto!=null){//en caso de que una imagen fuera cargada
                    StorageReference storageRef = storage.getReference().child(idProducto);//creamos la referencia para subir datos
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmapimagenProducto.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] data = baos.toByteArray();

                    bitmapimagenProducto = null;

                    UploadTask uploadTask = storageRef.putBytes(data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {//cargamos la imagen en el servidor
                        @Override
                        public void onFailure(@NonNull Exception exception) {//en caso de existir un error notificamos al cliente
                            Toast.makeText(getContext(),"Error al subir imagen",Toast.LENGTH_LONG).show();
                            dialogCargando.dismiss();//cerramos el cuadro de dialogo cargando
                            Log.e("ImagenError","Error al cargar la imagen",exception);
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            try {//en caso de averse subido la imagen cargamos los datos para actualizarlos
                                pedidoActualizacion.put("imagen",encrypt.encriptar(storageRef.getPath()));
                                //enviamos la actualizacion del pedido
                                databaseReference.child("Producto").child(producto.getIdProducto()).updateChildren(pedidoActualizacion).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){//en caso de actualizarse el pedido
                                            dialogCargando.dismiss(); //cerramos el dialogo cargando
                                            iniciarActividadPedido();
                                            Toast.makeText(getContext(),"Pedido actualizado con éxito",Toast.LENGTH_LONG).show();
                                        }else{
                                            dialogCargando.dismiss();
                                            Toast.makeText(getContext(),"No se pudo actualizar el pedido",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }else{//en caso de no existir imagen
                    //enviamos la actualizacion del producto sin imagen
                    databaseReference.child("Producto").child(producto.getIdProducto()).updateChildren(pedidoActualizacion).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                dialogCargando.dismiss();
                                iniciarActividadPedido();
                                Toast.makeText(getContext(),"Pedido actualizado con éxito",Toast.LENGTH_LONG).show();
                            }else{
                                dialogCargando.dismiss();
                                Toast.makeText(getContext(),"No se pudo actualizar el pedido",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

            }
        });

        btnCancelarProducto.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                iniciarActividadPedido();
            }
        });
        return root;
    }

    private void iniciarActividadPedido() {
        Fragment fragment = new FragmentoPedidoVendedor();

        if (actividadDeLaQueViene.equals("listaProductos")){
            fragment = new FragmentoListarProductos();
        }else{
            fragment = new FragmentoListarProductosSinImagne();
        }

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                .replace(R.id.home_content_vendedor, fragment)
                .commit();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICKER:
                if (resultCode == RESULT_OK) {
                    if (data!=null) {
                        Uri pathArchivo = data.getData(); //Obtenemos el uri de la imagen seleccionada
                        if (pathArchivo != null) {
                            if (!pathArchivo.equals("")) {
                                try {
                                    imagenProducto.setVisibility(View.VISIBLE);
                                    Bitmap bitImagen = getBitmapFromUri(pathArchivo);
                                    bitmapimagenProducto = bitImagen;
                                    Picasso.with(getContext()).load(pathArchivo).into(imagenProducto);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }else{
                        if (fotoTomada!=null) {
                            try {
                                Bitmap bitImagen = getBitmapFromUri(fotoTomada);
                                imagenProducto.setVisibility(View.VISIBLE);
                                bitmapimagenProducto = bitImagen;
                                Picasso.with(getContext()).load(fotoTomada).into(imagenProducto);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
                break;
        }
    }

    private void cargarImagen(){

    }

    private  void selectorImagenes(){//inicia el cuadro de dialogo para seleccionar una imagen
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {

            startActivityForResult(Intent.createChooser(intent,"Seleccione la imagen"),1);
        }catch (android.content.ActivityNotFoundException ex){
            Toast.makeText(getContext(),"Por favor instale un administrados de archivos",Toast.LENGTH_SHORT).show();
        }

    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException { //obtenemos un bitmap de la ruta real del archivo
        ParcelFileDescriptor parcelFileDescriptor =
                getActivity().getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }



    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
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
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.aplicacion.envivoapp",
                        photoFile);
                fotoTomada = photoURI;
                Picasso.with(getContext()).load(photoURI).into(imagenProducto);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File nuevaCarpeta = new File(getActivity().getExternalFilesDir(null), "/Pictures");
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
