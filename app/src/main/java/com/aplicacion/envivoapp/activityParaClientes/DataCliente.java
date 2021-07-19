package com.aplicacion.envivoapp.activityParaClientes;

import android.content.Intent;
import android.os.Bundle;

import com.aplicacion.envivoapp.activitysParaVendedores.DataLocal;
import com.aplicacion.envivoapp.activitysParaVendedores.DataVendedor;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

    private EditText nombre, cedula, direccion, telefono, celular;
    private Button guardar;

    Toolbar toolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_cliente);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        nombre = findViewById(R.id.txtNombreCliente);

        cedula = findViewById(R.id.txtCedulaCliente);
        direccion = findViewById(R.id.txtDireccionCliente);
        telefono = findViewById(R.id.txtTelefonoCliente);
        celular = findViewById(R.id.txtCelularCliente);
        guardar = findViewById(R.id.btnGuardarCliente);
        toolbar = findViewById(R.id.toolbaeDataCliente);
        buscarCliente();



        //Damos funcionalidad al menu
        Button btnListarVendedore = findViewById(R.id.btn_listar_vendedores_DataCliente);
        Button btnPerfil = findViewById(R.id.btn_perfil_DataClienter);
        Button btnPedido = findViewById(R.id.btn_carrito_DataCliente);
        Button btnSalir = findViewById(R.id.btn_perfil_DataClienter);
        Button btnMensje = findViewById(R.id.btnMensajeriaGlobalDataCliente);

        new Utilidades().cargarToolbar(btnListarVendedore,
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
                    Toast.makeText(DataCliente.this,"Igrese su nombre",Toast.LENGTH_LONG).show();
                }else if (cedula.getText().toString().isEmpty()){
                    Toast.makeText(DataCliente.this,"Igrese su número de cédula",Toast.LENGTH_LONG).show();
                }else if (direccion.getText().toString().isEmpty()){
                    Toast.makeText(DataCliente.this,"Igrese su dirección",Toast.LENGTH_LONG).show();
                }else if (telefono.getText().toString().isEmpty() && celular.getText().toString().isEmpty()){
                    Toast.makeText(DataCliente.this,"Igrese su telefono o celular",Toast.LENGTH_LONG).show();
                }
                if (!nombre.getText().toString().isEmpty()
                        &&!cedula.getText().toString().isEmpty()
                        &&!direccion.getText().toString().isEmpty()
                        &&!(telefono.getText().toString().isEmpty() && celular.getText().toString().isEmpty())){

                    Cliente cliente = new Cliente();
                    cliente.setIdCliente(UUID.randomUUID().toString());
                    cliente.setNombre(nombre.getText().toString());
                    cliente.setCedula(cedula.getText().toString());
                    cliente.setDireccion(direccion.getText().toString());
                    cliente.setTelefono(telefono.getText().toString());
                    cliente.setCelular(celular.getText().toString());
                    cliente.setUidUsuario(firebaseAuth.getCurrentUser().getUid());
                    databaseReference.child("Cliente").child(cliente.getIdCliente()).setValue(cliente).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(DataCliente.this,"Datos guardados con éxito",Toast.LENGTH_LONG).show();
                            Intent listarIntent = new Intent(DataCliente.this, ListarVendedores.class);
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
                        direccion.setText(cliente.getDireccion());
                        telefono.setText(cliente.getTelefono());
                        celular.setText(cliente.getCelular());
                        //actualizamos los datos del cliente
                        Cliente finalCliente = cliente;
                        guardar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(nombre.getText().toString().isEmpty()){
                                    Toast.makeText(DataCliente.this,"Igrese su nombre",Toast.LENGTH_LONG).show();
                                }else if (cedula.getText().toString().isEmpty()){
                                    Toast.makeText(DataCliente.this,"Igrese su número de cédula",Toast.LENGTH_LONG).show();
                                }else if (direccion.getText().toString().isEmpty()){
                                    Toast.makeText(DataCliente.this,"Igrese su dirección",Toast.LENGTH_LONG).show();
                                }else if (telefono.getText().toString().isEmpty() && celular.getText().toString().isEmpty()){
                                    Toast.makeText(DataCliente.this,"Igrese su telefono o celular",Toast.LENGTH_LONG).show();
                                }
                                if (!nombre.getText().toString().isEmpty()
                                        &&!cedula.getText().toString().isEmpty()
                                        &&!direccion.getText().toString().isEmpty()
                                        &&!(telefono.getText().toString().isEmpty() && celular.getText().toString().isEmpty())){

                                    //creamos el cliente
                                    Cliente clienteAux = new Cliente();
                                    clienteAux.setNombre(nombre.getText().toString());
                                    clienteAux.setCedula(cedula.getText().toString());
                                    clienteAux.setDireccion(direccion.getText().toString());
                                    clienteAux.setTelefono(telefono.getText().toString());
                                    clienteAux.setCelular(celular.getText().toString());

                                    //ingresamos en un map los datos que van a ser actualizados
                                    Map<String,Object> clienteActualizar= new HashMap<>();
                                    clienteActualizar.put("cedula",clienteAux.getCedula());
                                    clienteActualizar.put("celular",clienteAux.getCelular());
                                    clienteActualizar.put("direccion",clienteAux.getDireccion());
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