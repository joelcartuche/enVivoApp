package com.aplicacion.envivoapp.activitysParaVendedores;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activityParaClientes.MensajeriaCliente;
import com.aplicacion.envivoapp.adaptadores.AdapterGridMensajeriaCliente;
import com.aplicacion.envivoapp.adaptadores.AdapterGridMensajeriaVendedor;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroAceptarPedidoMensajeCliente;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MensajeriaVendedor extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private Button enviarMensaje;
    private EditText textoMensaje;
    private  String idVendedor,idCliente,idStreaming,urlStreaming;
    private RadioButton filtrarPedido,filtrarTodos;

    private List<Mensaje> listMensaje = new ArrayList<>();
    private GridView gridViewMensaje;
    private AdapterGridMensajeriaVendedor gridAdapterMensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajeria_vendedor);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        //incializaoms las variables
        enviarMensaje = findViewById(R.id.btnEnviarMensajeVendedor);
        textoMensaje = findViewById(R.id.txtMensajeVendedor);
        gridViewMensaje = findViewById(R.id.gridMensajeVendedor);
        filtrarPedido = findViewById(R.id.radioFiltrarPedidoMensajeriaVendedor);
        filtrarTodos  = findViewById(R.id.radioFiltrarTodosMensajeriaVendedor);
        filtrarTodos.setChecked(true);//para que siempre  liste todos los mensajes

        //fin de incializacion de variables

        textoMensaje.setVisibility(View.INVISIBLE);
        enviarMensaje.setVisibility(View.INVISIBLE);
        Bundle vendedor = MensajeriaVendedor.this.getIntent().getExtras();
        idVendedor = vendedor.getString("vendedor"); //recogemos los datos del vendedor
        urlStreaming = vendedor.getString("url"); //recogemos los datos del vendedor
        idStreaming = vendedor.getString("streaming");

        listarMensajes();

        filtrarTodos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarTodos.setChecked(true);
                listarMensajes();
            }
        });
        filtrarPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarTodos.setChecked(false);
                listarMensajes();
            }
        });

        //le damos funcionalidad al toolbar
        Button mensajeria = findViewById(R.id.btnMensajeriaGlobalMensajeriaVendedor);
        Button listarLocal = findViewById(R.id.btnListarLocalMensajeriaVendedor);
        Button perfil = findViewById(R.id.btnPerfilVendedorMensajeriaVendedor);
        Button pedido = findViewById(R.id.btnPedidoMensajeriaVendedor);
        Button videos = findViewById(R.id.btnVideosMensajeriaVendedor);
        Button salir = findViewById(R.id.btnSalirMensajeriaVendedor);
        Button clientes = findViewById(R.id.btnClientesMensajeriaVendedor);

        new Utilidades().cargarToolbarVendedor(listarLocal,
                perfil,
                pedido,
                mensajeria,
                salir,
                videos,
                clientes,
                MensajeriaVendedor.this,
                firebaseAuth);
    }

private void borrarGrid(){
        listMensaje.clear();
    //Inicialisamos el adaptador
    gridAdapterMensaje = new AdapterGridMensajeriaVendedor(MensajeriaVendedor.this,
            listMensaje,
            databaseReference,
            filtrarTodos.isChecked());
    gridViewMensaje.setAdapter(gridAdapterMensaje);
}
    public void listarMensajes(){
        databaseReference.child("Mensaje").orderByChild("fecha").addValueEventListener(new ValueEventListener() { //buscamos todos los datos en la tabla Video Streaming
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listMensaje.clear();//borramos en caso de quedar algo en la cache
                    for (final DataSnapshot ds : snapshot.getChildren()) {
                        Mensaje mensaje = ds.getValue(Mensaje.class);
                        if(mensaje != null && mensaje.getIdStreaming() !=null){
                            databaseReference.child("Cliente").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        Cliente cliente = null;
                                        for (DataSnapshot ds:snapshot.getChildren()){
                                            Cliente clienteAux = ds.getValue(Cliente.class);
                                            if (clienteAux.getIdCliente().equals(mensaje.getIdcliente())){
                                                cliente=clienteAux;
                                            }
                                        }

                                        if (cliente != null && !cliente.getBloqueado()){//verificamos si el usuario no esta bloqueado
                                            if(mensaje.getIdvendedor().equals(idVendedor)
                                                    && mensaje.getIdStreaming().equals(idStreaming)){//aceptamos los mensajes que sean del cliente y de el streaming actual
                                                if (filtrarPedido.isChecked()){//filtramos por pedido
                                                    if (mensaje.getTexto().indexOf("Quiero comprar:") == 0){
                                                        listMensaje.add(mensaje);
                                                    }
                                                }
                                                if (filtrarTodos.isChecked()){//filtramos a todos los usuarios
                                                    listMensaje.add(mensaje);

                                                }
                                            }
                                            //Inicialisamos el adaptador
                                            gridAdapterMensaje = new AdapterGridMensajeriaVendedor(MensajeriaVendedor.this,
                                                    listMensaje,
                                                    databaseReference,
                                                    filtrarTodos.isChecked());
                                            gridViewMensaje.setAdapter(gridAdapterMensaje);
                                        }
                                        if (listMensaje.size()==0){
                                            borrarGrid();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                    if (listMensaje.size() ==0){
                        borrarGrid();
                        gridViewMensaje.setAdapter(gridAdapterMensaje);
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