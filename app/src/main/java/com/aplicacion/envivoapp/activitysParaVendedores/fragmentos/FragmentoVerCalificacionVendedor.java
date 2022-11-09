package com.aplicacion.envivoapp.activitysParaVendedores.fragmentos;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Calificaciones;
import com.aplicacion.envivoapp.modelos.Vendedor;
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
import java.util.HashMap;
import java.util.Map;


public class FragmentoVerCalificacionVendedor extends Fragment {
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private PieChart lineChartVerCalificacionesVendedor;
    private Vendedor vendedorGlobal;
    private Dialog dialogCargando;
    private  int calificacionesBuenas,totalCalificaciones;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =inflater.inflate(R.layout.fragmento_ver_calificacion_vendedor, container, false);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        vendedorGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getVendedor();
        lineChartVerCalificacionesVendedor =  root.findViewById(R.id.lineChartVerCalificacionesVendedor);



        Utilidades util = new Utilidades();
        dialogCargando = util.dialogCargar(getContext()); //cargamos el cuadro de dialogo
        dialogCargando.show();
        cargarCalificaciones();
        return root;
    }

    private void sumarBuenasCalf() {
        /*
        Map<String,Object> actualizacionCalificacion = new HashMap<>();
        actualizacionCalificacion.put("Vendedor/"
                +vendedorGlobal.getIdVendedor()+"/numCalificacionesBuenas",);


        databaseReference.updateChildren(actualizacionCalificacion);

         */
        vendedorGlobal.setNumCalificacionesBuenas(vendedorGlobal.getNumCalificacionesBuenas()+1);

    }

    private void cargarCalificaciones() {
        Query queryProductos = databaseReference.child("Vendedor").child(vendedorGlobal.getIdVendedor());
        queryProductos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                calificacionesBuenas =0;
                totalCalificaciones =0;
                if (snapshot.exists()){
                    Vendedor vendedor = snapshot.getValue(Vendedor.class);
                    if (vendedor!=null){
                        calificacionesBuenas = vendedor.getNumCalificacionesBuenas();
                        totalCalificaciones = calificacionesBuenas+vendedor.getNumCalificacionesMalas();
                        cargarGrafico();
                    }
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

        if (totalCalificaciones==0){
            totalCalificaciones =1;
        }
        Integer porcentajeCalificacionesBuenas = (calificacionesBuenas*100)/totalCalificaciones;;
        Integer porcentajeCalificacionesMalas = 100-porcentajeCalificacionesBuenas;



        ArrayList<PieEntry> valoresY = new ArrayList<>();
        ArrayList<Integer> colores = new ArrayList<>();

        lineChartVerCalificacionesVendedor.setHoleRadius(40f);
        lineChartVerCalificacionesVendedor.setRotationEnabled(true);
        lineChartVerCalificacionesVendedor.animateXY(1500,1500);

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
        lineChartVerCalificacionesVendedor.setData(data);
        lineChartVerCalificacionesVendedor.highlightValue(null);
        lineChartVerCalificacionesVendedor.invalidate();

        //ocultamos descripcion
        Description description = new Description();
        description.setText("Gráfico de número de clientes");
        lineChartVerCalificacionesVendedor.setDescription(description);

        //ocultar leyenda
        lineChartVerCalificacionesVendedor.setDrawEntryLabels(false);

    }
}