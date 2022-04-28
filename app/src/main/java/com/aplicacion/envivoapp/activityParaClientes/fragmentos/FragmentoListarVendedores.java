package com.aplicacion.envivoapp.activityParaClientes.fragmentos;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterGridPedidoCliente;
import com.aplicacion.envivoapp.adaptadores.AdapterListarVendedores;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroListarVendedor;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje_Cliente_Vendedor;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.BuscadorVendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
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

import java.util.ArrayList;
import java.util.List;

public class FragmentoListarVendedores extends Fragment implements CuadroListarVendedor.resultadoDialogo, BuscadorVendedor.resultadoBuscadorVendedor {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private EncriptacionDatos encriptacionDatos= new EncriptacionDatos();

    private List<Vendedor> listVendedor = new ArrayList<>();
    private EditText txtPalabraBuscar;
    private Button btnBuscarVendedor;
    
    private ListAdapter adapterListVendedor;
    private GridView listaVendedorView;
    private Boolean  esMensajeGlobal ;
    private Cliente clienteGlobal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_listar_vendedores, container, false);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        listaVendedorView = root.findViewById(R.id.listVendedores);
        txtPalabraBuscar = root.findViewById(R.id.busquedaVendedor);
        btnBuscarVendedor = root.findViewById(R.id.btnBuscarVendedorListarVendedor);


        esMensajeGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getGlobal();

        clienteGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getCliente();

        if (clienteGlobal!=null) {
            if (esMensajeGlobal) {
                listaVendedorView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ((MyFirebaseApp) getActivity().getApplicationContext()).setVendedor(listVendedor.get(position));
                        Log.d("DATA1",listVendedor.get(position).getIdVendedor());
                        cargarFragment();
                    }
                });
            } else {

                listaVendedorView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ((MyFirebaseApp) getActivity().getApplicationContext()).setVendedor(listVendedor.get(position));
                        Log.d("DATA2", listVendedor.get(position).getIdVendedor());
                        new CuadroListarVendedor(getContext(),
                                listVendedor.get(position),
                                databaseReference,
                                firebaseAuth,
                                FragmentoListarVendedores.this::resultado);
                    }
                });


            }


            btnBuscarVendedor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String palabraBuscar = txtPalabraBuscar.getText().toString();
                    if (palabraBuscar.equals("")) {
                        //Inicialisamos el adaptador
                        adapterListVendedor = new AdapterListarVendedores(getContext(), listVendedor, databaseReference);
                        listaVendedorView.setAdapter(adapterListVendedor); //configuramos el view
                    } else {
                        new BuscadorVendedor(getContext(),
                                databaseReference,
                                palabraBuscar,
                                FragmentoListarVendedores.this::resultadoBuscadorVendedor);
                    }
                }
            });
        }

        //cargamos los datos en el grid
        new BuscadorVendedor(getContext(),
                databaseReference,
                "",
                FragmentoListarVendedores.this::resultadoBuscadorVendedor);

        return root;
    }


    @Override
    public void resultado(Boolean isVerStreamings, Vendedor vendedor) {
        if(isVerStreamings){
            if (clienteGlobal !=null){
                ((MyFirebaseApp) getActivity().getApplicationContext()).setVendedor(vendedor);
                //((MyFirebaseApp) getActivity().getApplicationContext()).setCliente(clienteGlobal);
                cargarFragment();
            }

        }
    }

    @Override
    public void resultadoBuscadorVendedor(List<Vendedor> vendedorList) {
        Log.d("tamanio",vendedorList.size()+"");
        if (vendedorList.size() > 0) {

            listVendedor.clear();

            //Inicialisamos el adaptador con los datos recividos
            adapterListVendedor = new AdapterListarVendedores(getContext(),vendedorList,databaseReference);
            listaVendedorView.setAdapter(adapterListVendedor); //configuramos el view

            listVendedor=vendedorList;
            //Inicialisamos el adaptador con los datos recividos
            adapterListVendedor = new AdapterListarVendedores(getContext(),vendedorList,databaseReference);
            listaVendedorView.setAdapter(adapterListVendedor); //configuramos el view

        }else{
            //cargamos los datos en el adaptador
            Toast.makeText(getContext(),"No se encontraron coincidencias",Toast.LENGTH_LONG).show();
            adapterListVendedor = new AdapterListarVendedores(getContext(),listVendedor,databaseReference);
            listaVendedorView.setAdapter(adapterListVendedor); //configuramos el view
        }
    }


    private void cargarFragment( ) {
        if (esMensajeGlobal){
            Fragment fragment = new FragmentoMensajeriaGlobal();
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                    .replace(R.id.home_content, fragment)
                    .commit();
        }else{
            Fragment fragment = new FragmentoStreamigsVendedor();
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                    .replace(R.id.home_content, fragment)
                    .commit();
        }
    }
}
