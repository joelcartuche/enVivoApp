package com.aplicacion.envivoapp.activityParaClientes;

import android.content.Intent;
import android.os.Bundle;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.aplicacion.envivoapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataCliente extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private EditText nombre, cedula, callePrincipa,calleSecundaria,referencia, telefono, celular;
    private Button guardar;
    private Toolbar toolbar;

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
                }else if (((MyFirebaseApp)getBaseContext().getApplicationContext()).getLatLng()== null){
                    Toast.makeText(DataCliente.this,"Error al obtener coordenadas del mapa",Toast.LENGTH_LONG).show();
                }else if (telefono.getText().toString().isEmpty() && celular.getText().toString().isEmpty()){
                    Toast.makeText(DataCliente.this,"Ingrese su teléfono o celular",Toast.LENGTH_LONG).show();
                }
                if (!nombre.getText().toString().isEmpty()
                        &&!cedula.getText().toString().isEmpty()
                        &&!callePrincipa.getText().toString().isEmpty()
                        &&!referencia.getText().toString().isEmpty()
                        &&((MyFirebaseApp)getBaseContext().getApplicationContext()).getLatLng()!=null
                        &&!(telefono.getText().toString().isEmpty() && celular.getText().toString().isEmpty())){

                    Cliente cliente = new Cliente();
                    cliente.setIdCliente(UUID.randomUUID().toString());
                    cliente.setNombre(nombre.getText().toString());
                    cliente.setCedula(cedula.getText().toString());
                    cliente.setCallePrincipal(callePrincipa.getText().toString());
                    cliente.setCalleSecundaria(calleSecundaria.getText().toString());
                    LatLng aux = ((MyFirebaseApp)getBaseContext().getApplicationContext()).getLatLng();
                    cliente.setLatitud(aux.latitude);
                    cliente.setLongitud(aux.longitude);

                    cliente.setTelefono(telefono.getText().toString());
                    cliente.setCelular(celular.getText().toString());
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
        databaseReference.child("Cliente").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Cliente cliente = null;
                    for (DataSnapshot ds:snapshot.getChildren()){
                        Cliente clienteAux = ds.getValue(Cliente.class);
                        if (clienteAux.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())){
                            cliente = clienteAux;
                            break;
                        }
                    }
                    if (cliente != null){
                        nombre.setText(cliente.getNombre());
                        cedula.setText(cliente.getCedula());
                        callePrincipa.setText(cliente.getCallePrincipal());
                        calleSecundaria.setText(cliente.getCalleSecundaria());
                        referencia.setText(cliente.getReferencia());
                        telefono.setText(cliente.getTelefono());
                        celular.setText(cliente.getCelular());
                        ((MyFirebaseApp)getBaseContext().getApplicationContext()).setLatLng(new LatLng(cliente.getLatitud(),cliente.getLongitud()));
                        //actualizamos los datos del cliente
                        Cliente finalCliente = cliente;
                        guardar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(nombre.getText().toString().isEmpty()){
                                    Toast.makeText(DataCliente.this,"Igrese su nombre",Toast.LENGTH_LONG).show();
                                }else if (cedula.getText().toString().isEmpty()){
                                    Toast.makeText(DataCliente.this,"Igrese su número de cédula",Toast.LENGTH_LONG).show();
                                }else if (callePrincipa.getText().toString().isEmpty()){
                                    Toast.makeText(DataCliente.this,"Igrese la calle principal",Toast.LENGTH_LONG).show();
                                }else if (referencia.getText().toString().isEmpty()){
                                    Toast.makeText(DataCliente.this,"Igrese una referencia",Toast.LENGTH_LONG).show();
                                }else if (((MyFirebaseApp) getBaseContext().getApplicationContext()).getLatLng()!=null){
                                    Toast.makeText(DataCliente.this,"Error al obtener coordenadas del mapa",Toast.LENGTH_LONG).show();
                                }else if (telefono.getText().toString().isEmpty() && celular.getText().toString().isEmpty()){
                                    Toast.makeText(DataCliente.this,"Igrese su telefono o celular",Toast.LENGTH_LONG).show();
                                }
                                if (!nombre.getText().toString().isEmpty()
                                        &&!cedula.getText().toString().isEmpty()
                                        &&!callePrincipa.getText().toString().isEmpty()
                                        &&!referencia.getText().toString().isEmpty()
                                        &&!(telefono.getText().toString().isEmpty() && celular.getText().toString().isEmpty())){

                                    //creamos el cliente
                                    Cliente clienteAux = new Cliente();
                                    clienteAux.setNombre(nombre.getText().toString());
                                    clienteAux.setCedula(cedula.getText().toString());
                                    clienteAux.setCallePrincipal(callePrincipa.getText().toString());
                                    if (!calleSecundaria.getText().toString().isEmpty()
                                            ||calleSecundaria.getText().toString().equals("")) {clienteAux.setCalleSecundaria(calleSecundaria.getText().toString());}
                                    clienteAux.setReferencia(referencia.getText().toString());
                                    clienteAux.setTelefono(telefono.getText().toString());
                                    clienteAux.setCelular(celular.getText().toString());

                                    LatLng latLng = ((MyFirebaseApp) getBaseContext().getApplicationContext()).getLatLng();
                                    clienteAux.setLatitud(latLng.latitude);
                                    clienteAux.setLongitud(latLng.longitude);

                                    //ingresamos en un map los datos que van a ser actualizados
                                    Map<String,Object> clienteActualizar= new HashMap<>();
                                    clienteActualizar.put("cedula",clienteAux.getCedula());
                                    clienteActualizar.put("celular",clienteAux.getCelular());
                                    clienteActualizar.put("callePrincipal",clienteAux.getCallePrincipal());
                                    clienteActualizar.put("calleSecundaria",clienteAux.getCalleSecundaria());
                                    clienteActualizar.put("referencia",clienteAux.getReferencia());

                                    clienteActualizar.put("latitud",clienteAux.getLatitud());
                                    clienteActualizar.put("longitud",clienteAux.getLongitud());

                                    clienteActualizar.put("nombre",clienteAux.getNombre());
                                    clienteActualizar.put("telefono",clienteAux.getTelefono());

                                    //buscamos el cliente
                                    databaseReference.child("Cliente").child(finalCliente.getIdCliente()).updateChildren(clienteActualizar).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(DataCliente.this,"Datos actualizados con éxito",Toast.LENGTH_LONG).show();
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
}