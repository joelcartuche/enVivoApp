package com.aplicacion.envivoapp.utilidades;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class BuscarClienteUid {

    EncriptacionDatos encrypt = new EncriptacionDatos();

    public interface resultadoBuscarClienteUid{
        void resultadoBuscarClienteUid(Cliente cliente);
    }
    private BuscarClienteUid.resultadoBuscarClienteUid iterfaceResultadoBuscarClienteUid;

    public BuscarClienteUid(Context context,
                             DatabaseReference reference,
                             String uidUsuario,
                            BuscarClienteUid.resultadoBuscarClienteUid result) {

        Dialog dialogBuscando = new Utilidades().dialogCargar(context);
        dialogBuscando.show();
        iterfaceResultadoBuscarClienteUid = result;

        reference.child("Cliente").
                orderByChild("uidUsuario").
                equalTo(uidUsuario).
                addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Cliente cliente = null;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        cliente = ds.getValue(Cliente.class);
                    }
                    dialogBuscando.dismiss();
                    iterfaceResultadoBuscarClienteUid.resultadoBuscarClienteUid(cliente);

                }else{
                    dialogBuscando.dismiss();
                    iterfaceResultadoBuscarClienteUid.resultadoBuscarClienteUid(null);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialogBuscando.dismiss();
                iterfaceResultadoBuscarClienteUid.resultadoBuscarClienteUid(null);

            }
        });
    }
}
