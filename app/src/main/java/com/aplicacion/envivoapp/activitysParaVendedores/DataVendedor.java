package com.aplicacion.envivoapp.activitysParaVendedores;

import android.content.Intent;
import android.os.Bundle;

import com.aplicacion.envivoapp.modelos.Vendedor;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class DataVendedor extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private EditText nombre,cedula,celular,telefono,diasEsperaCancelacion;
    private CheckBox tieneTienda;
    private Button guardar;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_vendedor);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        nombre = findViewById(R.id.txtNombre);
        cedula = findViewById(R.id.txtCedula);
        celular = findViewById(R.id.txtCelular);
        telefono = findViewById(R.id.txtTelefono);
        diasEsperaCancelacion = findViewById(R.id.txtNumeroDiasCancelacion);
        tieneTienda = findViewById(R.id.checkTieneLocal);
        guardar = findViewById(R.id.btnGuardar);


        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vendedor vendedor = new Vendedor();
                vendedor.setIdVendedor(UUID.randomUUID().toString());
                vendedor.setNombre(nombre.getText().toString());
                vendedor.setCedula(cedula.getText().toString());
                vendedor.setCelular(celular.getText().toString());
                vendedor.setTelefono(telefono.getText().toString());
                vendedor.setDiasEperaCancelacion(Integer.parseInt(diasEsperaCancelacion.getText().toString()));
                vendedor.setTieneTienda(tieneTienda.isChecked());
                vendedor.setUidUsuario(firebaseAuth.getCurrentUser().getUid());
                databaseReference.child("Vendedor").child(vendedor.getIdVendedor()).setValue(vendedor);
                Toast.makeText(DataVendedor.this,"Vendedor Creado con exito",Toast.LENGTH_LONG).show();

                if(vendedor.isTieneTienda()){
                    Intent dataVendedor = new Intent(DataVendedor.this, DataLocal.class);
                    startActivity(dataVendedor);
                    finish();
                }
            }
        });
    }
}