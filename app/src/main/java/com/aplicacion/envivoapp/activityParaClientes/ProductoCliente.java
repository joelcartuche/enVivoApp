package com.aplicacion.envivoapp.activityParaClientes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.GridView;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activitysParaVendedores.PedidoVendedor;
import com.aplicacion.envivoapp.adaptadores.AdapterGridPedidoCliente;
import com.aplicacion.envivoapp.adaptadores.AdapterGridPedidoVendedor;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroCancelarPedidoCliente;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Pedido;
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
        setContentView(R.layout.activity_pedido_cliente);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        gridViewPedido = findViewById(R.id.gridProductoCliente);

        listarProductos();

    }
    private void borrarGrid(){
        listPedido.clear();//borramos en caso de quedar algo en la cache
        //Inicialisamos el adaptador
        gridAdapterPedido = new AdapterGridPedidoCliente(ProductoCliente.this, listPedido,databaseReference);
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
                         if (clienteAux.getUidUsuario().equals(firebaseAuth.getUid())){
                             cliente=clienteAux;
                         }
                    }

                    if (cliente!= null){
                        databaseReference.child("Pedido").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    listPedido.clear();//borramos en caso de quedar algo en la cache
                                    for (final DataSnapshot ds : snapshot.getChildren()) {
                                        Pedido pedido = ds.getValue(Pedido.class);//obtenemos
                                        if (pedido.getAceptado()) {
                                            listPedido.add(pedido);
                                        }
                                    }
                                    //Inicialisamos el adaptador
                                    gridAdapterPedido = new AdapterGridPedidoCliente(ProductoCliente.this, listPedido,databaseReference);
                                    gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view

                                }else{
                                    if (listPedido.size()==0){
                                        Toast.makeText(ProductoCliente.this,"Usted no tiene pedidos",Toast.LENGTH_LONG).show();
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