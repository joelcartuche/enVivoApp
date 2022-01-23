package com.aplicacion.envivoapp.activitysParaVendedores;

import android.os.Bundle;

import com.aplicacion.envivoapp.adaptadores.AdapterListarLocal;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroEditarLocal;
import com.aplicacion.envivoapp.modelos.Local;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DataLocal extends AppCompatActivity implements CuadroEditarLocal.resultadoDialogo{

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private List<Local> listLocal = new ArrayList<>();
    private GridView gridViewLocal;
    private AdapterListarLocal gridAdapterLocal;

    private Button agregarLocal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_local);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        gridViewLocal = findViewById(R.id.gridLocal);
        agregarLocal = findViewById(R.id.btnAgregarLocal);

        agregarLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child("Vendedor").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Vendedor vendedor = null;
                            for (DataSnapshot ds:snapshot.getChildren()){
                                Vendedor vendedorAux = ds.getValue(Vendedor.class);
                                if (vendedorAux!=null){
                                    if (vendedorAux.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())){
                                        vendedor = vendedorAux;
                                        break;
                                    }
                                }
                            }
                            if (vendedor!= null){
                                ((MyFirebaseApp) getBaseContext().getApplicationContext()).setLatLng(null);
                                new CuadroEditarLocal(DataLocal.this,null,vendedor,true,databaseReference,DataLocal.this);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });



        //le damos funcionalidad al toolbar
        //le damos funcionalidad al toolbar
        Button mensajeria = findViewById(R.id.btnMensajeriaGlobalDataLocal);
        Button listarLocal = findViewById(R.id.btnListarLocalDataLocal);
        Button perfil = findViewById(R.id.btnPerfilVendedorDataLocal);
        Button pedido = findViewById(R.id.btnPedidoDataLocal);
        Button videos = findViewById(R.id.btnVideosDataLocal);
        Button salir = findViewById(R.id.btnSalirDataLocal);
        Button clientes = findViewById(R.id.btnClientesDataLocal);
        Button reporte = findViewById(R.id.btnReporteDataLocal);
        Button home = findViewById(R.id.btnHomeVendedorDataLocal);


        new Utilidades().cargarToolbarVendedor(home,
                listarLocal,
                perfil,
                pedido,
                mensajeria,
                salir,
                videos,
                clientes,
                reporte,
                DataLocal.this,
                firebaseAuth);

        listarLocal();
    }

    public  void listarLocal(){
        databaseReference.child("Local").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listLocal.clear();
                if (snapshot.exists()){
                    for (DataSnapshot ds:snapshot.getChildren()){
                        Local local = ds.getValue(Local.class);
                        if (local != null){// en caso de que exista el local
                            databaseReference.child("Vendedor").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        Vendedor vendedor = null;
                                        for (DataSnapshot ds:snapshot.getChildren()){
                                            Vendedor vendedorAux = ds.getValue(Vendedor.class);
                                            if (vendedorAux!=null){
                                                if (vendedorAux.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())){
                                                    vendedor = vendedorAux;
                                                }
                                            }
                                        }
                                        if (vendedor!= null){
                                            if(local.getIdVendedor().equals(vendedor.getIdVendedor())) {
                                                listLocal.add(local);
                                                gridAdapterLocal = new AdapterListarLocal(DataLocal.this, listLocal,firebaseAuth, databaseReference);
                                                gridViewLocal.setAdapter(gridAdapterLocal); //configuramos el view
                                                funcionalidadGrid();
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
                }else{

                    gridAdapterLocal = new AdapterListarLocal(DataLocal.this, listLocal, firebaseAuth,databaseReference);
                    gridViewLocal.setAdapter(gridAdapterLocal); //configuramos el view
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public void resultado() {

    }

    public void funcionalidadGrid(){
        gridViewLocal.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Local local = listLocal.get(position);
                databaseReference.child("Vendedor").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Vendedor vendedor = null;
                            for (DataSnapshot ds:snapshot.getChildren()){
                                Vendedor vendedorAux = ds.getValue(Vendedor.class);
                                if (vendedorAux!=null){
                                    if (vendedorAux.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())){
                                        vendedor = vendedorAux;
                                        break;
                                    }
                                }
                            }
                            if (vendedor!= null){
                                new CuadroEditarLocal(DataLocal.this,local,vendedor,false,databaseReference,DataLocal.this);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }
}