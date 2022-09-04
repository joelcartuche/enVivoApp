package com.aplicacion.envivoapp.activityParaClientes.fragmentos.navDataVendedor;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterListarLocalClientes;
import com.aplicacion.envivoapp.modelos.Local;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class Fragmento_listar_locales_vendedor extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private EncriptacionDatos encriptacionDatos= new EncriptacionDatos();

    private List<Local> listLocal = new ArrayList<>(); //lista que contendra los locales del vendedor
    private AdapterListarLocalClientes gridAdapterLocal; //iniciamos el adaptador del gridView

    private TextView tvNoHayDatosFragmentoListarLocalesVendedor;
    private GridView gridFragmentoListarLocalesVendedor;
    private Vendedor vendedorGlobal;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragmento_listar_locales_vendedor, container, false);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        tvNoHayDatosFragmentoListarLocalesVendedor = root.findViewById(R.id.tvNoHayDatosFragmentoListarLocalesVendedor);
        gridFragmentoListarLocalesVendedor = root.findViewById(R.id.gridFragmentoListarLocalesVendedor);
        tvNoHayDatosFragmentoListarLocalesVendedor.setVisibility(View.GONE);

        vendedorGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getVendedor();

        cargarLocales();

        return root;
    }

    private void cargarLocales() {
        //cargamos los locales del vendedor en el grid
        databaseReference.child("Local").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listLocal.clear();
                if (snapshot.exists()){
                    for (DataSnapshot ds:snapshot.getChildren()){
                        Local local = ds.getValue(Local.class);
                        if (local!=null){
                            if(local.getIdVendedor().equals(vendedorGlobal.getIdVendedor())){
                                try {
                                    local.setCallePrincipal(encriptacionDatos.desencriptar(local.getCallePrincipal()));
                                    local.setCelular(encriptacionDatos.desencriptar(local.getCelular()));
                                    local.setNombre(encriptacionDatos.desencriptar(local.getNombre()));
                                    local.setReferencia(encriptacionDatos.desencriptar(local.getReferencia()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    local.setCalleSecundaria(encriptacionDatos.desencriptar(local.getCalleSecundaria()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    local.setTelefono(encriptacionDatos.desencriptar(local.getTelefono()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                listLocal.add(local);
                            }
                        }
                    }

                    tvNoHayDatosFragmentoListarLocalesVendedor.setVisibility(View.GONE);
                    gridAdapterLocal = new AdapterListarLocalClientes(getContext(), listLocal, firebaseAuth, databaseReference);
                    gridFragmentoListarLocalesVendedor.setAdapter(gridAdapterLocal); //configuramos el view
                }else{
                    tvNoHayDatosFragmentoListarLocalesVendedor.setVisibility(View.VISIBLE);
                    gridAdapterLocal = new AdapterListarLocalClientes(getContext(), listLocal,firebaseAuth, databaseReference);
                    gridFragmentoListarLocalesVendedor.setAdapter(gridAdapterLocal); //configuramos el view
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvNoHayDatosFragmentoListarLocalesVendedor.setVisibility(View.GONE);
            }
        });
    }
}