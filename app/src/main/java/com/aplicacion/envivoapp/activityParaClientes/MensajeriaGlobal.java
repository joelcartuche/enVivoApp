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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MensajeriaGlobal extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private List<Mensaje> listMensaje = new ArrayList<>();
    private GridView gridViewMensaje;
    private AdapterMensajeriaGlobal gridAdapterMensaje;

    private TextView tituloMesaje;
    private EditText mensajeEnv;
    private Button enviar;
    private String idVendedor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajeria_global);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        tituloMesaje = findViewById(R.id.txtNombreVendedorMensajeriaGlobal);
        mensajeEnv = findViewById(R.id.txtMensajeMensajeriaGlobal);
        enviar = findViewById(R.id.btnEnviarMensajeriaGlobal);
        gridViewMensaje = findViewById(R.id.gridMensajeriaGlobal);

        Bundle vendedor = MensajeriaGlobal.this.getIntent().getExtras();
        idVendedor = vendedor.getString("vendedor"); //recogemos los datos del vendedor


        //Damos funcionalidad al menu
        Button btnListarVendedore = findViewById(R.id.btn_listar_vendedores_MensajeriaGlobal);
        Button btnPerfil = findViewById(R.id.btn_perfil_listar_MensajeriaGlobal);
        Button btnPedido = findViewById(R.id.btn_carrito_listar_MensajeriaGlobal);
        Button btnSalir = findViewById(R.id.btn_salir_MensajeriaGlobal);
        Button btnMensje = findViewById(R.id.btnMensajeriaGlobalMensajeriaGlobal);

        new Utilidades().cargarToolbar(btnListarVendedore,
                btnPerfil,
                btnPedido,
                btnSalir,
                btnMensje,
                MensajeriaGlobal.this,firebaseAuth,databaseReference);

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
        gridAdapterMensaje = new AdapterMensajeriaGlobal(MensajeriaGlobal.this, listMensaje, databaseReference);
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
                                        gridAdapterMensaje = new AdapterMensajeriaGlobal(MensajeriaGlobal.this, listMensaje, databaseReference);
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
}