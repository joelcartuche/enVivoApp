package com.aplicacion.envivoapp.activityParaClientes;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.aplicacion.envivoapp.adaptadores.AdapterListarVendedores;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroListarVendedor;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Vendedor;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;


import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.utilidades.RetornoParametroCliente;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListarVendedores extends AppCompatActivity implements CuadroListarVendedor.resultadoDialogo{

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private List<Vendedor> listVendedor = new ArrayList<>();
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
                    new CuadroListarVendedor(ListarVendedores.this, listVendedor.get(position),databaseReference,ListarVendedores.this);
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

        new Utilidades().cargarToolbar(btnHome,
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
            databaseReference.child("Cliente").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        Cliente cliente =null;
                        for ( DataSnapshot ds : dataSnapshot.getChildren()) {
                            Cliente clienteAux = ds.getValue(Cliente.class);//obtenemos el objeto cliente
                            if (clienteAux.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())){
                                cliente= clienteAux;
                            }
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