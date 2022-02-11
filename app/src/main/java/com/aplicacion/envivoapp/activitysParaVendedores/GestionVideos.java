package com.aplicacion.envivoapp.activitysParaVendedores;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.aplicacion.envivoapp.cuadroDialogo.CuadroEditarStraming;
import com.aplicacion.envivoapp.adaptadores.AdapterVideoStreaming;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.modelos.VideoStreaming;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.RadioButton;
import android.widget.SearchView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;

import java.util.List;


public class GestionVideos extends AppCompatActivity implements CuadroEditarStraming.resultadoDialogo {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private List<VideoStreaming> listStreaming = new ArrayList<>();
    private AdapterVideoStreaming adapterListStreaming;
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();


    private GridView listaStreamingView;
    private Button agregarStreaming;
    private String idVendedor;
    private RadioButton anadidos,eliminados;
    private EditText buscador;



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
        buscador = findViewById(R.id.busquedaStreamingsGestionVideos);


        anadidos = findViewById(R.id.radioAnadidoStreaming);
        eliminados= findViewById(R.id.radioEliminadosStreamings);

        anadidos.setChecked(true);
        //Inicialisamos el adaptador
        listarStreamings();

        anadidos.setOnClickListener(new View.OnClickListener() {//funcionalidad del boton añadir
            @Override
            public void onClick(View v) {
                //cambio de banderas
                eliminados.setChecked(false);
                anadidos.setChecked(true);
                listarStreamings();
            }
        });
        eliminados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cambio de banderas
                anadidos.setChecked(false);
                eliminados.setChecked(true);
                listarStreamings();
            }
        });

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


        //le damos funcionalidad al buscador
        buscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<VideoStreaming> listaAux = new ArrayList<>();
                for (VideoStreaming vd : listStreaming) {
                    if (vd.toString().indexOf(s.toString()) == 0) {
                        listaAux.add(vd);
                    }
                }
                if (listaAux.size() != 0) {
                    //Inicialisamos el adaptador
                    adapterListStreaming = new AdapterVideoStreaming(GestionVideos.this, listaAux, databaseReference);
                    listaStreamingView.setAdapter(adapterListStreaming); //configuramos el view
                }else{
                    //Inicialisamos el adaptador
                    adapterListStreaming = new AdapterVideoStreaming(GestionVideos.this, listStreaming, databaseReference);
                    listaStreamingView.setAdapter(adapterListStreaming); //configuramos el view
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

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
        Button reporte = findViewById(R.id.btnReporteGestionVideos);
        Button home = findViewById(R.id.btnHomeVendedorGestionVideos);

        new Utilidades().cargarToolbarVendedor(home,
                listarLocal,
                perfil,
                pedido,
                mensajeria,
                salir,
                videos,
                clientes,
                reporte,
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
        Query query = databaseReference.child("Vendedor").orderByChild("uidUsuario").equalTo(firebaseAuth.getCurrentUser().getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                limpiarGrid();
                if (snapshot.exists()){
                    Vendedor vendedor= null;
                    for (DataSnapshot ds:snapshot.getChildren()){
                        vendedor = ds.getValue(Vendedor.class);
                    }
                    if(vendedor!=null){
                        idVendedor = vendedor.getIdVendedor();
                        Query queryVideo = databaseReference.child("VideoStreaming").orderByChild("idVendedor").equalTo(vendedor.getIdVendedor());
                        queryVideo.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    limpiarGrid();
                                    for (DataSnapshot ds:snapshot.getChildren()){
                                        VideoStreaming videoStreaming = ds.getValue(VideoStreaming.class);
                                        if (videoStreaming != null){
                                            if (anadidos.isChecked() && !eliminados.isChecked()) {//en caso de que el filtro añadidos este habilitado
                                                if (!videoStreaming.getEliminado()) {
                                                    try {
                                                        videoStreaming.setUrlVideoStreaming(encriptacionDatos.desencriptar(videoStreaming.getUrlVideoStreaming()));
                                                        listStreaming.add(videoStreaming);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }

                                                }
                                            } else if (eliminados.isChecked() && !anadidos.isChecked()) {//en caso de que el filtro eliminados este habilitado
                                                if (videoStreaming.getEliminado()) {
                                                    try {
                                                        videoStreaming.setUrlVideoStreaming(encriptacionDatos.desencriptar(videoStreaming.getUrlVideoStreaming()));
                                                        listStreaming.add(videoStreaming);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
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