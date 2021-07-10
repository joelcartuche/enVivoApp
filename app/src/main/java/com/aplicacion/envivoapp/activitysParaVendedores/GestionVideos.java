package com.aplicacion.envivoapp.activitysParaVendedores;

import android.os.Bundle;

import com.aplicacion.envivoapp.cuadroDialogo.CuadroEditarStraming;
import com.aplicacion.envivoapp.adaptadores.AdapterVideoStreaming;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.modelos.VideoStreaming;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.List;
import java.util.UUID;


public class GestionVideos extends AppCompatActivity implements CuadroEditarStraming.resultadoDialogo {




    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private List<VideoStreaming> listStreaming = new ArrayList<>();
    private ListAdapter adapterListStreaming;

    private EditText urlVideoStreaming, fechaStreaming, horaStreaming;
    private ListView listaStreamingView;
    private Button guardarStreaming;
    private String idVendedor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_videos);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        //Inicializamos las variables
        urlVideoStreaming = findViewById(R.id.txtUrlCuadroStreaming);
        fechaStreaming = findViewById(R.id.txtFechaCuadroStreaming);
        horaStreaming = findViewById(R.id.txtHoraCuadroStreaming);
        listaStreamingView = findViewById(R.id.listStreaming);
        guardarStreaming = findViewById(R.id.btnGuardarEdicionStreaming);

        //Inicialisamos el adaptador
        listarStreamings();
        
        guardarStreaming.setOnClickListener(new View.OnClickListener() {//ponemos la accion onClick en el  boton guardar
            @Override
            public void onClick(View v) {
                if (!urlVideoStreaming.getText().toString().equals("")
                        && !fechaStreaming.getText().toString().equals("")
                        && !horaStreaming.getText().toString().equals("")) {

                    databaseReference.child("Vendedor").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                for(DataSnapshot ds:snapshot.getChildren()){
                                    Vendedor vendedor = ds.child("uidUsuario").getValue(Vendedor.class);
                                    if (vendedor.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())){
                                        VideoStreaming videoStreaming = new VideoStreaming();//inicializamos la variable que va a contener a la clase VideoStreaming
                                        videoStreaming.setUrlVideoStreaming(urlVideoStreaming.getText().toString()); //setiamos el url del video

                                        String[] fecha = fechaStreaming.getText().toString().split("/");//separamos la fecha en un arreglo
                                        if(Integer.parseInt(fecha[0])<10){
                                            fecha[0]="0"+fecha[0]; //añadimos un cero en caso de que la fecha se una sola unidad
                                        }
                                        if(Integer.parseInt(fecha[1])<10){
                                            fecha[1]="0"+fecha[1]; //añadimos un cero en la fecha en caso de que sea una solo unidad
                                        }

                                        String fechaFormat = fecha[0]+"-"+fecha[1]+"-"+fecha[2]+" "+horaStreaming.getText().toString(); //le damos formato a la fecha
                                        Date fechaTransmision = null;//creamos una variable de tipo date para luego almacenarla en  la clase videoStreaming
                                        try {
                                            fechaTransmision = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(fechaFormat); //almacenamos la fecha Date
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        fechaTransmision.setMonth(Integer.parseInt(fecha[1]));//editamos el mes ya que presenta error en la base
                                        fechaTransmision.setYear(Integer.parseInt(fecha[2])); //editamos el año por error en la base
                                        videoStreaming.setFechaTransmision(fechaTransmision);//seteamos la fecha de trasmision
                                        videoStreaming.setIdVideoStreaming(UUID.randomUUID().toString()); //seteamos el id
                                        videoStreaming.setIdVendedor(vendedor.getIdVendedor()); //seteamos el uid del vendedor
                                        databaseReference.child("VideoStreaming").child(videoStreaming.getIdVideoStreaming()).setValue(videoStreaming);
                                        Toast.makeText(GestionVideos.this, "Datos guardados con exito", Toast.LENGTH_LONG).show();
                                        listarStreamings();
                                    }

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } else {
                    Toast.makeText(GestionVideos.this, "Asegurese de aver ingresado todos los datos", Toast.LENGTH_LONG).show();
                }
            }
        });

        Utilidades utilidades = new Utilidades();
        fechaStreaming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                utilidades.abrirCalendario(v,GestionVideos.this,fechaStreaming);//mostramos el calendario
            }
        });

        horaStreaming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                utilidades.abrirCalendarioHora(v,GestionVideos.this,horaStreaming);//mostramos la hora
            }
        });

        listaStreamingView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView fechaAux = view.findViewById(R.id.txtItemFechaVideoStreaming);
                TextView horaAux = view.findViewById(R.id.txtItemHoraVideoStreaming);
                new CuadroEditarStraming(GestionVideos.this,listStreaming.get(position),fechaAux.getText().toString(),horaAux.getText().toString(),firebaseDatabase,GestionVideos.this);
            }
        });


    }

    public void listarStreamings(){
            databaseReference.child("VideoStreaming").addValueEventListener(new ValueEventListener() { //buscamos todos los datos en la tabla Video Streaming
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        listStreaming.clear();//borramos en caso de quedar algo en la cache
                        for (final DataSnapshot ds : snapshot.getChildren()) {
                            if(ds.child("idVendedor").getValue() != null){
                            if (ds.child("idVendedor").getValue().toString().equals(firebaseAuth.getCurrentUser().getUid())) {
                                VideoStreaming videoStreaming = ds.getValue(VideoStreaming.class);//obtenemos el objeto video streaming
                                listStreaming.add(videoStreaming);
                                //Inicialisamos el adaptador
                                adapterListStreaming = new AdapterVideoStreaming(GestionVideos.this, R.layout.item_list_video_streaming, listStreaming);
                                listaStreamingView.setAdapter(adapterListStreaming); //configuramos el view
                            }}
                        }
                    }else{
                        listStreaming.clear();//borramos los datos ya que no hay nada en la base
                        //Inicialisamos el adaptador
                        adapterListStreaming = new AdapterVideoStreaming(GestionVideos.this, R.layout.item_list_video_streaming, listStreaming);
                        listaStreamingView.setAdapter(adapterListStreaming); //configuramos el view
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

    }





    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void resultado(Boolean isEliminado) {
        if (isEliminado){
            listarStreamings();
        }
    }


}