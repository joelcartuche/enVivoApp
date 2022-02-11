package com.aplicacion.envivoapp.activitysParaVendedores;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Local;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Usuario;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.modelos.VideoStreaming;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class HomeVendedor extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private  Button local,pedidos,generarReportes,videoStreamings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_vendedor);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        local = findViewById(R.id.btnLocalHomeVendedor);
        pedidos = findViewById(R.id.btnPedidosHomeVendedor);
        generarReportes = findViewById(R.id.btnReportesHomeVendedor);
        videoStreamings = findViewById(R.id.btnStreamingsHomeVendedor);

        //le damos funcionalidad al toolbar
        Button mensajeria = findViewById(R.id.btnMensajeriaGlobalHomeVendedor);
        Button listarLocal = findViewById(R.id.btnListarLocalHomeVendedor);
        Button perfil = findViewById(R.id.btnPerfilVendedorHomeVendedor);
        Button pedido = findViewById(R.id.btnPedidoHomeVendedor);
        Button videos = findViewById(R.id.btnVideosHomeVendedor);
        Button salir = findViewById(R.id.btnSalirHomeVendedor);
        Button clientes = findViewById(R.id.btnClientesHomeVendedor);
        Button reporte = findViewById(R.id.btnReporteHomeVendedor);
        Button home = findViewById(R.id.btnHomeVendedor);



        new Utilidades().cargarToolbarVendedor(home,
                local,
                perfil,
                pedidos,
                mensajeria,
                salir,
                videoStreamings,
                clientes,
                generarReportes,
                HomeVendedor.this,
                firebaseAuth);

        new Utilidades().cargarToolbarVendedor(home,
                listarLocal,
                perfil,
                pedido,
                mensajeria,
                salir,
                videos,
                clientes,
                reporte,
                HomeVendedor.this,
                firebaseAuth);


    }

}