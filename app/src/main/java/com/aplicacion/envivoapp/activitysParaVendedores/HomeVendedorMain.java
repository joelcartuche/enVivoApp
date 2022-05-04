package com.aplicacion.envivoapp.activitysParaVendedores;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.aplicacion.envivoapp.MainActivity;
import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activityParaClientes.HomeClienteMain;
import com.aplicacion.envivoapp.activityParaClientes.fragmentos.FragmentoAyudaCliente;
import com.aplicacion.envivoapp.activityParaClientes.fragmentos.FragmentoDataCliente;
import com.aplicacion.envivoapp.activitysParaVendedores.fragmentos.FragmentoAyudaVendedor;
import com.aplicacion.envivoapp.activitysParaVendedores.fragmentos.FragmentoDataLocal;
import com.aplicacion.envivoapp.activitysParaVendedores.fragmentos.FragmentoDataVendedor;
import com.aplicacion.envivoapp.activitysParaVendedores.fragmentos.FragmentoGestionVideos;
import com.aplicacion.envivoapp.activitysParaVendedores.fragmentos.FragmentoHomeVendedor;
import com.aplicacion.envivoapp.activitysParaVendedores.fragmentos.FragmentoListarClientes;
import com.aplicacion.envivoapp.activitysParaVendedores.fragmentos.FragmentoPedidoVendedor;
import com.aplicacion.envivoapp.activitysParaVendedores.fragmentos.FragmentoReporte;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Mensaje_Cliente_Vendedor;
import com.aplicacion.envivoapp.modelos.Usuario;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.BuscarVendedorUid;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.facebook.login.LoginManager;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeVendedorMain extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener,
        DrawerLayout.DrawerListener,
        BuscarVendedorUid.resultadoBuscarVendedorUid{
    private int REQUEST_CODE = 200;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage ; //para la insersion de archivos
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();

    private DrawerLayout drawerLayout;
    private TextView nombreUsuario;
    private ImageView imgUsuario;
    private CardView contenedorImgUsuario;
    private ValueEventListener eventListener;


    private static final int PERMISO_FINELOCATION = 0;


    //botones del menu
    private Button btnHomeVendedor,
            btnListarLocal,
            btnPerfil,
            btnPedido,
            btnVideos,
            btnClientes,
            btnReportes,
            btnSalir,
            btnMensajeria;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_vendedor_main);

        pedirPermisos();
            firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
            firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
            databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
            storage = FirebaseStorage.getInstance();//inicializamos la variable storage}

            //intanciamos los componentes de la vista
            btnHomeVendedor = findViewById(R.id.btnHomeVendedor); //almacena el boton del home para el vendedor
            btnListarLocal = findViewById(R.id.btnListarLocalHomeVendedor); //almacena el boton listar local
            btnPerfil = findViewById(R.id.btnPerfilVendedorHomeVendedor); //almacena el boton perfil del vendedor
            btnPedido = findViewById(R.id.btnPedidoHomeVendedor);// almacena el boton pedido del vendedor
            btnVideos = findViewById(R.id.btnVideosHomeVendedor); // almacena el boton para acceder a  los videos creados por el vendedor
            btnClientes = findViewById(R.id.btnClientesHomeVendedor); // almacena el boton para el listado de los clientes
            btnReportes = findViewById(R.id.btnReporteHomeVendedor); // almacena el boton para la generacion de reportes

            btnSalir = findViewById(R.id.btnSalirHomeVendedor); //almacena el boton para salir del sistema
            btnMensajeria = findViewById(R.id.btnMensajeriaGlobalDataVendedor); //almacena el boton para acceder a la mensajeria del vendedor


           // pedirPermisos(); //pedimos permisos de almacenamiento y ubicacion
            //actualizarMensajes();

            quitarModoOscuro();

            Toolbar toolbar = findViewById(R.id.toolbar); //cargamos el toolbar
            setSupportActionBar((Toolbar) findViewById(R.id.toolbar)); //

            drawerLayout = findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = findViewById(R.id.navigation_view);
            navigationView.setNavigationItemSelectedListener(this);

            MenuItem menuItem = navigationView.getMenu().getItem(0);
            onNavigationItemSelected(menuItem);
            menuItem.setChecked(true);

            drawerLayout.addDrawerListener(this);

            View header = navigationView.getHeaderView(0);
            nombreUsuario = header.findViewById(R.id.nombre_vendedor);
            imgUsuario = header.findViewById(R.id.imgUsuarioVendedor);
            contenedorImgUsuario = header.findViewById(R.id.cardImgUsuarioVendedor);
            contenedorImgUsuario.setVisibility(View.GONE);

            nombreUsuario.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = new FragmentoDataVendedor();
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                            .replace(R.id.home_content_vendedor, fragment)
                            .commit();
                }
            });

            cargarUsuario();

            btnHomeVendedor.setOnClickListener(this::onClick);
            btnListarLocal.setOnClickListener(this::onClick);
            btnPerfil.setOnClickListener(this::onClick);
            btnPedido.setOnClickListener(this::onClick);
            btnVideos.setOnClickListener(this::onClick);
            btnClientes.setOnClickListener(this::onClick);
            btnReportes.setOnClickListener(this::onClick);
            btnSalir.setOnClickListener(this::onClick);
            btnMensajeria.setOnClickListener(this::onClick);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private Boolean pedirPermisos() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE

                    },
                    PERMISO_FINELOCATION);

        }else{
            return true;
        }

        return false;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case  PERMISO_FINELOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0  ) {
                    boolean permisosDados = true;
                    for (int i =0;i<grantResults.length;i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            permisosDados = false;
                            break;
                        }
                    }
                    if (!permisosDados) {

                    }else{
                        Toast.makeText(this,"Se debe habilitar los permisos para la ejecucion de la app",Toast.LENGTH_LONG).show();
                    }
                }  else {
                    Toast.makeText(this,"Se debe habilitar los permisos para la ejecucion de la app",Toast.LENGTH_LONG).show();
                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }


    public void cargarUsuario(){
        if (firebaseAuth.getCurrentUser()!=null) {
            cargarImagenUsuario();
            new BuscarVendedorUid(HomeVendedorMain.this,
                    databaseReference,
                    firebaseAuth.getCurrentUser().getUid(),
                    HomeVendedorMain.this::resultadoBuscarVendedorUid);

        }
    }

    @Override
    public void resultadoBuscarVendedorUid(Vendedor vendedor) {
        if (vendedor==null){
            Toast.makeText(HomeVendedorMain.this, "Ingrese sus datos personales", Toast.LENGTH_LONG).show();
            Fragment fragment = new FragmentoDataVendedor();
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                    .replace(R.id.home_content_vendedor, fragment)
                    .commit();
        }else{
            ((MyFirebaseApp) HomeVendedorMain.this.getApplicationContext()).setVendedor(vendedor);
        }
    }

    private void cargarImagenUsuario() {
        databaseReference.child("Usuario").orderByChild("uidUser").equalTo(firebaseAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Usuario usuario = null;
                    for (final DataSnapshot ds : snapshot.getChildren()) {
                        Usuario usuarioAux = ds.getValue(Usuario.class);
                        if (usuarioAux.getUidUser().equals(firebaseAuth.getCurrentUser().getUid())) {
                            usuario = usuarioAux;
                        }
                    }
                    if (usuario!=null) {
                        if (usuario.getImagen() != null) {
                            Picasso.with(HomeVendedorMain.this).load(usuario.getImagen()).into(imgUsuario);
                            contenedorImgUsuario.setVisibility(View.VISIBLE);
                        }

                    }}
            }
        });

    }



    @Override
    public void onClick(View view) {
        Button button = (Button) view;
        Fragment fragment = null;
        Boolean esSalir = false;
        switch (button.getId()){
            case R.id.btnHomeVendedor:
                fragment = new FragmentoHomeVendedor();
                break;
            case R.id.btnListarLocalHomeVendedor:
                fragment = new FragmentoDataLocal();
                break;
            case R.id.btnPerfilVendedorHomeVendedor:
                fragment = new FragmentoDataVendedor();
                break;
            case R.id.btnPedidoHomeVendedor:
                fragment = new FragmentoPedidoVendedor();
                break;
            case R.id.btnVideosHomeVendedor:
                fragment = new FragmentoGestionVideos();
                break;
            case R.id.btnClientesHomeVendedor:
                ((MyFirebaseApp) this.getApplication()).setGlobal(false);
                fragment = new FragmentoListarClientes();

                break;
            case R.id.btnReporteHomeVendedor:
                fragment = new FragmentoReporte();
                break;
            case R.id.btnMensajeriaGlobalDataVendedor:
                ((MyFirebaseApp) this.getApplication()).setGlobal(true);
                fragment = new FragmentoListarClientes();
                break;
            case R.id.btnSalirHomeVendedor:
                esSalir =true;
                cerrarSecion();

                break;
            default:
                throw new IllegalArgumentException("menu option not implemented!!");
        }
        if (!esSalir) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                    .replace(R.id.home_content_vendedor, fragment)
                    .commit();
            drawerLayout.closeDrawer(GravityCompat.START);
        }

    }
    private void quitarModoOscuro(){
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void cerrarSecion() {
        ((MyFirebaseApp) HomeVendedorMain.this.getApplicationContext()).resetearValores();
        FirebaseAuth.getInstance().signOut();
        AuthUI.getInstance().signOut(HomeVendedorMain.this).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {


                Intent streamingsIntent = new Intent(HomeVendedorMain.this, MainActivity.class);
                startActivity(streamingsIntent);
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(HomeVendedorMain.this,"Error al cerrar sesión",Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        Boolean esSalir = false;
        switch (item.getItemId()) {
            case R.id.home_vendedor:
                fragment = new FragmentoHomeVendedor();
                break;
            case R.id.data_vendedor:
                fragment = new FragmentoDataVendedor();
                break;
            case R.id.local:
                fragment = new FragmentoDataLocal();
                break;
            case R.id.videos:
                fragment = new FragmentoGestionVideos();
                break;
            case R.id.pedido_vendedor:
                fragment = new FragmentoPedidoVendedor();
                break;
            case R.id.mensajeria_global_vendedor:
                ((MyFirebaseApp) this.getApplication()).setGlobal(true);
                fragment = new FragmentoListarClientes();
                break;
            case R.id.listar_clientes_vendedor:
                ((MyFirebaseApp) this.getApplication()).setGlobal(false);
                fragment = new FragmentoListarClientes();
                break;
            case R.id.reporte:
                fragment = new FragmentoReporte();
                break;
            case R.id.ayuda_vendedor:
                fragment = new FragmentoAyudaVendedor();
                break;
            case R.id.salir_vendedor:
                esSalir = true;
                cerrarSecion();
                break;
            default:
                throw new IllegalArgumentException("menu option not implemented!!");
        }

        if(!esSalir) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                    .replace(R.id.home_content_vendedor, fragment)
                    .commit();
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        //cambio en la posición del drawer
    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {
        //el drawer se ha abierto completamente
    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {
        //el drawer se ha cerrado completamente
    }

    @Override
    public void onDrawerStateChanged(int newState) {
        //cambio de estado, puede ser STATE_IDLE, STATE_DRAGGING or STATE_SETTLING
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(verificarConexion()){
            //Toast.makeText(this, "Conectado", Toast.LENGTH_SHORT).show();
        }else{
            //Snackbar.make(this,"Sin  conexión",Snackbar.LENGTH_LONG);
            Toast.makeText(this, "Sin conexión", Toast.LENGTH_SHORT).show();
        }

    }

    private Boolean verificarConexion(){
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager !=null){
            NetworkInfo networkInfo  =connectivityManager.getActiveNetworkInfo();
            if (networkInfo!=null){
                return networkInfo.isConnected();
            }
        }
        return  false;

    }

    public void actualizarMensajes(){

        databaseReference.child("Mensaje_Cliente_Vendedor").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    for(DataSnapshot ds:snapshot.getChildren()) {
                        Mensaje_Cliente_Vendedor mensaje_cliente_vendedor = ds.getValue(Mensaje_Cliente_Vendedor.class);

                            String idVendedor = mensaje_cliente_vendedor.getIdCliente_idVendedor().split("_")[1];

                            databaseReference.child("Vendedor").child(idVendedor).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                @Override
                                public void onSuccess(DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        Vendedor vendedor = snapshot.getValue(Vendedor.class);
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("Mensaje_Cliente_Vendedor/" +
                                                        mensaje_cliente_vendedor.getIdCliente_idVendedor() +
                                                        "/vendedor",
                                                vendedor);
                                        databaseReference.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("Subiendo .. ", mensaje_cliente_vendedor.getIdCliente_idVendedor());

                                            }
                                        });
                                    }
                                }
                            });

                    }




                }
            }
        });


