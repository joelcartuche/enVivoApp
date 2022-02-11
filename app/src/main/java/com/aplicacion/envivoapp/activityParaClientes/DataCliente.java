package com.aplicacion.envivoapp.activityParaClientes;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.aplicacion.envivoapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataCliente extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private EditText nombre, cedula, callePrincipa,calleSecundaria,referencia, telefono, celular;
    private MapView mapView;
    private Button guardar;
    private Toolbar toolbar;
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();

    private  LatLng latLng=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_cliente);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        //Inicializacion de variables
        nombre = findViewById(R.id.txtNombreCliente);
        cedula = findViewById(R.id.txtCedulaCliente);
        callePrincipa = findViewById(R.id.txtcallePricipalDataCliente);
        calleSecundaria = findViewById(R.id.txtcalleSecundariaDataCliente);
        referencia = findViewById(R.id.txtreferenciaDataCliente);
        telefono = findViewById(R.id.txtTelefonoCliente);
        celular = findViewById(R.id.txtCelularCliente);
        guardar = findViewById(R.id.btnGuardarCliente);
        toolbar = findViewById(R.id.toolbaeDataCliente);

        mapView = findViewById(R.id.mapDataCliente);
        Intent intent = getIntent();
        mapView.onCreate(intent.getExtras());
        mapView.onResume();

        try {
            MapsInitializer.initialize(getApplicationContext());
        }catch (Exception e){
            e.printStackTrace();
        }


        buscarCliente();

        //Damos funcionalidad al menu
        Button btnListarVendedore = findViewById(R.id.btn_listar_vendedores_DataCliente);
        Button btnPerfil = findViewById(R.id.btn_perfil_DataClienter);
        Button btnPedido = findViewById(R.id.btn_carrito_DataCliente);
        Button btnSalir = findViewById(R.id.btn_salir_DataCliente);
        Button btnMensje = findViewById(R.id.btnMensajeriaGlobalDataCliente);
        Button btnHome = findViewById(R.id.btn_Home_Data_Clinte);


        new Utilidades().cargarToolbar(btnHome,
                btnListarVendedore,
                btnPerfil,
                btnPedido,
                btnSalir,
                btnMensje,
                DataCliente.this,firebaseAuth,databaseReference);
    }

    public void bloquearBotones(){
        toolbar.setVisibility(View.GONE);
        inicializarMapa(null);
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nombre.getText().toString().isEmpty()){
                    Toast.makeText(DataCliente.this,"Ingrese su nombre",Toast.LENGTH_LONG).show();
                }else if (cedula.getText().toString().isEmpty()){
                    Toast.makeText(DataCliente.this,"Ingrese su número de cédula",Toast.LENGTH_LONG).show();
                }else if (callePrincipa.getText().toString().isEmpty()){
                    Toast.makeText(DataCliente.this,"Ingrese calle principal",Toast.LENGTH_LONG).show();
                }else if (referencia.getText().toString().isEmpty()){
                    Toast.makeText(DataCliente.this,"Ingrese una referencia",Toast.LENGTH_LONG).show();
                }else if (latLng == null){
                    Toast.makeText(DataCliente.this,"Error al obtener coordenadas del mapa",Toast.LENGTH_LONG).show();
                }else if (telefono.getText().toString().isEmpty() && celular.getText().toString().isEmpty()){
                    Toast.makeText(DataCliente.this,"Ingrese su teléfono o celular",Toast.LENGTH_LONG).show();
                }
                if (!nombre.getText().toString().isEmpty()
                        &&!cedula.getText().toString().isEmpty()
                        &&!callePrincipa.getText().toString().isEmpty()
                        &&!referencia.getText().toString().isEmpty()
                        && latLng!=null
                        &&!(telefono.getText().toString().isEmpty() && celular.getText().toString().isEmpty())){

                    Cliente cliente = new Cliente();

                    cliente.setIdCliente(UUID.randomUUID().toString());
                    try {
                        cliente.setNombre(encriptacionDatos.encriptar(nombre.getText().toString()));
                        cliente.setCedula(encriptacionDatos.encriptar(cedula.getText().toString()));
                        cliente.setCallePrincipal(encriptacionDatos.encriptar(callePrincipa.getText().toString()));
                        cliente.setReferencia(encriptacionDatos.encriptar(referencia.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        cliente.setTelefono(encriptacionDatos.encriptar(telefono.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        cliente.setCelular(encriptacionDatos.encriptar(celular.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        cliente.setCalleSecundaria(encriptacionDatos.encriptar(calleSecundaria.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    cliente.setLatitud(latLng.latitude);
                    cliente.setLongitud(latLng.longitude);


                    cliente.setUidUsuario(firebaseAuth.getCurrentUser().getUid());
                    databaseReference.child("Cliente").child(cliente.getIdCliente()).setValue(cliente).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(DataCliente.this,"Datos guardados con éxito",Toast.LENGTH_LONG).show();
                            Intent listarIntent = new Intent(DataCliente.this, HomeCliente.class);
                            startActivity(listarIntent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(DataCliente.this,"Error al guardar los datos",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    public void buscarCliente(){
        Query query = databaseReference.child("Cliente").orderByChild("uidUsuario").equalTo(firebaseAuth.getCurrentUser().getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Cliente cliente = null;

                    for (DataSnapshot ds:snapshot.getChildren()){
                        cliente = ds.getValue(Cliente.class);
                    }

                    if (cliente != null){

                        if (!cliente.getBloqueado()) {

                            try {
                                cliente.setNombre(encriptacionDatos.desencriptar(cliente.getNombre()));
                                nombre.setText(cliente.getNombre());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {

                                cedula.setText(encriptacionDatos.desencriptar(cliente.getCedula()));
                                callePrincipa.setText(encriptacionDatos.desencriptar(cliente.getCallePrincipal()));
                                referencia.setText(encriptacionDatos.desencriptar(cliente.getReferencia()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                calleSecundaria.setText(encriptacionDatos.desencriptar(cliente.getCalleSecundaria()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                telefono.setText(encriptacionDatos.desencriptar(cliente.getTelefono()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                celular.setText(encriptacionDatos.desencriptar(cliente.getCelular()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //incializamos el mapa
                            inicializarMapa(cliente);

                            //actualizamos los datos del cliente
                            Cliente finalCliente = cliente;
                            guardar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (nombre.getText().toString().isEmpty()) {
                                        Toast.makeText(DataCliente.this, "Igrese su nombre", Toast.LENGTH_LONG).show();
                                    } else if (cedula.getText().toString().isEmpty()) {
                                        Toast.makeText(DataCliente.this, "Igrese su número de cédula", Toast.LENGTH_LONG).show();
                                    } else if (callePrincipa.getText().toString().isEmpty()) {
                                        Toast.makeText(DataCliente.this, "Igrese la calle principal", Toast.LENGTH_LONG).show();
                                    } else if (referencia.getText().toString().isEmpty()) {
                                        Toast.makeText(DataCliente.this, "Igrese una referencia", Toast.LENGTH_LONG).show();
                                    } else if (((MyFirebaseApp) getBaseContext().getApplicationContext()).getLatLng() != null) {
                                        Toast.makeText(DataCliente.this, "Error al obtener coordenadas del mapa", Toast.LENGTH_LONG).show();
                                    } else if (telefono.getText().toString().isEmpty() && celular.getText().toString().isEmpty()) {
                                        Toast.makeText(DataCliente.this, "Igrese su telefono o celular", Toast.LENGTH_LONG).show();
                                    }
                                    if (!nombre.getText().toString().isEmpty()
                                            && !cedula.getText().toString().isEmpty()
                                            && !callePrincipa.getText().toString().isEmpty()
                                            && !referencia.getText().toString().isEmpty()
                                            && !(telefono.getText().toString().isEmpty() && celular.getText().toString().isEmpty())) {

                                        //editamos los datos del cliente
                                        try {
                                            finalCliente.setNombre(encriptacionDatos.encriptar(nombre.getText().toString()));
                                            finalCliente.setCedula(encriptacionDatos.encriptar(cedula.getText().toString()));
                                            finalCliente.setCallePrincipal(encriptacionDatos.encriptar(callePrincipa.getText().toString()));
                                            finalCliente.setReferencia(encriptacionDatos.encriptar(referencia.getText().toString()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        if (!calleSecundaria.getText().toString().isEmpty()
                                                || calleSecundaria.getText().toString().equals("")) {
                                            try {
                                                finalCliente.setCalleSecundaria(encriptacionDatos.encriptar(calleSecundaria.getText().toString()));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        try {
                                            finalCliente.setTelefono(encriptacionDatos.encriptar(telefono.getText().toString()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            finalCliente.setCelular(encriptacionDatos.encriptar(celular.getText().toString()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }


                                        finalCliente.setLatitud(latLng.latitude);
                                        finalCliente.setLongitud(latLng.longitude);

                                        //ingresamos en un map los datos que van a ser actualizados
                                        Map<String, Object> clienteActualizar = new HashMap<>();
                                        clienteActualizar.put("cedula", finalCliente.getCedula());
                                        clienteActualizar.put("celular", finalCliente.getCelular());
                                        clienteActualizar.put("callePrincipal", finalCliente.getCallePrincipal());
                                        clienteActualizar.put("calleSecundaria", finalCliente.getCalleSecundaria());
                                        clienteActualizar.put("referencia", finalCliente.getReferencia());

                                        clienteActualizar.put("latitud", finalCliente.getLatitud());
                                        clienteActualizar.put("longitud", finalCliente.getLongitud());

                                        clienteActualizar.put("nombre", finalCliente.getNombre());
                                        clienteActualizar.put("telefono", finalCliente.getTelefono());

                                        //buscamos el cliente
                                        databaseReference.child("Cliente").child(finalCliente.getIdCliente()).updateChildren(clienteActualizar).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(DataCliente.this, "Datos actualizados con éxito", Toast.LENGTH_LONG).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(DataCliente.this, "Error al guardar los datos", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                }
                            });
                        }else{
                            Dialog dialog = new Utilidades().cuadroError(DataCliente.this, "Usted ha sido bloqueado, pongase en contacto con el vendedor");
                            dialog.show();

                        }
                    }else{
                        bloquearBotones();
                    }
                }else{
                    bloquearBotones();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                bloquearBotones();
            }
        });
    }

    public  void inicializarMapa(Cliente cliente){
        //inicializamos el mapa
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                Context context = DataCliente.this;
                if (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                googleMap.setMyLocationEnabled(true);


                //Obtenemos la ubicación actual
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                LocationManager locationManager = (LocationManager) ((Activity) context).getSystemService(Context.LOCATION_SERVICE);
                Location myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);


                if (cliente != null) {
                    latLng = new LatLng(cliente.getLatitud(),cliente.getLongitud());
                    cargarmarcador(cliente.getNombre(), googleMap);
                }else{
                    latLng = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
                    cargarmarcador("My ubicación",googleMap);
                }

                if (latLng ==null){
                    Toast.makeText(context,"Error al obtener su ubicación",Toast.LENGTH_LONG).show();
                }

                //observamos si el mapa es movido
                googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDrag(@NonNull Marker marker) { }

                    @Override
                    public void onMarkerDragEnd(@NonNull Marker marker) {
                        latLng= marker.getPosition();
                    }

                    @Override
                    public void onMarkerDragStart(@NonNull Marker marker) {}
                });

            }
        });
    }

    private void cargarmarcador(String tituloMarcador,GoogleMap googleMap){
        Utilidades utilidades = new Utilidades();
        googleMap.addMarker(new MarkerOptions().
                position(latLng).
                title(tituloMarcador).
                icon(BitmapDescriptorFactory.fromBitmap(utilidades.cargarIconocliente())).
                draggable(true));
        CameraPosition cameraPosition = new CameraPosition.Builder().
                target(latLng).
                zoom(17).
                build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}