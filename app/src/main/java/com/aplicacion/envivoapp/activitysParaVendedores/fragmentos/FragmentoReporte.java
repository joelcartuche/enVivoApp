package com.aplicacion.envivoapp.activitysParaVendedores.fragmentos;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterGridReporteVendedor;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

public class FragmentoReporte extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private File nuevaCarpeta;
    private Date fecha;
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();


    private RadioButton reportePedidosCancelados,reportePedidosFinalizados,reportePedidosEliminados;
    private Button generarReporte,irRutaArchivo;
    private TextView rutaArchivo;
    private ArrayList<Pedido> listDAta = new ArrayList<>();
    private AdapterGridReporteVendedor adapterListDatos;
    private RecyclerView listaDatosView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_reporte_vendedor, container, false);


        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        reportePedidosCancelados = root.findViewById(R.id.radioPedidosCanceladosReporteVendedor);
        reportePedidosEliminados = root.findViewById(R.id.radioPedidosEliminadosReporteVendedor);
        reportePedidosFinalizados = root.findViewById(R.id.radioPedidosFinalizadosReporteVendedor);
        generarReporte = root.findViewById(R.id.btnGenerarReporteVendedor);
        listaDatosView = root.findViewById(R.id.gridDatosReporteVendedor);
        rutaArchivo = root.findViewById(R.id.txtRutaArchivo);
        irRutaArchivo = root.findViewById(R.id.btnIrRutaArchivo);

        //ocultamos la ruta y el boton para ir a dicha ruta
        rutaArchivo.setVisibility(View.GONE);
        irRutaArchivo.setVisibility(View.GONE);
        generarReporte.setVisibility(View.GONE);

        //creamos la carpeta que contendra los reportes

        nuevaCarpeta = new File(getActivity().getExternalFilesDir(null), "/reportesEnvivoApp");
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
                    Dialog cargar = cargar(getContext());
                    cargar.show();

                    for (int i = 0; i < listDAta.size(); i += 7) {
                        Pedido pedido = listDAta.get(i);
                        String[] dato = new String[]{
                                pedido.getCodigoProducto(),
                                pedido.getNombreProducto(),
                                pedido.getPrecioProducto()+"",
                                pedido.getCantidadProducto()+"",
                                pedido.getDescripcionProducto(),
                                pedido.getFechaPedido().getDate()+"/"+pedido.getFechaPedido().getMonth()+"/"+pedido.getFechaPedido().getYear(),
                                pedido.getFechaPedido().getHours()+":"+pedido.getFechaPedido().getMinutes()
                        };
                        writer.writeNext(dato);
                    }

                    cargar.dismiss();
                    Log.d("Directorio", csv);
                    accederRuta(csv,nuevaCarpeta.getPath());//le damos funcionaledad a los botones
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getContext(), "El archivo csv a sido generado exitosamente", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getContext(), "Seleccione una opción para generar el informe", Toast.LENGTH_SHORT).show();
                }
            }
        });




        return root;
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
                        //cargamos el cuadro cargar
                        Dialog cargar = cargar(getContext());
                        cargar.show();
                        databaseReference.child("Pedido").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                            @Override
                            public void onSuccess(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    listDAta.clear();
                                    adapterListDatos = new AdapterGridReporteVendedor(getContext(),listDAta);
                                    listaDatosView.setAdapter(adapterListDatos); //configuramos el view

                                    for (DataSnapshot ds:dataSnapshot.getChildren()){
                                        Pedido pedido = ds.getValue(Pedido.class);
                                        if (pedido.getIdVendedor().equals(finalVendedor.getIdVendedor())){
                                            try {
                                                pedido.setNombreProducto(encriptacionDatos.desencriptar(pedido.getNombreProducto()));
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
                                                pedido.setImagen(encriptacionDatos.desencriptar(pedido.getImagen()));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            if (reportePedidosEliminados.isChecked()
                                                    &&!pedido.getCancelado()
                                                    &&!pedido.getAceptado()
                                                    &&!pedido.getPagado()
                                                    &&pedido.getEliminado()){
                                                listDAta.add(pedido);
                                            }

                                            if (reportePedidosCancelados.isChecked()
                                                    &&pedido.getCancelado()
                                                    &&!pedido.getAceptado()
                                                    &&!pedido.getPagado()
                                                    &&!pedido.getEliminado()){
                                                listDAta.add(pedido);
                                            }

                                            if (reportePedidosFinalizados.isChecked()
                                                    &&!pedido.getCancelado()
                                                    &&!pedido.getAceptado()
                                                    &&pedido.getPagado()
                                                    &&!pedido.getEliminado()){
                                                listDAta.add(pedido);
                                            }
                                        }
                                    }
                                    cargar.dismiss();
                                    adapterListDatos = new AdapterGridReporteVendedor(getContext(),listDAta);
                                    listaDatosView.setAdapter(adapterListDatos); //configuramos el view
                                    listaDatosView.setLayoutManager(new LinearLayoutManager(getContext()));



                                    //ocultamos el boton generar reportes csv
                                    if (listDAta.size() >= 1){
                                        generarReporte.setVisibility(View.VISIBLE);

                                    }else{
                                        generarReporte.setVisibility(View.GONE);
                                        rutaArchivo.setVisibility(View.GONE);
                                        irRutaArchivo.setVisibility(View.GONE);
                                    }
                                }else{
                                    cargar.dismiss();
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
            Toast.makeText(getContext(),"Error al generar archivo",Toast.LENGTH_LONG);
        }

    }
    public Dialog cargar(Context context){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);//el dialogo se presenta sin el titulo
        dialog.setCancelable(false); //impedimos el cancelamiento del dialogo
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.));//le damos un color de fondo transparente
        dialog.setContentView(R.layout.cuadro_cargando); //le asisganos el layout
        return dialog;
    }
}
