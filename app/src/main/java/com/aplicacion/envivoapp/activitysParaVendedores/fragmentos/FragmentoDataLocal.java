package com.aplicacion.envivoapp.activitysParaVendedores.fragmentos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterListarLocal;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroEditarLocal;
import com.aplicacion.envivoapp.modelos.Local;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
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

public class FragmentoDataLocal extends Fragment implements
        CuadroEditarLocal.resultadoDialogo{

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private List<Local> listLocal = new ArrayList<>();
    private GridView gridViewLocal;
    private AdapterListarLocal gridAdapterLocal;
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();
    private Vendedor vendedorGlobal;

    private Button agregarLocal;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_data_local, container, false);
        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        gridViewLocal = root.findViewById(R.id.gridLocal);
        agregarLocal = root.findViewById(R.id.btnAgregarLocal);
        vendedorGlobal =  ((MyFirebaseApp) getContext().getApplicationContext()).getVendedor();

        if (vendedorGlobal!=null){
            agregarLocal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        vendedorGlobal.setCedula(encriptacionDatos.desencriptar(vendedorGlobal.getCedula()));
                        vendedorGlobal.setCelular(encriptacionDatos.desencriptar(vendedorGlobal.getCelular()));
                        vendedorGlobal.setNombre(encriptacionDatos.desencriptar(vendedorGlobal.getNombre()));
                        new CuadroEditarLocal(getContext(),null,vendedorGlobal,true,databaseReference,FragmentoDataLocal.this::borrarGrid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });

            listarLocal();
        }
        return root;
    }

    private  void  borrarGrid(){
        listLocal.clear();
        gridAdapterLocal = new AdapterListarLocal(getContext(), listLocal,firebaseAuth, databaseReference);
        gridViewLocal.setAdapter(gridAdapterLocal); //configuramos el view
    }


    public  void listarLocal(){
        Query queryLocal = databaseReference.child("Local").orderByChild("idVendedor").equalTo(vendedorGlobal.getIdVendedor());
        queryLocal.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                borrarGrid();
                if (snapshot.exists()){
                    for (DataSnapshot ds:snapshot.getChildren()) {
                        Local local = ds.getValue(Local.class);

                        try {//desencriptamos los datos
                            local.setCallePrincipal(encriptacionDatos.desencriptar(local.getCallePrincipal()));
                            if(local.getCalleSecundaria()!= null){
                                local.setCalleSecundaria(encriptacionDatos.desencriptar(local.getCalleSecundaria()));
                            }
                            local.setCelular(encriptacionDatos.desencriptar(local.getCelular()));
                            local.setNombre(encriptacionDatos.desencriptar(local.getNombre()));
                            local.setReferencia(encriptacionDatos.desencriptar(local.getReferencia()));
                            if(local.getTelefono()!=null){
                                local.setTelefono(encriptacionDatos.desencriptar(local.getTelefono()));
                            }


                            listLocal.add(local);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    gridAdapterLocal = new AdapterListarLocal(getContext(), listLocal,firebaseAuth, databaseReference);
                    gridViewLocal.setAdapter(gridAdapterLocal); //configuramos el view
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                borrarGrid();
            }
        });
    }
    @Override
    public void resultado() {

    }


}

