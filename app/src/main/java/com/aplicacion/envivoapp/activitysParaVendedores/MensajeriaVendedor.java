package com.aplicacion.envivoapp.activitysParaVendedores;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activityParaClientes.MensajeriaCliente;
import com.aplicacion.envivoapp.adaptadores.AdapterGridMensajeriaCliente;
import com.aplicacion.envivoapp.adaptadores.AdapterGridMensajeriaVendedor;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroAceptarPedidoMensajeCliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MensajeriaVendedor extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private Button enviarMensaje;
    private EditText textoMensaje;
    private  String idVendedor,idCliente,idStreaming,urlStreaming;
    private RadioButton filtrarPedido,filtrarTodos;

    private List<Mensaje> listMensaje = new ArrayList<>();
    private GridView gridViewMensaje;
    private AdapterGridMensajeriaVendedor gridAdapterMensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajeria_vendedor);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        //incializaoms las variables
        enviarMensaje = findViewById(R.id.btnEnviarMensajeVendedor);
        textoMensaje = findViewById(R.id.txtMensajeVendedor);
        gridViewMensaje = findViewById(R.id.gridMensajeVendedor);
        filtrarPedido = findViewById(R.id.radioFiltrarPedidoMensajeriaVendedor);
        filtrarTodos  = findViewById(R.id.radioFiltrarTodosMensajeriaVendedor);
        filtrarTodos.setChecked(true);//para que siempre  liste todos los mensajes

        //fin de incializacion de variables

        textoMensaje.setVisibility(View.INVISIBLE);
        enviarMensaje.setVisibility(View.INVISIBLE);
        Bundle vendedor = MensajeriaVendedor.this.getIntent().getExtras();
        idVendedor = vendedor.getString("vendedor"); //recogemos los datos del vendedor
        urlStreaming = vendedor.getString("url"); //recogemos los datos del vendedor
        idStreaming = vendedor.getString("streaming");

        listarMensajes();

        filtrarTodos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarTodos.setChecked(true);
                listarMensajes();
            }
        });
        filtrarPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarTodos.setChecked(false);
                listarMensajes();
            }
        });

    }


    public void listarMensajes(){
        databaseReference.child("Mensaje").orderByChild("fecha").addValueEventListener(new ValueEventListener() { //buscamos todos los datos en la tabla Video Streaming
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listMensaje.clear();//borramos en caso de quedar algo en la cache
                    int contPedido = 0;//almacena el numero de pedidos
                    for (final DataSnapshot ds : snapshot.getChildren()) {
                        Mensaje mensaje = ds.getValue(Mensaje.class);
                        if(mensaje != null){
                            if(mensaje.getIdvendedor().equals(idVendedor)
                                    && mensaje.getIdStreaming().equals(idStreaming)){//aceptamos los mensades que sean del cliente y de el streaming actual
                                if (filtrarPedido.isChecked()){
                                    if (mensaje.getTexto().indexOf("Quiero comprar:") == 0){
                                        listMensaje.add(mensaje);
                                        contPedido+=1;
                                    }
                                }
                                if (filtrarTodos.isChecked()){
                                    listMensaje.add(mensaje);
                                }
                                //Inicialisamos el adaptador
                                gridAdapterMensaje = new AdapterGridMensajeriaVendedor(MensajeriaVendedor.this,
                                        listMensaje,
                                        databaseReference,
                                        filtrarTodos.isChecked());
                                gridViewMensaje.setAdapter(gridAdapterMensaje);
                            }}
                    }
                    if(contPedido== 0){
                        Toast.makeText(MensajeriaVendedor.this,"Usted no tiene pedidos",Toast.LENGTH_LONG).show();
                    }
                }else{
                    listMensaje.clear();//borramos los datos ya que no hay nada en la base
                    //Inicialisamos el adaptador

                    gridAdapterMensaje = new AdapterGridMensajeriaVendedor(MensajeriaVendedor.this,listMensaje,databaseReference,false);
                    gridViewMensaje.setAdapter(gridAdapterMensaje);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }


}