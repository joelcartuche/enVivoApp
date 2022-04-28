package com.aplicacion.envivoapp.activitysParaVendedores.fragmentos;

import android.app.Dialog;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activityParaClientes.fragmentos.FragmentoMensajeriaGlobal;
import com.aplicacion.envivoapp.adaptadores.AdapterListarClientes;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Mensaje_Cliente_Vendedor;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.BuscadorCliente;
import com.aplicacion.envivoapp.utilidades.BuscadorMensajeVendedorACliente;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FragmentoListarClientes extends Fragment implements
        BuscadorCliente.resultadoBusquedaCliente{

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();

    private List<Cliente> listCliente = new ArrayList<>();
    private AdapterListarClientes adapterListCliente;
    private GridView listaClienteView;
    private EditText txtPalabraBuscar;
    private Button btnBuscarCliente;
    private  Boolean esMensajeGlobal;
    private  Vendedor vendedorGlobal;
    private  Boolean esPrimerMensajeCliVen;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_listar_clientes, container, false);


        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        listaClienteView = root.findViewById(R.id.gridClientes);
        txtPalabraBuscar = root.findViewById(R.id.txtBusquedaClienteListarClientes);
        btnBuscarCliente = root.findViewById(R.id.btnBuscarClienteListarClientes);

        esMensajeGlobal = ((MyFirebaseApp) getContext().getApplicationContext()).getGlobal();
        vendedorGlobal = ((MyFirebaseApp) getContext().getApplicationContext()).getVendedor();

        if (vendedorGlobal != null) {
            esMensajeGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getGlobal(); //recogemos los datos del vendedor
            if (esMensajeGlobal) {
                cargarMensajeriaGlobal();
                listaClienteView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ((MyFirebaseApp) getActivity().getApplicationContext()).setCliente(listCliente.get(position));
                        cargarFragmento(listCliente.get(position));
                    }
                });
            }else{
                cargarMensajeriaParaBloqueoDesbloqueo();
            }


            btnBuscarCliente.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (txtPalabraBuscar.getText().toString().equals("")){
                        //Inicialisamos el adaptador
                        adapterListCliente = new AdapterListarClientes(getContext(),
                                listCliente,
                                databaseReference,
                                esMensajeGlobal,vendedorGlobal,getActivity());
                        listaClienteView.setAdapter(adapterListCliente); //configuramos el view
                    }else {
                        new BuscadorCliente(getContext(),
                                databaseReference,
                                txtPalabraBuscar.getText().toString(),
                                vendedorGlobal,
                                esMensajeGlobal,
                                FragmentoListarClientes.this::resultadoBusquedaCliente);
                    }
                }
            });
        }

        return root;
    }

    private void cargarFragmento(Cliente cliente) {
        Dialog dialogCargando = new Utilidades().dialogCargar(getContext());
        dialogCargando.show();
        String idCliIdVen = cliente.getIdCliente()+"_"+vendedorGlobal.getIdVendedor();
        databaseReference.
                child("Mensaje_Cliente_Vendedor").
                child(idCliIdVen).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                ((MyFirebaseApp) getActivity().getApplicationContext()).
                        setEsPrimerMensajeClienteVendedor(false);
                cargarFragmentMensajeriaGlobal();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ((MyFirebaseApp) getActivity().getApplicationContext()).
                        setEsPrimerMensajeClienteVendedor(true);
                cargarFragmentMensajeriaGlobal();
            }
        });

    }

    public void cargarFragmentMensajeriaGlobal(){
        Fragment fragment = new FragmentoMensajeriaGlobal();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                .replace(R.id.home_content_vendedor, fragment)
                .commit();
    }

    public  void borrarGrid(){
        listCliente.clear();
        //Inicialisamos el adaptador
        adapterListCliente = new AdapterListarClientes(getContext(),
                listCliente,
                databaseReference,
                esMensajeGlobal,vendedorGlobal,getActivity());
        listaClienteView.setAdapter(adapterListCliente); //configuramos el view
    }

    public void  cargarMensajeriaParaBloqueoDesbloqueo() {
        Query consultaClientes = databaseReference.
                child("Mensaje_Cliente_Vendedor").
                orderByChild("vendedor/idVendedor").
                equalTo(vendedorGlobal.getIdVendedor());
        consultaClientes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    borrarGrid();
                    for (DataSnapshot ds:snapshot.getChildren()){
                        Mensaje_Cliente_Vendedor mensaje_cliente_vendedor = ds.getValue(Mensaje_Cliente_Vendedor.class);
                        if (mensaje_cliente_vendedor!=null){
                            Cliente cliente = mensaje_cliente_vendedor.getCliente();
                            try {
                                cliente.setNombre(encriptacionDatos.desencriptar(cliente.getNombre()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                cliente.setTelefono(encriptacionDatos.desencriptar(cliente.getTelefono()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                cliente.setCelular(encriptacionDatos.desencriptar(cliente.getCelular()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            listCliente.add(cliente);
                        }
                    }

                    //Inicialisamos el adaptador
                    adapterListCliente = new AdapterListarClientes(getContext(),
                            listCliente,
                            databaseReference,
                            esMensajeGlobal,vendedorGlobal,getActivity());
                    listaClienteView.setAdapter(adapterListCliente); //configuramos el view
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                borrarGrid();
            }
        });
    }
    public void  cargarMensajeriaGlobal(){
        Query consultaClientes = databaseReference.
                child("Mensaje_Cliente_Vendedor").
                orderByChild("vendedor/idVendedor").
                equalTo(vendedorGlobal.getIdVendedor());
        consultaClientes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    borrarGrid();
                    for (DataSnapshot ds:snapshot.getChildren()){
                        Mensaje_Cliente_Vendedor mensaje_cliente_vendedor = ds.getValue(Mensaje_Cliente_Vendedor.class);
                        if (mensaje_cliente_vendedor!=null){
                            if (!mensaje_cliente_vendedor.getCliente().getBloqueado()) {
                                Cliente cliente = mensaje_cliente_vendedor.getCliente();
                                try {
                                    cliente.setNombre(encriptacionDatos.desencriptar(cliente.getNombre()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    cliente.setTelefono(encriptacionDatos.desencriptar(cliente.getTelefono()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    cliente.setCelular(encriptacionDatos.desencriptar(cliente.getCelular()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                listCliente.add(cliente);
                            }
                        }
                    }
                    //Inicialisamos el adaptador
                    adapterListCliente = new AdapterListarClientes(getContext(),
                            listCliente,
                            databaseReference,
                            esMensajeGlobal,vendedorGlobal,getActivity());
                    listaClienteView.setAdapter(adapterListCliente); //configuramos el view
                }
                else{
                    borrarGrid();
                    Toast.makeText(getContext(),"Usted no tiene mensajes nuevos",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                borrarGrid();
            }
        });


    }


    @Override
    public void resultadoBusquedaCliente(List<Cliente> clienteList) {
        if (clienteList.size()>0){
            List<Cliente> auxClienteList = new ArrayList<>();
            //Borramos los datos de la lista
            adapterListCliente = new AdapterListarClientes(getContext(),
                    auxClienteList,
                    databaseReference,
                    esMensajeGlobal,
                    vendedorGlobal,
                    getActivity());
            listaClienteView.setAdapter(adapterListCliente); //configuramos el view

            //ingresamos los nuevos datos
            //Inicialisamos el adaptador
            adapterListCliente = new AdapterListarClientes(getContext(),
                    clienteList,
                    databaseReference,
                    esMensajeGlobal,
                    vendedorGlobal,
                    getActivity());
            listaClienteView.setAdapter(adapterListCliente); //configuramos el view
        }else{
            Toast.makeText(getContext(),"No se encontraron coincidencias",Toast.LENGTH_LONG).show();
            //Cargamos los datos en caso de no existir coicidencias
            adapterListCliente = new AdapterListarClientes(getContext(),
                    listCliente,
                    databaseReference,
                    esMensajeGlobal,vendedorGlobal,getActivity());
            listaClienteView.setAdapter(adapterListCliente); //configuramos el view
        }
    }


}
