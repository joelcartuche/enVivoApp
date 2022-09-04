package com.aplicacion.envivoapp.activityParaClientes.fragmentos.navDataVendedor;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterGridProductosVendedor;
import com.aplicacion.envivoapp.adaptadores.AdapterGridProductosVendedorCliente;
import com.aplicacion.envivoapp.adaptadores.AdapterListarLocalClientes;
import com.aplicacion.envivoapp.modelos.Local;
import com.aplicacion.envivoapp.modelos.Producto;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;


public class Fragmento_listar_productos_vendedor extends Fragment {


    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage ; //para la insersion de archivos
    private EncriptacionDatos encrypt = new EncriptacionDatos();


    private List<Producto> listProduct = new ArrayList<>(); //lista que contendra los locales del vendedor
    private AdapterGridProductosVendedorCliente adapterGridProductosVendedor; //iniciamos el adaptador del gridView

    private TextView tvNoHayDatosFragmentoListarProductosVendedor;
    private RecyclerView gridProductosVendedor;
    private Vendedor vendedorGlobal;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root= inflater.inflate(R.layout.fragment_fragmento_listar_productos_vendedor, container, false);
        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        storage = FirebaseStorage.getInstance();//inicializamos la variable storage

        tvNoHayDatosFragmentoListarProductosVendedor = root.findViewById(R.id.tvNoHayDatosFragmentoListarProductosVendedor);
        gridProductosVendedor = root.findViewById(R.id.gridFragmentoListarProductosVendedor);
        vendedorGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getVendedor();

        tvNoHayDatosFragmentoListarProductosVendedor.setVisibility(View.GONE);
        cargarProductos();

        return root;
    }

    public  void borrarGrid(){
        listProduct.clear();//borramos en caso de quedar algo en la cache
        //Inicialisamos el adaptador
        adapterGridProductosVendedor = new AdapterGridProductosVendedorCliente(getContext(),
                listProduct,
                storage);
        gridProductosVendedor.setAdapter(adapterGridProductosVendedor); //configuramos el view
    }

    public void  cargarProductos(){
        borrarGrid();
        Query queryProductos = databaseReference.child("Producto").orderByChild("idVendedor").equalTo(vendedorGlobal.getIdVendedor());

        queryProductos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    listProduct.clear();

                    for (DataSnapshot ds:snapshot.getChildren()){
                        Log.d("Datos",ds.toString());
                        Producto producto = ds.getValue(Producto.class);

                        if (producto!=null){
                            try {
                                producto.setNombreProducto(encrypt.desencriptar(producto.getNombreProducto()));
                                producto.setCodigoProducto(encrypt.desencriptar(producto.getCodigoProducto()));
                                producto.setDescripcionProducto(encrypt.desencriptar(producto.getDescripcionProducto()));

                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(),"Error al cargar el producto",Toast.LENGTH_LONG).show();
                            }

                            listProduct.add(producto);
                        }
                    }

                    adapterGridProductosVendedor = new AdapterGridProductosVendedorCliente(getContext(),
                            listProduct,
                            storage);
                    gridProductosVendedor.setAdapter(adapterGridProductosVendedor); //configuramos el view
                    gridProductosVendedor.setLayoutManager(new LinearLayoutManager(getContext()));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),"Usted no tiene productos",Toast.LENGTH_LONG).show();
            }
        });


    }

}