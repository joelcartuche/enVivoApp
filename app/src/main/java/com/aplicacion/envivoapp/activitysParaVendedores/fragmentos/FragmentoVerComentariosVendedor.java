package com.aplicacion.envivoapp.activitysParaVendedores.fragmentos;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapteGridComentariosVendedor;
import com.aplicacion.envivoapp.modelos.Comentario;
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
import java.util.List;


public class FragmentoVerComentariosVendedor extends Fragment {
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private Vendedor vendedorGlobal;
    private Dialog dialogCargando;
    private List<Comentario> listComentario = new ArrayList<>(); //lista que contendra los locales del vendedor

    private RecyclerView gridComentariosVerComentariosVendedor;
    private PieChart lineChartVerComentariosVendedor;
    private AdapteGridComentariosVendedor adapteGridComentariosVendedor;
    private RadioButton rbComentariosVerComentariosVendedor,rbDenunciasVerComentariosVendedor,rbGraficoVerComentariosVendedor;
    private Integer numeroComentarios=0,totalComentarios =0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_fragmento_ver_comentarios_vendedor, container, false);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        gridComentariosVerComentariosVendedor = root.findViewById(R.id.gridComentariosVerComentariosVendedor);
        lineChartVerComentariosVendedor = root.findViewById(R.id.lineChartVerComentariosVendedor);
        rbComentariosVerComentariosVendedor = root.findViewById(R.id.rbComentariosVerComentariosVendedor);
        rbDenunciasVerComentariosVendedor = root.findViewById(R.id.rbDenunciasVerComentariosVendedor);
        rbGraficoVerComentariosVendedor = root.findViewById(R.id.rbGraficoVerComentariosVendedor);

        lineChartVerComentariosVendedor.setVisibility(View.GONE);//ocultamos el grafico estadistico

        rbComentariosVerComentariosVendedor.setChecked(true); //mostramos por defecto solo comentarios

        vendedorGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getVendedor();

        Utilidades util = new Utilidades();
        dialogCargando = util.dialogCargar(getContext()); //cargamos el cuadro de dialogo
        dialogCargando.show();

        cargarComentarios();

        rbComentariosVerComentariosVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargarComentarios();
            }
        });
        rbDenunciasVerComentariosVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargarComentarios();
            }
        });
        rbGraficoVerComentariosVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargarComentarios();
            }
        });

        return root;
    }
    public  void borrarGrid(){
        totalComentarios=0;
        listComentario.clear();//borramos en caso de quedar algo en la cache
        //Inicialisamos el adaptador
        adapteGridComentariosVendedor = new AdapteGridComentariosVendedor(getContext(),listComentario);
        gridComentariosVerComentariosVendedor.
                setAdapter(adapteGridComentariosVendedor); //configuramos el view
    }

    private void cargarComentarios() {
        borrarGrid();
        Query queryProductos = databaseReference.child("Comentario").orderByChild("vendedor/idVendedor").equalTo(vendedorGlobal.getIdVendedor());
        queryProductos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    listComentario.clear();
                    for (DataSnapshot ds:snapshot.getChildren()){
                        Comentario comentario = ds.getValue(Comentario.class);
                        if (comentario!=null){
                            if (rbComentariosVerComentariosVendedor.isChecked()){
                                if (!comentario.getEsDenuncia()){
                                    listComentario.add(comentario);
                                    gridComentariosVerComentariosVendedor.setVisibility(View.VISIBLE);
                                    lineChartVerComentariosVendedor.setVisibility(View.GONE);
                                }
                            } else if(rbDenunciasVerComentariosVendedor.isChecked()){
                                if (comentario.getEsDenuncia()){
                                    listComentario.add(comentario);
                                    gridComentariosVerComentariosVendedor.setVisibility(View.VISIBLE);
                                    lineChartVerComentariosVendedor.setVisibility(View.GONE);
                                }
                            }else if(rbGraficoVerComentariosVendedor.isChecked()){//contamos el numero de comentarios para el grafico
                                if (!comentario.getEsDenuncia()){
                                    listComentario.add(comentario);
                                    gridComentariosVerComentariosVendedor.setVisibility(View.GONE);
                                    lineChartVerComentariosVendedor.setVisibility(View.VISIBLE);
                                }
                            }
                            totalComentarios++;
                        }
                    }

                    if (rbGraficoVerComentariosVendedor.isChecked()){
                        cargarGrafico();
                    }

                    adapteGridComentariosVendedor = new AdapteGridComentariosVendedor(getContext(),
                            listComentario);
                    gridComentariosVerComentariosVendedor.setAdapter(adapteGridComentariosVendedor); //configuramos el view
                    gridComentariosVerComentariosVendedor.setLayoutManager(new LinearLayoutManager(getContext()));

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
        int porcentajeComentarios = 0;
        int porcentajeDenuncias =0;

        //calculamos los porcentajos

        porcentajeDenuncias = (listComentario.size()*100)/totalComentarios;
        porcentajeComentarios = 100-porcentajeDenuncias;


        ArrayList<PieEntry> valoresY = new ArrayList<>();
        ArrayList<Integer> colores = new ArrayList<>();

        lineChartVerComentariosVendedor.setHoleRadius(40f);
        lineChartVerComentariosVendedor.setRotationEnabled(true);
        lineChartVerComentariosVendedor.animateXY(1500,1500);

        valoresY.add(new PieEntry(porcentajeComentarios,"Comentarios"));
        valoresY.add(new PieEntry(porcentajeDenuncias,"Denuncias"));

        colores.add(getResources().getColor(R.color.verde));
        colores.add(getResources().getColor(R.color.rojo));

        PieDataSet set = new PieDataSet(valoresY,"");

        //seteamos los colores
        set.setSliceSpace(5f);
        set.setColors(colores);


        //seteamos los valores de x
        PieData data = new PieData(set);
        lineChartVerComentariosVendedor.setData(data);
        lineChartVerComentariosVendedor.highlightValue(null);
        lineChartVerComentariosVendedor.invalidate();

        //ocultamos descripcion
        Description description = new Description();
        description.setText("Gráfico estadistico");
        lineChartVerComentariosVendedor.setDescription(description);

        //ocultar leyenda
        lineChartVerComentariosVendedor.setDrawEntryLabels(false);

    }


}