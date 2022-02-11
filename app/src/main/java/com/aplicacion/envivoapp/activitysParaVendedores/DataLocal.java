package com.aplicacion.envivoapp.activitysParaVendedores;

import android.os.Bundle;

import com.aplicacion.envivoapp.adaptadores.AdapterListarLocal;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroEditarLocal;
import com.aplicacion.envivoapp.modelos.Local;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.maps.model.LatLng;
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

public class DataLocal extends AppCompatActivity implements CuadroEditarLocal.resultadoDialogo{

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private List<Local> listLocal = new ArrayList<>();
    private GridView gridViewLocal;
    private AdapterListarLocal gridAdapterLocal;
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();

    private Button agregarLocal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_local);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        gridViewLocal = findViewById(R.id.gridLocal);
        agregarLocal = findViewById(R.id.btnAgregarLocal);

        agregarLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query query = databaseReference.child("Vendedor").orderByChild("uidUsuario").equalTo(firebaseAuth.getCurrentUser().getUid());
                query.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Vendedor vendedor = null;
                            for (DataSnapshot ds:snapshot.getChildren()){
                                vendedor = ds.getValue(Vendedor.class);
                                if (vendedor!=null){
                                    if (vendedor.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())){
                                        try {
                                            vendedor.setCedula(encriptacionDatos.desencriptar(vendedor.getCedula()));
                                            vendedor.setCelular(encriptacionDatos.desencriptar(vendedor.getCelular()));
                                            vendedor.setNombre(encriptacionDatos.desencriptar(vendedor.getNombre()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            if (vendedor!= null){
                                new CuadroEditarLocal(DataLocal.this,null,vendedor,true,databaseReference,DataLocal.this);
                            }
                        }
                    }
                });
            }
        });



        //le damos funcionalidad al toolbar
        //le damos funcionalidad al toolbar
        Button mensajeria = findViewById(R.id.btnMensajeriaGlobalDataLocal);
        Button listarLocal = findViewById(R.id.btnListarLocalDataLocal);
        Button perfil = findViewById(R.id.btnPerfilVendedorDataLocal);
        Button pedido = findViewById(R.id.btnPedidoDataLocal);
        Button videos = findViewById(R.id.btnVideosDataLocal);
        Button salir = findViewById(R.id.btnSalirDataLocal);
        Button clientes = findViewById(R.id.btnClientesDataLocal);
        Button reporte = findViewById(R.id.btnReporteDataLocal);
        Button home = findViewById(R.id.btnHomeVendedorDataLocal);


        new Utilidades().cargarToolbarVendedor(home,
                listarLocal,
                perfil,
                pedido,
                mensajeria,
                salir,
                videos,
                clientes,
                reporte,
                DataLocal.this,
                firebaseAuth);

        listarLocal();
    }

    private  void  borrarGrid(){
        listLocal.clear();
        gridAdapterLocal = new AdapterListarLocal(DataLocal.this, listLocal,firebaseAuth, databaseReference);
        gridViewLocal.setAdapter(gridAdapterLocal); //configuramos el view
    }
    public  void listarLocal(){
        Query query = databaseReference.child("Vendedor").orderByChild("uidUsuario").equalTo(firebaseAuth.getCurrentUser().getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                borrarGrid();
                if (snapshot.exists()){
                    Vendedor vendedor = null;
                    for (DataSnapshot ds:snapshot.getChildren()) {
                        vendedor = ds.getValue(Vendedor.class);
                    }
                    if (vendedor!=null){
                        Query queryLocal = databaseReference.child("Local").orderByChild("idVendedor").equalTo(vendedor.getIdVendedor());
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
                                    gridAdapterLocal = new AdapterListarLocal(DataLocal.this, listLocal,firebaseAuth, databaseReference);
                                    gridViewLocal.setAdapter(gridAdapterLocal); //configuramos el view
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                borrarGrid();
                            }
                        });

                    }else{
                        borrarGrid();
                    }
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

    public void funcionalidadGrid(){
        gridViewLocal.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Local local = listLocal.get(position);
                databaseReference.child("Vendedor").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Vendedor vendedor = null;
                            for (DataSnapshot ds:snapshot.getChildren()){
                                Vendedor vendedorAux = ds.getValue(Vendedor.class);
                                if (vendedorAux!=null){
                                    if (vendedorAux.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())){
                                        vendedor = vendedorAux;
                                        break;
                                    }
                                }
                            }
                            if (vendedor!= null){
                                new CuadroEditarLocal(DataLocal.this,local,vendedor,false,databaseReference,DataLocal.this);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }
}