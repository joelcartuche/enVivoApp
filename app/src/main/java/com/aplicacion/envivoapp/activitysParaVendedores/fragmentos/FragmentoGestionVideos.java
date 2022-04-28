package com.aplicacion.envivoapp.activitysParaVendedores.fragmentos;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activitysParaVendedores.HomeVendedorMain;
import com.aplicacion.envivoapp.adaptadores.AdapterVideoStreaming;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroEditarStraming;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.modelos.VideoStreaming;
import com.aplicacion.envivoapp.utilidades.BuscarVendedorUid;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FragmentoGestionVideos extends Fragment implements
        CuadroEditarStraming.resultadoDialogo{

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
    private  Vendedor vendedorGlobal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_gestion_videos, container, false);
        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        //Inicializamos las variables

        listaStreamingView = root.findViewById(R.id.listStreaming);
        agregarStreaming = root.findViewById(R.id.btnAgregarStreaming);
        buscador = root.findViewById(R.id.busquedaStreamingsGestionVideos);


        anadidos = root.findViewById(R.id.radioAnadidoStreaming);
        eliminados= root.findViewById(R.id.radioEliminadosStreamings);
        buscador.setVisibility(View.GONE);
        anadidos.setChecked(true);
        vendedorGlobal = ((MyFirebaseApp) getContext().getApplicationContext()).getVendedor();
        //Inicialisamos el adaptador
        if (vendedorGlobal!=null){
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
                    new CuadroEditarStraming(getContext(),
                            listStreaming.get(position),
                            false,firebaseAuth,firebaseDatabase,databaseReference,FragmentoGestionVideos.this::resultado,getActivity());
                }
            });

            agregarStreaming.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new CuadroEditarStraming(getContext(),
                            null,
                            true,firebaseAuth,firebaseDatabase,databaseReference,FragmentoGestionVideos.this::resultado,getActivity());

                }
            });
        }

        return root;
    }


    public void limpiarGrid(){
        listStreaming.clear();
        //Inicialisamos el adaptador
        adapterListStreaming = new AdapterVideoStreaming(getContext(), listStreaming,databaseReference);
        listaStreamingView.setAdapter(adapterListStreaming); //configuramos el view
    }
    public void listarStreamings(){
        Query queryVideo = databaseReference.child("VideoStreaming").orderByChild("idVendedor").equalTo(vendedorGlobal.getIdVendedor());
        queryVideo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    limpiarGrid();
                    for (DataSnapshot ds:snapshot.getChildren()){
                        VideoStreaming videoStreaming = ds.getValue(VideoStreaming.class);
                        if (videoStreaming != null){
                            if (anadidos.isChecked() && !eliminados.isChecked()) {//en caso de que el filtro añadidos este habilitado
                                if (!videoStreaming.getEliminado() && !videoStreaming.getEliminadoCompleto()) {
                                    try {
                                        videoStreaming.setUrlVideoStreaming(encriptacionDatos.desencriptar(videoStreaming.getUrlVideoStreaming()));
                                        listStreaming.add(videoStreaming);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            } else if (eliminados.isChecked() && !anadidos.isChecked()) {//en caso de que el filtro eliminados este habilitado
                                if (videoStreaming.getEliminado()&& !videoStreaming.getEliminadoCompleto()) {
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
                    adapterListStreaming = new AdapterVideoStreaming(getContext(), listStreaming,databaseReference);
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
    }



    @Override
    public void resultado(Boolean isEliminado, Boolean isIrStreaming, VideoStreaming videoStreaming) {
        if (isEliminado){
            listarStreamings();
        }
        if (isIrStreaming){//en caso de que el usuario quiera iniciar el streaming
            ((MyFirebaseApp) getActivity().getApplicationContext()).setUrl(videoStreaming.getUrlVideoStreaming()); //recogemos los datos del vendedor
            ((MyFirebaseApp) getActivity().getApplicationContext()).setIdStreaming(videoStreaming.getIdVideoStreaming()); //recogemos los datos del vendedor

            Fragment fragment = new FragmentoMensajeriaVendedor();
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                    .replace(R.id.home_content_vendedor, fragment)
                    .commit();

        }
    }


}
