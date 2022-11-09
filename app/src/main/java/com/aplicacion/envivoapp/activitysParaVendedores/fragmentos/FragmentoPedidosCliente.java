package com.aplicacion.envivoapp.activitysParaVendedores.fragmentos;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterGridPedidoVendedor;
import com.aplicacion.envivoapp.adaptadores.AdapterGridPedidosCliente;
import com.aplicacion.envivoapp.modelos.Calificaciones;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FragmentoPedidosCliente extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage ; //para la insersion de archivos
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();


    private List<Pedido> listPedido = new ArrayList<>();
    private AdapterGridPedidosCliente gridAdapterPedidos;
    private Vendedor vendedorGlobal;

    private Button btnPagarPedidosCliente;
    private TextView tvTotalPagarPedidosCliente;
    private GridView gridPedidosCliente;


    private Cliente clienteGlobal;
    private Dialog dialogCargando;
    private Dialog dialogSuccess;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =inflater.inflate(R.layout.fragmento_pedidos_cliente, container, false);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        storage = FirebaseStorage.getInstance();//inicializamos la variable storage

        btnPagarPedidosCliente = root.findViewById(R.id.btnPagarPedidosCliente);
        tvTotalPagarPedidosCliente = root.findViewById(R.id.tvTotalPagarPedidosCliente);
        gridPedidosCliente = root.findViewById(R.id.gridPedidosCliente);

        vendedorGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getVendedor(); //recogemos los datos del vendedor
        clienteGlobal =((MyFirebaseApp) getActivity().getApplicationContext()).getCliente();
        Utilidades util = new Utilidades();
        dialogCargando = util.dialogCargar(getContext()); //cargamos el cuadro de dialogo
        dialogSuccess = util.dialogSuccess(getContext());
        dialogCargando.show();


        btnPagarPedidosCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pagarPedidos();
            }
        });
        listarPedidos();

        return root;
    }

    private void pagarPedidos() {
        dialogCargando.show();

        Map<String,Object> actualizarPedido = new HashMap<>();
        for(Pedido p:listPedido){
            String codigoPrincipal = "Pedido/"+p.getIdPedido();
            actualizarPedido.put(codigoPrincipal+"/pagado",true);
            actualizarPedido.put(codigoPrincipal+"/cancelado",false);
            actualizarPedido.put(codigoPrincipal+"/aceptado",false);
            actualizarPedido.put(codigoPrincipal+"/eliminado",false);
        }

        databaseReference.updateChildren(actualizarPedido).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialogCargando.dismiss();
                if (task.isSuccessful()){


                    Calificaciones calificaciones = new Calificaciones();
                    calificaciones.setIdCliente(clienteGlobal.getIdCliente());
                    calificaciones.setEsNuevo(true);
                    calificaciones.setIdCalificaciones(databaseReference.push().getKey());
                    calificaciones.setVendedor(vendedorGlobal);
                    calificaciones.setIdCliente_esNuevo(clienteGlobal.getIdCliente()+"_true");

                    //subimos las calificaciones para que el cliente inicie su calificacion
                    databaseReference.child("Calificaciones").child(calificaciones.getIdCalificaciones()).setValue(calificaciones).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Log.d("Success", "calificacion subida");
                            }else{
                                Log.d("Success", "error en subir calificacion");
                            }
                        }
                    });

                    //mostramos el mensaje success correspondiente
                    mostrarSucces();
                    Fragment fragment = new FragmentoPedidoVendedor();
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                            .replace(R.id.home_content_vendedor, fragment)
                            .commit();

                }else{
                    Toast.makeText(getContext(),"Error de red",Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private  void mostrarSucces(){
        dialogSuccess.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                dialogSuccess.dismiss();
            }
        }, 2000);
    }

    private void borrarGrid(){
        dialogCargando.dismiss();
        listPedido.clear();//borramos en caso de quedar algo en la cache
        //Inicialisamos el adaptador
        gridAdapterPedidos = new AdapterGridPedidosCliente(getContext(),
                listPedido,
                databaseReference,
                storage);
        gridPedidosCliente.setAdapter(gridAdapterPedidos); //configuramos el view
    }

    private void listarPedidos() {
        Query queryVendedor = databaseReference.child("Pedido").orderByChild("idCliente_idVendedor").equalTo(clienteGlobal.getIdCliente()+"_"+vendedorGlobal.getIdVendedor());
        queryVendedor.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Double sumaTotal = 0.0;
                    for (DataSnapshot ds:snapshot.getChildren()){
                        Pedido pedido = ds.getValue(Pedido.class);

                        if(pedido !=null){
                            //filtramos solo los pedidos aceptados que tiene el cliente
                            if(pedido.getAceptado()&&
                                    !pedido.getEliminado()&&
                                    !pedido.getCancelado()&&
                                    !pedido.getPagado()){
                                //transformamos los datos encriptados
                                try {
                                    pedido.setImagen(encriptacionDatos.desencriptar(pedido.getImagen()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    pedido.setCodigoProducto(encriptacionDatos.desencriptar(pedido.getCodigoProducto()));
                                    pedido.setDescripcionProducto(encriptacionDatos.desencriptar(pedido.getDescripcionProducto()));
                                    pedido.setNombreProducto(encriptacionDatos.desencriptar(pedido.getNombreProducto()));

                                    sumaTotal += pedido.getPrecioProducto()*pedido.getCantidadProducto();
                                    tvTotalPagarPedidosCliente.setText("Total: "+sumaTotal);
                                    listPedido.add(pedido);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    }
                    btnPagarPedidosCliente.setVisibility(View.VISIBLE);
                    dialogCargando.dismiss();
                    gridAdapterPedidos = new AdapterGridPedidosCliente(getContext(),
                            listPedido,
                            databaseReference,
                            storage);
                    gridPedidosCliente.setAdapter(gridAdapterPedidos); //configuramos el view


                }else{
                    btnPagarPedidosCliente.setVisibility(View.GONE);
                    borrarGrid();
                    if (getContext()!=null){
                        Toast.makeText(getContext(),"Error de red",Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                btnPagarPedidosCliente.setVisibility(View.GONE);
                Log.e("error","Error pedidos cliente",error.toException());
                borrarGrid();
            }
        });
    }
}