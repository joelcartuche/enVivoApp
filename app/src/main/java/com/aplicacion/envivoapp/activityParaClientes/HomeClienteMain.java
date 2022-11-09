package com.aplicacion.envivoapp.activityParaClientes;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.aplicacion.envivoapp.MainActivity;
import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activityParaClientes.fragmentos.FragmentoAyudaCliente;
import com.aplicacion.envivoapp.activityParaClientes.fragmentos.FragmentoDataCliente;
import com.aplicacion.envivoapp.activityParaClientes.fragmentos.FragmentoHomeCliente;
import com.aplicacion.envivoapp.activityParaClientes.fragmentos.FragmentoListarVendedores;
import com.aplicacion.envivoapp.activityParaClientes.fragmentos.FragmentoPedidoCliente;
import com.aplicacion.envivoapp.activityParaClientes.fragmentos.navDataVendedor.FragmentoStreamigsVendedor;
import com.aplicacion.envivoapp.activitysParaVendedores.HomeVendedorMain;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroCalificarVendedor;
import com.aplicacion.envivoapp.modelos.Calificaciones;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje_Cliente_Vendedor;
import com.aplicacion.envivoapp.modelos.Usuario;
import com.aplicacion.envivoapp.utilidades.BuscarClienteUid;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

public class HomeClienteMain extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, View.OnClickListener,
        DrawerLayout.DrawerListener,
        BuscarClienteUid.resultadoBuscarClienteUid,
