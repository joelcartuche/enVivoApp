package com.aplicacion.envivoapp.utilidades;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class BuscadorPedidoCliente {
    EncriptacionDatos encrypt = new EncriptacionDatos();

    public interface resultadoBusquedaVendedorPedido{
        void resultadoBusquedaVendedorPedido(List<Pedido> pedidoList);
    }
    private BuscadorPedidoCliente.resultadoBusquedaVendedorPedido iterfaceResultadoBusquedaVendedorPedido;

    private  int finalIndice = 0;
    public BuscadorPedidoCliente(Context context,
                                 List<Pedido> pedidoList,
                                 DatabaseReference reference,
                                 String palabraBuscar,
                                 BuscadorPedidoCliente.resultadoBusquedaVendedorPedido result) {

        Dialog dialogBuscando = new Utilidades().dialogCargar(context);
        dialogBuscando.show();
        iterfaceResultadoBusquedaVendedorPedido = result;
        List<Pedido> auxPedidoList = new ArrayList<>();

        for (Pedido pedido:pedidoList){
            reference.child("Vendedor").child(pedido.getIdVendedor()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Vendedor vendedor = snapshot.getValue(Vendedor.class);
                        if (vendedor != null) {
                            String nombre = "";
                            try {
                                nombre = encrypt.desencriptar(vendedor.getNombre());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (!nombre.equals("")) {
                                if (nombre.toLowerCase().contains(palabraBuscar.toLowerCase())) {
                                    auxPedidoList.add(pedido);
                                }else if (pedido.getNombreProducto().toLowerCase().contains(palabraBuscar.toLowerCase())){
                                    auxPedidoList.add(pedido);
                                }
                            }
                        }
                    }
                    if (finalIndice == pedidoList.size()-1){
                        iterfaceResultadoBusquedaVendedorPedido.resultadoBusquedaVendedorPedido(auxPedidoList);
                        dialogBuscando.dismiss();
                    }
                    finalIndice++;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finalIndice++;
                }
            });

        }
    }
}
