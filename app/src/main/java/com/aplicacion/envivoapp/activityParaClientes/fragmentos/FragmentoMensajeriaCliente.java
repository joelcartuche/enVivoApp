package com.aplicacion.envivoapp.activityParaClientes.fragmentos;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activityParaClientes.HomeClienteMain;
import com.aplicacion.envivoapp.activityParaClientes.fragmentos.navDataVendedor.FragmentoStreamigsVendedor;
import com.aplicacion.envivoapp.adaptadores.AdapterGridMensajeriaCliente;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Mensaje_Cliente_Vendedor;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.nmd.screenshot.Screenshot;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.jetbrains.annotations.NotNull;

import java.io.FileDescriptor;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FragmentoMensajeriaCliente extends Fragment implements YouTubePlayer.OnInitializedListener,
        YouTubePlayer.PlaybackEventListener, KeyEvent.Callback{
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage; //para la insersion de archivos
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();

    private Button enviarMensaje,quieroComprar;
    private EditText textoMensaje;
    private Cliente clienteGlobal;
    private Vendedor vendedorGlobal;
    private String idStreaming,urlStreaming;


    private List<Mensaje> listMensaje = new ArrayList<>();
    private RecyclerView gridViewMensaje;
    private AdapterGridMensajeriaCliente gridAdapterMensaje;
    private Bitmap bitmapCapturaPantalla;
    private YouTubePlayerView reproductorYoutube;
    private  Boolean esPrimerMensajeVendedor ;
    private TextView textoMensajeInfo;
    private Screenshot screenshot;
    private final int PICKER = 1;
    Boolean full = false; //almaceda el full screen del video de youtube

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_mensajeria_cliente, container, false);


        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        storage = FirebaseStorage.getInstance();//inicializamos la variable storage

        //incializaoms las variables
        enviarMensaje = root.findViewById(R.id.btnEnviarMensajeCliente);
        textoMensaje = root.findViewById(R.id.txtMensajeCliente);
        gridViewMensaje = root.findViewById(R.id.gridMensajeCliente);
        quieroComprar = root.findViewById(R.id.btnQuieroComprarMensajeCliente);
        textoMensajeInfo =root.findViewById(R.id.txtInfoMensaje);

        reproductorYoutube = root.findViewById(R.id.videoYoutubeMensajeriaCliente);
        getLifecycle().addObserver(reproductorYoutube);


        vendedorGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getVendedor(); //recogemos los datos del vendedor
        clienteGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getCliente();
        urlStreaming = ((MyFirebaseApp) getActivity().getApplicationContext()).getUrl(); //recogemos los datos del vendedor
        idStreaming = ((MyFirebaseApp) getActivity().getApplicationContext()).getIdStreaming();


        existeMensajeriaClienteVendedor();

        //en caso de que el cliente desee comprar
        quieroComprar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //selectorImagenes();
                textoMensaje.setText("Quiero comprar: ");
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

                Fragment fragment = new FragmentoStreamigsVendedor();


                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                        .replace(R.id.home_content, fragment)
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

        enviarMensaje.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                Mensaje mensaje = new Mensaje(); //instanciamos el mensaje

                LocalDateTime tiempoActual = LocalDateTime.now(); //obtenemos la hora y fecha actual
                // creamos la fecha
                Date fecha = new Date();
                fecha.setDate(tiempoActual.getDayOfMonth());
                fecha.setMonth(tiempoActual.getMonth().getValue());
                fecha.setYear(tiempoActual.getYear());
                fecha.setHours(tiempoActual.getHour());
                fecha.setMinutes(tiempoActual.getMinute());
                fecha.setSeconds(tiempoActual.getSecond());

                //creamo el mensaje
                mensaje.setFecha(fecha);
                String idMensaje = databaseReference.push().getKey();
                mensaje.setIdMensaje(idMensaje);
                try {
                    mensaje.setTexto(encriptacionDatos.encriptar(textoMensaje.getText().toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mensaje.setCliente(clienteGlobal);
                mensaje.setVendedor(vendedorGlobal);
                mensaje.setIdStreaming(idStreaming);
                mensaje.setPedidoAceptado(false);
                mensaje.setPedidoCancelado(false);
                mensaje.setEsVededor(false);
                mensaje.setPedidoCancelado(false);
                mensaje.setPedidoAceptado(false);
                mensaje.setIdCliente_idVendedor(clienteGlobal.getIdCliente()+"_"+vendedorGlobal.getIdVendedor());
                mensaje.setIdVendedor_idStreaming(vendedorGlobal.getIdVendedor()+"_"+idStreaming);
                textoMensaje.setText("");
                databaseReference.child("Mensaje").child(idMensaje).setValue(mensaje);
                if (esPrimerMensajeVendedor){
                    try {
                        crearMensajeriaClienteVendedor();
                        esPrimerMensajeVendedor = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        listarMensajes();
        return root;
    }

    private void existeMensajeriaClienteVendedor(){
        String idCliVende= clienteGlobal.getIdCliente()+"_"+vendedorGlobal.getIdVendedor();

        Dialog dialogoCargando = new Utilidades().dialogCargar(getContext());
        dialogoCargando.show();

        databaseReference.child("Mensaje_Cliente_Vendedor").
                child(idCliVende).
                get().
                addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        esPrimerMensajeVendedor = true;
                        dialogoCargando.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                if ( snapshot.exists()){
                    esPrimerMensajeVendedor = false;
                    dialogoCargando.dismiss();
                }else{
                    esPrimerMensajeVendedor = true;
                    dialogoCargando.dismiss();
                }

            }
        });
    }


    private void crearMensajeriaClienteVendedor() throws Exception {
        //encriptamos los datos del vendedor
        Vendedor vendeorAux = vendedorGlobal;
        vendeorAux.setCelular(encriptacionDatos.encriptar(vendeorAux.getCelular()));
        vendeorAux.setTelefono(encriptacionDatos.encriptar(vendeorAux.getTelefono()));
        vendeorAux.setNombre(encriptacionDatos.encriptar(vendeorAux.getNombre()));
        vendeorAux.setCedula(encriptacionDatos.encriptar(vendeorAux.getCedula()));

        Mensaje_Cliente_Vendedor mensaje_cliente_vendedor = new Mensaje_Cliente_Vendedor();
        mensaje_cliente_vendedor.setCliente(clienteGlobal);
        mensaje_cliente_vendedor.setVendedor(vendeorAux);
        mensaje_cliente_vendedor.setIdCliente_idVendedor(clienteGlobal.getIdCliente()+"_"+vendedorGlobal.getIdVendedor());
        databaseReference.
                child("Mensaje_Cliente_Vendedor").
                child(mensaje_cliente_vendedor.getIdCliente_idVendedor()).
                setValue(mensaje_cliente_vendedor);
    }


    public  void borrarGrid(){
        listMensaje.clear();//borramos los datos ya que no hay nada en la base
        gridAdapterMensaje = new AdapterGridMensajeriaCliente(getContext(),listMensaje,databaseReference,storage);
        gridViewMensaje.setAdapter(gridAdapterMensaje);
        gridViewMensaje.setLayoutManager(new LinearLayoutManager(getContext()));

    }
    public void listarMensajes(){
        Query query = databaseReference.child("Mensaje").
                orderByChild("idCliente_idVendedor").
                equalTo(clienteGlobal.getIdCliente()+"_"+vendedorGlobal.getIdVendedor());
        query.addValueEventListener(new ValueEventListener() { //buscamos todos los datos en la tabla Video Streaming
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listMensaje.clear();//borramos en caso de quedar algo en la cache
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Mensaje mensaje = ds.getValue(Mensaje.class);
                        if (mensaje.getIdStreaming() !=null) {
                            if (!mensaje.getCliente().getBloqueado()) {
                                if (mensaje.getVendedor().getIdVendedor().equals(vendedorGlobal.getIdVendedor())) {
                                    if (mensaje.getIdStreaming().equals(idStreaming)) {//aceptamos los mensades que sean del cliente y de el streaming actual
                                        if (mensaje.getImagen() != null) {
                                            try {
                                                mensaje.setImagen(encriptacionDatos.desencriptar(mensaje.getImagen()));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        try {
                                            mensaje.setTexto(encriptacionDatos.desencriptar(mensaje.getTexto()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        listMensaje.add(mensaje);
                                    }
                                }
                            }
                        }
                    }

                    gridAdapterMensaje = new AdapterGridMensajeriaCliente(getContext(), listMensaje, databaseReference, storage);
                    gridViewMensaje.setAdapter(gridAdapterMensaje);
                    gridViewMensaje.setLayoutManager(new LinearLayoutManager(getContext()));
                    gridViewMensaje.getLayoutManager().scrollToPosition(listMensaje.size()-1);

                }else{
                    borrarGrid();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean fueRestaurado) {
        if(!fueRestaurado){
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

            youTubePlayer.cueVideo(resultado);//https://www.youtube.com/watch?v=QN7BKarpltI

            listarMensajes();//listamos los mensajes del cliente
        }else{

            Toast.makeText(getContext(),"Error al reproducir el video",Toast.LENGTH_LONG).show();

            ((MyFirebaseApp) getActivity().getApplicationContext()).setLinkAcceso(null);
            Fragment fragment = new FragmentoStreamigsVendedor();

            ((HomeClienteMain) getActivity()).getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                    .replace(R.id.home_content, fragment)
                    .commit();
        }
    }


    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        if (youTubeInitializationResult.isUserRecoverableError()){
            youTubeInitializationResult.getErrorDialog(getActivity(),1).show();
        }else{//en caso de que el error no sea de youtube o este no lo conosca.
            String error ="Error al inisializar Youtube"+youTubeInitializationResult.toString();
            Toast.makeText(getContext(),error,Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PICKER:
                if (resultCode == RESULT_OK) {
                    Uri pathArchivo = data.getData(); //Obtenemos el uri de la imagen seleccionada
                    if(!pathArchivo.equals("")) {
                        try {
                            Bitmap bitImagen = getBitmapFromUri(pathArchivo);
                            bitmapCapturaPantalla = bitImagen;
                            textoMensaje.setText("Quiero comprar: ");
                            Toast.makeText(getContext(),"Ingrese el nombre o código y la cantidad del producto que desea",Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        }

    }


    @Override
    public void onPlaying() {

    }

    @Override
    public void onPaused() {

    }

    @Override
    public void onStopped() {

    }

    @Override
    public void onBuffering(boolean b) {

    }

    @Override
    public void onSeekTo(int i) {

    }



    private Bitmap getBitmapFromUri(Uri uri) throws IOException { //obtenemos un bitmap de la ruta real del archivo
        ParcelFileDescriptor parcelFileDescriptor =
                getActivity().getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    @Override
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        return false;
    }

    @Override
    public boolean onKeyLongPress(int i, KeyEvent keyEvent) {
        return false;
    }

    @Override
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        return false;
    }

    @Override
    public boolean onKeyMultiple(int i, int i1, KeyEvent keyEvent) {
        return false;
    }
}
