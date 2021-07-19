package com.aplicacion.envivoapp.activitysParaVendedores;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activityParaClientes.MensajeriaGlobal;
import com.aplicacion.envivoapp.adaptadores.AdapterMensajeriaGlobal;
import com.aplicacion.envivoapp.adaptadores.AdapterMensajeriaGlobalVendedores;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Vendedor;
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

public class MensajeriaGlobalVendedor extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private List<Mensaje> listMensaje = new ArrayList<>();
    private GridView gridViewMensaje;
    private AdapterMensajeriaGlobalVendedores gridAdapterMensaje;

    private Button enviar;
    private EditText textoMensaje;
    private  String idCliente;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajeria_global_vendedor);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        gridViewMensaje = findViewById(R.id.gridMensajeriaGlobalVendedor);
        enviar= findViewById(R.id.btnEnviarMensajeriaGlobalVendedor);
        textoMensaje = findViewById(R.id.txtTextiMensajeriaGlobalVendedor);

        Bundle vendedor = MensajeriaGlobalVendedor.this.getIntent().getExtras();
        idCliente = vendedor.getString("cliente"); //recogemos los datos del vendedor

        enviar.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (!textoMensaje.getText().toString().equals("")){
                    databaseReference.child("Vendedor").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                Vendedor vendedor = null;
                                for (DataSnapshot ds:snapshot.getChildren()){
                                    Vendedor vendedorAux = ds.getValue(Vendedor.class);
                                    if (vendedorAux.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())){
                                        vendedor = vendedorAux;
                                        break;
                                    }
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
                                    mensaje.setTexto(textoMensaje.getText().toString());
                                    mensaje.setIdcliente(idCliente);
                                    mensaje.setIdvendedor(vendedor.getIdVendedor());
                                    mensaje.setIdStreaming(null);
                                    mensaje.setEsGlobal(true);
                                    mensaje.setEsVededor(true);
                                    databaseReference.child("Mensaje").child(idMensaje).setValue(mensaje);


                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

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

        new Utilidades().cargarToolbarVendedor(listarLocal,
                perfil,
                pedido,
                mensajeria,
                salir,
                videos,
                clientes,
                MensajeriaGlobalVendedor.this,
                firebaseAuth);


    }
    private void borrarGrid(){
        listMensaje.clear();//borramos en caso de quedar algo en la cache
        //Inicialisamos el adaptador
        gridAdapterMensaje = new AdapterMensajeriaGlobalVendedores(MensajeriaGlobalVendedor.this, listMensaje, databaseReference);
        gridViewMensaje.setAdapter(gridAdapterMensaje); //configuramos el view
    }
    private void leerMensaje() {

            databaseReference.child("Vendedor").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    listMensaje.clear();
                    if (snapshot.exists()) {
                        Vendedor vendedor = null;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Vendedor vendedorAux = ds.getValue(Vendedor.class);
                            if (vendedorAux.getUidUsuario().equals(firebaseAuth.getUid())) {
                                vendedor = vendedorAux;
                            }
                        }
                        if (vendedor != null) {
                            Vendedor finalVendedor = vendedor;
                            databaseReference.child("Mensaje").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    borrarGrid();
                                    Mensaje mensaje = null;
                                    for (DataSnapshot ds1 : snapshot.getChildren()) {
                                        Mensaje mensajeAux = ds1.getValue(Mensaje.class);
                                        if (mensajeAux.getIdvendedor().equals(finalVendedor.getIdVendedor())) {
                                            mensaje = mensajeAux;
                                        }
                                        if (mensaje != null && mensaje.getEsGlobal()) {
                                            listMensaje.add(mensaje);
                                        }
                                    }
                                    if (listMensaje.size() == 0){
                                        borrarGrid();
                                    }else {
                                        gridAdapterMensaje = new AdapterMensajeriaGlobalVendedores(MensajeriaGlobalVendedor.this, listMensaje, databaseReference);
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