package com.aplicacion.envivoapp.utilidades;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.aplicacion.envivoapp.adaptadores.AdapterListarClientes;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Mensaje_Cliente_Vendedor;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BuscadorCliente {
    EncriptacionDatos encrypt = new EncriptacionDatos();

    ArrayList<Cliente> clienteList;
    Dialog dialogBuscando;
            
    public interface resultadoBusquedaCliente{
        void resultadoBusquedaCliente(List<Cliente> clienteList);
    }
    private BuscadorCliente.resultadoBusquedaCliente iterfaceResultadoBusquedaCliente;

    private  int finalIndice = 0;
    public BuscadorCliente(Context context,
                                 DatabaseReference reference,
                                 String palabraBuscar,
                                 Vendedor vendedor,
                                 Boolean esGlobal,
                                 BuscadorCliente.resultadoBusquedaCliente result) {

        dialogBuscando = new Utilidades().dialogCargar(context);
        dialogBuscando.show();
        iterfaceResultadoBusquedaCliente = result;
        clienteList = new ArrayList<>();


        //buscamos los clientes que no estan bloqueados
        mensajeriaGlobal(reference, vendedor,palabraBuscar,esGlobal);

        
    }



    private void mensajeriaGlobal(DatabaseReference databaseReference,
                                  Vendedor vendedor,
                                  String palabraBuscar,
                                  Boolean esGlobal) {

        Query mensajeVendedor = databaseReference.
                child("Mensaje_Cliente_Vendedor").
                orderByChild("vendedor/idVendedor").
                equalTo(vendedor.getIdVendedor());

        //Query  mensajeVendedor = databaseReference.child("Mensaje").orderByChild("vendedor/idVendedor").equalTo(vendedor.getIdVendedor());
        mensajeVendedor.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds:snapshot.getChildren()){
                        Mensaje_Cliente_Vendedor mensaje_cliente_vendedor= ds.getValue(Mensaje_Cliente_Vendedor.class);
                        if (mensaje_cliente_vendedor!=null)
                        {
                            Cliente cliente= mensaje_cliente_vendedor.getCliente();
                            try {
                                cliente.setNombre(encrypt.desencriptar(cliente.getNombre()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                cliente.setTelefono(encrypt.desencriptar(cliente.getTelefono()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                cliente.setCelular(encrypt.desencriptar(cliente.getCelular()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (esGlobal) {
                                if (!cliente.getBloqueado()) {
                                    if (cliente.getNombre().toLowerCase().contains(palabraBuscar.toLowerCase())) {
                                        clienteList.add(mensaje_cliente_vendedor.getCliente());
                                    }
                                }
                            }else{
                                if (cliente.getNombre().toLowerCase().contains(palabraBuscar.toLowerCase())) {
                                    clienteList.add(mensaje_cliente_vendedor.getCliente());
                                }
                            }
                        }


                        //retornamos la lista
                        if (finalIndice == snapshot.getChildrenCount()-1){
                            dialogBuscando.dismiss();
                            iterfaceResultadoBusquedaCliente.resultadoBusquedaCliente(clienteList);
                        }
                        finalIndice++;
                    }


                }else{
                    dialogBuscando.dismiss();
                    iterfaceResultadoBusquedaCliente.resultadoBusquedaCliente(clienteList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialogBuscando.dismiss();
                iterfaceResultadoBusquedaCliente.resultadoBusquedaCliente(clienteList);
            }
        });


    }

    private void agregarListaCliente(Mensaje mensaje,String palabraBuscar) {
        Cliente cliente = mensaje.getCliente();
        try {
            cliente.setCallePrincipal(encrypt.desencriptar(cliente.getCallePrincipal()));
            cliente.setCedula(encrypt.desencriptar(cliente.getCedula()));
            cliente.setNombre(encrypt.desencriptar(cliente.getNombre()));
            cliente.setReferencia(encrypt.desencriptar(cliente.getReferencia()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            cliente.setCalleSecundaria(encrypt.desencriptar(cliente.getCalleSecundaria()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            cliente.setCelular(encrypt.desencriptar(cliente.getCelular()));
        } catch (Exception e) { e.printStackTrace();
        }
        try {
            cliente.setTelefono(encrypt.desencriptar(cliente.getTelefono()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cliente.getNombre().toLowerCase().contains(palabraBuscar)) {
            clienteList.add(cliente);
        }

    }
}
