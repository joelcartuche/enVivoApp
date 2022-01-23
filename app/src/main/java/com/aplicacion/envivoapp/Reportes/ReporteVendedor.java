package com.aplicacion.envivoapp.Reportes;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterGridReporteVendedor;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

public class ReporteVendedor extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private File nuevaCarpeta;
    private  Date fecha;


    private RadioButton reportePedidosCancelados,reportePedidosFinalizados,reportePedidosEliminados;
    private Button generarReporte,irRutaArchivo;
    private TextView rutaArchivo;
    private ArrayList<String> listDAta = new ArrayList<>();
    private AdapterGridReporteVendedor adapterListDatos;
    private GridView listaDatosView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte_vendedor);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos



        reportePedidosCancelados = findViewById(R.id.radioPedidosCanceladosReporteVendedor);
        reportePedidosEliminados = findViewById(R.id.radioPedidosEliminadosReporteVendedor);
        reportePedidosFinalizados = findViewById(R.id.radioPedidosFinalizadosReporteVendedor);
        generarReporte = findViewById(R.id.btnGenerarReporteVendedor);
        listaDatosView = findViewById(R.id.gridDatosReporteVendedor);
        rutaArchivo = findViewById(R.id.txtRutaArchivo);
        irRutaArchivo = findViewById(R.id.btnIrRutaArchivo);

        //ocultamos la ruta y el boton para ir a dicha ruta
        rutaArchivo.setVisibility(View.GONE);
        irRutaArchivo.setVisibility(View.GONE);
        generarReporte.setVisibility(View.GONE);

        //creamos la carpeta que contendra los reportes

        nuevaCarpeta = new File(getExternalFilesDir(null), "/reportesEnvivoApp");
        if (!nuevaCarpeta.exists()) {
            nuevaCarpeta.mkdirs();
        }
        //creamos la fecha actual para los reportes
        LocalDateTime tiempoActual = LocalDateTime.now(); //obtenemos la hora y fecha actual
        // creamos la fecha
        fecha = new Date();
        fecha.setDate(tiempoActual.getDayOfMonth());
        fecha.setMonth(tiempoActual.getMonth().getValue());
        fecha.setYear(tiempoActual.getYear());
        fecha.setHours(tiempoActual.getHour());
        fecha.setMinutes(tiempoActual.getMinute());
        fecha.setSeconds(tiempoActual.getSecond());



        reportePedidosEliminados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportePedidosCancelados.setChecked(false);
                reportePedidosFinalizados.setChecked(false);
                reportePedidosEliminados.setChecked(true);
                reportePedidosEliminados();
            }
        });
        reportePedidosFinalizados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportePedidosCancelados.setChecked(false);
                reportePedidosFinalizados.setChecked(true);
                reportePedidosEliminados.setChecked(false);
                reportePedidosEliminados();
            }
        });

        reportePedidosCancelados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportePedidosCancelados.setChecked(true);
                reportePedidosFinalizados.setChecked(false);
                reportePedidosEliminados.setChecked(false);
                reportePedidosEliminados();
            }
        });
        //generamos el informe acorde a los datos ingresados al gridView
        generarReporte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reportePedidosEliminados.isChecked()
                        || reportePedidosFinalizados.isChecked()
                        || reportePedidosCancelados.isChecked()) {
                    String csv = nuevaCarpeta.getAbsolutePath() + "/" + fecha.getDate()
                            + "_" + fecha.getMonth()
                            + "_" + fecha.getYear()
                            + "_" + fecha.getHours()
                            + "_" + fecha.getMinutes() + "_ReporteMensajes.csv";
                    CSVWriter writer = null;
                    try {
                        writer = new CSVWriter(new FileWriter(csv));
                        writer.writeNext(new String[]{"Codigo", "Nombre", "Precio", "Cantidad", "Descripcion", "Fecha pedido", "Hora pedido"});

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    for (int i = 0; i < listDAta.size(); i += 7) {
                        String[] dato = new String[]{
                                listDAta.get(i),
                                listDAta.get(i + 1),
                                listDAta.get(i + 2),
                                listDAta.get(i + 3),
                                listDAta.get(i + 4),
                                listDAta.get(i + 5),
                                listDAta.get(i + 6)
                        };

                        writer.writeNext(dato);
                    }

                    Log.d("Directorio", csv);
                    accederRuta(csv,nuevaCarpeta.getPath());//le damos funcionaledad a los botones
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(ReporteVendedor.this, "El archivo csv a sido generado exitosamente", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(ReporteVendedor.this, "Seleccione una opción para generar el informe", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //le damos funcionalidad al toolbar
        //Damos funcionalidad al menu
        //le damos funcionalidad al toolbar
        Button mensajeria = findViewById(R.id.btnMensajeriaGlobalReporteVendedor);
        Button listarLocal = findViewById(R.id.btnListarLocalReporteVendedor);
        Button perfil = findViewById(R.id.btnPerfilVendedorReporteVendedor);
        Button pedido = findViewById(R.id.btnPedidoReporteVendedor);
        Button videos = findViewById(R.id.btnVideosReporteVendedor);
        Button salir = findViewById(R.id.btnSalirReporteVendedor);
        Button clientes = findViewById(R.id.btnClientesReporteVendedor);
        Button reporte = findViewById(R.id.btnReporteReporteVendedor);
        Button home = findViewById(R.id.btnHomeVendedorReporteVendedor);

        new Utilidades().cargarToolbarVendedor(home,listarLocal,
                perfil,
                pedido,
                mensajeria,
                salir,
                videos,
                clientes,
                reporte,
                ReporteVendedor.this,
                firebaseAuth);

    }

    private void reportePedidosEliminados() {
        databaseReference.child("Vendedor").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Vendedor vendedor = null;
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Vendedor vendedorAux = ds.getValue(Vendedor.class);
                        if (vendedorAux.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())){
                            vendedor=vendedorAux;
                            break;
                        }
                    }
                    if (vendedor!=  null) {
                        Vendedor finalVendedor = vendedor;
                        databaseReference.child("Pedido").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                            @Override
                            public void onSuccess(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    listDAta.clear();
                                    adapterListDatos = new AdapterGridReporteVendedor(ReporteVendedor.this,listDAta);
                                    listaDatosView.setNumColumns(7);
                                    listaDatosView.setAdapter(adapterListDatos); //configuramos el view


                                    listDAta.add("Codigo");listDAta.add("Nombre");listDAta.add("Precio");listDAta.add("Cantidad");listDAta.add("Descripcion");listDAta.add("Fecha pedido");listDAta.add("Hora pedido"); //cargamos el encabezado del archivo
                                    for (DataSnapshot ds:dataSnapshot.getChildren()){
                                        Pedido pedido = ds.getValue(Pedido.class);
                                        if (pedido.getIdVendedor().equals(finalVendedor.getIdVendedor())){
                                            if (reportePedidosEliminados.isChecked()
                                                    &&!pedido.getCancelado()
                                                    &&!pedido.getAceptado()
                                                    &&!pedido.getPagado()
                                                    &&pedido.getEliminado()){
                                                listDAta.add(pedido.getCodigoProducto());
                                                listDAta.add(pedido.getNombreProducto());
                                                listDAta.add(pedido.getPrecioProducto()+"");
                                                listDAta.add(pedido.getCantidadProducto()+"");
                                                listDAta.add(pedido.getDescripcionProducto());
                                                listDAta.add(pedido.getFechaPedido().getDate()+"/"+pedido.getFechaPedido().getMonth()+"/"+pedido.getFechaPedido().getYear());
                                                listDAta.add(pedido.getFechaPedido().getHours()+":"+pedido.getFechaPedido().getMinutes());
                                                adapterListDatos = new AdapterGridReporteVendedor(ReporteVendedor.this,listDAta);
                                                listaDatosView.setNumColumns(7);
                                                listaDatosView.setAdapter(adapterListDatos); //configuramos el view
                                                Toast.makeText(ReporteVendedor.this,"cargando datos...",Toast.LENGTH_SHORT).show();

                                            }

                                            if (reportePedidosCancelados.isChecked()
                                                    &&pedido.getCancelado()
                                                    &&!pedido.getAceptado()
                                                    &&!pedido.getPagado()
                                                    &&!pedido.getEliminado()){
                                                listDAta.add(pedido.getCodigoProducto());
                                                listDAta.add(pedido.getNombreProducto());
                                                listDAta.add(pedido.getPrecioProducto()+"");
                                                listDAta.add(pedido.getCantidadProducto()+"");
                                                listDAta.add(pedido.getDescripcionProducto());
                                                listDAta.add(pedido.getFechaPedido().getDate()+"/"+pedido.getFechaPedido().getMonth()+"/"+pedido.getFechaPedido().getYear());
                                                listDAta.add(pedido.getFechaPedido().getHours()+":"+pedido.getFechaPedido().getMinutes());
                                                adapterListDatos = new AdapterGridReporteVendedor(ReporteVendedor.this,listDAta);
                                                listaDatosView.setNumColumns(7);
                                                listaDatosView.setAdapter(adapterListDatos); //configuramos el view
                                                Toast.makeText(ReporteVendedor.this,"cargando datos...",Toast.LENGTH_SHORT).show();
                                            }

                                            if (reportePedidosFinalizados.isChecked()
                                                    &&!pedido.getCancelado()
                                                    &&!pedido.getAceptado()
                                                    &&pedido.getPagado()
                                                    &&!pedido.getEliminado()){
                                                listDAta.add(pedido.getCodigoProducto());
                                                listDAta.add(pedido.getNombreProducto());
                                                listDAta.add(pedido.getPrecioProducto()+"");
                                                listDAta.add(pedido.getCantidadProducto()+"");
                                                listDAta.add(pedido.getDescripcionProducto());
                                                listDAta.add(pedido.getFechaPedido().getDate()+"/"+pedido.getFechaPedido().getMonth()+"/"+pedido.getFechaPedido().getYear());
                                                listDAta.add(pedido.getFechaPedido().getHours()+":"+pedido.getFechaPedido().getMinutes());
                                                adapterListDatos = new AdapterGridReporteVendedor(ReporteVendedor.this,listDAta);
                                                listaDatosView.setNumColumns(7);
                                                listaDatosView.setAdapter(adapterListDatos); //configuramos el view
                                                Toast.makeText(ReporteVendedor.this,"cargando datos...",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                    //ocultamos el boton generar reportes csv
                                    if (listDAta.size() >= 8){
                                        generarReporte.setVisibility(View.VISIBLE);

                                    }else{
                                        generarReporte.setVisibility(View.GONE);
                                        rutaArchivo.setVisibility(View.GONE);
                                        irRutaArchivo.setVisibility(View.GONE);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void accederRuta(String ruta,String directorio){
        if (!ruta.equals("")){
            rutaArchivo.setVisibility(View.VISIBLE);
            irRutaArchivo.setVisibility(View.VISIBLE);
            rutaArchivo.setText("Ruta del archivo: "+ ruta);
            irRutaArchivo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse(directorio);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "*/*");
                    startActivity(Intent.createChooser(intent, "Open folder"));
                }
            });

        }else{
            rutaArchivo.setVisibility(View.GONE);
            irRutaArchivo.setVisibility(View.GONE);
            Toast.makeText(ReporteVendedor.this,"Error al generar archivo",Toast.LENGTH_LONG);
        }

    }
/*
    String csv = nuevaCarpeta.getAbsolutePath()+"/"+fecha.getDate()
            +"_"+ fecha.getMonth()
            +"_"+ fecha.getYear()
            +"_"+ fecha.getHours()
            +"_"+fecha.getMinutes()+"_ReporteMensajes.csv";
    CSVWriter writer = null;
                    try {
        writer = new CSVWriter(new FileWriter(csv));
        writer.writeNext(new String[]{"Fecha", "Hora", "Cliente", "Texto"});

    } catch (IOException e) {
        e.printStackTrace();
    }

                    writer.writeNext(new String[]{mensaje.getFecha().getDate()+"/"+mensaje.getFecha().getMonth()+"/"+mensaje.getFecha().getYear()
            ,mensaje.getFecha().getHours()+":"+mensaje.getFecha().getMinutes()
            , nombreCliente
            ,mensaje.getTexto()});
            Log.d("Directorio",csv);
            */

}