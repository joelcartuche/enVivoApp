package com.aplicacion.envivoapp.activitysParaVendedores;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activityParaClientes.MensajeriaGlobal;
import com.aplicacion.envivoapp.adaptadores.AdapterMensajeriaGlobal;
import com.aplicacion.envivoapp.adaptadores.AdapterMensajeriaGlobalVendedores;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MensajeriaGlobalVendedor extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage ; //para la insersion de archivos
    private StorageReference storageReference;
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();

    private List<Mensaje> listMensaje = new ArrayList<>();
    private RecyclerView gridViewMensaje;
    private AdapterMensajeriaGlobalVendedores gridAdapterMensaje;

    private Button enviar,imagen;
    private EditText textoMensaje;
    private  String idCliente;
    private final int PICKER = 1;
    private  String pathArchivo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajeria_global_vendedor);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        storage = FirebaseStorage.getInstance(); //para la insersion de archivos
        storageReference= storage.getReference();

        gridViewMensaje = findViewById(R.id.gridMensajeriaGlobalVendedor);
        enviar= findViewById(R.id.btnEnviarMensajeriaGlobalVendedor);
        textoMensaje = findViewById(R.id.txtTextiMensajeriaGlobalVendedor);
        imagen= findViewById(R.id.btnImagenGlobalVendedor);

        Bundle vendedor = MensajeriaGlobalVendedor.this.getIntent().getExtras();
        idCliente = vendedor.getString("cliente"); //recogemos los datos del vendedor

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectorImagenes();
            }
        });
        enviar.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (!textoMensaje.getText().toString().equals("")){
                    Query queryVendedor = databaseReference.child("Vendedor").orderByChild("uidUsuario").equalTo(firebaseAuth.getCurrentUser().getUid());
                    queryVendedor.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                Vendedor vendedor = null;
                                for (DataSnapshot ds:snapshot.getChildren()){
                                    vendedor = ds.getValue(Vendedor.class);
                                                                    }
                                if (vendedor!=null){
                                    // creamos el mensaje
                                    Mensaje mensaje = new Mensaje(); //instanciamos el mensaje

                                    LocalDateTime tiempoActual = LocalDateTime.now(); //obtenemos la hora y fecha actual
                                    // creamos la fecha
                                    Date fecha = new Date();
                                    fecha.setDate(tiempoActual.getDayOfMonth());
                                    fecha.setMonth(tiempoActual.getMonth().getValue());
                                    fecha.setYear(tiempoActual.getYear());
                                    fecha.setHours(tiempoActual.getHour());
                                    fecha.setMinutes(tiempoActual.getMinute());
                                    fecha.setSeconds(tiempoActual.getSecond());

                                    //creamo el mensaje
                                    mensaje.setFecha(fecha);
                                    String idMensaje = databaseReference.push().getKey();
                                    mensaje.setIdMensaje(idMensaje);
                                    try {
                                        mensaje.setTexto(encriptacionDatos.encriptar(textoMensaje.getText().toString()));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    mensaje.setIdcliente(idCliente);
                                    mensaje.setIdvendedor(vendedor.getIdVendedor());
                                    mensaje.setIdStreaming(null);
                                    mensaje.setEsGlobal(true);
                                    mensaje.setEsVededor(true);

                                    databaseReference.child("Mensaje").child(idMensaje).setValue(mensaje).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            textoMensaje.setText("");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(MensajeriaGlobalVendedor.this,"Error al enviar el mensaje intentelo de nuevo",Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                        }
                    });

                }
            }
        });
        //cargamos los mensajes
        leerMensaje();


        //le damos funcionalidad al toolbar
        Button mensajeria = findViewById(R.id.btnMensajeriaGlobalMensajeriaGlobalVendedor);
        Button listarLocal = findViewById(R.id.btnListarLocalMensajeriaGlobalVendedor);
        Button perfil = findViewById(R.id.btnPerfilVendedorMensajeriaGlobalVendedor);
        Button pedido = findViewById(R.id.btnPedidoMensajeriaGlobalVendedor);
        Button videos = findViewById(R.id.btnVideosMensajeriaGlobalVendedor);
        Button salir = findViewById(R.id.btnSalirMensajeriaGlobalVendedor);
        Button clientes = findViewById(R.id.btnClientesGlobalVendedor);
        Button reporte= findViewById(R.id.btnReporteGlobalVendedor);
        Button home = findViewById(R.id.btnHomeVendedorMensajeriaGlobalVendedor);

        new Utilidades().cargarToolbarVendedor(home,
                listarLocal,
                perfil,
                pedido,
                mensajeria,
                salir,
                videos,
                clientes,
                reporte,
                MensajeriaGlobalVendedor.this,
                firebaseAuth);


    }
    private void borrarGrid(){
        listMensaje.clear();//borramos en caso de quedar algo en la cache
        //Inicialisamos el adaptador
        gridAdapterMensaje = new AdapterMensajeriaGlobalVendedores(MensajeriaGlobalVendedor.this,null,
                listMensaje,
                databaseReference,
                storage,
                firebaseAuth);
        gridViewMensaje.setAdapter(gridAdapterMensaje); //configuramos el view
        gridViewMensaje.setLayoutManager(new LinearLayoutManager(MensajeriaGlobalVendedor.this));
    }
    private void leerMensaje() {
        Query queryMensajeVendedro = databaseReference.child("Vendedor").orderByChild("uidUsuario").equalTo(firebaseAuth.getCurrentUser().getUid());
            queryMensajeVendedro.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    listMensaje.clear();
                    if (snapshot.exists()) {
                        Vendedor vendedor = null;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            vendedor = ds.getValue(Vendedor.class);
                            if (vendedor !=null) {
                                try {
                                    vendedor.setCedula(encriptacionDatos.desencriptar(vendedor.getCedula()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    vendedor.setCelular(encriptacionDatos.desencriptar(vendedor.getCelular()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    vendedor.setNombre(encriptacionDatos.desencriptar(vendedor.getNombre()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    vendedor.setTelefono(encriptacionDatos.desencriptar(vendedor.getTelefono()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }else{
                                Log.e("Error","Mesajeria global vendedor vendedor null 237");
                            }
                        }
                        if (vendedor != null) {
                            Query queryMensaje = databaseReference.child("Mensaje").orderByChild("idvendedor").equalTo(vendedor.getIdVendedor());
                            Vendedor finalVendedor = vendedor;
                            queryMensaje.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    borrarGrid();
                                    for (DataSnapshot ds1 : snapshot.getChildren()) {
                                        Mensaje mensajeAux = ds1.getValue(Mensaje.class);
                                        if (mensajeAux !=null
                                            && mensajeAux.getIdcliente().equals(idCliente)
                                            && !mensajeAux.getEsEliminado()
                                            && mensajeAux.getEsGlobal()) {
                                            try {
                                                mensajeAux.setImagen(encriptacionDatos.desencriptar(mensajeAux.getImagen()));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            try {
                                                mensajeAux.setTexto(encriptacionDatos.desencriptar(mensajeAux.getTexto()));
                                                listMensaje.add(mensajeAux);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        }else{
                                            Log.e("Error","Mensajeria global null 266");
                                        }
                                    }

                                    gridAdapterMensaje = new AdapterMensajeriaGlobalVendedores(MensajeriaGlobalVendedor.this, finalVendedor,
                                            listMensaje,
                                            databaseReference,
                                            storage,
                                            firebaseAuth);
                                    gridViewMensaje.setAdapter(gridAdapterMensaje); //configuramos el view
                                    gridViewMensaje.setLayoutManager(new LinearLayoutManager(MensajeriaGlobalVendedor.this));
                                    gridViewMensaje.getLayoutManager().scrollToPosition(listMensaje.size()-1);

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    borrarGrid();
                                }
                            });
                        }else{
                            borrarGrid();
                        }
                    }else{
                        borrarGrid();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    borrarGrid();
                }
            });
        }

    private  void selectorImagenes(){//inicia el cuadro de dialogo para seleccionar una imagen
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent,"Seleccione un archivo para subir"),1);
        }catch (android.content.ActivityNotFoundException ex){
            Toast.makeText(MensajeriaGlobalVendedor.this,"Por favorm instale un administrados de archivos",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case PICKER:
                if (resultCode == RESULT_OK){
                    Uri pathArchivo = data.getData();
                    //cargamos el bitmap
                    Query queryVendedor = databaseReference.child("Vendedor").orderByChild("uidUsuario").equalTo(firebaseAuth.getCurrentUser().getUid());
                    queryVendedor.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onSuccess(DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                Vendedor vendedor = null;
                                for (DataSnapshot ds:snapshot.getChildren()){
                                    vendedor = ds.getValue(Vendedor.class);
                                }

                                if (vendedor!=null){
                                    // creamos el mensaje
                                    Mensaje mensaje = new Mensaje(); //instanciamos el mensaje

                                    LocalDateTime tiempoActual = LocalDateTime.now(); //obtenemos la hora y fecha actual
                                    // creamos la fecha
                                    Date fecha = new Date();
                                    fecha.setDate(tiempoActual.getDayOfMonth());
                                    fecha.setMonth(tiempoActual.getMonth().getValue());
                                    fecha.setYear(tiempoActual.getYear());
                                    fecha.setHours(tiempoActual.getHour());
                                    fecha.setMinutes(tiempoActual.getMinute());
                                    fecha.setSeconds(tiempoActual.getSecond());

                                    //creamo el mensaje
                                    mensaje.setFecha(fecha);
                                    String idMensaje = databaseReference.push().getKey();
                                    mensaje.setIdMensaje(idMensaje);
                                    try {
                                        mensaje.setTexto(encriptacionDatos.encriptar(""));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    mensaje.setIdcliente(idCliente);
                                    mensaje.setIdvendedor(vendedor.getIdVendedor());
                                    mensaje.setIdStreaming(null);
                                    mensaje.setEsGlobal(true);
                                    mensaje.setEsVededor(true);
                                    if (!pathArchivo.equals("")){
                                        //cargamos la imagen
                                        try {
                                            StorageReference storageRef = storage.getReference().child(idMensaje);//creamos la referencia para subir datos
                                            mensaje.setImagen(storageRef.getPath());
                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                            Bitmap bitImagen = getBitmapFromUri(pathArchivo);
                                            bitImagen.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                            byte[] data = baos.toByteArray();
                                            UploadTask uploadTask = storageRef.putBytes(data);
                                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    Toast.makeText(MensajeriaGlobalVendedor.this,"Error al subir la imagen intentelo de nuevo",Toast.LENGTH_LONG).show();
                                                    Log.w("ImagenError","Error al cargar la imagen",exception);
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    try {
                                                        mensaje.setImagen(encriptacionDatos.encriptar(mensaje.getImagen()));
                                                        databaseReference.child("Mensaje").child(idMensaje).setValue(mensaje);
                                                    } catch (Exception e) {
                                                        Toast.makeText(MensajeriaGlobalVendedor.this,"Error al subir la imagen intentelo de nuevo",Toast.LENGTH_LONG).show();
                                                        e.printStackTrace();
                                                    }

                                                }
                                            });

                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }


                                    }
                                }else{
                                    Log.e("Error","Mensajeria Global Vendedor null 398");
                                }
                            }
                        }
                    });
                }
                break;
        }

    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

}