package com.aplicacion.envivoapp.activityParaClientes;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.aplicacion.envivoapp.activitysParaVendedores.GestionVideos;
import com.aplicacion.envivoapp.adaptadores.AdapterListarVendedores;
import com.aplicacion.envivoapp.adaptadores.AdapterVideoStreaming;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroListarVendedor;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Vendedor;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.Toast;


import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.VideoStreaming;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.Utilidades;
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

public class ListarVendedores extends AppCompatActivity implements CuadroListarVendedor.resultadoDialogo{

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private EncriptacionDatos encriptacionDatos= new EncriptacionDatos();

    private List<Vendedor> listVendedor = new ArrayList<>();
    private EditText buscarVendedor;
    private ListAdapter adapterListVendedor;
    private GridView listaVendedorView;
    private String   esMensajeGlobal=null;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_vendedores);


        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        listaVendedorView = findViewById(R.id.listVendedores);
        buscarVendedor = findViewById(R.id.busquedaVendedor);


        Bundle vendedor = ListarVendedores.this.getIntent().getExtras(); //recogemos si es o no un mensaje global
        esMensajeGlobal = vendedor!=null?vendedor.getString("global"):null; //recogemos los datos del vendedor

        if (esMensajeGlobal != null && esMensajeGlobal.equals("1")) {
            listaVendedorView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Bundle parametros = new Bundle();
                    parametros.putString("vendedor", listVendedor.get(position).getIdVendedor());
                    Intent streamingsIntent = new Intent(ListarVendedores.this, MensajeriaGlobal.class);
                    streamingsIntent.putExtras(parametros);
                    startActivity(streamingsIntent);
                }
            });
        }else{
            listaVendedorView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    new CuadroListarVendedor(ListarVendedores.this,
                            listVendedor.get(position),
                            databaseReference,
                            firebaseAuth,
                            ListarVendedores.this.getIntent().getExtras(),
                            ListarVendedores.this);
                }
            });
        }



        //Damos funcionalidad al menu
        Button btnListarVendedore = findViewById(R.id.btn_listar_vendedores_ListarVendedor);
        Button btnPerfil = findViewById(R.id.btn_perfil_listar_ListarVendedor);
        Button btnPedido = findViewById(R.id.btn_carrito_listar_ListarVendedor);
        Button btnSalir = findViewById(R.id.btn_salir_ListarVendedor);
        Button btnMensje = findViewById(R.id.btnMensajeriaGlobalListarVendedor);

        Button btnHome = findViewById(R.id.btn_Home_Listar_Vendedores);


        //le damos funcionalidad al buscador
        buscarVendedor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<Vendedor> listaAux = new ArrayList<>();

                for (Vendedor vd : listVendedor) {

                    if (vd.toString().toLowerCase().indexOf(s.toString().toLowerCase()) == 0) {
                        listaAux.add(vd);
                    }
                }

                if (listaAux.size() != 0) {
                    //Inicialisamos el adaptador
                    adapterListVendedor = new AdapterListarVendedores(ListarVendedores.this, listaAux, databaseReference);
                    listaVendedorView.setAdapter(adapterListVendedor); //configuramos el view
                }else{
                    //Inicialisamos el adaptador
                    adapterListVendedor = new AdapterListarVendedores(ListarVendedores.this,   listVendedor, databaseReference);
                    listaVendedorView.setAdapter(adapterListVendedor); //configuramos el view
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        Utilidades util = new Utilidades();
        util.buscarClientebloqueado(ListarVendedores.this,firebaseAuth,databaseReference);
        util.cargarToolbar(btnHome,
                btnListarVendedore,
                btnPerfil,
                btnPedido,
                btnSalir,
                btnMensje,
                ListarVendedores.this,firebaseAuth,databaseReference);

        listarVendedores();
    }

    public void listarVendedores(){
        databaseReference.child("Vendedor").addValueEventListener(new ValueEventListener() { //buscamos todos los datos en la tabla Video Streaming
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listVendedor.clear();//borramos en caso de quedar algo en la cache
                    for (final DataSnapshot ds : snapshot.getChildren()) {
                        Vendedor vendedor = ds.getValue(Vendedor.class);//obtenemos el objeto video streaming
                        try {
                            vendedor.setCedula(encriptacionDatos.desencriptar(vendedor.getCedula()));
                            vendedor.setNombre(encriptacionDatos.desencriptar(vendedor.getNombre()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            vendedor.setCelular(encriptacionDatos.desencriptar(vendedor.getCelular()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            vendedor.setTelefono(encriptacionDatos.desencriptar(vendedor.getTelefono()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        listVendedor.add(vendedor);
                    }
                    //Inicialisamos el adaptador
                    adapterListVendedor = new AdapterListarVendedores(ListarVendedores.this,listVendedor,databaseReference);
                    listaVendedorView.setAdapter(adapterListVendedor); //configuramos el view
                }else{
                    listVendedor.clear();//borramos los datos ya que no hay nada en la base
                    //Inicialisamos el adaptador
                    adapterListVendedor = new AdapterListarVendedores(ListarVendedores.this, listVendedor,databaseReference);
                    listaVendedorView.setAdapter(adapterListVendedor); //configuramos el view
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void resultado(Boolean isVerStreamings, Vendedor vendedor) {
        if(isVerStreamings){
            Query query = databaseReference.child("Cliente").orderByChild("uidUsuario").equalTo(firebaseAuth.getCurrentUser().getUid());
            query.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        Cliente cliente =null;
                        for ( DataSnapshot ds : dataSnapshot.getChildren()) {
                            cliente = ds.getValue(Cliente.class);//obtenemos el objeto cliente

                        }
                        if (cliente !=null){
                            Bundle parametros = new Bundle();
                            parametros.putString("vendedor",vendedor.getIdVendedor());
                            parametros.putString("cliente",cliente.getIdCliente());
                            Intent streamingsIntent = new Intent(ListarVendedores.this,ListarStreamingsVendedor.class);
                            streamingsIntent.putExtras(parametros);
                            startActivity(streamingsIntent);
                        }
                    }
                }
            });
        }
    }


}