package com.aplicacion.envivoapp.activitysParaVendedores;

import android.content.Intent;
import android.os.Bundle;

import com.aplicacion.envivoapp.modelos.Local;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class DataLocal extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private EditText nombreLocal,direccionLocal,telefonoLocal,celularLocal;
    private Button guardarLocal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_local);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        nombreLocal = findViewById(R.id.txtNombreLocal);
        direccionLocal = findViewById(R.id.txtDireccionLocal);
        telefonoLocal = findViewById(R.id.txtTelefonoLocal);
        celularLocal = findViewById(R.id.txtCelularLocal);
        guardarLocal = findViewById(R.id.btnGuardarLocal);

        guardarLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Local local = new Local();
                local.setIdLocal(UUID.randomUUID().toString());
                local.setNombre(nombreLocal.getText().toString());
                local.setDireccion(direccionLocal.getText().toString());
                local.setTelefono(telefonoLocal.getText().toString());
                local.setCelular(celularLocal.getText().toString());
                local.setIdVendedor(firebaseAuth.getCurrentUser().getUid());
                databaseReference.child("local").child(local.getIdLocal()).setValue(local);
                Toast.makeText(DataLocal.this,"Local guardado con éxito",Toast.LENGTH_LONG).show();
                startActivity(new Intent(DataLocal.this, GestionVideos.class));
                finish();
            }
        });
    }
}