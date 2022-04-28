package com.aplicacion.envivoapp.utilidades;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class BuscadorVendedor {
    EncriptacionDatos encrypt = new EncriptacionDatos();

    public interface resultadoBuscadorVendedor{
        void resultadoBuscadorVendedor(List<Vendedor> vendedorList);
    }
    private BuscadorVendedor.resultadoBuscadorVendedor iterfaceResultadoBuscadorVendedor;

    private  int finalIndice = 0;
    public BuscadorVendedor(Context context,
                                 DatabaseReference reference,
                                 String palabraBuscar,
                                 BuscadorVendedor.resultadoBuscadorVendedor result) {

        Dialog dialogBuscando = new Utilidades().dialogCargar(context);
        dialogBuscando.show();
        iterfaceResultadoBuscadorVendedor = result;
        List<Vendedor> vendedorList = new ArrayList<>();
        reference.child("Vendedor").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot ds: snapshot.getChildren()){
                        Vendedor vendedor = ds.getValue(Vendedor.class);
                        try {
                            vendedor.setCelular(encrypt.desencriptar(vendedor.getCelular()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            vendedor.setTelefono(encrypt.desencriptar(vendedor.getTelefono()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            vendedor.setCedula(encrypt.desencriptar(vendedor.getCedula()));
                            vendedor.setNombre(encrypt.desencriptar(vendedor.getNombre()));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        if (palabraBuscar.equals("")){
                            vendedorList.add(vendedor);
                        } else{
                            if (vendedor != null) {
                                if (vendedor.getNombre().toLowerCase().contains(palabraBuscar.toLowerCase())) {
                                    vendedorList.add(vendedor);
                                }
                            }
                        }
                        Log.d("contadorChil ",snapshot.getChildrenCount()+"");
                        if (finalIndice == snapshot.getChildrenCount()-1){
                            iterfaceResultadoBuscadorVendedor.resultadoBuscadorVendedor(vendedorList);
                            dialogBuscando.dismiss();
                        }
                        finalIndice++;

                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iterfaceResultadoBuscadorVendedor.resultadoBuscadorVendedor(vendedorList);
                dialogBuscando.dismiss();
            }
        });

    }

}
