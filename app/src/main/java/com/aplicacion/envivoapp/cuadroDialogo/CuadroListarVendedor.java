package com.aplicacion.envivoapp.cuadroDialogo;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.modelos.VideoStreaming;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CuadroListarVendedor {
    public interface resultadoDialogo{
        void resultado(Boolean isVerStreamings,Vendedor vendedor);
    }
    private CuadroListarVendedor.resultadoDialogo interfaceResultadoDialogo;

    public CuadroListarVendedor(Context context,
                                Vendedor vendedor,
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
        TextView tieneLocalVendedor = dialog.findViewById(R.id.txtCuadroListaVendedorTieneLocalVendedor);

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

        nombreVendedor.setText(vendedor.getNombre());
        telefonoVendedor.setText(vendedor.getTelefono());
        celularVendedor.setText(vendedor.getCelular());
        if(vendedor.isTieneTienda()){
            tieneLocalVendedor.setText("Si");
        }else{
            tieneLocalVendedor.setText("No");
        }

        dialog.show();

    }

}
