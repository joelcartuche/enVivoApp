package com.aplicacion.envivoapp.cuadroDialogo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterListarLocal;
import com.aplicacion.envivoapp.adaptadores.AdapterListarLocalClientes;
import com.aplicacion.envivoapp.modelos.Local;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.modelos.VideoStreaming;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CuadroListarVendedor {

    List<Local> listLocal = new ArrayList<>(); //lista que contendra los locales del vendedor
    AdapterListarLocalClientes gridAdapterLocal; //iniciamos el adaptador del gridView
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();

    public interface resultadoDialogo{
        void resultado(Boolean isVerStreamings,Vendedor vendedor);
    }
    private CuadroListarVendedor.resultadoDialogo interfaceResultadoDialogo;

    public CuadroListarVendedor(Context context,
                                Vendedor vendedor,
                                DatabaseReference reference,
                                FirebaseAuth firebaseAuth,
                                CuadroListarVendedor.resultadoDialogo result){
        interfaceResultadoDialogo = result;
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);//el dialogo se presenta sin el titulo
        dialog.setCancelable(false); //impedimos el cancelamiento del dialogo
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.));//le damos un color de fondo transparente
        dialog.setContentView(R.layout.cuadro_lista_vendedor); //le asisganos el layout



        TextView nombreVendedor = dialog.findViewById(R.id.txtCuadroListaVendedorNombreVendedor);
        TextView telefonoVendedor = dialog.findViewById(R.id.txtCuadroListaVendedorTelefonoVendedor);
        TextView celularVendedor = dialog.findViewById(R.id.txtItemCuadroListaVendedorCelularVendedor);
        GridView localVendedorView = dialog.findViewById(R.id.gridCuadroListaVendedorTieneLocalVendedor);


        Button verStreamings = dialog.findViewById(R.id.btnCuadroListaVendedorVerStreamings);
        Button atras = dialog.findViewById(R.id.btnCuadroListaVendedorCancelar);
        final Vendedor vendedorAux = vendedor;
        interfaceResultadoDialogo.resultado(false,vendedorAux);//enviamos falso ya que no se iso click en el boton ver streamings
        verStreamings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interfaceResultadoDialogo.resultado(true,vendedorAux);//cambiamos el estado de la bansera
                dialog.dismiss();

            }
        });

        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //cargamos los locales del vendedor en el grid
        reference.child("Local").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listLocal.clear();
                if (snapshot.exists()){
                    for (DataSnapshot ds:snapshot.getChildren()){
                        Local local = ds.getValue(Local.class);
                        if (local!=null){
                            if(local.getIdVendedor().equals(vendedor.getIdVendedor())){
                                try {
                                    local.setCallePrincipal(encriptacionDatos.desencriptar(local.getCallePrincipal()));
                                    local.setCelular(encriptacionDatos.desencriptar(local.getCelular()));
                                    local.setNombre(encriptacionDatos.desencriptar(local.getNombre()));
                                    local.setReferencia(encriptacionDatos.desencriptar(local.getReferencia()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    local.setCalleSecundaria(encriptacionDatos.desencriptar(local.getCalleSecundaria()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    local.setTelefono(encriptacionDatos.desencriptar(local.getTelefono()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                listLocal.add(local);
                            }
                        }
                    }



                    gridAdapterLocal = new AdapterListarLocalClientes(context, listLocal, firebaseAuth, reference);
                    localVendedorView.setAdapter(gridAdapterLocal); //configuramos el view
                }else{
                    gridAdapterLocal = new AdapterListarLocalClientes(context, listLocal,firebaseAuth, reference);
                    localVendedorView.setAdapter(gridAdapterLocal); //configuramos el view
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        nombreVendedor.setText(vendedor.getNombre());
        telefonoVendedor.setText(vendedor.getTelefono());
        celularVendedor.setText(vendedor.getCelular());

        dialog.show();

    }

}