/*
        databaseReference.child("Mensaje").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                if (snapshot.exists()){
                    List<String> idClientes = new ArrayList<>();
                    for (DataSnapshot ds:snapshot.getChildren()){
                        Mensaje ms = ds.getValue(Mensaje.class);
                        Boolean esNuevo = false;

                        Log.d("Dato", ms.getIdCliente_idVendedor());
                        if (idClientes.isEmpty()) {
                            idClientes.add(ms.getCliente().getIdCliente());
                            esNuevo = true;
                        }else{
                            if (idClientes.indexOf(ms.getCliente().getIdCliente()) == -1){
                                idClientes.add(ms.getCliente().getIdCliente());
                                esNuevo =true;
                            }
                        }






/*
                        if (ms.getCliente()!=null) {
                            map.put(ms.getIdMensaje() +
                                            "/idCliente_idVendedor",
                                    ms.getCliente().getIdCliente() + "_" +
                                            ms.getVendedor().getIdVendedor());
                        }else{
                            Log.d("Dato", ms.getIdMensaje());

                        }

 */
/*
                        if (ms.getIdStreaming() !=null){
                            map.put(ms.getIdMensaje()+
                                            "/idVendedor_idStreaming",
                                    ms.getVendedor().getIdVendedor()+"_"+
                                            ms.getIdStreaming());
                        }
*/



