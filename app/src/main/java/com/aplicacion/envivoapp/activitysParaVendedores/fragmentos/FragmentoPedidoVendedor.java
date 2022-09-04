package com.aplicacion.envivoapp.activitysParaVendedores.fragmentos;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activitysParaVendedores.HomeVendedorMain;
import com.aplicacion.envivoapp.adaptadores.AdapterGridPedidoVendedor;
import com.aplicacion.envivoapp.modelos.Pedido;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FragmentoPedidoVendedor extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage ; //para la insersion de archivos
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();

    private List<Pedido> listPedido = new ArrayList<>();
    private GridView gridViewPedido;
    private AdapterGridPedidoVendedor gridAdapterPedido;
    private  Vendedor vendedorGlobal;

    private RadioButton filtrarAceptados, filtrarEliminados, filtrarTodos,filtrarPagados,filtrarFechaPasado;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_pedido_vendedor, container, false);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        storage = FirebaseStorage.getInstance();//inicializamos la variable storage

        gridViewPedido = root.findViewById(R.id.gridPedidoVendedor);


        filtrarAceptados = root.findViewById(R.id.radioAceptadoPedidoVendedor);
        filtrarEliminados = root.findViewById(R.id.radioEliminadosPedidoVendedor);
        filtrarTodos = root.findViewById(R.id.radioTodosPedidoVendedor);
        filtrarPagados = root.findViewById(R.id.radioPagadoPedidoVendedor);
        filtrarFechaPasado = root.findViewById(R.id.radioPasadosFecha);
        vendedorGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getVendedor();
        if (vendedorGlobal!=null) {
            if (filtrarTodos.isChecked()) {
                filtrarTodos.setChecked(true);
                filtrarAceptados.setChecked(false);
                filtrarEliminados.setChecked(false);
                filtrarPagados.setChecked(false);
                filtrarFechaPasado.setChecked(false);
                listarPedidos();
            }

            filtrarAceptados.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filtrarAceptados.setChecked(true);
                    filtrarEliminados.setChecked(false);
                    filtrarTodos.setChecked(false);
                    filtrarPagados.setChecked(false);
                    filtrarFechaPasado.setChecked(false);
                    listarPedidos();
                }
            });
            filtrarEliminados.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filtrarEliminados.setChecked(true);
                    filtrarAceptados.setChecked(false);
                    filtrarTodos.setChecked(false);
                    filtrarPagados.setChecked(false);
                    filtrarFechaPasado.setChecked(false);
                    listarPedidos();
                }
            });
            filtrarTodos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filtrarTodos.setChecked(true);
                    filtrarAceptados.setChecked(false);
                    filtrarEliminados.setChecked(false);
                    filtrarPagados.setChecked(false);
                    filtrarFechaPasado.setChecked(false);
                    listarPedidos();
                }
            });

            filtrarPagados.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filtrarTodos.setChecked(false);
                    filtrarAceptados.setChecked(false);
                    filtrarEliminados.setChecked(false);
                    filtrarPagados.setChecked(true);
                    filtrarFechaPasado.setChecked(false);
                    listarPedidos();
                }
            });

            filtrarFechaPasado.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filtrarTodos.setChecked(false);
                    filtrarAceptados.setChecked(false);
                    filtrarEliminados.setChecked(false);
                    filtrarPagados.setChecked(false);
                    filtrarFechaPasado.setChecked(true);
                    listarPedidos();
                }
            });

        }
        return root;
    }

    private void borrarGrid(){
        listPedido.clear();//borramos en caso de quedar algo en la cache
        //Inicialisamos el adaptador
        gridAdapterPedido = new AdapterGridPedidoVendedor(getContext(),
                listPedido,
                databaseReference,
                storage,
                getActivity(),vendedorGlobal,null,null);
        gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view
    }
    private void listarPedidos() {
        borrarGrid();//borramos los datos del grid

        Query queryVendedor = databaseReference.child("Vendedor").orderByChild("uidUsuario").equalTo(firebaseAuth.getCurrentUser().getUid());


        queryVendedor.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Vendedor vendedor = null;
                    for (DataSnapshot ds:snapshot.getChildren()){
                        vendedor = ds.getValue(Vendedor.class);
                    }
                    if (vendedor!= null){
                        Vendedor finalVendedor = vendedor;
                        Query queryPedido = databaseReference.child("Pedido").orderByChild("idVendedor").equalTo(vendedor.getIdVendedor());
                        queryPedido.addValueEventListener(new ValueEventListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    List<String> clientesMasDeUnPedido = new ArrayList<>();
                                    List<Integer> contadorClientesMasDeUnPedido = new ArrayList<>();
                                    listPedido.clear();
                                    for (DataSnapshot ds:snapshot.getChildren()){
                                        Pedido pedido = ds.getValue(Pedido.class);

                                        //transformamos los datos encriptados
                                        try {
                                            pedido.setImagen(encriptacionDatos.desencriptar(pedido.getImagen()));
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
                                            pedido.setNombreProducto(encriptacionDatos.desencriptar(pedido.getNombreProducto()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }


                                        //filtramos acorde al radio seleccionado
                                        if (!pedido.getEliminado()
                                                && filtrarTodos.isChecked()){
                                            listPedido.add(pedido);
                                            //Toast.makeText(getActivity().getBaseContext(),"todos",Toast.LENGTH_LONG).show();
                                        }

                                        //fultro para pedidos eliminados
                                        if (filtrarEliminados.isChecked()
                                                && !pedido.getAceptado()
                                                && pedido.getCancelado()
                                                && !pedido.getEliminado()){
                                            listPedido.add(pedido);
                                            //Toast.makeText(getActivity().getBaseContext(),"cancelados",Toast.LENGTH_LONG).show();
                                        }

                                        //filtro para pedidos aceptados
                                        if (filtrarAceptados.isChecked()
                                                &&  pedido.getAceptado()
                                                && !pedido.getCancelado()
                                                && !pedido.getEliminado()){
                                            listPedido.add(pedido);
                                            if (clientesMasDeUnPedido!=null){
                                                if (!clientesMasDeUnPedido.contains(pedido.getIdCliente())) {
                                                    clientesMasDeUnPedido.add(pedido.getIdCliente());
                                                    contadorClientesMasDeUnPedido.add(1);
                                                }else{
                                                    int indiceCliente = clientesMasDeUnPedido.indexOf(pedido.getIdCliente());
                                                    contadorClientesMasDeUnPedido.set(indiceCliente,
                                                            contadorClientesMasDeUnPedido.get(indiceCliente)+1);
                                                }
                                            }
                                            //Toast.makeText(getActivity().getBaseContext(),"aceptados",Toast.LENGTH_LONG).show();
                                        }
                                        if (filtrarPagados.isChecked()
                                                && pedido.getPagado()
                                                &&!pedido.getAceptado()
                                                && !pedido.getCancelado()
                                                &&!pedido.getEliminado()){
                                            listPedido.add(pedido);
                                            //Toast.makeText(getActivity().getBaseContext(),"pagados",Toast.LENGTH_LONG).show();
                                        }
                                        if (pedido.getAceptado()
                                                && !pedido.getCancelado()
                                                && filtrarFechaPasado.isChecked()
                                                && !pedido.getEliminado()
                                                && !pedido.getPagado()){//filtramos por pedido que es aceptado y pasado de fecha

                                            //creamos el date en base la hora actual
                                            LocalDateTime tiempoActual = LocalDateTime.now();//obtenemos la fecha actual
                                            Date fecha = new Date();
                                            fecha.setDate(tiempoActual.getDayOfMonth());

                                            fecha.setMonth(tiempoActual.getMonthValue()-1);
                                            fecha.setYear(tiempoActual.getYear());
                                            fecha.setHours(tiempoActual.getHour());
                                            fecha.setMinutes(tiempoActual.getMinute());
                                            fecha.setSeconds(tiempoActual.getSecond());

                                            Log.w("id","id"+pedido.getIdPedido());
                                            Log.w("Fechas","Actual "+fecha.toString()+" Final "+pedido.getFechaFinalPedido().toString());
                                            Log.w("FechasAños","Actual "+fecha.getYear()+" Final "+pedido.getFechaFinalPedido().getYear());
                                            if (fecha.before(pedido.getFechaFinalPedido())){//comparamos las fechas
                                                listPedido.add(pedido);
                                                //Toast.makeText(,"pasados de fecha",Toast.LENGTH_LONG).show();
                                            }


                                        }

                                    }

                                    //fin de los filtros
                                    //Inicialisamos el adaptador
                                    gridAdapterPedido = new AdapterGridPedidoVendedor(getContext(),
                                            listPedido,
                                            databaseReference,
                                            storage,
                                            getActivity(),
                                            vendedorGlobal,clientesMasDeUnPedido,
                                            contadorClientesMasDeUnPedido
                                            );
                                    gridViewPedido.setAdapter(gridAdapterPedido); //configuramos el view


                                }else{
                                    borrarGrid();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                borrarGrid();
                            }
                        });
                    }else{
                        borrarGrid();
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

}
