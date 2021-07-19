package com.aplicacion.envivoapp.activitysParaVendedores;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activityParaClientes.ListarVendedores;
import com.aplicacion.envivoapp.activityParaClientes.MensajeriaGlobal;
import com.aplicacion.envivoapp.adaptadores.AdapterListarClientes;
import com.aplicacion.envivoapp.adaptadores.AdapterListarVendedores;
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

import java.util.ArrayList;
import java.util.List;

public class ListarClientes extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private List<Cliente> listCliente = new ArrayList<>();
    private AdapterListarClientes adapterListCliente;
    private GridView listaClienteView;
    private  String esMensajeGlobal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_clientes);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        listaClienteView = findViewById(R.id.gridClientes);
        listarVendedores();

        Bundle vendedor = ListarClientes.this.getIntent().getExtras(); //recogemos si es o no un mensaje global
        esMensajeGlobal = vendedor.getString("global"); //recogemos los datos del vendedor


        if (esMensajeGlobal.equals("1")) {
            listaClienteView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Bundle parametros = new Bundle();
                    parametros.putString("cliente", listCliente.get(position).getIdCliente());
                    Intent streamingsIntent = new Intent(ListarClientes.this,
                            MensajeriaGlobalVendedor.class);
                    streamingsIntent.putExtras(parametros);
                    startActivity(streamingsIntent);
                }
            });
        }

        //le damos funcionalidad al toolbar
        Button mensajeria = findViewById(R.id.btnMensajeriaGlobalListarClientes);
        Button listarLocal = findViewById(R.id.btnListarLocalListarClientes);
        Button perfil = findViewById(R.id.btnPerfilVendedorListarClientes);
        Button pedido = findViewById(R.id.btnPedidoListarClientes);
        Button videos = findViewById(R.id.btnVideosListarClientes);
        Button salir = findViewById(R.id.btnSalirListarClientes);
        Button clientes = findViewById(R.id.btnClientesDataListarClientes);

        new Utilidades().cargarToolbarVendedor(listarLocal,
                perfil,
                pedido,
                mensajeria,
                salir,
                videos,
                clientes,
                ListarClientes.this,
                firebaseAuth);


    }

    public void listarVendedores(){
        databaseReference.child("Cliente").addValueEventListener(new ValueEventListener() { //buscamos todos los datos en la tabla Video Streaming
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listCliente.clear();//borramos en caso de quedar algo en la cache
                    for (final DataSnapshot ds : snapshot.getChildren()) {
                        Cliente cliente = ds.getValue(Cliente.class);//obtenemos el objeto video streaming
                        if (cliente!=null && !cliente.getBloqueado() && esMensajeGlobal.equals("1") ){ //en caso de haber un cliente bloqueado no se muestra el cliente en la mensajeria
                            //buscamos si el cliente a realizado un mensaje al vendedor
                            databaseReference.child("Mensaje").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        Mensaje mensaje = null;
                                        for (DataSnapshot ds:snapshot.getChildren()){//visualizar que cliente a enviado un mensaje al vendedor
                                            Mensaje mensajeAux = ds.getValue(Mensaje.class);
                                            if (mensajeAux.getIdcliente().equals(cliente.getIdCliente())
                                                    && mensajeAux.getEsGlobal()){
                                                mensaje = mensajeAux;
                                                break;
                                            }
                                        }
                                        if (mensaje!=null){
                                            listCliente.add(cliente);
                                            //Inicialisamos el adaptador
                                            adapterListCliente = new AdapterListarClientes(ListarClientes.this,listCliente,databaseReference,esMensajeGlobal);
                                            listaClienteView.setAdapter(adapterListCliente); //configuramos el view
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }else{//listamos todos los clientes
                            //buscamos si el cliente a realizado un mensaje al vendedor
                            databaseReference.child("Mensaje").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        Mensaje mensaje = null;
                                        for (DataSnapshot ds:snapshot.getChildren()){//visualizar que cliente a enviado un mensaje al vendedor
                                            Mensaje mensajeAux = ds.getValue(Mensaje.class);
                                            if (mensajeAux.getIdcliente().equals(cliente.getIdCliente())
                                                    && mensajeAux.getEsGlobal()){
                                                mensaje = mensajeAux;
                                                break;
                                            }
                                        }
                                        if (mensaje!=null){
                                            listCliente.add(cliente);
                                            //Inicialisamos el adaptador
                                            adapterListCliente = new AdapterListarClientes(ListarClientes.this,listCliente,databaseReference,esMensajeGlobal);
                                            listaClienteView.setAdapter(adapterListCliente); //configuramos el view
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                }else{
                    listCliente.clear();//borramos los datos ya que no hay nada en la base
                    //Inicialisamos el adaptador
                    adapterListCliente = new AdapterListarClientes(ListarClientes.this,listCliente,databaseReference,esMensajeGlobal);
                    listaClienteView.setAdapter(adapterListCliente); //configuramos el view
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}