/*
                        if (esNuevo) {
                            databaseReference.child("Cliente").child(ms.getIdcliente()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                @Override
                                public void onSuccess(DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        Cliente cliente = snapshot.getValue(Cliente.class);
                                        Map<String, Object> map = new HashMap<>();
                                        String idMensajeClienteV = databaseReference.push().getKey();
                                        map.put("Mensaje_Cliente_Vendedor/" +
                                                        idMensajeClienteV +
                                                        "/idMensaje_Cliente_Vendedor",
                                                idMensajeClienteV);

                                        map.put("Mensaje_Cliente_Vendedor/" +
                                                        idMensajeClienteV +
                                                        "/cliente",
                                                cliente);

                                        map.put("Mensaje_Cliente_Vendedor/" +
                                                        idMensajeClienteV +
                                                        "/idCliente_idVendedor",
                                                ms.getCliente().getIdCliente() + "_" + ms.getVendedor().getIdVendedor());

                                        databaseReference.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("Subiendo .. ", cliente.getNombre());
                                                Log.d("Subiendo .. ", ms.getIdMensaje());

                                            }
                                        });
                                    }
                                }
                            });

                        }else{
                            Log.d("Dato", ms.getIdMensaje());
                        }



                    }


                    databaseReference.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(HomeVendedorMain.this,"Logrado",Toast.LENGTH_LONG).show();

                        }
                    });

 */




/*
                }
            }
        });

        */
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}
