package com.aplicacion.envivoapp.activityParaClientes.fragmentos;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activityParaClientes.fragmentos.navDataVendedor.FragmentoStreamigsVendedor;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje_Cliente_Vendedor;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FragmentoDataCliente extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private EditText nombre, cedula, callePrincipa,calleSecundaria,referencia, telefono, celular;
    private MapView mapView;
    private Button guardar,maximizar,minimizar;
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();
    private Cliente clienteGlobal;

    private LatLng latLng=null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.activity_data_cliente, container, false);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        //Inicializacion de variables
        nombre = root.findViewById(R.id.txtNombreCliente);
        cedula = root.findViewById(R.id.txtCedulaCliente);
        callePrincipa = root.findViewById(R.id.txtcallePricipalDataCliente);
        calleSecundaria = root.findViewById(R.id.txtcalleSecundariaDataCliente);
        referencia = root.findViewById(R.id.txtreferenciaDataCliente);
        telefono = root.findViewById(R.id.txtTelefonoCliente);
        celular = root.findViewById(R.id.txtCelularCliente);
        guardar = root.findViewById(R.id.btnGuardarCliente);
        maximizar = root.findViewById(R.id.maximizarUbicacionDataCliente);
        minimizar = root.findViewById(R.id.minimizarUbicacionDataCliente);

        clienteGlobal = ((MyFirebaseApp) getContext().getApplicationContext()).getCliente();

            mapView = root.findViewById(R.id.mapDataCliente);

            mapView.setVisibility(View.GONE);
            minimizar.setVisibility(View.GONE);

            Intent intent = getActivity().getIntent();
            mapView.onCreate(intent.getExtras());
            mapView.onResume();

            try {
                MapsInitializer.initialize(getContext());
            } catch (Exception e) {
                e.printStackTrace();
            }

            maximizar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mapView.setVisibility(View.VISIBLE);
                    maximizar.setVisibility(View.GONE);
                    minimizar.setVisibility(View.VISIBLE);
                }
            });

            minimizar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mapView.setVisibility(View.GONE);
                    maximizar.setVisibility(View.VISIBLE);
                    minimizar.setVisibility(View.GONE);
                }
            });




        if(clienteGlobal == null){
            enviarNuevoDatoCliente();
        }else{
            if (clienteGlobal != null){
                if (!clienteGlobal.getBloqueado()) {
                    try {
                        nombre.setText(encriptacionDatos.desencriptar(clienteGlobal.getNombre()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {

                        cedula.setText(encriptacionDatos.desencriptar(clienteGlobal.getCedula()));
                        callePrincipa.setText(encriptacionDatos.desencriptar(clienteGlobal.getCallePrincipal()));
                        referencia.setText(encriptacionDatos.desencriptar(clienteGlobal.getReferencia()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        calleSecundaria.setText(encriptacionDatos.desencriptar(clienteGlobal.getCalleSecundaria()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        telefono.setText(encriptacionDatos.desencriptar(clienteGlobal.getTelefono()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        celular.setText(encriptacionDatos.desencriptar(clienteGlobal.getCelular()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //incializamos el mapa
                    inicializarMapa(clienteGlobal);

                }else{
                    Dialog dialog = new Utilidades().cuadroError(getContext(), "Usted ha sido bloqueado, pongase en contacto con el vendedor");
                    dialog.show();
                }
            }

            guardar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actualizarDatosCliente();
                }
            });
        }
        return root;
    }

    public void enviarNuevoDatoCliente(){
        inicializarMapa(null);
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearDatosCliente();
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

                        }else{
                            Dialog dialog = new Utilidades().cuadroError(getContext(), "Usted ha sido bloqueado, pongase en contacto con el vendedor");
                            dialog.show();
                        }
                    }else{
                        enviarNuevoDatoCliente();
                    }
                }else{
                    enviarNuevoDatoCliente();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                enviarNuevoDatoCliente();
            }
        });
    }

    public  void inicializarMapa(Cliente cliente){
        //inicializamos el mapa
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                Context context = getContext();
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

    private Boolean  validarCampos (){
        if(nombre.getText().toString().isEmpty()){
            nombre.setError("Ingrese nombre");
        }else if (callePrincipa.getText().toString().isEmpty()){
            callePrincipa.setError("Ingrese calle principal");
        }else if (referencia.getText().toString().isEmpty()){
            referencia.setError("Ingrese referencia");
        }else if (cedula.getText().toString().isEmpty()){
            cedula.setError("Ingrese su número de cédula");
        }else if(cedula.getText().length()!=10){
            cedula.setError("Cédula no tiene 10 dígitos");
        }else if (!validarCedula(cedula.getText().toString())) {
            cedula.setError("Cédula incorrecta");
        }else if (telefono.getText().toString().isEmpty() && celular.getText().toString().isEmpty()){
            celular.setError("Ingrese su celular o teléfono");
        }else if (latLng == null){
            Toast.makeText(getContext(),"Error al obtener coordenadas del mapa",Toast.LENGTH_LONG).show();
        }else if(!validarTelefono()){
        }else if (!validarCelular()){
        }else{
            return true;
        }
        return false;
    }

    public Boolean validarTelefono(){
        if (!telefono.getText().toString().isEmpty()) {
            if (telefono.getText().toString().length() != 8) {
                if (telefono.getText().toString().length() != 9) {
                    telefono.setError("Teléfono no válido");
                    return false;
                }
            } else if (telefono.getText().toString().length() != 9) {
                if (telefono.getText().toString().length() != 8) {
                    telefono.setError("Teléfono no válido");
                    return false;
                }
            }
        }
        return true;
    }

    public Boolean validarCelular(){
        if (!celular.getText().toString().isEmpty()){
            if (celular.getText().toString().length() !=10){
                celular.setError("Celular no válido");
                return false;
            }
        }
        return true;
    }


    private void crearDatosCliente(){
        if (validarCampos()&&
                !nombre.getText().toString().isEmpty()
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
                    Uri linkAcceso = ((MyFirebaseApp) getActivity().getApplicationContext()).getLinkAcceso();
                    if (linkAcceso!=null){
                        FragmentoStreamigsVendedor fragment = new FragmentoStreamigsVendedor();
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                                .replace(R.id.home_content, fragment)
                                .commit();
                        Toast.makeText(getContext(), "Datos guardados con éxito", Toast.LENGTH_LONG).show();
                    }else {
                        FragmentoHomeCliente fragment = new FragmentoHomeCliente();
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                                .replace(R.id.home_content, fragment)
                                .commit();
                        Toast.makeText(getContext(), "Datos guardados con éxito", Toast.LENGTH_LONG).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),"Error al guardar los datos",Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void actualizarDatosCliente(){
        if (validarCampos() &&
                !nombre.getText().toString().isEmpty()
                && !cedula.getText().toString().isEmpty()
                && !callePrincipa.getText().toString().isEmpty()
                && !referencia.getText().toString().isEmpty()
                && !(telefono.getText().toString().isEmpty() && celular.getText().toString().isEmpty())) {


            //actualizamos los datos del cliente
            Cliente finalCliente = new Cliente();
            //editamos los datos del cliente
            try {
                finalCliente.setIdCliente(clienteGlobal.getIdCliente());
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

            Log.d("idClienteFinal",finalCliente.getIdCliente());
            Log.d("idCliente",clienteGlobal.getIdCliente());

            Query consulta = databaseReference.
                    child("Mensaje_Cliente_Vendedor").
                    orderByChild("cliente/idCliente").
                    equalTo(finalCliente.getIdCliente());
            consulta.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        Mensaje_Cliente_Vendedor mensaje_cliente_vendedor = null;
                        for (DataSnapshot ds:snapshot.getChildren()){
                            Mensaje_Cliente_Vendedor mensaje_cliente_vendedorAux = ds.getValue(Mensaje_Cliente_Vendedor.class);
                            if (mensaje_cliente_vendedorAux !=null){
                                mensaje_cliente_vendedor = mensaje_cliente_vendedorAux;
                            }
                        }
                        if (mensaje_cliente_vendedor!=null){
                            String codigoActualizacion = "Mensaje_Cliente_Vendedor/"+
                                    mensaje_cliente_vendedor.getIdCliente_idVendedor()+"/cliente/nombre";
                            Map<String,Object> mensajeActualizar = new HashMap<>();
                            mensajeActualizar.put(codigoActualizacion,finalCliente.getNombre());
                            databaseReference.updateChildren(mensajeActualizar).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "Error al guardar los datos del mensaje", Toast.LENGTH_LONG).show();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    actualizarCliente(clienteActualizar);
                                }
                            });
                        }else{
                            actualizarCliente(clienteActualizar);
                        }
                    }else{
                        actualizarCliente(clienteActualizar);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error al guardar los datos intentelo de nuevo", Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    public void actualizarCliente (Map<String,Object> clienteActualizar){
        Dialog dialog = new Utilidades().dialogCargar(getContext());
        TextView txt = dialog.findViewById(R.id.txtCargando);
        txt.setText("Guardando datos");

        dialog.show();
        //buscamos el cliente
        databaseReference.child("Cliente").child(clienteGlobal.getIdCliente()).updateChildren(clienteActualizar).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                dialog.dismiss();
                FragmentoHomeCliente fragment = new FragmentoHomeCliente();
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                        .replace(R.id.home_content, fragment)
                        .commit();
                /*
                DialogInterface.OnClickListener dialoOnClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                };
                */
                //String titulo ="Atención";
                //String cuerpo = "Datos actualizados, los mensajes enviados al vendedor no seran actualizados";
                //new Utilidades().cuadroDialogo(getContext(),dialoOnClickListener,titulo,cuerpo);

                Toast.makeText(getContext(), "Datos actualizados con éxito", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error al guardar los datos", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
    }

    public static boolean validarCedula(String x) {
        int suma = 0;
        if (x.length() == 9) {
            return false;
        } else {
            int a[] = new int[x.length() / 2];
            int b[] = new int[(x.length() / 2)];
            int c = 0;
            int d = 1;
            for (int i = 0; i < x.length() / 2; i++) {
                a[i] = Integer.parseInt(String.valueOf(x.charAt(c)));
                c = c + 2;
                if (i < (x.length() / 2) - 1) {
                    b[i] = Integer.parseInt(String.valueOf(x.charAt(d)));
                    d = d + 2;
                }
            }

            for (int i = 0; i < a.length; i++) {
                a[i] = a[i] * 2;
                if (a[i] > 9) {
                    a[i] = a[i] - 9;
                }
                suma = suma + a[i] + b[i];
            }
            int aux = suma / 10;
            int dec = (aux + 1) * 10;
            if ((dec - suma) == Integer.parseInt(String.valueOf(x.charAt(x.length() - 1))))
                return true;
            else if (suma % 10 == 0 && x.charAt(x.length() - 1) == '0') {
                return true;
            } else {
                return false;
            }

        }
    }
}
