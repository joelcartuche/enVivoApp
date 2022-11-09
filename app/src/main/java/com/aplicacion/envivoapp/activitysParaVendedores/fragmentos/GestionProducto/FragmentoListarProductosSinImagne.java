package com.aplicacion.envivoapp.activitysParaVendedores.fragmentos.GestionProducto;

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
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterGridProductosVendedor;
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


public class FragmentoListarProductosSinImagne extends Fragment {
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage ; //para la insersion de archivos
    private EncriptacionDatos encrypt = new EncriptacionDatos();

    private List<Producto> listProduct = new ArrayList<>();

    RecyclerView gridProductosSinImagen;
    private AdapterGridProductosVendedor adapterGridProductosVendedor;
    private Vendedor vendedorGlobal;

    private Dialog dialogCargando;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =inflater.inflate(R.layout.fragmento_listar_productos_sin_imagne, container, false);
        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        storage = FirebaseStorage.getInstance();//inicializamos la variable storage
        vendedorGlobal =  ((MyFirebaseApp) getActivity().getApplicationContext()).getVendedor();

        gridProductosSinImagen = root.findViewById(R.id.gridProductosSinImagen);

        cargarProductos();

        return root;
    }

    public  void borrarGrid(){
        listProduct.clear();//borramos en caso de quedar algo en la cache
        //Inicialisamos el adaptador
        adapterGridProductosVendedor = new AdapterGridProductosVendedor(getContext(),
                listProduct,
                storage,getActivity(),
                "listaProductosImagen",databaseReference);
        gridProductosSinImagen.setAdapter(adapterGridProductosVendedor); //configuramos el view
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
                        Producto producto = ds.getValue(Producto.class);

                        if (producto!=null){
                            if (!producto.getEsEliminado()) {
                                if (producto.getImagen() == null) {//filtramos los productos que no tienen iimagne
                                    try {
                                        producto.setNombreProducto(encrypt.desencriptar(producto.getNombreProducto()));
                                        producto.setCodigoProducto(encrypt.desencriptar(producto.getCodigoProducto()));
                                        producto.setDescripcionProducto(encrypt.desencriptar(producto.getDescripcionProducto()));

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(getContext(), "Error al cargar el producto", Toast.LENGTH_LONG).show();
                                    }

                                    listProduct.add(producto);

                                }
                            }
                        }
                    }

                    adapterGridProductosVendedor = new AdapterGridProductosVendedor(getContext(),
                            listProduct,
                            storage,getActivity(),
                            "listaProductosImagen",databaseReference);
                    gridProductosSinImagen.setAdapter(adapterGridProductosVendedor); //configuramos el view
                    gridProductosSinImagen.setLayoutManager(new LinearLayoutManager(getContext()));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),"Usted no tiene productos",Toast.LENGTH_LONG).show();
            }
        });


    }
}