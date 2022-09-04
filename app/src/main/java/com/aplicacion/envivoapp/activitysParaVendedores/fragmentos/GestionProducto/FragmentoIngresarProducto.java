package com.aplicacion.envivoapp.activitysParaVendedores.fragmentos.GestionProducto;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Handler;
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

import com.aplicacion.envivoapp.R;
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

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class FragmentoIngresarProducto extends Fragment {
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage  storage;

    private EditText txtCodigoIngresarProducto,
            txtNombreIngresarProducto,
            txtCantidadIngresarProducto,
            txtPrecioIngresarProducto,
            txtDescripcionIngresarProducto;

    private Button btnTomarFotoIngresarProducto,
            btnSeleccionarImagenIngresarProducto,
            btnAtrasIngresarProducto,

            btnGuardarIngresarProducto;

    Dialog dialogSucces;


    private ImageView imgProductoIngresarProducto;

    private  String idProducto;
    private Vendedor vendedorGlobal;

    private EncriptacionDatos encrypt= new EncriptacionDatos();

    private final int PICKER = 1; //variable para seleccionador de imagenes
    static final int REQUEST_IMAGE_CAPTURE = 2; //variable para foto tomada con la camara
    static final int REQUEST_TAKE_PHOTO = 1;
    private Uri fotoTomada=null;
    String currentPhotoPath;

    private Bitmap bitmapImagenPedido = null;
    private  Producto producto;
    private Uri imagenGlobal= null ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root =inflater.inflate(R.layout.fragmento_ingresar_producto, container, false);
        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        storage = FirebaseStorage.getInstance();

        //inicalizamos las variables
        txtCodigoIngresarProducto = root.findViewById(R.id.txtCodigoIngresarProducto);
        txtNombreIngresarProducto= root.findViewById(R.id.txtNombreIngresarProducto);
        txtCantidadIngresarProducto= root.findViewById(R.id.txtCantidadIngresarProducto);
        txtPrecioIngresarProducto= root.findViewById(R.id.txtPrecioIngresarProducto);
        txtDescripcionIngresarProducto= root.findViewById(R.id.txtDescripcionIngresarProducto);
        btnTomarFotoIngresarProducto= root.findViewById(R.id.btnTomarFotoIngresarProducto);
        btnSeleccionarImagenIngresarProducto= root.findViewById(R.id.btnSeleccionarImagenIngresarProducto);
        imgProductoIngresarProducto= root.findViewById(R.id.imgProductoIngresarProducto);
        btnAtrasIngresarProducto= root.findViewById(R.id.btnAtrasIngresarProducto);
        btnGuardarIngresarProducto= root.findViewById(R.id.btnGuardarIngresarProducto);


        //ocultamos la imagen del pedido
        imgProductoIngresarProducto.setVisibility(View.GONE);
        idProducto = ((MyFirebaseApp) getActivity().getApplicationContext()).getIdProducto();
        vendedorGlobal =  ((MyFirebaseApp) getActivity().getApplicationContext()).getVendedor();

        //cargamos los datos del producto
       // cargarDatosProducto();

        //le damos funcionalidad al boton seleccionar imagen
        btnSeleccionarImagenIngresarProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmapImagenPedido = null;
                selectorImagenes();
            }
        });

        //le damos funcionalidad al boton tomar foto
        btnTomarFotoIngresarProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmapImagenPedido = null;
                fotoTomada = null;
                dispatchTakePictureIntent();
            }
        });

        //le damos funcionalidad al boton atras
        btnAtrasIngresarProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regresarFragment();
            }
        });


        Utilidades util = new Utilidades();
        Dialog dialogCargando = util.dialogCargar(getContext()); //cargamos el cuadro de dialogo
        dialogSucces = util.dialogSuccess(getContext()); //cargamos el cuadro de dialogo
        //le damos funcionalidad al botón guardar
        btnGuardarIngresarProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarCampos()) {
                    //activamos el cuadro de dialogo
                    dialogCargando.show();
                    try {
                        producto = new Producto();
                        producto.setIdProducto(vendedorGlobal.getIdVendedor()+"_"+txtCodigoIngresarProducto.getText().toString());
                        producto.setNombreProducto(encrypt.encriptar(txtNombreIngresarProducto.getText().toString()));
                        producto.setCantidadProducto(Integer.parseInt(txtCantidadIngresarProducto.getText().toString()));
                        producto.setCodigoProducto(encrypt.encriptar(txtCodigoIngresarProducto.getText().toString()));
                        producto.setPrecioProducto(Double.parseDouble(txtPrecioIngresarProducto.getText().toString()));
                        producto.setDescripcionProducto(encrypt.encriptar(txtDescripcionIngresarProducto.getText().toString()));
                        producto.setIdVendedor(vendedorGlobal.getIdVendedor());
                        producto.setIdVendedor_codigoProducto(vendedorGlobal.getIdVendedor()+"_"+txtCodigoIngresarProducto.getText().toString());

                        cargarProductoConImagen(dialogCargando);
                    } catch (Exception e) {
                        e.printStackTrace();
                        dialogCargando.dismiss();
                        Toast.makeText(getContext(),"Error al cargar el producto",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });




        return root;
    }

    private void regresarFragment() {
        Fragment fragment = new FragmentoListarProductos();

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                .replace(R.id.home_content_vendedor, fragment)
                .commit();
    }
    private  void mostrarSucces(){
        dialogSucces.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                dialogSucces.dismiss();
            }
        }, 2000);
    }

    private void recargarFragment() {
        Fragment fragment = new FragmentoIngresarProducto();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                .replace(R.id.home_content_vendedor, fragment)
                .commit();
    }

    public boolean validarCampos(){
        if (txtCodigoIngresarProducto.getText().toString().equals("")){
            txtCodigoIngresarProducto.setError("Ingrese el código");
        }else if (txtNombreIngresarProducto.getText().toString().equals("")){
            txtNombreIngresarProducto.setError("Ingrese el nombre");
        }else if (txtCantidadIngresarProducto.getText().toString().equals("")){
            txtCantidadIngresarProducto.setError("Ingrese el cantidad");
        }else if (txtPrecioIngresarProducto.getText().toString().equals("")){
            txtPrecioIngresarProducto.setError("Ingrese el precio");
        }else if (txtDescripcionIngresarProducto.getText().toString().equals("")){
            txtDescripcionIngresarProducto.setError("Ingrese el descripción");
        }else if( bitmapImagenPedido ==null ){
            Toast.makeText(getContext(),"Ingrese una imagen referencial",Toast.LENGTH_LONG).show();
        }else{
            return true;
        }
        return false;
    };

    public void cargarProductoConImagen(Dialog dialogCargando){


        StorageReference riversRef = storage.getReference().child(producto.getIdProducto()); //creamos la referencia para subir datos
        UploadTask uploadTask = riversRef.putFile(imagenGlobal);
        //ponemos la imagen en el producto
        producto.setImagen(riversRef.getPath());
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getContext(),"Error al subir imagen",Toast.LENGTH_LONG).show();
                dialogCargando.dismiss();//cerramos el cuadro de dialogo cargando
                Log.e("ImagenError","Error al cargar la imagen",exception);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                try {//en caso de averse subido la imagen cargamos los datos para actualizarlos
                    //enviamos el nuevo producto

                    databaseReference.child("Producto").child(producto.getIdProducto()).setValue(producto).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){//en caso de actualizarse el pedido
                                dialogCargando.dismiss(); //cerramos el dialogo cargando
                                recargarFragment();//recargamos el fragmento
                                Toast.makeText(getContext(),"Producto creado con éxito",Toast.LENGTH_LONG).show();
                                mostrarSucces();
                            }else{
                                dialogCargando.dismiss();
                                Log.e("Error al crear producto",task.getException().getMessage());
                                Toast.makeText(getContext(),"No se pudo crear el producto",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

/*

        StorageReference storageRef = storage.getReference().child(producto.getIdProducto());//creamos la referencia para subir datos
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmapImagenPedido.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        bitmapImagenPedido = null;

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
                    //enviamos el nuevo producto
                    databaseReference.child("Producto").child(producto.getIdProducto()).setValue(producto).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){//en caso de actualizarse el pedido
                                dialogCargando.dismiss(); //cerramos el dialogo cargando
                                Toast.makeText(getContext(),"Producto creado con éxito",Toast.LENGTH_LONG).show();
                            }else{
                                dialogCargando.dismiss();
                                Toast.makeText(getContext(),"No se pudo crear el producto",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

 */
    }

    public void cargarDatosProducto (){
        databaseReference.child("Producto").child(idProducto).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                if (snapshot.exists()){
                    producto = snapshot.getValue(Producto.class);
                    try {
                        txtNombreIngresarProducto.setText(encrypt.desencriptar(producto.getNombreProducto()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    txtCantidadIngresarProducto.setText(producto.getCantidadProducto()+"");
                    try {
                        txtCodigoIngresarProducto.setText(encrypt.desencriptar(producto.getCodigoProducto()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    txtPrecioIngresarProducto.setText(producto.getPrecioProducto()+"");
                    try {
                        txtDescripcionIngresarProducto.setText(encrypt.desencriptar(producto.getDescripcionProducto()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (producto.getImagen()!=null){
                        try {
                            producto.setImagen(encrypt.desencriptar(producto.getImagen()));
                            imgProductoIngresarProducto.setVisibility(View.VISIBLE);
                            storage.getReference().child(producto.getImagen()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.with(getContext()).load(uri).into(imgProductoIngresarProducto);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    imgProductoIngresarProducto.setVisibility(View.GONE);
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        });
    }

    public void selectorImagenes(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {

            startActivityForResult(Intent.createChooser(intent,"Seleccione la imagen"),1);
        }catch (android.content.ActivityNotFoundException ex){
            Toast.makeText(getContext(),"Por favor instale un administrados de archivos",Toast.LENGTH_SHORT).show();
        }
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
                                    imagenGlobal = pathArchivo;
                                    imgProductoIngresarProducto.setVisibility(View.VISIBLE);
                                    Bitmap bitImagen = getBitmapFromUri(pathArchivo);
                                    bitmapImagenPedido = bitImagen;
                                    Picasso.with(getContext()).load(pathArchivo).into(imgProductoIngresarProducto);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }else{
                        if (fotoTomada!=null) {
                            try {
                                Bitmap bitImagen = getBitmapFromUri(fotoTomada);
                                imgProductoIngresarProducto.setVisibility(View.VISIBLE);
                                bitmapImagenPedido = bitImagen;
                                Picasso.with(getContext()).load(fotoTomada).into(imgProductoIngresarProducto);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                break;
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
                imagenGlobal = photoURI;
                Picasso.with(getContext()).load(photoURI).into(imgProductoIngresarProducto);
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