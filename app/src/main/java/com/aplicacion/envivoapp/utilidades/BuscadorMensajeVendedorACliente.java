package com.aplicacion.envivoapp.utilidades;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.aplicacion.envivoapp.adaptadores.AdapterListarClientes;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BuscadorMensajeVendedorACliente {

    EncriptacionDatos encrypt = new EncriptacionDatos();
    List<Cliente> clienteList;
    Dialog dialogBuscando;

    public interface resultadoBuscadorMensajeVendedorACliente{
        void resultadoBuscadorMensajeVendedorACliente(List<Cliente> clienteList);
    }
    private BuscadorMensajeVendedorACliente.resultadoBuscadorMensajeVendedorACliente iterfaceResultadoBuscadorMensajeVendedorACliente;

    private  int finalIndice = 0;
    public BuscadorMensajeVendedorACliente(Context context,
                                           DatabaseReference reference,
                                           Vendedor vendedor,
                                           Boolean esMensajeGlobal,
                                           BuscadorMensajeVendedorACliente.resultadoBuscadorMensajeVendedorACliente result) {

        dialogBuscando = new Utilidades().dialogCargar(context);
        dialogBuscando.show();
        iterfaceResultadoBuscadorMensajeVendedorACliente = result;
        clienteList = new ArrayList<>();


        if (esMensajeGlobal) {
            //buscamos los clientes que no estan bloqueados
            mensajeriaGlobal(reference, vendedor);
        }else{
            mensajeriaEstandar(reference,vendedor);
        }

    }

    private void mensajeriaEstandar(DatabaseReference reference, Vendedor vendedor) {
        Query  mensajeVendedor = reference.child("Mensaje").orderByChild("vendedor/idVendedor").equalTo(vendedor.getIdVendedor());
        mensajeVendedor.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                if (snapshot.exists()){
                    clienteList = new ArrayList<>();
                    Log.d("EditoDatos","edotp//////////---------");
                    List<String> idClientes = new ArrayList<>();
                    int cont=1;

                    for (DataSnapshot ds:snapshot.getChildren()){
                        Mensaje mensaje= ds.getValue(Mensaje.class);
                        if (mensaje!=null)
                        {
                            if (cont == 1)
                            { //extraemos el usuario del primer mensaje
                                idClientes.add(mensaje.getCliente().getIdCliente());
                                agregarListaCliente(mensaje);
                            } else
                            {
                                if (idClientes.indexOf(mensaje.getCliente().getIdCliente()) == -1)
                                {//en caso de que no existaa elcliente en la lista
                                    idClientes.add(mensaje.getCliente().getIdCliente());
                                    agregarListaCliente(mensaje);
                                }
                            }
                        }
                        //retornamos la lista
                        if (finalIndice == snapshot.getChildrenCount()-1){
                            dialogBuscando.dismiss();
                            iterfaceResultadoBuscadorMensajeVendedorACliente.resultadoBuscadorMensajeVendedorACliente(clienteList);
                        }
                        finalIndice++;
                        cont++;
                    }
                }else{
                    dialogBuscando.dismiss();
                    iterfaceResultadoBuscadorMensajeVendedorACliente.resultadoBuscadorMensajeVendedorACliente(clienteList);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialogBuscando.dismiss();
                iterfaceResultadoBuscadorMensajeVendedorACliente.resultadoBuscadorMensajeVendedorACliente(clienteList);
            }
        });
    }

    private void mensajeriaGlobal(DatabaseReference databaseReference,
                                  Vendedor vendedor) {

        Query  mensajeVendedor = databaseReference.child("Mensaje").orderByChild("vendedor/idVendedor").equalTo(vendedor.getIdVendedor());
        mensajeVendedor.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    List<String> idClientes = new ArrayList<>();
                    int cont=1;

                    for (DataSnapshot ds:snapshot.getChildren()){
                        Mensaje mensaje= ds.getValue(Mensaje.class);
                        if (mensaje!=null)
                        {
                            if (!mensaje.getEsClienteBloqueado()
                                    && mensaje.getEsGlobal())
                            {
                                if (cont==1){ //extraemos el usuario del primer mensaje
                                    idClientes.add(mensaje.getCliente().getIdCliente());
                                    agregarListaCliente(mensaje);
                                }else{
                                    if (idClientes.indexOf(mensaje.getCliente().getIdCliente()) == -1){//en caso de que no existaa elcliente en la lista
                                        idClientes.add(mensaje.getCliente().getIdCliente());
                                        agregarListaCliente(mensaje);
                                    }
                                }
                            }
                        }

                        //retornamos la lista
                        if (finalIndice == snapshot.getChildrenCount()-1){
                            dialogBuscando.dismiss();
                            iterfaceResultadoBuscadorMensajeVendedorACliente.resultadoBuscadorMensajeVendedorACliente(clienteList);
                        }
                        finalIndice++;
                        cont++;

                    }


                }else{
                    dialogBuscando.dismiss();
                    iterfaceResultadoBuscadorMensajeVendedorACliente.resultadoBuscadorMensajeVendedorACliente(clienteList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialogBuscando.dismiss();
                iterfaceResultadoBuscadorMensajeVendedorACliente.resultadoBuscadorMensajeVendedorACliente(clienteList);
            }
        });


    }

    private void agregarListaCliente(Mensaje mensaje) {
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
        clienteList.add(cliente);
    }
}
