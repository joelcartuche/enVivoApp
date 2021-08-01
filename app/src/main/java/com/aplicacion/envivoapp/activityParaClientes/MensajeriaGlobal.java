package com.aplicacion.envivoapp.activityParaClientes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activitysParaVendedores.MensajeriaVendedor;
import com.aplicacion.envivoapp.activitysParaVendedores.PedidoVendedor;
import com.aplicacion.envivoapp.adaptadores.AdapterGridMensajeriaCliente;
import com.aplicacion.envivoapp.adaptadores.AdapterGridPedidoVendedor;
import com.aplicacion.envivoapp.adaptadores.AdapterListarVendedores;
import com.aplicacion.envivoapp.adaptadores.AdapterMensajeriaGlobal;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MensajeriaGlobal extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage ; //para la insersion de archivos
    private StorageReference storageReference;

    private List<Mensaje> listMensaje = new ArrayList<>();
    private GridView gridViewMensaje;
    private AdapterMensajeriaGlobal gridAdapterMensaje;

    private TextView tituloMesaje;
    private EditText mensajeEnv;
    private Button enviar, imagen;
    private String idVendedor;
    private final int PICKER = 1;
    private  String pathArchivo = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajeria_global);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        storage = FirebaseStorage.getInstance();//inicializamos la variable storage
        storageReference = storage.getReference();

        tituloMesaje = findViewById(R.id.txtNombreVendedorMensajeriaGlobal);
        mensajeEnv = findViewById(R.id.txtMensajeMensajeriaGlobal);
        enviar = findViewById(R.id.btnEnviarMensajeriaGlobal);
        gridViewMensaje = findViewById(R.id.gridMensajeriaGlobal);
        imagen = findViewById(R.id.btnImagenGlobalMensajeria);

        Bundle vendedor = MensajeriaGlobal.this.getIntent().getExtras();
        idVendedor = vendedor.getString("vendedor"); //recogemos los datos del vendedor


        //Damos funcionalidad al menu
        Button btnListarVendedore = findViewById(R.id.btn_listar_vendedores_MensajeriaGlobal);
        Button btnPerfil = findViewById(R.id.btn_perfil_listar_MensajeriaGlobal);
        Button btnPedido = findViewById(R.id.btn_carrito_listar_MensajeriaGlobal);
        Button btnSalir = findViewById(R.id.btn_salir_MensajeriaGlobal);
        Button btnMensje = findViewById(R.id.btnMensajeriaGlobalMensajeriaGlobal);
        Button btnHome = findViewById(R.id.btn_Home_Mensajeria_Global);

        new Utilidades().cargarToolbar(btnHome,
                btnListarVendedore,
                btnPerfil,
                btnPedido,
                btnSalir,
                btnMensje,
                MensajeriaGlobal.this,firebaseAuth,databaseReference);

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
                if (!mensajeEnv.getText().toString().equals("")){
                    //buscamos el cliente
                    databaseReference.child("Cliente").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Cliente cliente = null;
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    Cliente clienteAux = ds.getValue(Cliente.class);
                                    if (clienteAux.getUidUsuario().equals(firebaseAuth.getUid())) {
                                        cliente = clienteAux;
                                        break;
                                    }
                                }
                                if (cliente != null) {
                                    Cliente finalCliente = cliente;
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
                                    mensaje.setTexto(mensajeEnv.getText().toString());
                                    mensaje.setIdcliente(finalCliente.getIdCliente());
                                    mensaje.setIdvendedor(idVendedor);
                                    mensaje.setIdStreaming(null);
                                    mensaje.setEsGlobal(true);
                                    mensajeEnv.setText("");
                                    databaseReference.child("Mensaje").child(idMensaje).setValue(mensaje).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(MensajeriaGlobal.this,"Error al enviar el mensaje",Toast.LENGTH_LONG).show();
                                        }
                                    });

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            borrarGrid();
                        }
                    });




                }
            }
        });
        leerMensaje();
    }

    private void borrarGrid(){
        listMensaje.clear();//borramos en caso de quedar algo en la cache
        //Inicialisamos el adaptador
        gridAdapterMensaje = new AdapterMensajeriaGlobal(MensajeriaGlobal.this, listMensaje, databaseReference, storage);
        gridViewMensaje.setAdapter(gridAdapterMensaje); //configuramos el view
    }

    private void leerMensaje() {
        borrarGrid();
            databaseReference.child("Cliente").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    listMensaje.clear();
                    if (snapshot.exists()) {
                        Cliente cliente = null;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Cliente clienteAux = ds.getValue(Cliente.class);
                            if (clienteAux.getUidUsuario().equals(firebaseAuth.getUid())) {
                                cliente = clienteAux;
                                break;
                            }
                        }
                        if (cliente != null) {
                            Cliente finalCliente = cliente;
                            databaseReference.child("Mensaje").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                   borrarGrid();
                                    Mensaje mensaje = null;
                                    for (DataSnapshot ds1 : snapshot.getChildren()) {
                                        Mensaje mensajeAux = ds1.getValue(Mensaje.class);
                                        if (mensajeAux.getIdcliente().equals(finalCliente.getIdCliente())) {
                                            mensaje = mensajeAux;
                                        }
                                        if (mensaje != null && mensaje.getEsGlobal()) {
                                            listMensaje.add(mensaje);
                                        }
                                    }
                                    if (listMensaje.size() == 0){
                                        borrarGrid();
                                    }else {
                                        gridAdapterMensaje = new AdapterMensajeriaGlobal(MensajeriaGlobal.this, listMensaje, databaseReference, storage);
                                        gridViewMensaje.setAdapter(gridAdapterMensaje); //configuramos el view
                                    }
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
            Toast.makeText(MensajeriaGlobal.this,"Por favorm instale un administrados de archivos",Toast.LENGTH_SHORT).show();
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
                    databaseReference.child("Cliente").addValueEventListener(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                Cliente cliente = null;
                                for (DataSnapshot ds:snapshot.getChildren()){
                                    Cliente clienteAux = ds.getValue(Cliente.class);
                                    if (clienteAux.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())){
                                        cliente = clienteAux;
                                        break;
                                    }
                                }
                                if (cliente!=null){
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
                                    mensaje.setTexto("");
                                    mensaje.setIdcliente(cliente.getIdCliente());
                                    mensaje.setIdvendedor(idVendedor);
                                    mensaje.setIdStreaming(null);
                                    mensaje.setEsGlobal(true);
                                    mensaje.setEsVededor(true);
                                    if (!pathArchivo.equals("")){
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
                                                    Log.w("ImagenError","Error al cargar la imagen",exception);
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    databaseReference.child("Mensaje").child(idMensaje).setValue(mensaje);
                                                }
                                            });

                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                    }



                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
                break;
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

}