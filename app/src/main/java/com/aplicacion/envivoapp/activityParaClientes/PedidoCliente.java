package com.aplicacion.envivoapp.activityParaClientes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterGridPedidoCliente;
import com.aplicacion.envivoapp.adaptadores.AdapterListarVendedores;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroCancelarPedidoCliente;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class PedidoCliente extends AppCompatActivity implements CuadroCancelarPedidoCliente.resultadoDialogo {
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage ; //para la insersion de archivos
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();

    private List<Pedido> listPedido = new ArrayList<>();
    private GridView gridViewPedido;
    private AdapterGridPedidoCliente gridAdapterPedido;
    private EditText buscarVendedor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido_cliente);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        storage = FirebaseStorage.getInstance();//inicializamos la variable storage}


        gridViewPedido = findViewById(R.id.gridProductoCliente);
        buscarVendedor = findViewById(R.id.buscarVendedorPedidoCliente);

        listarProductos();
        //Damos funcionalidad al menu
        Button btnListarVendedore = findViewById(R.id.btn_listar_vendedores_PedidoCliente);
        Button btnPerfil = findViewById(R.id.btn_perfil_listar_PedidoCliente);
        Button btnPedido = findViewById(R.id.btn_carrito_listar_PedidoCliente);
        Button btnSalir = findViewById(R.id.btn_salir_PedidoCliente);
        Button btnMensje = findViewById(R.id.btnMensajeriaGlobalListarPedidoCliente);
        Button btnHome = findViewById(R.id.btn_Home_Pedido_Cliente);

        Utilidades util = new Utilidades();
        util.buscarClientebloqueado(PedidoCliente.this,firebaseAuth,databaseReference);
        util.cargarToolbar(btnHome,btnListarVendedore,
                btnPerfil,
                btnPedido,
                btnSalir,
                btnMensje,
                PedidoCliente.this,firebaseAuth,databaseReference);

    }
    private void borrarGrid(){
        listPedido.clear();//borramos en caso de quedar algo en la cache
        //Inicialisamos el adaptador
        gridAdapterPedido = new AdapterGridPedidoCliente(PedidoCliente.this,
                listPedido,
                databaseReference,
                storage);
        gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view

    }
    public void listarProductos(){
        Query query = databaseReference.child("Cliente").orderByChild("uidUsuario").equalTo(firebaseAuth.getCurrentUser().getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Cliente cliente =null;
                    for (final DataSnapshot ds : snapshot.getChildren()) {
                         cliente= ds.getValue(Cliente.class);
                    }

                    if (cliente!= null){

                        Query queryPedido = databaseReference.child("Pedido").orderByChild("idCliente").equalTo(cliente.getIdCliente());
                        queryPedido.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    listPedido.clear();//borramos en caso de quedar algo en la cache
                                    for (final DataSnapshot ds : snapshot.getChildren()) {
                                        Pedido pedido = ds.getValue(Pedido.class);//obtenemos

                                            try {
                                                pedido.setCodigoProducto(encriptacionDatos.desencriptar(pedido.getCodigoProducto()));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            try {
                                                pedido.setDescripcionProducto(encriptacionDatos.desencriptar(pedido.getDescripcionProducto()));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            try {
                                                pedido.setImagen(encriptacionDatos.desencriptar(pedido.getImagen()));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            try {
                                                pedido.setNombreProducto(encriptacionDatos.desencriptar(pedido.getNombreProducto()));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            listPedido.add(pedido);

                                    }
                                    //Inicialisamos el adaptador
                                    gridAdapterPedido = new AdapterGridPedidoCliente(PedidoCliente.this,
                                            listPedido,
                                            databaseReference,
                                            storage);
                                    gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view

                                }else{
                                    if (listPedido.size()==0){
                                        Toast.makeText(PedidoCliente.this,"Usted no tiene pedidos",Toast.LENGTH_LONG).show();
                                    }

                                    borrarGrid();
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

            }
        });

    }

    @Override
    public void resultado(Boolean isAcepatado, Boolean isCancelado) {

    }
}