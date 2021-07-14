package com.aplicacion.envivoapp.activitysParaVendedores;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RadioButton;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activityParaClientes.ListarStreamingsVendedor;
import com.aplicacion.envivoapp.activityParaClientes.ProductoCliente;
import com.aplicacion.envivoapp.adaptadores.AdapterGridPedidoCliente;
import com.aplicacion.envivoapp.adaptadores.AdapterGridPedidoVendedor;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PedidoVendedor extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private List<Pedido> listPedido = new ArrayList<>();
    private GridView gridViewPedido;
    private AdapterGridPedidoVendedor gridAdapterPedido;

    private RadioButton filtrarAceptados, filtrarEliminados, filtrarTodos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido_vendedor);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        gridViewPedido = findViewById(R.id.gridPedidoVendedor);

        filtrarAceptados = findViewById(R.id.radioAceptadoPedidoVendedor);
        filtrarEliminados = findViewById(R.id.radioEliminadosPedidoVendedor);
        filtrarTodos = findViewById(R.id.radioTodosPedidoVendedor);

        filtrarAceptados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarAceptados.setChecked(true);
                filtrarEliminados.setChecked(false);
                filtrarTodos.setChecked(false);
                listarFiltroAceptados();
            }
        });
        filtrarEliminados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarEliminados.setChecked(true);
                filtrarAceptados.setChecked(false);
                filtrarTodos.setChecked(false);
                listarFiltroEliminados();
            }
        });
        filtrarTodos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarTodos.setChecked(true);
                filtrarAceptados.setChecked(false);
                filtrarEliminados.setChecked(false);
                listarFiltroTodos();
            }
        });
    }


    private void borrarGrid(){
        listPedido.clear();//borramos en caso de quedar algo en la cache
        //Inicialisamos el adaptador
        gridAdapterPedido = new AdapterGridPedidoVendedor(PedidoVendedor.this, listPedido,databaseReference);
        gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view
    }
    private void listarFiltroTodos() {
        borrarGrid();
        databaseReference.child("Pedido").addValueEventListener(new ValueEventListener() { //buscamos todos los datos en la tabla Video Streaming
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listPedido.clear();//borramos en caso de quedar algo en la cache
                    for (final DataSnapshot ds : snapshot.getChildren()) {
                        Pedido pedido = ds.getValue(Pedido.class);//obtenemos el objeto pedido

                            databaseReference.child("Vendedor").child(pedido.getIdVendedor()).addValueEventListener(new ValueEventListener() {//buscamos el vendedor actual
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        Vendedor vendedor = snapshot.getValue(Vendedor.class);
                                        if (vendedor.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())) { //si el id es igual al uid del usuario actual y si el pedido esta aceptado
                                            listPedido.add(pedido);
                                            //Inicialisamos el adaptador
                                            gridAdapterPedido = new AdapterGridPedidoVendedor(PedidoVendedor.this, listPedido, databaseReference);
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
                    borrarGrid();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void listarFiltroEliminados() {
        borrarGrid();
        databaseReference.child("Pedido").addValueEventListener(new ValueEventListener() { //buscamos todos los datos en la tabla Video Streaming
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listPedido.clear();//borramos en caso de quedar algo en la cache
                    for (final DataSnapshot ds : snapshot.getChildren()) {
                        Pedido pedido = ds.getValue(Pedido.class);//obtenemos el objeto pedido
                        databaseReference.child("Vendedor").child(pedido.getIdVendedor()).addValueEventListener(new ValueEventListener() {//buscamos el vendedor actual
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    Vendedor vendedor = snapshot.getValue(Vendedor.class);
                                    if(vendedor.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())
                                            && filtrarEliminados.isChecked() &&
                                            pedido.getAceptado()==false
                                            &&pedido.getCancelado()==true){ //si el id es igual al uid del usuario actual y si el pedido esta aceptado
                                        listPedido.add(pedido);
                                        //Inicialisamos el adaptador
                                        gridAdapterPedido = new AdapterGridPedidoVendedor(PedidoVendedor.this, listPedido,databaseReference);
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
                    gridAdapterPedido = new AdapterGridPedidoVendedor(PedidoVendedor.this, listPedido,databaseReference);
                    gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    private void listarFiltroAceptados() {
        borrarGrid();
        databaseReference.child("Pedido").addValueEventListener(new ValueEventListener() { //buscamos todos los datos en la tabla Video Streaming
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listPedido.clear();//borramos en caso de quedar algo en la cache
                    for (final DataSnapshot ds : snapshot.getChildren()) {
                        Pedido pedido = ds.getValue(Pedido.class);//obtenemos el objeto pedido
                        databaseReference.child("Vendedor").child(pedido.getIdVendedor()).addValueEventListener(new ValueEventListener() {//buscamos el vendedor actual
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    Vendedor vendedor = snapshot.getValue(Vendedor.class);
                                    if(vendedor.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())
                                            && pedido.getAceptado()
                                            &&filtrarAceptados.isChecked()){ //si el id es igual al uid del usuario actual y si el pedido esta aceptado
                                        listPedido.add(pedido);
                                        //Inicialisamos el adaptador
                                        gridAdapterPedido = new AdapterGridPedidoVendedor(PedidoVendedor.this, listPedido,databaseReference);
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
                    gridAdapterPedido = new AdapterGridPedidoVendedor(PedidoVendedor.this, listPedido,databaseReference);
                    gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}