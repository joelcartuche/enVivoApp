package com.aplicacion.envivoapp.activityParaClientes;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterGridMensajeriaCliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MensajeriaCliente extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private Button enviarMensaje,quieroComprar;
    private EditText textoMensaje;
    private  String idVendedor,idCliente,idStreaming,urlStreaming;

    private List<Mensaje> listMensaje = new ArrayList<>();
    private GridView gridViewMensaje;
    private AdapterGridMensajeriaCliente gridAdapterMensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajeria_cliente);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

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

        new Utilidades().cargarToolbar(btnListarVendedore,
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
                        if(mensaje.getIdcliente() != null && mensaje.getIdStreaming() !=null){
                        if(mensaje.getIdcliente().equals(idCliente)
                                && mensaje.getIdStreaming().equals(idStreaming)){//aceptamos los mensades que sean del cliente y de el streaming actual
                            listMensaje.add(mensaje);
                            gridAdapterMensaje = new AdapterGridMensajeriaCliente(MensajeriaCliente.this,listMensaje,databaseReference);
                            gridViewMensaje.setAdapter(gridAdapterMensaje);

                        }}
                    }
                }else{
                    listMensaje.clear();//borramos los datos ya que no hay nada en la base
                    gridAdapterMensaje = new AdapterGridMensajeriaCliente(MensajeriaCliente.this,listMensaje,databaseReference);
                    gridViewMensaje.setAdapter(gridAdapterMensaje);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }
}