package com.aplicacion.envivoapp.activityParaClientes;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterGridMensajeriaCliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
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
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicMarkableReference;


public class MensajeriaCliente extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage ; //para la insersion de archivos

    private Button enviarMensaje,quieroComprar;
    private EditText textoMensaje;
    private  String idVendedor,idCliente,idStreaming,urlStreaming;

    private List<Mensaje> listMensaje = new ArrayList<>();
    private GridView gridViewMensaje;
    private AdapterGridMensajeriaCliente gridAdapterMensaje;
    private Bitmap bitmapCapturaPantalla;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajeria_cliente);



        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        storage = FirebaseStorage.getInstance();//inicializamos la variable storage

        //incializaoms las variables
        enviarMensaje = findViewById(R.id.btnEnviarMensajeCliente);
        textoMensaje = findViewById(R.id.txtMensajeCliente);
        gridViewMensaje = findViewById(R.id.gridMensajeCliente);
        quieroComprar = findViewById(R.id.btnQuieroComprarMensajeCliente);

        //fin de incializacion de variables


        Bundle vendedor = MensajeriaCliente.this.getIntent().getExtras();
        idVendedor = vendedor.getString("vendedor"); //recogemos los datos del vendedor
        idCliente = vendedor.getString("cliente");
        urlStreaming = vendedor.getString("url"); //recogemos los datos del vendedor
        idStreaming = vendedor.getString("streaming");


        listarMensajes();//listamos los mensajes del cliente

        //Damos funcionalidad al menu
        Button btnListarVendedore = findViewById(R.id.btn_listar_vendedores_MensajeriaCliente);
        Button btnPerfil = findViewById(R.id.btn_perfil_listar_MensajeriaCliente);
        Button btnPedido = findViewById(R.id.btn_carrito_listar_MensajeriaCliente);
        Button btnSalir = findViewById(R.id.btn_salir_MensajeriaCliente);
        Button btnMensje = findViewById(R.id.btnMensajeriaGlobalMensajeriaCliente);

        Button btnHome = findViewById(R.id.btn_Home_Mensajeria_Cliente);

        new Utilidades().cargarToolbar(btnHome,
                btnListarVendedore,
                btnPerfil,
                btnPedido,
                btnSalir,
                btnMensje,
                MensajeriaCliente.this,firebaseAuth,databaseReference);

        //en caso de que el cliente desee comprar
        quieroComprar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textoMensaje.setText("Quiero comprar: ");
                bitmapCapturaPantalla=tomarCapturaPantalla();
                Toast.makeText(MensajeriaCliente.this,"Ingrese el nombre o código y la cantidad del producto que desea",Toast.LENGTH_LONG).show();
            }
        });

        enviarMensaje.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
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
                mensaje.setTexto(textoMensaje.getText().toString());
                mensaje.setIdcliente(idCliente);
                mensaje.setIdvendedor(idVendedor);
                mensaje.setIdStreaming(idStreaming);
                mensaje.setPedidoAceptado(false);
                mensaje.setPedidoCancelado(false);
                mensaje.setEsVededor(false);
                mensaje.setPedidoCancelado(false);
                mensaje.setPedidoAceptado(false);
                textoMensaje.setText("");
                if (mensaje.getTexto().indexOf("Quiero comprar:") == 0) {
                    if (bitmapCapturaPantalla != null) {
                        StorageReference storageRef = storage.getReference().child(idMensaje);//creamos la referencia para subir datos
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmapCapturaPantalla.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();
                        UploadTask uploadTask = storageRef.putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.w("ImagenError","Error al cargar la imagen",exception);
                            }
                        });
                        mensaje.setImagen(storageRef.getPath());
                        databaseReference.child("Mensaje").child(idMensaje).setValue(mensaje);
                    }
                }else{
                    databaseReference.child("Mensaje").child(idMensaje).setValue(mensaje);
                }
                
            }
        });
    }

    private Bitmap tomarCapturaPantalla() {
        Bitmap bitmap = null;
        try {
            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }

        return bitmap;
    }
    public  void borrarGrid(){
        listMensaje.clear();//borramos los datos ya que no hay nada en la base
        gridAdapterMensaje = new AdapterGridMensajeriaCliente(MensajeriaCliente.this,listMensaje,databaseReference,storage);
        gridViewMensaje.setAdapter(gridAdapterMensaje);
    }

    public void listarMensajes(){
        databaseReference.child("Mensaje").orderByChild("fecha").addValueEventListener(new ValueEventListener() { //buscamos todos los datos en la tabla Video Streaming
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listMensaje.clear();//borramos en caso de quedar algo en la cache
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Mensaje mensaje = ds.getValue(Mensaje.class);
                        if(mensaje.getIdcliente() != null && mensaje.getIdStreaming() !=null) {
                            if (mensaje.getIdcliente().equals(idCliente)
                                    && mensaje.getIdStreaming().equals(idStreaming)) {//aceptamos los mensades que sean del cliente y de el streaming actual
                                listMensaje.add(mensaje);
                            }
                        }
                    }
                    gridAdapterMensaje = new AdapterGridMensajeriaCliente(MensajeriaCliente.this, listMensaje, databaseReference, storage);
                    gridViewMensaje.setAdapter(gridAdapterMensaje);
                }else{
                   borrarGrid();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }
}