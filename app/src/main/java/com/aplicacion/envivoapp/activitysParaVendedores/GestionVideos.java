package com.aplicacion.envivoapp.activitysParaVendedores;

import android.content.Intent;
import android.os.Bundle;

import com.aplicacion.envivoapp.cuadroDialogo.CuadroEditarLocal;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroEditarStraming;
import com.aplicacion.envivoapp.adaptadores.AdapterVideoStreaming;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.modelos.VideoStreaming;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;

import java.util.List;


public class GestionVideos extends AppCompatActivity implements CuadroEditarStraming.resultadoDialogo {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private List<VideoStreaming> listStreaming = new ArrayList<>();
    private ListAdapter adapterListStreaming;


    private GridView listaStreamingView;
    private Button agregarStreaming;
    private String idVendedor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_videos);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        //Inicializamos las variables

        listaStreamingView = findViewById(R.id.listStreaming);
        agregarStreaming = findViewById(R.id.btnAgregarStreaming);

        //Inicialisamos el adaptador
        listarStreamings();


        listaStreamingView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new CuadroEditarStraming(GestionVideos.this,
                        listStreaming.get(position),
                        false,firebaseAuth,firebaseDatabase,databaseReference,GestionVideos.this);
            }
        });

        agregarStreaming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CuadroEditarStraming(GestionVideos.this,
                        null,
                        true,firebaseAuth,firebaseDatabase,databaseReference,GestionVideos.this);

            }
        });

        //le damos funcionalidad al toolbar
        Button mensajeria = findViewById(R.id.btnMensajeriaGlobalGestionVideos);
        Button listarLocal = findViewById(R.id.btnListarLocalGestionVideos);
        Button perfil = findViewById(R.id.btnPerfilVendedorGestionVideos);
        Button pedido = findViewById(R.id.btnPedidoGestionVideos);
        Button videos = findViewById(R.id.btnVideosGestionVideos);
        Button salir = findViewById(R.id.btnSalirGestionVideos);
        Button clientes = findViewById(R.id.btnClientesGestionVideos);
        new Utilidades().cargarToolbarVendedor(listarLocal,
                perfil,
                pedido,
                mensajeria,
                salir,
                videos,
                clientes,
                GestionVideos.this,
                firebaseAuth);
    }

    public void limpiarGrid(){
        listStreaming.clear();
        //Inicialisamos el adaptador
        adapterListStreaming = new AdapterVideoStreaming(GestionVideos.this, listStreaming,databaseReference);
        listaStreamingView.setAdapter(adapterListStreaming); //configuramos el view
    }
    public void listarStreamings(){
        databaseReference.child("Vendedor").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Vendedor vendedor= null;
                    for (DataSnapshot ds:snapshot.getChildren()){
                        Vendedor vendedorAux = ds.getValue(Vendedor.class);
                        if(vendedorAux.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())){
                            vendedor=vendedorAux;
                            break;
                        }
                    }
                    if(vendedor!=null){
                        Vendedor finalVendedor = vendedor;
                        idVendedor = vendedor.getIdVendedor();
                        databaseReference.child("VideoStreaming").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    listStreaming.clear();
                                    for (DataSnapshot ds:snapshot.getChildren()){
                                        VideoStreaming videoStreaming = ds.getValue(VideoStreaming.class);
                                        if (videoStreaming.getIdVendedor().equals(finalVendedor.getIdVendedor())){
                                            listStreaming.add(videoStreaming);
                                        }
                                    }
                                        //Inicialisamos el adaptador
                                    adapterListStreaming = new AdapterVideoStreaming(GestionVideos.this, listStreaming,databaseReference);
                                    listaStreamingView.setAdapter(adapterListStreaming); //configuramos el view
                                }else{
                                   limpiarGrid();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                limpiarGrid();
                            }
                        });
                    }else{
                        limpiarGrid();
                    }
                }else{
                    limpiarGrid();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                limpiarGrid();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void resultado(Boolean isEliminado, Boolean isIrStreaming, VideoStreaming videoStreaming) {
        if (isEliminado){
            listarStreamings();
        }
        if (isIrStreaming){//en caso de que el usuario quiera iniciar el streaming
            Bundle parametros = new Bundle();
            parametros.putString("vendedor",idVendedor);
            parametros.putString("url",videoStreaming.getUrlVideoStreaming());
            parametros.putString("streaming",videoStreaming.getIdVideoStreaming());
            Intent streamingsIntent = new Intent(GestionVideos.this, MensajeriaVendedor.class);
            streamingsIntent.putExtras(parametros);
            startActivity(streamingsIntent);
        }
    }
}