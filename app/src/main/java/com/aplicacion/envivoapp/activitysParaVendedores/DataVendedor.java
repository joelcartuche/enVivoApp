package com.aplicacion.envivoapp.activitysParaVendedores;

import android.content.Intent;
import android.os.Bundle;

import com.aplicacion.envivoapp.modelos.Vendedor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class DataVendedor extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private EditText nombre,cedula,celular,telefono,diasEsperaCancelacion;
    private CheckBox tieneTienda;
    private Button guardar;

    //botones del toolbar
    private Button mensajeria;
    private Button listarLocal;
    private Button perfil ;
    private Button pedido ;
    private Button videos;
    private Button salir;



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
                    vendedor.setNombre(nombre.getText().toString());
                    vendedor.setCedula(cedula.getText().toString());
                    vendedor.setCelular(celular.getText().toString());
                    vendedor.setTelefono(telefono.getText().toString());
                    vendedor.setDiasEperaCancelacion(Integer.parseInt(diasEsperaCancelacion.getText().toString()));
                    vendedor.setUidUsuario(firebaseAuth.getCurrentUser().getUid());
                    databaseReference.child("Vendedor").child(vendedor.getIdVendedor()).setValue(vendedor).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {//evaluamos si los datos se guardaron satisfactoriamente
                            Toast.makeText(DataVendedor.this, "Los datos se han guardado correctamente", Toast.LENGTH_LONG).show();
                            Intent dataVendedor = new Intent(DataVendedor.this, DataLocal.class);
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

        llenarCuadros();

        //cargamos el toolbar
        //le damos funcionalidad al toolbar
        mensajeria = findViewById(R.id.btnMensajeriaGlobalDataVendedor);
        listarLocal = findViewById(R.id.btnListarLocalDataVendedor);
        perfil = findViewById(R.id.btnPerfilVendedorDataVendedor);
        pedido = findViewById(R.id.btnPedidoDataVendedor);
        videos = findViewById(R.id.btnVideosDataVendedor);
        salir = findViewById(R.id.btnSalirDataVendedor);
        Button clientes = findViewById(R.id.btnClientesDataVendedor);

        new Utilidades().cargarToolbarVendedor(listarLocal,
                perfil,
                pedido,
                mensajeria,
                salir,
                videos,
                clientes,
                DataVendedor.this,
                firebaseAuth);
    }
    public void llenarCuadros(){

        databaseReference.child("Vendedor").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Vendedor vendedor = null;
                    for (DataSnapshot ds:snapshot.getChildren()){
                        Vendedor vendedorAux  = ds.getValue(Vendedor.class);
                        if (vendedorAux!=null) {
                            if (vendedorAux.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())) {
                                vendedor = vendedorAux;
                                break;
                            }
                        }
                    }
                    if (vendedor!=null){
                        nombre.setText(vendedor.getNombre());
                        cedula.setText(vendedor.getCedula());
                        celular.setText(vendedor.getCelular());
                        telefono.setText(vendedor.getTelefono());
                        diasEsperaCancelacion.setText(vendedor.getDiasEperaCancelacion()+"");

                        mensajeria.setVisibility(View.VISIBLE);
                        listarLocal.setVisibility(View.VISIBLE);
                        perfil.setVisibility(View.VISIBLE);
                        pedido.setVisibility(View.VISIBLE);
                        videos.setVisibility(View.VISIBLE);
                        salir.setVisibility(View.VISIBLE);

                    }else{
                        //desabilitamos los botones

                        mensajeria.setVisibility(View.GONE);
                        listarLocal.setVisibility(View.GONE);
                        perfil.setVisibility(View.GONE);
                        pedido.setVisibility(View.GONE);
                        videos.setVisibility(View.GONE);
                        salir.setVisibility(View.GONE);

                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}