CuadroCalificarVendedor.resultadoCuadroCalificarVendedor{

    private static final int REQUEST_CODE = 200;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage ; //para la insersion de archivos
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();

    private DrawerLayout drawerLayout;
    private TextView nombreUsuario;
    private ImageView imgUsuario;
    private CardView contenedorImgUsuario;
    private ValueEventListener eventListener;
    private  Cliente cliente = null;
    private  Intent intent;

    //botones del menu
    private  Button btnListarVendedore ,btnPerfil,btnPedido, btnSalir,btnMensje,btnHome;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_cliente_main);

        solicitarPermiso();

            firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
            firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
            databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
            storage = FirebaseStorage.getInstance();//inicializamos la variable storage}

            estadoNotificacion();//en caso de existir alguna notificacion
            quitarModoOscuro();


            Uri linkAcceso = ((MyFirebaseApp) HomeClienteMain.this.getApplicationContext()).getLinkAcceso();
            if (linkAcceso == null) {
                //inicio de recivimiento de datos a traves de link
                FirebaseDynamicLinks.getInstance()
                        .getDynamicLink(getIntent())
                        .addOnSuccessListener(HomeClienteMain.this, new OnSuccessListener<PendingDynamicLinkData>() {
                            @Override
                            public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                                // Get deep link from result (may be null if no link is found)
                                Uri deepLink = null;
                                if (pendingDynamicLinkData != null) {
                                    deepLink = pendingDynamicLinkData.getLink();
                                    ((MyFirebaseApp) HomeClienteMain.this.getApplicationContext()).setLinkAcceso(deepLink);
                                    startActivity(new Intent(HomeClienteMain.this, MainActivity.class));
                                    finish();//para que el usuario no pueda regresar a activitys anteriores
                                    Log.d("DatosLink", deepLink.toString());
                                } else {
                                    Log.d("DatosLink", "null");
                                    ((MyFirebaseApp) HomeClienteMain.this.getApplicationContext()).setLinkAcceso(null);

                                }
                            }
                        })
                        .addOnFailureListener(HomeClienteMain.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("ERROR", "getDynamicLink:onFailure", e);

                            }
                        });
                //fin de link
            }


            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

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
            nombreUsuario = header.findViewById(R.id.header_title);
            imgUsuario = header.findViewById(R.id.imgUsuarioCliente);
            contenedorImgUsuario = header.findViewById(R.id.cardImgUsuarioCliente);
            contenedorImgUsuario.setVisibility(View.GONE);

            nombreUsuario.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = new FragmentoDataCliente();
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                            .replace(R.id.home_content, fragment)
                            .commit();
                }
            });

            cargarUsuario(); //cargamos los datos del usuario
            cargarCliente(); //cargamos los datos del cliente

            //Damos funcionalidad al menu
            btnListarVendedore = findViewById(R.id.btn_listar_vendedores_DataCliente);
            btnPerfil = findViewById(R.id.btn_perfil_DataClienter);
            btnPedido = findViewById(R.id.btn_carrito_DataCliente);
            btnSalir = findViewById(R.id.btn_salir_DataCliente);
            btnMensje = findViewById(R.id.btnMensajeriaGlobalDataCliente);
            btnHome = findViewById(R.id.btn_Home_Data_Clinte);

            btnListarVendedore.setOnClickListener(this::onClick);
            btnPerfil.setOnClickListener(this::onClick);
            btnPedido.setOnClickListener(this::onClick);
            btnSalir.setOnClickListener(this::onClick);
            btnMensje.setOnClickListener(this::onClick);
            btnHome.setOnClickListener(this::onClick);


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private Boolean solicitarPermiso(){
        int access_fine_location =  ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);
        int access_coarse_location = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION);

        if(access_fine_location != PackageManager.PERMISSION_GRANTED
                &&access_coarse_location != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        }else{
            return  true;
        }
        return  false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case  REQUEST_CODE:
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


    private void quitarModoOscuro(){
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO);
    }


    private void cargarCliente() {
        if (firebaseAuth.getCurrentUser()!=null) {
            new BuscarClienteUid(HomeClienteMain.this,
                    databaseReference,
                    firebaseAuth.getCurrentUser().getUid(),
                    HomeClienteMain.this::resultadoBuscarClienteUid);
        }
    }

    public void estadoNotificacion(){
        onNewIntent(getIntent());
        //int codigo = ((MyFirebaseApp) getApplicationContext()).getCodigo();
        Bundle extras = getIntent().getExtras();
        int codigo = 0;
        String idPedido = "";
        if (extras!=null) {
            codigo = extras.getInt("codigo", 0);
            idPedido = extras.getString("idPedido");
        }

        if (codigo!=0 && idPedido!=null) {

            Fragment fragment = null;
            switch (codigo) {
                case 1:
                    ((MyFirebaseApp) getApplicationContext()).setCodigo(codigo);
                    ((MyFirebaseApp) getApplicationContext()).setIdPedido(idPedido);
                    fragment = new FragmentoPedidoCliente();
                    break;
                case 2:
                    ((MyFirebaseApp) getApplicationContext()).setCodigo(codigo);
                    ((MyFirebaseApp) getApplicationContext()).setIdPedido(idPedido);
                    fragment = new FragmentoPedidoCliente();
                    break;
                default:
                    Log.w("Notificacion/", "No se inserto notificacion");
            }
            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                        .replace(R.id.home_content, fragment)
                        .commit();
            }
        }

    }

    private void cerrarSecion() {

        AuthUI.getInstance().signOut(HomeClienteMain.this).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ((MyFirebaseApp) HomeClienteMain.this.getApplicationContext()).resetearValores();
                FirebaseAuth.getInstance().signOut();
                Intent streamingsIntent = new Intent(HomeClienteMain.this, MainActivity.class);
                startActivity(streamingsIntent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(HomeClienteMain.this,"Error al cerrar sesión",Toast.LENGTH_LONG).show();
            }
        });
    }
    public void cargarUsuario(){
        if (firebaseAuth.getCurrentUser()!=null) {
            databaseReference.child("Usuario").orderByChild("uidUser").equalTo(firebaseAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        Usuario usuario = null;
                        for (final DataSnapshot ds : snapshot.getChildren()) {
                            usuario = ds.getValue(Usuario.class);
                        }
                        if (usuario!=null) {
                            //Log.d("uid ","id: "+firebaseAuth.getCurrentUser().getUid().toString());
                            if (usuario.getUidUser().equals(firebaseAuth.getCurrentUser().getUid())) {
                                if (usuario.getImagen() != null) {
                                    Picasso.with(HomeClienteMain.this).load(usuario.getImagen()).into(imgUsuario);
                                    contenedorImgUsuario.setVisibility(View.VISIBLE);
                                }
                            }
                        }}
                    }
            });


        }
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
        boolean salir = false;
        Fragment fragment = null;
        switch (item.getItemId()) {
            case R.id.home_cliente:
                fragment = new FragmentoHomeCliente();
                break;
            case R.id.data_cliente:
                fragment = new FragmentoDataCliente();
                break;
            case R.id.pedido_cliente:
                fragment = new FragmentoPedidoCliente();
                break;
            case R.id.mensajeria_global_cliente:
                ((MyFirebaseApp) this.getApplication()).setGlobal(true);
                fragment = new FragmentoListarVendedores();
                break;
            case R.id.listar_vendedores_cliente:
                ((MyFirebaseApp) this.getApplication()).setGlobal(false);
                fragment = new FragmentoListarVendedores();
                break;
            case R.id.ayuda_cliente:
                fragment = new FragmentoAyudaCliente();
                break;
            case R.id.salir_cliente:
                salir=true;
                cerrarSecion();
                break;
            default:
                throw new IllegalArgumentException("menu option not implemented!!");
        }
        if (!salir) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                    .replace(R.id.home_content, fragment)
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
        estadoNotificacion();
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


    @Override
    public void onClick(View view) {
        Button b = (Button) view;
        Fragment fragment = null;
        switch (b.getId()){
            case R.id.btn_listar_vendedores_DataCliente:
                ((MyFirebaseApp) this.getApplication()).setGlobal(false);
                fragment = new FragmentoListarVendedores();
                break;
            case R.id.btn_perfil_DataClienter:
                fragment = new FragmentoDataCliente();
                break;
            case R.id.btn_carrito_DataCliente:
                fragment = new FragmentoPedidoCliente();
                break;
            case R.id.btn_salir_DataCliente:
                cerrarSecion();
                break;
            case R.id.btnMensajeriaGlobalDataCliente:
                ((MyFirebaseApp) this.getApplication()).setGlobal(true);
                fragment = new FragmentoListarVendedores();
                break;
            case R.id.btn_Home_Data_Clinte:
                fragment = new FragmentoHomeCliente();
                break;
            default:
                throw new IllegalArgumentException("menu option not implemented!!");

        }
        if (fragment!=null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                    .replace(R.id.home_content, fragment)
                    .commit();
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }




    @Override
    protected void onPause() {
        super.onPause();
        estadoNotificacion();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }



    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void resultadoBuscarClienteUid(Cliente cliente) {
        if (cliente == null) {
            Toast.makeText(HomeClienteMain.this, "Ingrese sus datos personales", Toast.LENGTH_LONG).show();
            Fragment fragment = new FragmentoDataCliente();
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                    .replace(R.id.home_content, fragment)
                    .commit();
        } else {
            if(firebaseAuth.getCurrentUser().getUid()!=null) {
                if (cliente.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())) {
                    if (cliente.getBloqueado()) {
                        databaseReference.
                                child("Mensaje_Cliente_Vendedor").
                                orderByChild("cliente/idCliente").
                                equalTo(cliente.getIdCliente()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    bloquearCliente(snapshot);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(HomeClienteMain.this, "Error al cargar datos", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        if (dialogBloqueo != null) {
                            dialogBloqueo.dismiss();

                        }

                        buscarCalificacionNueva(cliente.getIdCliente());//buscamos si existe un nuevo vendedor por calificar

                        ((MyFirebaseApp) HomeClienteMain.this.getApplicationContext()).setCliente(cliente);
                        Uri linkAcceso = ((MyFirebaseApp) HomeClienteMain.this.getApplicationContext()).getLinkAcceso();
                        if (linkAcceso != null) {
                            ((MyFirebaseApp) HomeClienteMain.this.getApplicationContext()).getLinkAcceso();
                            ((MyFirebaseApp) HomeClienteMain.this.getApplicationContext()).setGlobal(false);
                            Fragment fragment = new FragmentoStreamigsVendedor();
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                                    .replace(R.id.home_content, fragment)
                                    .commit();
                        }
                    }
                }
            }
        }
    }

    private void buscarCalificacionNueva(String idCliente) {
        //buscamos si existe una calificacion nueva
        Log.d("SSSSS","-------------ZZ "+idCliente);
        databaseReference.child("Calificaciones").orderByChild("idCliente_esNuevo").equalTo(idCliente+"_true").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("SSSSS","-----------------000000000");
                if (snapshot.exists()){
                    Log.d("SSSSS","-----------------1111111111111");
                    for (DataSnapshot ds: snapshot.getChildren()){
                        Calificaciones calificaciones = ds.getValue(Calificaciones.class);
                        if (calificaciones!=null){
                            Log.d("SSSSS","-----------------2222222");
                            new CuadroCalificarVendedor(HomeClienteMain.this,
                                    databaseReference,
                                    HomeClienteMain.this::resultadoCuadroCalificarVendedor,
                                    calificaciones);
                        }
                    }
                }else{
                    Log.d("SSSSS","-----------------3333333333333");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ERROR","error en la calificacion",error.toException());
                Log.d("SSSSS","-----------------000000000");
            }
        });
    }

    Dialog dialogBloqueo = null;
    private void bloquearCliente(DataSnapshot snapshot){
        Mensaje_Cliente_Vendedor msCliVen = null;
        for (DataSnapshot ds:snapshot.getChildren()){
            Mensaje_Cliente_Vendedor msCliVenAux = ds.getValue(Mensaje_Cliente_Vendedor.class);
            if (msCliVenAux!=null){
                msCliVen =msCliVenAux;
            }
        }
        if (!msCliVen.getElVendedorBloqueoCliente()){//El cliente no esta bloqueado
            if (dialogBloqueo!=null){
                dialogBloqueo.dismiss();
                dialogBloqueo = null;
                cerrarSecion();
            }
            ((MyFirebaseApp) HomeClienteMain.this.getApplicationContext()).setCliente(cliente);
        }else{// el cliente esta bloqueado por algun vendedor

                String mensajeDialogo = "";
                String mensajeDialogoTelefonoCelular = "";

                mensajeDialogo = "Usted ha sido bloqueado por un vendedor para ser desbloqueado:" +
                        "comuniquese con el vendedor: ";
                try {
                    msCliVen.getVendedor().setTelefono(encriptacionDatos.desencriptar(msCliVen.getVendedor().getTelefono()));
                    mensajeDialogoTelefonoCelular = mensajeDialogoTelefonoCelular + "\n Teléfono: " + msCliVen.getVendedor().getTelefono();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    msCliVen.getVendedor().setCelular(encriptacionDatos.desencriptar(msCliVen.getVendedor().getCelular()));
                    mensajeDialogoTelefonoCelular = mensajeDialogoTelefonoCelular + "\n Célular: " + msCliVen.getVendedor().getCelular();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                try {
                    msCliVen.getVendedor().setNombre(encriptacionDatos.desencriptar(msCliVen.getVendedor().getNombre()));
                    mensajeDialogo = mensajeDialogo + "\n Nombre: " + msCliVen.getVendedor().getNombre() + mensajeDialogoTelefonoCelular;
                    dialogBloqueo = new Utilidades().
                            cuadroError(HomeClienteMain.this,
                                    mensajeDialogo);
                    dialogBloqueo.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
    }
}

    @Override
    public void resultadoCuadroCalificarVendedor(Boolean isAcepatado, Boolean isCancelado, int position) {

    }
}
