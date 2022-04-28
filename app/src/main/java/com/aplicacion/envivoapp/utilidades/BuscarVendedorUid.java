package com.aplicacion.envivoapp.utilidades;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BuscarVendedorUid {

    EncriptacionDatos encrypt = new EncriptacionDatos();

    public interface resultadoBuscarVendedorUid{
        void resultadoBuscarVendedorUid(Vendedor vendedor);
    }
    private BuscarVendedorUid.resultadoBuscarVendedorUid iterfaceResultadoBuscarVendedorUid;

    public BuscarVendedorUid(Context context,
                           DatabaseReference reference,
                           String uidUsuario,
                             BuscarVendedorUid.resultadoBuscarVendedorUid result) {

        Dialog dialogBuscando = new Utilidades().dialogCargar(context);
        dialogBuscando.show();
        iterfaceResultadoBuscarVendedorUid = result;

        reference.child("Vendedor").orderByChild("uidUsuario").equalTo(uidUsuario).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Vendedor vendedor = null;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        vendedor = ds.getValue(Vendedor.class);
                    }
                    dialogBuscando.dismiss();
                    iterfaceResultadoBuscarVendedorUid.resultadoBuscarVendedorUid(vendedor);

                }else{
                    dialogBuscando.dismiss();
                    iterfaceResultadoBuscarVendedorUid.resultadoBuscarVendedorUid(null);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialogBuscando.dismiss();
                iterfaceResultadoBuscarVendedorUid.resultadoBuscarVendedorUid(null);

            }
        });
    }

}
