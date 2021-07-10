package com.aplicacion.envivoapp.activityParaClientes;

import android.content.Intent;
import android.os.Bundle;

import com.aplicacion.envivoapp.activitysParaVendedores.DataLocal;
import com.aplicacion.envivoapp.activitysParaVendedores.DataVendedor;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class DataCliente extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private EditText nombre,apellido,cedula,direccion,telefono,celular;
    private Button guardar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_cliente);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        nombre = findViewById(R.id.txtNombreCliente);
        apellido = findViewById(R.id.txtApellidoCliente);
        cedula = findViewById(R.id.txtCedulaCliente);
        direccion = findViewById(R.id.txtDireccionCliente);
        telefono = findViewById(R.id.txtTelefonoCliente);
        celular = findViewById(R.id.txtCelularCliente);
        guardar = findViewById(R.id.btnGuardarCliente);


            guardar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(nombre.getText().toString().isEmpty()){
                        Toast.makeText(DataCliente.this,"Igrese su nombre",Toast.LENGTH_LONG).show();
                    }else if (apellido.getText().toString().isEmpty()){
                        Toast.makeText(DataCliente.this,"Igrese su apellido",Toast.LENGTH_LONG).show();
                    }else if (cedula.getText().toString().isEmpty()){
                        Toast.makeText(DataCliente.this,"Igrese su número de cédula",Toast.LENGTH_LONG).show();
                    }else if (direccion.getText().toString().isEmpty()){
                        Toast.makeText(DataCliente.this,"Igrese su dirección",Toast.LENGTH_LONG).show();
                    }else if (telefono.getText().toString().isEmpty() && celular.getText().toString().isEmpty()){
                        Toast.makeText(DataCliente.this,"Igrese su telefono o celular",Toast.LENGTH_LONG).show();
                    }
                    if (!nombre.getText().toString().isEmpty()
                            &&!apellido.getText().toString().isEmpty()
                            &&!cedula.getText().toString().isEmpty()
                            &&!direccion.getText().toString().isEmpty()
                            &&!(telefono.getText().toString().isEmpty() && celular.getText().toString().isEmpty())){

                        Cliente cliente = new Cliente();
                        cliente.setIdCliente(UUID.randomUUID().toString());
                        cliente.setNombre(nombre.getText().toString()+" "+apellido.getText().toString());
                        cliente.setCedula(cedula.getText().toString());
                        cliente.setDireccion(direccion.getText().toString());
                        cliente.setTelefono(telefono.getText().toString());
                        cliente.setCelular(celular.getText().toString());
                        cliente.setUidUsuario(firebaseAuth.getCurrentUser().getUid());
                        databaseReference.child("Cliente").child(cliente.getIdCliente()).setValue(cliente);
                        Intent listarIntent = new Intent(DataCliente.this, ListarVendedores.class);
                        startActivity(listarIntent);
                        finish();

                    }


                }
            });

    }
}