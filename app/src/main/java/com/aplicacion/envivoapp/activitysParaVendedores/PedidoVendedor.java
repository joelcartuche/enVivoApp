package com.aplicacion.envivoapp.activitysParaVendedores;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterGridPedidoVendedor;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroCambiarPedido;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PedidoVendedor extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage ; //para la insersion de archivos
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();

    private List<Pedido> listPedido = new ArrayList<>();
    private GridView gridViewPedido;
    private AdapterGridPedidoVendedor gridAdapterPedido;

    private RadioButton filtrarAceptados, filtrarEliminados, filtrarTodos,filtrarPagados,filtrarFechaPasado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido_vendedor);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        storage = FirebaseStorage.getInstance();//inicializamos la variable storage

        gridViewPedido = findViewById(R.id.gridPedidoVendedor);

        filtrarAceptados = findViewById(R.id.radioAceptadoPedidoVendedor);
        filtrarEliminados = findViewById(R.id.radioEliminadosPedidoVendedor);
        filtrarTodos = findViewById(R.id.radioTodosPedidoVendedor);
        filtrarPagados = findViewById(R.id.radioPagadoPedidoVendedor);
        filtrarFechaPasado = findViewById(R.id.radioPasadosFecha);
        if(filtrarTodos.isChecked()){
            filtrarTodos.setChecked(true);
            filtrarAceptados.setChecked(false);
            filtrarEliminados.setChecked(false);
            filtrarPagados.setChecked(false);
            filtrarFechaPasado.setChecked(false);
            listarPedidos();
        }

        filtrarAceptados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarAceptados.setChecked(true);
                filtrarEliminados.setChecked(false);
                filtrarTodos.setChecked(false);
                filtrarPagados.setChecked(false);
                filtrarFechaPasado.setChecked(false);
                listarPedidos();
            }
        });
        filtrarEliminados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarEliminados.setChecked(true);
                filtrarAceptados.setChecked(false);
                filtrarTodos.setChecked(false);
                filtrarPagados.setChecked(false);
                filtrarFechaPasado.setChecked(false);
                listarPedidos();
            }
        });
        filtrarTodos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarTodos.setChecked(true);
                filtrarAceptados.setChecked(false);
                filtrarEliminados.setChecked(false);
                filtrarPagados.setChecked(false);
                filtrarFechaPasado.setChecked(false);
                listarPedidos();
            }
        });

        filtrarPagados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarTodos.setChecked(false);
                filtrarAceptados.setChecked(false);
                filtrarEliminados.setChecked(false);
                filtrarPagados.setChecked(true);
                filtrarFechaPasado.setChecked(false);
                listarPedidos();
            }
        });

        filtrarFechaPasado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarTodos.setChecked(false);
                filtrarAceptados.setChecked(false);
                filtrarEliminados.setChecked(false);
                filtrarPagados.setChecked(false);
                filtrarFechaPasado.setChecked(true);
                listarPedidos();
            }
        });

        //le damos funcionalidad al toolbar
        Button mensajeria = findViewById(R.id.btnMensajeriaGlobalPedidoVendedor);
        Button listarLocal = findViewById(R.id.btnListarLocalPedidoVendedor);
        Button perfil = findViewById(R.id.btnPerfilVendedorPedidoVendedor);
        Button pedido = findViewById(R.id.btnPedidoPedidoVendedor);
        Button videos = findViewById(R.id.btnVideosPedidoVendedor);
        Button salir = findViewById(R.id.btnSalirPedidoVendedor);
        Button clientes = findViewById(R.id.btnClientesPedidoVendedo);
        Button reporte = findViewById(R.id.btnReportePedidoVendedor);
        Button home = findViewById(R.id.btnHomeVendedorPedidoVendedor);

        new Utilidades().cargarToolbarVendedor(home,
                listarLocal,
                perfil,
                pedido,
                mensajeria,
                salir,
                videos,
                clientes,
                reporte,
                PedidoVendedor.this,
                firebaseAuth);
    }


    private void borrarGrid(){
        listPedido.clear();//borramos en caso de quedar algo en la cache
        //Inicialisamos el adaptador
        gridAdapterPedido = new AdapterGridPedidoVendedor(PedidoVendedor.this, listPedido,databaseReference,storage);
        gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view
    }
    private void listarPedidos() {
        borrarGrid();//borramos los datos del grid

        Query queryVendedor = databaseReference.child("Vendedor").orderByChild("uidUsuario").equalTo(firebaseAuth.getCurrentUser().getUid());


        queryVendedor.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Vendedor vendedor = null;
                    for (DataSnapshot ds:snapshot.getChildren()){
                        vendedor = ds.getValue(Vendedor.class);
                                            }
                    if (vendedor!= null){
                        Vendedor finalVendedor = vendedor;
                        Query queryPedido = databaseReference.child("Pedido").orderByChild("idVendedor").equalTo(vendedor.getIdVendedor());
                        queryPedido.addValueEventListener(new ValueEventListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    listPedido.clear();
                                    for (DataSnapshot ds:snapshot.getChildren()){
                                        Pedido pedido = ds.getValue(Pedido.class);
                                        try {
                                            pedido.setImagen(encriptacionDatos.desencriptar(pedido.getImagen()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            pedido.setCodigoProducto(encriptacionDatos.desencriptar(pedido.getCodigoProducto()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            pedido.setDescripcionProducto(encriptacionDatos.desencriptar(pedido.getDescripcionProducto()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            pedido.setNombreProducto(encriptacionDatos.desencriptar(pedido.getNombreProducto()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }





                                            if (!pedido.getEliminado()
                                                    && filtrarTodos.isChecked()){
                                                listPedido.add(pedido);
                                            }
                                            if (filtrarEliminados.isChecked()
                                                    && !pedido.getAceptado()
                                                    && pedido.getCancelado()
                                                    && !pedido.getEliminado()){
                                                listPedido.add(pedido);
                                            }
                                            if (pedido.getAceptado()
                                                    && !pedido.getCancelado()
                                                    &&filtrarAceptados.isChecked()
                                                    &&!pedido.getEliminado()){
                                                listPedido.add(pedido);
                                            }
                                            if (pedido.getAceptado()
                                                    && !pedido.getCancelado()
                                                    &&filtrarPagados.isChecked()
                                                    &&!pedido.getEliminado()
                                                    && pedido.getPagado()){
                                                listPedido.add(pedido);
                                            }
                                            if (pedido.getAceptado()
                                                    && !pedido.getCancelado()
                                                    && filtrarFechaPasado.isChecked()
                                                    && !pedido.getEliminado()
                                                    && !pedido.getPagado()){//filtramos por pedido que es aceptado y pasado de fecha

                                                //creamos el date en base la hora actual
                                                LocalDateTime tiempoActual = LocalDateTime.now();//obtenemos la fecha actual
                                                Date fecha = new Date();
                                                fecha.setDate(tiempoActual.getDayOfMonth());
                                                fecha.setMonth(tiempoActual.getMonth().getValue());
                                                fecha.setYear(tiempoActual.getYear());
                                                fecha.setHours(tiempoActual.getHour());
                                                fecha.setMinutes(tiempoActual.getMinute());
                                                fecha.setSeconds(tiempoActual.getSecond());

                                                if (pedido.getFechaFinalPedido().after(fecha)){//comparamos las fechas
                                                    listPedido.add(pedido);
                                                }

                                            }

                                    }
                                    //Inicialisamos el adaptador
                                    gridAdapterPedido = new AdapterGridPedidoVendedor(PedidoVendedor.this, listPedido,databaseReference,storage);
                                    gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view


                                }else{
                                    borrarGrid();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                borrarGrid();
                            }
                        });
                    }else{
                        borrarGrid();
                    }

                }else{
                    borrarGrid();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}