package com.aplicacion.envivoapp.activityParaClientes;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterListarVendedores;
import com.aplicacion.envivoapp.adaptadores.AdapterMensajeriaCliente;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MensajeriaCliente extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private List<Mensaje> listMensaje = new ArrayList<>();
    private ListAdapter adapterListMensaje;
    private ListView listaMensajeView;

    private Button enviarMensaje;
    private EditText textoMensaje;
    private  String idVendedor,idCliente,idStreaming,urlStreaming;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajeria_cliente);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        listaMensajeView = findViewById(R.id.listMensajeriaCliente);
        enviarMensaje = findViewById(R.id.btnEnviarMensajeCliente);
        textoMensaje = findViewById(R.id.txtMensajeCliente);
        Bundle vendedor = MensajeriaCliente.this.getIntent().getExtras();
        idVendedor = vendedor.getString("vendedor"); //recogemos los datos del vendedor
        idCliente = vendedor.getString("cliente");
        urlStreaming = vendedor.getString("url"); //recogemos los datos del vendedor
        idStreaming = vendedor.getString("streaming");

        listarMensajes();
        enviarMensaje.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                Mensaje mensaje = new Mensaje();

                LocalDateTime tiempoActual = LocalDateTime.now();

                Date fecha = new Date();
                fecha.setDate(tiempoActual.getDayOfMonth());
                fecha.setMonth(tiempoActual.getMonth().getValue());
                fecha.setYear(tiempoActual.getYear());
                fecha.setHours(tiempoActual.getHour());
                fecha.setMinutes(tiempoActual.getMinute());
                fecha.setSeconds(tiempoActual.getSecond());


                mensaje.setFecha(fecha);
                String idMensaje = databaseReference.push().getKey();
                mensaje.setIdMensaje(idMensaje);
                mensaje.setTexto(textoMensaje.getText().toString());
                mensaje.setIdcliente(idCliente);
                mensaje.setIdvendedor(idVendedor);
                mensaje.setIdStreaming(idStreaming);
                databaseReference.child("Mensaje").child(idMensaje).setValue(mensaje);

            }
        });
    }

    public void listarMensajes(){
        databaseReference.child("Mensaje").orderByChild("fecha").addValueEventListener(new ValueEventListener() { //buscamos todos los datos en la tabla Video Streaming
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listMensaje.clear();//borramos en caso de quedar algo en la cache
                    for (final DataSnapshot ds : snapshot.getChildren()) {
                        Mensaje mensaje = ds.getValue(Mensaje.class);
                        if(mensaje != null){
                        if(mensaje.getIdcliente().equals(idCliente)){
                            listMensaje.add(mensaje);
                            //Inicialisamos el adaptador
                            adapterListMensaje = new AdapterMensajeriaCliente(MensajeriaCliente.this,R.layout.item_list_mensajeria_cliente,listMensaje,databaseReference);
                            listaMensajeView.setAdapter(adapterListMensaje);//configuramos el view
                        }}
                    }
                }else{
                    listMensaje.clear();//borramos los datos ya que no hay nada en la base
                    //Inicialisamos el adaptador
                    adapterListMensaje = new AdapterMensajeriaCliente(MensajeriaCliente.this,R.layout.item_list_mensajeria_cliente,listMensaje,databaseReference);
                    listaMensajeView.setAdapter(adapterListMensaje); //configuramos el view
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }
}