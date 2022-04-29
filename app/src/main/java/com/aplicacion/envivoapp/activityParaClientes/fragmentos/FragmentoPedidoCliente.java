package com.aplicacion.envivoapp.activityParaClientes.fragmentos;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterGridPedidoCliente;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroCancelarPedidoCliente;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.aplicacion.envivoapp.utilidades.BuscadorPedidoCliente;
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

public class FragmentoPedidoCliente extends Fragment implements
        CuadroCancelarPedidoCliente.resultadoDialogo, BuscadorPedidoCliente.resultadoBusquedaVendedorPedido{

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage ; //para la insersion de archivos
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();

    private List<Pedido> listPedido = new ArrayList<>();
    private GridView gridViewPedido;
    private AdapterGridPedidoCliente gridAdapterPedido;
    private EditText buscarVendedor;
    private Button btnBuscar;
    private  int codigo  ;
    private  String idPedido;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_pedido_cliente, container, false);
        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        storage = FirebaseStorage.getInstance();//inicializamos la variable storage}


        gridViewPedido = root.findViewById(R.id.gridProductoCliente);
        buscarVendedor = root.findViewById(R.id.buscarVendedorPedidoCliente);
        btnBuscar = root.findViewById(R.id.btnBuscarPedidoCliente);

        codigo =((MyFirebaseApp) getActivity().getApplicationContext()).getCodigo();
        idPedido = ((MyFirebaseApp) getActivity().getApplicationContext()).getIdPedido()==null?"":((MyFirebaseApp) getActivity().getApplicationContext()).getIdPedido();

        listarPedido();

        //le damos funcionalidad al buscador
        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String palabraBuscar= buscarVendedor.getText().toString();
                if (palabraBuscar.equals("")) {
                    //Inicialisamos el adaptador
                    gridAdapterPedido = new AdapterGridPedidoCliente(getContext(),
                            listPedido,
                            databaseReference,
                            storage,
                            getActivity(),
                            codigo,
                            idPedido);
                    gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view
                }else {
                    new BuscadorPedidoCliente(getContext(),
                            listPedido,
                            databaseReference,
                            buscarVendedor.getText().toString(),
                            FragmentoPedidoCliente.this::resultadoBusquedaVendedorPedido);
                }
            }
        });
        return root;
    }

    private void borrarGrid(){
        listPedido.clear();//borramos en caso de quedar algo en la cache
        //Inicialisamos el adaptador
        gridAdapterPedido = new AdapterGridPedidoCliente(getContext(),
                listPedido,
                databaseReference,
                storage,
                getActivity(),
                0,
                "");
        gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view

    }

    public void listarPedido(){
        Query query = databaseReference.child("Cliente").orderByChild("uidUsuario").equalTo(firebaseAuth.getCurrentUser().getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Dialog dialogBuscando = new Utilidades().dialogCargar(getContext());
                    dialogBuscando.show();

                    Cliente cliente =null;
                    for (final DataSnapshot ds : snapshot.getChildren()) {
                        cliente= ds.getValue(Cliente.class);
                    }

                    if (cliente!= null){
                        Query queryPedido = databaseReference.child("Pedido").orderByChild("idCliente").equalTo(cliente.getIdCliente());
                        queryPedido.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){

                                    listPedido.clear();//borramos en caso de quedar algo en la cache
                                    for (final DataSnapshot ds : snapshot.getChildren()) {
                                        Pedido pedido = ds.getValue(Pedido.class);//obtenemos
                                        if(!pedido.getCancelado() && !pedido.getEliminado()){
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
                                            try {
                                                pedido.setNombreProducto(encriptacionDatos.desencriptar(pedido.getNombreProducto()));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            //en caso de que el producto sufrio un cambio
                                            if (listPedido.size()>0) {
                                                if (codigo != 0) {
                                                    if (pedido.getIdPedido().equals(idPedido)) {
                                                        Pedido aux = listPedido.get(0);
                                                        listPedido.set(0, pedido);
                                                        pedido = aux;

                                                    }
                                                }
                                            }
                                            listPedido.add(pedido);
                                        }

                                    }
                                    //Inicialisamos el adaptador
                                    gridAdapterPedido = new AdapterGridPedidoCliente(getContext(),
                                            listPedido,
                                            databaseReference,
                                            storage,
                                            getActivity(),
                                            codigo,
                                            idPedido);


                                    gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view

                                    dialogBuscando.dismiss();

                                }else{
                                    if (listPedido.size()==0){
                                        Toast.makeText(getContext(),"Usted no tiene pedidos",Toast.LENGTH_LONG).show();
                                    }

                                    borrarGrid();
                                    dialogBuscando.dismiss();

                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                borrarGrid();
                                dialogBuscando.dismiss();
                            }
                        });
                    }else{
                        borrarGrid();
                        dialogBuscando.dismiss();
                    }
                }else{
                    borrarGrid();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void resultado(Boolean isAcepatado, Boolean isCancelado) {

    }


    @Override
    public void resultadoBusquedaVendedorPedido(List<Pedido> pedidoList) {
        Log.d("tamanio",pedidoList.size()+"");
        if (pedidoList.size() > 0) {
            Log.d("datalista", "dato "+ pedidoList.get(0).getNombreProducto());
            List<Pedido> pedidoListaAuxiliar = new ArrayList<>();

            //Inicialisamos el adaptador
            gridAdapterPedido = new AdapterGridPedidoCliente(getContext(),
                    pedidoListaAuxiliar,
                    databaseReference,
                    storage,
                    getActivity(),
                    codigo,
                    idPedido);
            gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view

            //Inicialisamos el adaptador
            gridAdapterPedido = new AdapterGridPedidoCliente(getContext(),
                    pedidoList,
                    databaseReference,
                    storage,
                    getActivity(),
                    codigo,
                    idPedido);
            gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view

        }else{
            //cargamos los datos en el adaptador
            Toast.makeText(getContext(),"No se encontraron coincidencias",Toast.LENGTH_LONG).show();
            gridAdapterPedido = new AdapterGridPedidoCliente(getContext(),
                    listPedido,
                    databaseReference,
                    storage,
                    getActivity(),
                    codigo,
                    idPedido);
            gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view
        }
    }
}
