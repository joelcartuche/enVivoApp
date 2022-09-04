package com.aplicacion.envivoapp.activityParaClientes.fragmentos.navDataVendedor;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapteGridComentariosVendedor;
import com.aplicacion.envivoapp.adaptadores.AdapterGridProductosVendedorCliente;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroRealizarComentarioAlVendedor;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroRealizarDenunciaAlVendedor;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Comentario;
import com.aplicacion.envivoapp.modelos.Producto;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
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


public class Fragmento_comentarios_vendedor extends Fragment
        implements CuadroRealizarComentarioAlVendedor.resultadoCuadroRealizarComentarioAlVendedor,
        CuadroRealizarDenunciaAlVendedor.resultadoCuadroRealizarDenuncialVendedor {
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private EncriptacionDatos encriptacionDatos= new EncriptacionDatos();
    private Button btnRealizarComentarioFragmentoComentarioVendedor,
            btnRealizarDenunciaFragmentoComentarioVendedor;

    private List<Comentario> listComentario = new ArrayList<>(); //lista que contendra los locales del vendedor
    private AdapteGridComentariosVendedor adapteGridComentariosVendedor;
    private RecyclerView gridComentariosVendedorFragmentoComentarioVendedor;


    private PieChart lineChartComentariosVendedor;
    private Cliente clienteGlobal;
    private Vendedor vendedorGlobal;
    private RadioButton radioComentariosComentariosVendedor;
    private RadioButton radioDenunciasComentarioVendedor;
    private Dialog dialogCargando;
    private Integer numeroComentarios=0,totalComentarios =0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragmento_comentarios_vendedor, container, false);
        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        btnRealizarComentarioFragmentoComentarioVendedor = root.findViewById(R.id.btnRealizarComentarioFragmentoComentarioVendedor);
        btnRealizarDenunciaFragmentoComentarioVendedor = root.findViewById(R.id.btnRealizarDenunciaFragmentoComentarioVendedor);
        gridComentariosVendedorFragmentoComentarioVendedor = root.findViewById(R.id.gridComentariosVendedorFragmentoComentarioVendedor);
        lineChartComentariosVendedor = root.findViewById(R.id.lineChartComentariosVendedor);
        radioComentariosComentariosVendedor = root.findViewById(R.id.radioComentariosComentariosVendedor);
        radioDenunciasComentarioVendedor = root.findViewById(R.id.radioDenunciasComentarioVendedor);

        radioComentariosComentariosVendedor.setChecked(true);

        vendedorGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getVendedor();
        clienteGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getCliente();


        Utilidades util = new Utilidades();
        dialogCargando = util.dialogCargar(getContext()); //cargamos el cuadro de dialogo
        dialogCargando.show();

        btnRealizarComentarioFragmentoComentarioVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CuadroRealizarComentarioAlVendedor(getContext(),
                        databaseReference,
                        clienteGlobal,
                        vendedorGlobal,
                        Fragmento_comentarios_vendedor.this::resultadoCuadroRealizarComentarioAlVendedor);
            }
        });
        btnRealizarDenunciaFragmentoComentarioVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CuadroRealizarDenunciaAlVendedor(getContext(),
                        databaseReference,
                        clienteGlobal,
                        vendedorGlobal,
                        Fragmento_comentarios_vendedor.this::resultadoCuadroRealizarDenuncialVendedor);
            }
        });

        cargarComentarios();
        radioComentariosComentariosVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargarComentarios();
            }
        });
        radioDenunciasComentarioVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargarComentarios();
            }
        });

        return root;
    }

    private void cargarGrafico() {

        Integer porcentajeDenuncias = 0;
        Integer porcentajeComentarios = 0;

        //calculamos los porcentajos
        if (radioDenunciasComentarioVendedor.isChecked()){
            porcentajeDenuncias = (listComentario.size()*100)/totalComentarios;
            porcentajeComentarios = 100-porcentajeDenuncias;
        }else if(radioComentariosComentariosVendedor.isChecked()) {
            porcentajeComentarios = (listComentario.size()*100)/totalComentarios;
            porcentajeDenuncias = 100-porcentajeComentarios;
        }

        ArrayList<PieEntry> valoresY = new ArrayList<>();
        ArrayList<Integer> colores = new ArrayList<>();

        lineChartComentariosVendedor.setHoleRadius(40f);
        lineChartComentariosVendedor.setRotationEnabled(true);
        lineChartComentariosVendedor.animateXY(1500,1500);

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
        lineChartComentariosVendedor.setData(data);
        lineChartComentariosVendedor.highlightValue(null);
        lineChartComentariosVendedor.invalidate();

        //ocultamos descripcion
        Description description = new Description();
        description.setText("Gráfico de número de clientes");
        lineChartComentariosVendedor.setDescription(description);

        //ocultar leyenda
        lineChartComentariosVendedor.setDrawEntryLabels(false);

    }

    public  void borrarGrid(){
        totalComentarios=0;
        listComentario.clear();//borramos en caso de quedar algo en la cache
        //Inicialisamos el adaptador
        adapteGridComentariosVendedor = new AdapteGridComentariosVendedor(getContext(),listComentario);
        gridComentariosVendedorFragmentoComentarioVendedor.
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
                            if (radioComentariosComentariosVendedor.isChecked()){
                                if (!comentario.getEsDenuncia()){
                                    listComentario.add(comentario);
                                }
                            }else{
                                if (comentario.getEsDenuncia()){
                                    listComentario.add(comentario);
                                }
                            }
                            totalComentarios++;
                        }
                    }

                    cargarGrafico();

                    adapteGridComentariosVendedor = new AdapteGridComentariosVendedor(getContext(),
                            listComentario);
                    gridComentariosVendedorFragmentoComentarioVendedor.setAdapter(adapteGridComentariosVendedor); //configuramos el view
                    gridComentariosVendedorFragmentoComentarioVendedor.setLayoutManager(new LinearLayoutManager(getContext()));

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

    @Override
    public void resultadoCuadroRealizarComentarioAlVendedor(Boolean isAcepatado, Boolean isCancelado, int position) {

    }

    @Override
    public void resultadoCuadroRealizarDenuncialVendedor(String resultadoDenuncia) {

    }
}