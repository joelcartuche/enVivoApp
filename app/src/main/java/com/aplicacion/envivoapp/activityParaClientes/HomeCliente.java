package com.aplicacion.envivoapp.activityParaClientes;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeCliente extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private Button streaming,pedido,chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_cliente);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        streaming = findViewById(R.id.btnStreamingsHomeComprador);
        pedido = findViewById(R.id.btnPedidosHomeComprador);
        chat =  findViewById(R.id.btnMensajeriaHomeComprador);


        //Damos funcionalidad al menu
        Button btnListarVendedore = findViewById(R.id.btn_listar_vendedores_HomeCliente);
        Button btnPerfil = findViewById(R.id.btn_perfil_HomeCliente);
        Button btnPedido = findViewById(R.id.btn_carrito_HomeCliente);
        Button btnSalir = findViewById(R.id.btn_salir_HomeCliente);
        Button btnMensje = findViewById(R.id.btnMensajeriaGlobalHomeCliente);
        Button btnHome = findViewById(R.id.btn_Home_Home_Clinte);

        new Utilidades().cargarToolbar(btnHome,
                streaming,
                btnPerfil,
                pedido,
                btnSalir,
                chat,
                HomeCliente.this,firebaseAuth,databaseReference);

        new Utilidades().cargarToolbar(btnHome,
                btnListarVendedore,
                btnPerfil,
                btnPedido,
                btnSalir,
                btnMensje,
                HomeCliente.this,firebaseAuth,databaseReference);


    }


}