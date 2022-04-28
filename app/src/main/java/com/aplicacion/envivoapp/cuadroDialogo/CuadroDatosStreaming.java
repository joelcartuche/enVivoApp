package com.aplicacion.envivoapp.cuadroDialogo;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Vendedor;

public class CuadroDatosStreaming {
    public interface resultadoDialogo{
        void resultado(Boolean isVerStreamings, Vendedor vendedor,Cliente cliente,String idStreaming,String urlStreaming);
    }
    private CuadroDatosStreaming.resultadoDialogo interfaceResultadoDialogo;

    public CuadroDatosStreaming(Context context,
                                Vendedor vendedor,
                                Cliente cliente,
                                String idStreaming,
                                String url,
                                String fecha,
                                String hora,
                                CuadroDatosStreaming.resultadoDialogo result) {
        interfaceResultadoDialogo = result;
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);//el dialogo se presenta sin el titulo
        dialog.setCancelable(false); //impedimos el cancelamiento del dialogo
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.));//le damos un color de fondo transparente
        dialog.setContentView(R.layout.cuadro_datos_streaming); //le asisganos el layout


        EditText urlStreaming = dialog.findViewById(R.id.txtUrlCuadroStreaming);
        EditText fechaStreaming = dialog.findViewById(R.id.txtFechaCuadroStreaming);
        EditText horaStreaming = dialog.findViewById(R.id.txtHoraCuadroStreaming);
        urlStreaming.setFocusable(false);
        fechaStreaming.setFocusable(false);
        horaStreaming.setFocusable(false);
        urlStreaming.setText(url);
        fechaStreaming.setText(fecha);
        horaStreaming.setText(hora);


        Button verStreamings = dialog.findViewById(R.id.btnIrStreamingCuadroStreraming);
        Button atras = dialog.findViewById(R.id.btnCancelarCuadroStreamings);
        interfaceResultadoDialogo.resultado(false,vendedor,cliente,idStreaming,urlStreaming.getText().toString());//enviamos falso ya que no se iso click en el boton ver streamings
        verStreamings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interfaceResultadoDialogo.resultado(true,
                        vendedor,
                        cliente,
                        idStreaming,
                        urlStreaming.getText().toString());//cambiamos el estado de la bansera
                dialog.dismiss();
            }
        });

        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();
    }
}
