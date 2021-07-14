package com.aplicacion.envivoapp.activityParaClientes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.GridView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterGridMensajeriaCliente;
import com.aplicacion.envivoapp.adaptadores.AdapterGridPedidoCliente;
import com.aplicacion.envivoapp.adaptadores.AdapterListarVendedores;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroCancelarPedidoCliente;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProductoCliente extends AppCompatActivity implements CuadroCancelarPedidoCliente.resultadoDialogo {
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private List<Pedido> listPedido = new ArrayList<>();
    private GridView gridViewPedido;
    private AdapterGridPedidoCliente gridAdapterPedido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_cliente);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        gridViewPedido = findViewById(R.id.gridProductoCliente);

        listarProductos();

    }

    public void listarProductos(){
        databaseReference.child("Pedido").addValueEventListener(new ValueEventListener() { //buscamos todos los datos en la tabla Video Streaming
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listPedido.clear();//borramos en caso de quedar algo en la cache
                    for (final DataSnapshot ds : snapshot.getChildren()) {
                        Pedido pedido = ds.getValue(Pedido.class);//obtenemos el objeto video streaming
                        databaseReference.child("Cliente").child(pedido.getIdCliente()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    Cliente cliente = snapshot.getValue(Cliente.class);
                                    if(cliente.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())){
                                        listPedido.add(pedido);
                                        //Inicialisamos el adaptador
                                        gridAdapterPedido = new AdapterGridPedidoCliente(ProductoCliente.this, listPedido,databaseReference);
                                        gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                }else{
                    listPedido.clear();//borramos en caso de quedar algo en la cache
                    //Inicialisamos el adaptador
                    gridAdapterPedido = new AdapterGridPedidoCliente(ProductoCliente.this, listPedido,databaseReference);
                    gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view
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