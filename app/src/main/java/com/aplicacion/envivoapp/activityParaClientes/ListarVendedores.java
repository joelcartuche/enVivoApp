package com.aplicacion.envivoapp.activityParaClientes;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.aplicacion.envivoapp.adaptadores.AdapterListarVendedores;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroListarVendedor;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Vendedor;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toolbar;


import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.utilidades.RetornoParametroCliente;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.common.api.Api;
import com.google.android.material.bottomnavigation.BottomNavigationMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListarVendedores extends AppCompatActivity implements CuadroListarVendedor.resultadoDialogo{

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private List<Vendedor> listVendedor = new ArrayList<>();
    private ListAdapter adapterListVendedor;
    private ListView listaVendedorView;
    private  Boolean irStreaming=false;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_vendedores);


        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        listaVendedorView = findViewById(R.id.listVendedores);


        listarVendedores();

        listaVendedorView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new CuadroListarVendedor(ListarVendedores.this,listVendedor.get(position),ListarVendedores.this);
            }
        });



    }

    public void listarVendedores(){
        databaseReference.child("Vendedor").addValueEventListener(new ValueEventListener() { //buscamos todos los datos en la tabla Video Streaming
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listVendedor.clear();//borramos en caso de quedar algo en la cache
                    for (final DataSnapshot ds : snapshot.getChildren()) {
                        Vendedor vendedor = ds.getValue(Vendedor.class);//obtenemos el objeto video streaming
                        listVendedor.add(vendedor);
                        //Inicialisamos el adaptador
                        adapterListVendedor = new AdapterListarVendedores(ListarVendedores.this, R.layout.item_list_vendedores, listVendedor);
                        listaVendedorView.setAdapter(adapterListVendedor); //configuramos el view
                    }
                }else{
                    listVendedor.clear();//borramos los datos ya que no hay nada en la base
                    //Inicialisamos el adaptador
                    adapterListVendedor = new AdapterListarVendedores(ListarVendedores.this, R.layout.item_list_vendedores, listVendedor);
                    listaVendedorView.setAdapter(adapterListVendedor); //configuramos el view
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void resultado(Boolean isVerStreamings, Vendedor vendedor) {
        if(isVerStreamings){
            databaseReference.child("Cliente").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        for ( DataSnapshot ds : snapshot.getChildren()) {
                            Cliente cliente = ds.getValue(Cliente.class);//obtenemos el objeto video streaming
                            Bundle parametros = new Bundle();
                            parametros.putString("vendedor",vendedor.getIdVendedor());
                            parametros.putString("cliente",cliente.getIdCliente());
                            Intent streamingsIntent = new Intent(ListarVendedores.this,ListarStreamingsVendedor.class);
                            streamingsIntent.putExtras(parametros);
                            startActivity(streamingsIntent);
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