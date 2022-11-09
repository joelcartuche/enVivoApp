package com.aplicacion.envivoapp.activityParaClientes.fragmentos.navDataVendedor;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Calificaciones;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class Fragmento_calificaciones_vendedor extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private EncriptacionDatos encriptacionDatos= new EncriptacionDatos();

    private PieChart lineChartCalificacionesVendedor;
    private Vendedor vendedorGlobal;
    private Cliente clienteGlobal;
    private Integer totalCalificaciones=0,calificacionesBuenas =0;
    private Dialog dialogCargando;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragmento_calificaciones_vendedor, container, false);
        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos


        vendedorGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getVendedor();
        clienteGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getCliente();

        lineChartCalificacionesVendedor =  root.findViewById(R.id.lineChartCalificacionesVendedor);


        Utilidades util = new Utilidades();
        dialogCargando = util.dialogCargar(getContext()); //cargamos el cuadro de dialogo
        dialogCargando.show();
        cargarCalificaciones();
        return root;
    }

    private void cargarCalificaciones() {
        Query queryProductos = databaseReference.child("Calificaciones").orderByChild("vendedor/idVendedor").equalTo(vendedorGlobal.getIdVendedor());
        queryProductos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                calificacionesBuenas =0;
                totalCalificaciones =0;
                if (snapshot.exists()){

                    for (DataSnapshot ds:snapshot.getChildren()){
                        Calificaciones calificaciones = ds.getValue(Calificaciones.class);
                        if (calificaciones!=null){
                            if(calificaciones.getEsCalificacionBuena()){
                                calificacionesBuenas++;
                            }

                        }
                        totalCalificaciones++;
                    }

                    cargarGrafico();


                }
                dialogCargando.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),"Usted no tiene comentarios",Toast.LENGTH_LONG).show();
                dialogCargando.dismiss();
            }
        });
    }

    private void cargarGrafico() {

        Integer porcentajeCalificacionesBuenas = (calificacionesBuenas*100)/totalCalificaciones;;
        Integer porcentajeCalificacionesMalas = 100-porcentajeCalificacionesBuenas;



        ArrayList<PieEntry> valoresY = new ArrayList<>();
        ArrayList<Integer> colores = new ArrayList<>();

        lineChartCalificacionesVendedor.setHoleRadius(40f);
        lineChartCalificacionesVendedor.setRotationEnabled(true);
        lineChartCalificacionesVendedor.animateXY(1500,1500);

        valoresY.add(new PieEntry(porcentajeCalificacionesBuenas,"Buenas"));
        valoresY.add(new PieEntry(porcentajeCalificacionesMalas,"Malas"));

        colores.add(getResources().getColor(R.color.verde));
        colores.add(getResources().getColor(R.color.rojo));

        PieDataSet set = new PieDataSet(valoresY,"Calificaciones");

        //seteamos los colores
        set.setSliceSpace(5f);
        set.setColors(colores);


        //seteamos los valores de x
        PieData data = new PieData(set);
        lineChartCalificacionesVendedor.setData(data);
        lineChartCalificacionesVendedor.highlightValue(null);
        lineChartCalificacionesVendedor.invalidate();

        //ocultamos descripcion
        Description description = new Description();
        description.setText("Gráfico de número de clientes");
        lineChartCalificacionesVendedor.setDescription(description);

        //ocultar leyenda
        lineChartCalificacionesVendedor.setDrawEntryLabels(false);

    }
}