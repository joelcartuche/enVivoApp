package com.aplicacion.envivoapp.activityParaClientes.fragmentos.navDataVendedor;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Usuario;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

public class Fragmento_principal extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private EncriptacionDatos encriptacionDatos= new EncriptacionDatos();

    private BottomNavigationView bottomNavigationView;
    private View root;

    private TextView txtTituloNombreVendedor;
    private ImageView imgDatosVendedorCliente;
    private Dialog dialogCargando;
    private Vendedor vendedorGlobal;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_datos_vendedor_cliente, container, false);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        txtTituloNombreVendedor = root.findViewById(R.id.txtTituloNombreVendedor);
        imgDatosVendedorCliente  = root.findViewById(R.id.imgDatosVendedorCliente);


        vendedorGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getVendedor();

        txtTituloNombreVendedor.setText(vendedorGlobal.getNombre());
        Utilidades util = new Utilidades();
        dialogCargando = util.dialogCargar(getContext()); //cargamos el cuadro de dialogo
        dialogCargando.show();
        cargarDatosVendedor();
        return root ;
    }

    private void cargarDatosVendedor(){
        Query queryVendedor = databaseReference.child("Usuario").orderByChild("uidUser").equalTo(vendedorGlobal.getUidUsuario());
        queryVendedor.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Usuario usuario = null;
                    for (final DataSnapshot ds : snapshot.getChildren()) {
                        Usuario usuarioAux = ds.getValue(Usuario.class);
                        if (usuarioAux.getUidUser().equals(vendedorGlobal.getUidUsuario())) {
                            usuario = usuarioAux;
                        }
                    }
                    if (usuario!=null) {
                        if (usuario.getImagen() != null) {
                            Picasso.with(getContext()).load(usuario.getImagen()).into(imgDatosVendedorCliente);
                        }

                    }
                }
                dialogCargando.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("ERROR","imagen: ",e);
                dialogCargando.dismiss();
            }
        });


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomNavigationView = root.findViewById(R.id.nvgNavegacionFuncionVendedor);
        bottomNavigationView.setItemIconTintList(null);

        FragmentManager fragmentManager = getFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment, new Fragmento_listar_locales_vendedor()).commit();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                if (item.getItemId() == R.id.fragmento_listar_locales_vendedor) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.nav_host_fragment, new Fragmento_listar_locales_vendedor()).commit();
                } else if (item.getItemId() == R.id.fragmento_listar_productos_vendedor) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.nav_host_fragment, new Fragmento_listar_productos_vendedor()).commit();
                }else if(item.getItemId() == R.id.streamings_vendedor) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.nav_host_fragment, new FragmentoStreamigsVendedor()).commit();
                }else if(item.getItemId() == R.id.calificaciones_vendedor) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.nav_host_fragment, new Fragmento_calificaciones_vendedor()).commit();
                }else if(item.getItemId() == R.id.cometarios_vendedor) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.nav_host_fragment, new Fragmento_comentarios_vendedor()).commit();
                }


                return true;
            }
        });
    }
}
