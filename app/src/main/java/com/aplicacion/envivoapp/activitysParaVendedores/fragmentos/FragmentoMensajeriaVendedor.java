package com.aplicacion.envivoapp.activitysParaVendedores.fragmentos;

import android.app.Dialog;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterGridMensajeriaVendedor;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Mensaje_Cliente_Vendedor;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class FragmentoMensajeriaVendedor extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage ; //para la insersion de archivos

    private  String idCliente,idStreaming,urlStreaming;
    private Vendedor vendedorGlobal;
    private RadioButton filtrarPedido,filtrarTodos;
    private Button minimizarVideo;
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();

    private List<Mensaje> listMensaje = new ArrayList<>();
    private GridView gridViewMensaje;
    private AdapterGridMensajeriaVendedor gridAdapterMensaje;
    private YouTubePlayerView reproductorYoutube;
    private Boolean esMensajeNuevoCliente;
    private  boolean minimizar;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_mensajeria_vendedor, container, false);
        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        storage = FirebaseStorage.getInstance();//inicializamos la variable storage

        //incializaoms las variables

        gridViewMensaje = root.findViewById(R.id.gridMensajeVendedor);
        filtrarPedido = root.findViewById(R.id.radioFiltrarPedidoMensajeriaVendedor);
        filtrarTodos  = root.findViewById(R.id.radioFiltrarTodosMensajeriaVendedor);
        minimizarVideo = root.findViewById(R.id.btnMinimizarVideoMensajeriaVendedor);
        reproductorYoutube = root.findViewById(R.id.videoYutubeMensajeriaVendedor);
        //eproductorYoutube.initialize(new Utilidades().getClaveYoutube(), MensajeriaVendedor.this);
        //filtrarTodos.setChecked(true);//para que siempre  liste todos los mensajes


        //fin de incializacion de variables

        vendedorGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getVendedor(); //recogemos los datos del vendedor
        urlStreaming = ((MyFirebaseApp) getActivity().getApplicationContext()).getUrl();
        idStreaming =((MyFirebaseApp) getActivity().getApplicationContext()).getIdStreaming();

        minimizar = false;
        cargarClientesBloqueados();

        minimizarVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!minimizar) {
                    reproductorYoutube.setVisibility(View.GONE);
                    minimizarVideo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_maximizar, 0, 0);
                    minimizar = true;
                }else{
                    reproductorYoutube.setVisibility(View.VISIBLE);
                    minimizarVideo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_minimizar, 0, 0);
                    minimizar = false;
                }
            }
        });




        filtrarTodos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarPedido.setChecked(false);
                filtrarTodos.setChecked(true);
                listarMensajes();
            }
        });
        filtrarPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarPedido.setChecked(true);
                filtrarTodos.setChecked(false);
                listarMensajes();
            }
        });

        reproductorYoutube.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NotNull com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer youTubePlayer) {
                String resultado = urlStreaming;

                if(resultado.contains("https://youtu.be/")){
                    resultado = resultado.replace("https://youtu.be/","");
                }
                if(resultado.contains("https://youtube.com/")){
                    resultado = resultado.replace("https://youtube.com/","");
                }
                if(resultado.contains("watch?v=")){
                    resultado =  resultado.replace("watch?v=","");
                }
                if(resultado.contains("&")){
                    resultado = resultado.split("&")[0];

                }

                String videoId = resultado;
                youTubePlayer.loadVideo(videoId, 0);
            }

            @Override
            public void onError(@NotNull com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer youTubePlayer, @NotNull PlayerConstants.PlayerError error) {
                Toast.makeText(getContext(),"Error al reproducir el video",Toast.LENGTH_LONG).show();

                Fragment fragment = new FragmentoGestionVideos();
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                        .replace(R.id.home_content_vendedor, fragment)
                        .commit();
            }
        });

        reproductorYoutube.addFullScreenListener(new YouTubePlayerFullScreenListener() {
            @Override
            public void onYouTubePlayerEnterFullScreen() {
                reproductorYoutube.exitFullScreen();
            }

            @Override
            public void onYouTubePlayerExitFullScreen() {

            }
        });

        return root;
    }





    private void borrarGrid() {
        listMensaje.clear();
        //Inicialisamos el adaptador
        gridAdapterMensaje = new AdapterGridMensajeriaVendedor(getContext(),
                listMensaje,
                databaseReference,
                filtrarTodos.isChecked(),
                storage);
        gridViewMensaje.setAdapter(gridAdapterMensaje);
    }

    List<String> bloqueados = null;
    public void cargarClientesBloqueados(){
        Query queryCli= databaseReference.child("Mensaje_Cliente_Vendedor").
                orderByChild("vendedor/idVendedor").
                equalTo(vendedorGlobal.getIdVendedor());
        Dialog cargando = new Utilidades().dialogCargar(getContext());
        bloqueados = new ArrayList<>();
        cargando.show();
        filtrarTodos.setChecked(true);
        filtrarPedido.setChecked(false);
        queryCli.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        Mensaje_Cliente_Vendedor ms = ds.getValue(Mensaje_Cliente_Vendedor.class);
                        if (ms.getCliente().getBloqueado()){
                            bloqueados.add(ms.getCliente().getIdCliente());
                        }
                    }
                }
                cargando.dismiss();

                listarMensajes();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                cargando.dismiss();
            }
        });
    }

    public void listarMensajes(){
        borrarGrid();
        Query queryMensaje = databaseReference.child("Mensaje").
                orderByChild("idVendedor_idStreaming").
                equalTo(vendedorGlobal.getIdVendedor()+"_"+idStreaming);
        queryMensaje.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    listMensaje.clear();
                    for (DataSnapshot ds:snapshot.getChildren()){
                        Mensaje mensaje= ds.getValue(Mensaje.class);
                        if (mensaje!=null){
                            if (!bloqueados.isEmpty()) {
                                if (!bloqueados.contains(mensaje.getCliente().getIdCliente())) {//filtramos los mensajes de los clientes bloqueados
                                    filtrarMensajesPorCheckBox(mensaje);
                                }
                            }else{
                                filtrarMensajesPorCheckBox(mensaje);
                            }
                        }
                    }
                    gridAdapterMensaje = new AdapterGridMensajeriaVendedor(getContext(),
                            listMensaje,
                            databaseReference,
                            filtrarTodos.isChecked(),
                            storage);
                    gridViewMensaje.setAdapter(gridAdapterMensaje);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void filtrarMensajesPorCheckBox(Mensaje mensaje){
        if (filtrarPedido.isChecked()
                && !mensaje.getPedidoAceptado()
                && !mensaje.getPedidoCancelado()) {//filtramos por pedido

            try {
                mensaje.setTexto(encriptacionDatos.desencriptar(mensaje.getTexto()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mensaje.getTexto().indexOf("Quiero comprar:") == 0) {
                try {
                    mensaje.setImagen(encriptacionDatos.desencriptar(mensaje.getImagen()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                listMensaje.add(mensaje);
            }
        }
        if (filtrarTodos.isChecked()) {//filtramos a todos los usuarios
            try {
                mensaje.setImagen(encriptacionDatos.desencriptar(mensaje.getImagen()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mensaje.setTexto(encriptacionDatos.desencriptar(mensaje.getTexto()));
                listMensaje.add(mensaje);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
