package com.aplicacion.envivoapp.activitysParaVendedores;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterListarClientes;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListarClientes extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();

    private List<Cliente> listCliente = new ArrayList<>();
    private AdapterListarClientes adapterListCliente;
    private GridView listaClienteView;
    private EditText buscarCliente;
    private  String esMensajeGlobal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_clientes);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        listaClienteView = findViewById(R.id.gridClientes);
        buscarCliente = findViewById(R.id.busquedaCliente);

        listarClientes();

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
        Button reporte = findViewById(R.id.btnReporteListarClientes);
        Button home = findViewById(R.id.btnHomeVendedorListarClientes);

        new Utilidades().cargarToolbarVendedor(home,
                listarLocal,
                perfil,
                pedido,
                mensajeria,
                salir,
                videos,
                clientes,
                reporte,
                ListarClientes.this,
                firebaseAuth);

        buscarCliente.setHint("Ingrese nombre del cliente");
        //le damos funcionalidad al buscador
        buscarCliente.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<Cliente> listaAux = new ArrayList<>();

                for (Cliente vd : listCliente) {

                    if (vd.toString().toLowerCase().indexOf(s.toString().toLowerCase()) == 0) {
                        listaAux.add(vd);
                    }
                }

                if (listaAux.size() != 0) {
                    //Inicialisamos el adaptador

                    adapterListCliente = new AdapterListarClientes(ListarClientes.this,listaAux,databaseReference,esMensajeGlobal);
                    listaClienteView.setAdapter(adapterListCliente); //configuramos el view
                }else{
                    //Inicialisamos el adaptador
                    adapterListCliente = new AdapterListarClientes(ListarClientes.this,listaAux,databaseReference,esMensajeGlobal);
                    listaClienteView.setAdapter(adapterListCliente); //configuramos el view
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    public  void borrarGrid(){
        listCliente.clear();
        //Inicialisamos el adaptador
        adapterListCliente = new AdapterListarClientes(ListarClientes.this,listCliente,databaseReference,esMensajeGlobal);
        listaClienteView.setAdapter(adapterListCliente); //configuramos el view
    }

    public void listarClientes(){
        Query query = databaseReference.child("Vendedor").orderByChild("uidUsuario").equalTo(firebaseAuth.getCurrentUser().getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                borrarGrid();
                if (snapshot.exists()){
                    Log.w("Vendedoruid ",snapshot.toString());
                    Vendedor vendedor = null;
                    for (DataSnapshot ds:snapshot.getChildren()){
                        vendedor = ds.getValue(Vendedor.class);
                    }
                    if (vendedor != null){

                        Vendedor finalVendedor = vendedor;
                        if (esMensajeGlobal.equals("1")){
                            //buscamos los clientes que no estan bloqueados
                            Query queryClienteNoBloqueado = databaseReference.child("Cliente").orderByChild("bloqueado").equalTo(false);
                            queryClienteNoBloqueado.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        Log.w("Global. ", snapshot.toString());
                                        for (final DataSnapshot ds : snapshot.getChildren()) {
                                            Cliente cliente = ds.getValue(Cliente.class);//obtenemos el objeto video streaming
                                            if (cliente != null) {
                                                //buscamos si el cliente a realizado un mensaje al vendedor
                                                Query queryMensaje = databaseReference.child("Mensaje").orderByChild("idvendedor").equalTo(finalVendedor.getIdVendedor());
                                                queryMensaje.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            Mensaje mensaje = null;
                                                            for (DataSnapshot ds : snapshot.getChildren()) {//visualizar que cliente a enviado un mensaje al vendedor
                                                                Mensaje mensajeAux = ds.getValue(Mensaje.class);
                                                                if (mensajeAux.getIdcliente().equals(cliente.getIdCliente())
                                                                        && mensajeAux.getEsGlobal()) {
                                                                    try {
                                                                        cliente.setCallePrincipal(encriptacionDatos.desencriptar(cliente.getCallePrincipal()));
                                                                        cliente.setCedula(encriptacionDatos.desencriptar(cliente.getCedula()));
                                                                        cliente.setNombre(encriptacionDatos.desencriptar(cliente.getNombre()));
                                                                        cliente.setReferencia(encriptacionDatos.desencriptar(cliente.getReferencia()));
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }

                                                                    try {
                                                                        cliente.setCalleSecundaria(encriptacionDatos.desencriptar(cliente.getCalleSecundaria()));
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    try {
                                                                        cliente.setCelular(encriptacionDatos.desencriptar(cliente.getCelular()));
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }

                                                                    try {
                                                                        cliente.setTelefono(encriptacionDatos.desencriptar(cliente.getTelefono()));
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }

                                                                    try {
                                                                        mensaje.setImagen(encriptacionDatos.desencriptar(mensaje.getImagen()));
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }

                                                                    try {
                                                                        mensaje.setTexto(encriptacionDatos.desencriptar(mensaje.getTexto()));
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    mensaje = mensajeAux;

                                                                    break;
                                                                }
                                                            }
                                                            if (mensaje != null) {
                                                                int cont = -1;
                                                                cont = listCliente.indexOf(cliente);
                                                                if (cont == -1) {
                                                                    listCliente.add(cliente);
                                                                    //Inicialisamos el adaptador
                                                                    adapterListCliente = new AdapterListarClientes(ListarClientes.this, listCliente, databaseReference, esMensajeGlobal);
                                                                    listaClienteView.setAdapter(adapterListCliente); //configuramos el view
                                                                }

                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }else if (esMensajeGlobal.equals("0")){
                            //listamos todos los clientes
                            //buscamos si el cliente a realizado un mensaje al vendedor
                            borrarGrid();

                            Query  mensajeVendedor = databaseReference.child("Mensaje").orderByChild("idvendedor").equalTo(finalVendedor.getIdVendedor());
                            mensajeVendedor.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        List<String> idClientes = new ArrayList<>();
                                        int cont=1;

                                        for (DataSnapshot ds:snapshot.getChildren()){
                                            Mensaje mensaje= ds.getValue(Mensaje.class);
                                            if (cont==1){
                                                idClientes.add(mensaje.getIdcliente());
                                                Query clientes = databaseReference.child("Cliente").child(mensaje.getIdcliente());
                                                clientes.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DataSnapshot snapshot) {
                                                        if (snapshot.exists()){
                                                            Cliente cliente = snapshot.getValue(Cliente.class);
                                                            if (cliente!=null) {
                                                                try {
                                                                    cliente.setCallePrincipal(encriptacionDatos.desencriptar(cliente.getCallePrincipal()));
                                                                    cliente.setCedula(encriptacionDatos.desencriptar(cliente.getCedula()));
                                                                    cliente.setNombre(encriptacionDatos.desencriptar(cliente.getNombre()));
                                                                    cliente.setReferencia(encriptacionDatos.desencriptar(cliente.getReferencia()));
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }

                                                                try {
                                                                    cliente.setCalleSecundaria(encriptacionDatos.desencriptar(cliente.getCalleSecundaria()));
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                                try {
                                                                    cliente.setCelular(encriptacionDatos.desencriptar(cliente.getCelular()));
                                                                } catch (Exception e) { e.printStackTrace();
                                                                }
                                                                try {
                                                                    cliente.setTelefono(encriptacionDatos.desencriptar(cliente.getTelefono()));
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                                listCliente.add(cliente);
                                                                //Inicialisamos el adaptador
                                                                adapterListCliente = new AdapterListarClientes(ListarClientes.this,listCliente,databaseReference,esMensajeGlobal);
                                                                listaClienteView.setAdapter(adapterListCliente); //configuramos el view
                                                            }
                                                        }
                                                    }
                                                });


                                            }else{
                                                if (idClientes.indexOf(mensaje.getIdcliente()) == -1){
                                                    idClientes.add(mensaje.getIdcliente());
                                                    Query clientes = databaseReference.child("Cliente").child(mensaje.getIdcliente());
                                                    clientes.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DataSnapshot snapshot) {
                                                            if (snapshot.exists()){
                                                                Cliente cliente = snapshot.getValue(Cliente.class);
                                                                if (cliente!=null) {
                                                                    try {
                                                                        cliente.setCallePrincipal(encriptacionDatos.desencriptar(cliente.getCallePrincipal()));
                                                                        cliente.setCedula(encriptacionDatos.desencriptar(cliente.getCedula()));
                                                                        cliente.setNombre(encriptacionDatos.desencriptar(cliente.getNombre()));
                                                                        cliente.setReferencia(encriptacionDatos.desencriptar(cliente.getReferencia()));
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }

                                                                    try {
                                                                        cliente.setCalleSecundaria(encriptacionDatos.desencriptar(cliente.getCalleSecundaria()));
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    try {
                                                                        cliente.setCelular(encriptacionDatos.desencriptar(cliente.getCelular()));
                                                                    } catch (Exception e) { e.printStackTrace();
                                                                    }
                                                                    try {
                                                                        cliente.setTelefono(encriptacionDatos.desencriptar(cliente.getTelefono()));
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    listCliente.add(cliente);
                                                                    //Inicialisamos el adaptador
                                                                    adapterListCliente = new AdapterListarClientes(ListarClientes.this,listCliente,databaseReference,esMensajeGlobal);
                                                                    listaClienteView.setAdapter(adapterListCliente); //configuramos el view
                                                                }
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                            cont++;
                                        }


                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
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