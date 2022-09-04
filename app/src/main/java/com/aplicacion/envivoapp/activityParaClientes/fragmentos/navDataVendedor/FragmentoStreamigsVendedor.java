package com.aplicacion.envivoapp.activityParaClientes.fragmentos.navDataVendedor;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activityParaClientes.fragmentos.FragmentoHomeCliente;
import com.aplicacion.envivoapp.activityParaClientes.fragmentos.FragmentoMensajeriaCliente;
import com.aplicacion.envivoapp.adaptadores.AdapterVideoStreaming;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroDatosStreaming;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.modelos.VideoStreaming;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FragmentoStreamigsVendedor extends Fragment implements CuadroDatosStreaming.resultadoDialogo {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private  FirebaseAuth.AuthStateListener authStateListener;
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();

    private List<VideoStreaming> listStreaming = new ArrayList<>();
    private ListAdapter adapterListStreaming;
    private GridView listaStreamingView;
    private Cliente clienteGlobal;
    private Vendedor vendedorGlobal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_listar_streamings_vendedor, container, false);
        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        listaStreamingView = root.findViewById(R.id.listListarStreamingsVendedor);

        vendedorGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getVendedor(); //recogemos los datos del vendedor
        clienteGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getCliente();
        Uri linkAcceso = ((MyFirebaseApp) getActivity().getApplicationContext()).getLinkAcceso();
        if (linkAcceso != null){

            if (linkAcceso!=null){
                Log.d("Vendedor",linkAcceso.getQueryParameter("idvendedor"));
                Log.d("Vendedor",linkAcceso.getQueryParameter("urlStreaming"));
                databaseReference.child("Vendedor").child(linkAcceso.getQueryParameter("idvendedor")).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Vendedor vendedor = snapshot.getValue(Vendedor.class);
                            if (vendedor!=null){
                                databaseReference.child("VideoStreaming").child(linkAcceso.getQueryParameter("idStreaming")).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                    @Override
                                    public void onSuccess(DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            VideoStreaming videoStreaming = snapshot.getValue(VideoStreaming.class);
                                            if (videoStreaming != null){

                                                try {
                                                    //recogemos los datos que fueron enviados desde el link
                                                    ((MyFirebaseApp) getActivity().getApplicationContext()).setVendedor(vendedor);
                                                    ((MyFirebaseApp) getActivity().getApplicationContext()).setIdStreaming(videoStreaming.getIdVideoStreaming());
                                                    ((MyFirebaseApp) getActivity().getApplicationContext()).setUrl(encriptacionDatos.desencriptar(videoStreaming.getUrlVideoStreaming()));
                                                    ((MyFirebaseApp) getActivity().getApplicationContext()).setGlobal(false);

                                                    //enviamos a la mensajeria del video
                                                    Fragment fragment = new FragmentoMensajeriaCliente();
                                                    getActivity().getSupportFragmentManager()
                                                            .beginTransaction()
                                                            .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                                                            .replace(R.id.home_content, fragment)
                                                            .commit();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    errorLinkAcceso();
                                                }

                                            }else{
                                                errorLinkAcceso();
                                            }
                                        }else {
                                            errorLinkAcceso();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        errorLinkAcceso();
                                    }
                                });




                            }else{
                                errorLinkAcceso();
                            }
                        }else{
                            errorLinkAcceso();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        errorLinkAcceso();
                    }
                });
            }else {//en caso de que no exista un uri lo redireccionamos a la pagina principal
                errorLinkAcceso();
            }
        }else {
            listarStreamings();

            listaStreamingView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (listStreaming.get(position).getIniciado()) {
                        TextView fechaAux = view.findViewById(R.id.txtItemFechaVideoStreaming);
                        TextView horaAux = view.findViewById(R.id.txtItemHoraVideoStreaming);
                        TextView urlAux = view.findViewById(R.id.txtItemUrlVideoStreaming);

                        new CuadroDatosStreaming(getContext(),
                                vendedorGlobal,
                                clienteGlobal,
                                listStreaming.get(position).getIdVideoStreaming(),
                                urlAux.getText().toString(), fechaAux.getText().toString(),
                                horaAux.getText().toString(),
                                FragmentoStreamigsVendedor.this::resultado);
                    } else {
                        Toast.makeText(getContext(), "El vendedor a terminado o no se inicia la transmision del video", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


        return root;
    }

    public  void  errorLinkAcceso(){
        Toast.makeText(getContext(),"Error en el link de acceso",Toast.LENGTH_LONG).show();
        ((MyFirebaseApp) getActivity().getApplicationContext()).setLinkAcceso(null);
        Fragment fragment = new FragmentoHomeCliente();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                .replace(R.id.home_content, fragment)
                .commit();
    }
    public void listarStreamings(){
        Query query = databaseReference.child("VideoStreaming").orderByChild("idVendedor").equalTo(vendedorGlobal.getIdVendedor());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listStreaming.clear();//borramos en caso de quedar algo en la cache
                    for (final DataSnapshot ds : snapshot.getChildren()) {
                        VideoStreaming videoStreaming = ds.getValue(VideoStreaming.class);
                        if (!videoStreaming.getEliminado()) { //listamos los streamings del vendedor que no han sido eliminados
                            try {
                                videoStreaming.setUrlVideoStreaming(encriptacionDatos.desencriptar(videoStreaming.getUrlVideoStreaming()));
                                listStreaming.add(videoStreaming);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //Inicialisamos el adaptador
                    adapterListStreaming = new AdapterVideoStreaming(getContext(),listStreaming,databaseReference);
                    listaStreamingView.setAdapter(adapterListStreaming); //configuramos el view
                }else{
                    listStreaming.clear();//borramos los datos ya que no hay nada en la base
                    //Inicialisamos el adaptador
                    adapterListStreaming = new AdapterVideoStreaming(getContext(), listStreaming,databaseReference);
                    listaStreamingView.setAdapter(adapterListStreaming); //configuramos el view
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void resultado(Boolean isVerStreamings, Vendedor vendedor, Cliente cliente, String idStreaming, String urlStreaming) {
        if (isVerStreamings){

            ((MyFirebaseApp) getActivity().getApplicationContext()).setUrl(urlStreaming);
            ((MyFirebaseApp) getActivity().getApplicationContext()).setIdStreaming(idStreaming);

            Fragment fragment = new FragmentoMensajeriaCliente();
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                    .replace(R.id.home_content, fragment)
                    .commit();

        }
    }
}
