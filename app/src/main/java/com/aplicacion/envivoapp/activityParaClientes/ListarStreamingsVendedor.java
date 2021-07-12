package com.aplicacion.envivoapp.activityParaClientes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activitysParaVendedores.GestionVideos;
import com.aplicacion.envivoapp.adaptadores.AdapterVideoStreaming;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroDatosStreaming;
import com.aplicacion.envivoapp.modelos.VideoStreaming;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListarStreamingsVendedor extends AppCompatActivity implements CuadroDatosStreaming.resultadoDialogo {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private List<VideoStreaming> listStreaming = new ArrayList<>();
    private ListAdapter adapterListStreaming;
    private ListView listaStreamingView;
    private  String idVendedor,idCliente;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_streamings_vendedor);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        listaStreamingView = findViewById(R.id.listListarStreamingsVendedor);

        Bundle vendedor = ListarStreamingsVendedor.this.getIntent().getExtras();
        idVendedor = vendedor.getString("vendedor"); //recogemos los datos del vendedor
        idCliente = vendedor.getString("cliente");


        listarStreamings();


        listaStreamingView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView fechaAux = view.findViewById(R.id.txtItemFechaVideoStreaming);
                TextView horaAux = view.findViewById(R.id.txtItemHoraVideoStreaming);
                TextView urlAux = view.findViewById(R.id.txtItemUrlVideoStreaming);

                new CuadroDatosStreaming(ListarStreamingsVendedor.this,
                        idVendedor,
                        idCliente,
                        listStreaming.get(position).getIdVideoStreaming(),
                        urlAux.getText().toString(),fechaAux.getText().toString(),
                        horaAux.getText().toString(),
                        ListarStreamingsVendedor.this);
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
                        if (ds.child("idVendedor").getValue().toString().equals(idVendedor)) {
                            VideoStreaming videoStreaming = ds.getValue(VideoStreaming.class);//obtenemos el objeto video streaming
                            listStreaming.add(videoStreaming);
                            //Inicialisamos el adaptador
                            adapterListStreaming = new AdapterVideoStreaming(ListarStreamingsVendedor.this, R.layout.item_list_video_streaming, listStreaming);
                            listaStreamingView.setAdapter(adapterListStreaming); //configuramos el view
                        }
                    }
                }else{
                    listStreaming.clear();//borramos los datos ya que no hay nada en la base
                    //Inicialisamos el adaptador
                    adapterListStreaming = new AdapterVideoStreaming(ListarStreamingsVendedor.this, R.layout.item_list_video_streaming, listStreaming);
                    listaStreamingView.setAdapter(adapterListStreaming); //configuramos el view
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    @Override
    public void resultado(Boolean isVerStreamings,String idVendedor,String idCliente, String idStreaming,String url) {
        if (isVerStreamings){
            Bundle parametros = new Bundle();
            parametros.putString("vendedor",idVendedor);
            parametros.putString("url",url);
            parametros.putString("cliente",idCliente);
            parametros.putString("streaming",idStreaming);
            Intent streamingsIntent = new Intent(ListarStreamingsVendedor.this,MensajeriaCliente.class);
            streamingsIntent.putExtras(parametros);
            startActivity(streamingsIntent);
        }
    }
}