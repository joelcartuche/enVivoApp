package com.aplicacion.envivoapp.activitysParaVendedores;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RadioButton;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterGridPedidoVendedor;
import com.aplicacion.envivoapp.modelos.Pedido;
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

        //le damos funcionalidad al toolbar
        Button mensajeria = findViewById(R.id.btnMensajeriaGlobalPedidoVendedor);
        Button listarLocal = findViewById(R.id.btnListarLocalPedidoVendedor);
        Button perfil = findViewById(R.id.btnPerfilVendedorPedidoVendedor);
        Button pedido = findViewById(R.id.btnPedidoPedidoVendedor);
        Button videos = findViewById(R.id.btnVideosPedidoVendedor);
        Button salir = findViewById(R.id.btnSalirPedidoVendedor);
        Button clientes = findViewById(R.id.btnClientesPedidoVendedo);

        new Utilidades().cargarToolbarVendedor(listarLocal,
                perfil,
                pedido,
                mensajeria,
                salir,
                videos,
                clientes,
                PedidoVendedor.this,
                firebaseAuth);
    }


    private void borrarGrid(){
        listPedido.clear();//borramos en caso de quedar algo en la cache
        //Inicialisamos el adaptador
        gridAdapterPedido = new AdapterGridPedidoVendedor(PedidoVendedor.this, listPedido,databaseReference);
        gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view
    }
    private void listarFiltroTodos() {
        borrarGrid();//borramos los datos del grid
        databaseReference.child("Vendedor").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Vendedor vendedor = null;
                    for (DataSnapshot ds:snapshot.getChildren()){
                        Vendedor vendedorAux = ds.getValue(Vendedor.class);
                        if (vendedorAux!=null){
                            if (vendedorAux.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())){
                                vendedor=vendedorAux;
                                break;
                            }
                        }
                    }
                    if (vendedor!= null){
                        Vendedor finalVendedor = vendedor;
                        databaseReference.child("Pedido").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    for (DataSnapshot ds:snapshot.getChildren()){
                                        Pedido pedido = ds.getValue(Pedido.class);
                                        if (pedido.getIdVendedor().equals(finalVendedor.getIdVendedor())){
                                            listPedido.add(pedido);
                                        }
                                    }
                                    if (listPedido.size()==0){
                                        borrarGrid();
                                    }else{
                                        //Inicialisamos el adaptador
                                        gridAdapterPedido = new AdapterGridPedidoVendedor(PedidoVendedor.this, listPedido,databaseReference);
                                        gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view
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

    private void listarFiltroEliminados() {
        borrarGrid();//borramos los datos del grid
        databaseReference.child("Vendedor").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Vendedor vendedor = null;
                    for (DataSnapshot ds:snapshot.getChildren()){
                        Vendedor vendedorAux = ds.getValue(Vendedor.class);
                        if (vendedorAux!=null){
                            if (vendedorAux.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())){
                                vendedor=vendedorAux;
                                break;
                            }
                        }
                    }
                    if (vendedor!= null){
                        Vendedor finalVendedor = vendedor;
                        databaseReference.child("Pedido").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    for (DataSnapshot ds:snapshot.getChildren()){
                                        Pedido pedido = ds.getValue(Pedido.class);
                                        if (pedido.getIdVendedor().equals(finalVendedor.getIdVendedor())
                                                && filtrarEliminados.isChecked()
                                                && !pedido.getAceptado()
                                                && pedido.getCancelado()){
                                            listPedido.add(pedido);
                                        }
                                    }
                                    if (listPedido.size()==0){
                                        borrarGrid();
                                    }else{
                                        //Inicialisamos el adaptador
                                        gridAdapterPedido = new AdapterGridPedidoVendedor(PedidoVendedor.this, listPedido,databaseReference);
                                        gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view
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


    private void listarFiltroAceptados() {
        borrarGrid();//borramos los datos del grid
        databaseReference.child("Vendedor").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Vendedor vendedor = null;
                    for (DataSnapshot ds:snapshot.getChildren()){
                        Vendedor vendedorAux = ds.getValue(Vendedor.class);
                        if (vendedorAux!=null){
                            if (vendedorAux.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())){
                                vendedor=vendedorAux;
                                break;
                            }
                        }
                    }
                    if (vendedor!= null){
                        Vendedor finalVendedor = vendedor;
                        databaseReference.child("Pedido").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    for (DataSnapshot ds:snapshot.getChildren()){
                                        Pedido pedido = ds.getValue(Pedido.class);
                                        if (pedido.getIdVendedor().equals(finalVendedor.getIdVendedor())
                                                && pedido.getAceptado()
                                                &&filtrarAceptados.isChecked()){
                                            listPedido.add(pedido);
                                        }
                                    }
                                    if (listPedido.size()==0){
                                        borrarGrid();
                                    }else{
                                        //Inicialisamos el adaptador
                                        gridAdapterPedido = new AdapterGridPedidoVendedor(PedidoVendedor.this, listPedido,databaseReference);
                                        gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view
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