package com.aplicacion.envivoapp.activityParaClientes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterGridPedidoCliente;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroCancelarPedidoCliente;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class PedidoCliente extends AppCompatActivity implements CuadroCancelarPedidoCliente.resultadoDialogo {
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage ; //para la insersion de archivos

    private List<Pedido> listPedido = new ArrayList<>();
    private GridView gridViewPedido;
    private AdapterGridPedidoCliente gridAdapterPedido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido_cliente);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        storage = FirebaseStorage.getInstance();//inicializamos la variable storage

        gridViewPedido = findViewById(R.id.gridProductoCliente);

        listarProductos();
        //Damos funcionalidad al menu
        Button btnListarVendedore = findViewById(R.id.btn_listar_vendedores_PedidoCliente);
        Button btnPerfil = findViewById(R.id.btn_perfil_listar_PedidoCliente);
        Button btnPedido = findViewById(R.id.btn_carrito_listar_PedidoCliente);
        Button btnSalir = findViewById(R.id.btn_salir_PedidoCliente);
        Button btnMensje = findViewById(R.id.btnMensajeriaGlobalListarPedidoCliente);
        Button btnHome = findViewById(R.id.btn_Home_Pedido_Cliente);

        new Utilidades().cargarToolbar(btnHome,btnListarVendedore,
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

        databaseReference.child("Cliente").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Cliente cliente =null;
                    for (final DataSnapshot ds : snapshot.getChildren()) {
                         Cliente clienteAux= ds.getValue(Cliente.class);
                         if (clienteAux.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())){
                             cliente=clienteAux;
                             break;
                         }
                    }

                    if (cliente!= null){
                        Cliente finalCliente = cliente;
                        databaseReference.child("Pedido").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    listPedido.clear();//borramos en caso de quedar algo en la cache
                                    for (final DataSnapshot ds : snapshot.getChildren()) {
                                        Pedido pedido = ds.getValue(Pedido.class);//obtenemos
                                        if (pedido.getAceptado() && pedido.getIdCliente().equals(finalCliente.getIdCliente())) {
                                            listPedido.add(pedido);
                                        }
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