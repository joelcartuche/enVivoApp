package com.aplicacion.envivoapp.activitysParaVendedores;

import android.content.Intent;
import android.os.Bundle;

import com.aplicacion.envivoapp.modelos.Vendedor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;


import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.Utilidades;
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

public class DataVendedor extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private EditText nombre,cedula,celular,telefono,diasEsperaCancelacion;
    private CheckBox tieneTienda;
    private Button guardar;
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();
    //botones del toolbar
    private Toolbar dataVendedor;


    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

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
        guardar = findViewById(R.id.btnGuardar);
        dataVendedor = findViewById(R.id.toolbar_DataVendedor);

        cargarDatosVendedor();

        //cargamos el toolbar
        //le damos funcionalidad al toolbar
        Button mensajeria = findViewById(R.id.btnMensajeriaGlobalDataVendedor);
        Button listarLocal = findViewById(R.id.btnListarLocalDataVendedor);
        Button perfil = findViewById(R.id.btnPerfilVendedorDataVendedor);
        Button pedido = findViewById(R.id.btnPedidoDataVendedor);
        Button videos = findViewById(R.id.btnVideosDataVendedor);
        Button salir = findViewById(R.id.btnSalirDataVendedor);
        Button clientes = findViewById(R.id.btnClientesDataVendedor);
        Button reporte = findViewById(R.id.btnReporteDataVendedor);
        Button home = findViewById(R.id.btnHomeVendedorDataVendedor);

        new Utilidades().cargarToolbarVendedor(home,
                listarLocal,
                perfil,
                pedido,
                mensajeria,
                salir,
                videos,
                clientes,
                reporte,
                DataVendedor.this,
                firebaseAuth);
    }
    public void cargarDatosVendedor(){
        Query query = databaseReference.child("Vendedor").orderByChild("uidUsuario").equalTo(firebaseAuth.getCurrentUser().getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Vendedor vendedor = null;

                    for (DataSnapshot ds:snapshot.getChildren()){
                        vendedor  = ds.getValue(Vendedor.class);
                    }

                    if (vendedor!=null){
                        try {
                            nombre.setText(encriptacionDatos.desencriptar(vendedor.getNombre()));
                            cedula.setText(encriptacionDatos.desencriptar(vendedor.getCedula()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            celular.setText(encriptacionDatos.desencriptar(vendedor.getCelular()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            telefono.setText(encriptacionDatos.desencriptar(vendedor.getTelefono()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        diasEsperaCancelacion.setText(vendedor.getDiasEperaCancelacion()+"");
                        actualizar(vendedor);
                        dataVendedor.setVisibility(View.VISIBLE);

                    }else{
                        //desabilitamos los botones
                        guardar();
                        dataVendedor.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void guardar(){
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nombre.getText().toString().equals("")){
                    Toast.makeText(DataVendedor.this,"Ingrese el nombre",Toast.LENGTH_LONG).show();
                }else if (cedula.getText().toString().equals("")){
                    Toast.makeText(DataVendedor.this,"Ingrese el numero de cedula",Toast.LENGTH_LONG).show();
                }else if (celular.getText().toString().equals("")
                        &&telefono.getText().toString().equals("")){
                    Toast.makeText(DataVendedor.this,"Ingrese el un telefono o celular",Toast.LENGTH_LONG).show();
                }else {
                    Vendedor vendedor = new Vendedor();
                    vendedor.setIdVendedor(UUID.randomUUID().toString());
                    try {
                        vendedor.setNombre(encriptacionDatos.encriptar(nombre.getText().toString()));
                        vendedor.setCedula(encriptacionDatos.encriptar(cedula.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        vendedor.setCelular(encriptacionDatos.encriptar(celular.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        vendedor.setTelefono(encriptacionDatos.encriptar(telefono.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    vendedor.setDiasEperaCancelacion(Integer.parseInt(diasEsperaCancelacion.getText().toString()));
                    vendedor.setUidUsuario(firebaseAuth.getCurrentUser().getUid());
                        databaseReference.child("Vendedor").child(vendedor.getIdVendedor()).setValue(vendedor).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {//evaluamos si los datos se guardaron satisfactoriamente
                                Toast.makeText(DataVendedor.this, "Los datos se han guardado correctamente", Toast.LENGTH_LONG).show();
                                Intent dataVendedor = new Intent(DataVendedor.this, HomeVendedor.class);
                                startActivity(dataVendedor);
                            }
                        }).addOnFailureListener(new OnFailureListener() { //evaluamos si a ocurrido algun error al guardar los datos
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(DataVendedor.this, "No se a podido guardar los datos", Toast.LENGTH_LONG).show();
                            }
                        });
                }
            }
        });
    }

    private void actualizar(Vendedor vendedor){
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nombre.getText().toString().equals("")){
                    Toast.makeText(DataVendedor.this,"Ingrese el nombre",Toast.LENGTH_LONG).show();
                }else if (cedula.getText().toString().equals("")){
                    Toast.makeText(DataVendedor.this,"Ingrese el numero de cedula",Toast.LENGTH_LONG).show();
                }else if (celular.getText().toString().equals("")
                        &&telefono.getText().toString().equals("")){
                    Toast.makeText(DataVendedor.this,"Ingrese el un telefono o celular",Toast.LENGTH_LONG).show();
                }else {

                    try {
                        vendedor.setNombre(encriptacionDatos.encriptar(nombre.getText().toString()));
                        vendedor.setCedula(encriptacionDatos.encriptar(cedula.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        vendedor.setCelular(encriptacionDatos.encriptar(celular.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        vendedor.setTelefono(encriptacionDatos.encriptar(telefono.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    vendedor.setDiasEperaCancelacion(Integer.parseInt(diasEsperaCancelacion.getText().toString()));
                    vendedor.setUidUsuario(firebaseAuth.getCurrentUser().getUid());

                        Map<String, Object> map = new HashMap<>();
                        map.put("nombre",vendedor.getNombre());
                        map.put("cedula",vendedor.getCedula());
                        if (vendedor.getCelular()!=null) {
                            map.put("celular", vendedor.getCelular());
                        }
                        if(vendedor.getTelefono()!=null) {
                            map.put("telefono", vendedor.getTelefono());
                        }
                        map.put("diasEperaCancelacion",vendedor.getDiasEperaCancelacion());
                        databaseReference.child("Vendedor").child(vendedor.getIdVendedor()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(DataVendedor.this,"Datos actualizados con éxito",Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(DataVendedor.this,"Error al actualizar los datos intentelo de nuevo",Toast.LENGTH_LONG).show();
                            }
                        });


                }
            }
        });
    }

}