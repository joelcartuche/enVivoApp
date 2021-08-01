package com.aplicacion.envivoapp.activityParaClientes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activitysParaVendedores.GestionVideos;
import com.aplicacion.envivoapp.adaptadores.AdapterVideoStreaming;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroDatosStreaming;
import com.aplicacion.envivoapp.modelos.VideoStreaming;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private  FirebaseAuth.AuthStateListener authStateListener;

    private List<VideoStreaming> listStreaming = new ArrayList<>();
    private ListAdapter adapterListStreaming;
    private GridView listaStreamingView;
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
                if (listStreaming.get(position).getIniciado()) {
                    TextView fechaAux = view.findViewById(R.id.txtItemFechaVideoStreaming);
                    TextView horaAux = view.findViewById(R.id.txtItemHoraVideoStreaming);
                    TextView urlAux = view.findViewById(R.id.txtItemUrlVideoStreaming);

                    new CuadroDatosStreaming(ListarStreamingsVendedor.this,
                            idVendedor,
                            idCliente,
                            listStreaming.get(position).getIdVideoStreaming(),
                            urlAux.getText().toString(), fechaAux.getText().toString(),
                            horaAux.getText().toString(),
                            ListarStreamingsVendedor.this);
                }else{
                    Toast.makeText(ListarStreamingsVendedor.this,"El vendedor a terminado o no se inicia la transmision del video",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Damos funcionalidad al menu
        Button btnListarVendedore = findViewById(R.id.btn_listar_vendedores_listar_streaming);
        Button btnPerfil = findViewById(R.id.btn_perfil_listar_streaming_vendedor);
        Button btnPedido = findViewById(R.id.btn_carrito_listar_streaming_vendedor);
        Button btnSalir = findViewById(R.id.btn_perfil_listar_streaming_vendedor);
        Button btnMensje = findViewById(R.id.btnMensajeriaGlobalListarStremingsVendedor);

        Button btnHome = findViewById(R.id.btn_Home_Streaming_Vendedor);

        new Utilidades().cargarToolbar(btnHome,btnListarVendedore,
                btnPerfil,
                btnPedido,
                btnSalir,
                btnMensje,
                ListarStreamingsVendedor.this,firebaseAuth,databaseReference);
    }

    public void listarStreamings(){
        databaseReference.child("VideoStreaming").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listStreaming.clear();//borramos en caso de quedar algo en la cache
                    for (final DataSnapshot ds : snapshot.getChildren()) {
                        VideoStreaming videoStreaming = ds.getValue(VideoStreaming.class);
                        if (videoStreaming.getIdVendedor().equals(idVendedor)
                                && !videoStreaming.getEliminado()) { //listamos los streamings del vendedor que no han sido eliminados
                            listStreaming.add(videoStreaming);
                        }
                    }
                    //Inicialisamos el adaptador
                    adapterListStreaming = new AdapterVideoStreaming(ListarStreamingsVendedor.this,listStreaming,databaseReference);
                    listaStreamingView.setAdapter(adapterListStreaming); //configuramos el view
                }else{
                    listStreaming.clear();//borramos los datos ya que no hay nada en la base
                    //Inicialisamos el adaptador
                    adapterListStreaming = new AdapterVideoStreaming(ListarStreamingsVendedor.this, listStreaming,databaseReference);
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