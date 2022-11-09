package com.aplicacion.envivoapp.cuadroDialogo;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Calificaciones;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Producto;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CuadroCalificarVendedor {

    EncriptacionDatos encrypt  = new EncriptacionDatos();
    DatabaseReference reference ;
    Context context;
    ImageButton btnCalificarBienVendedor,
            btnCalificarMalVendedor;



    public interface resultadoCuadroCalificarVendedor{
        void resultadoCuadroCalificarVendedor(Boolean isAcepatado,Boolean isCancelado, int position);
    }
    private CuadroCalificarVendedor.resultadoCuadroCalificarVendedor interfaceResultadoDialogo;



    public CuadroCalificarVendedor(Context context,
                                       DatabaseReference reference,
                                   CuadroCalificarVendedor.resultadoCuadroCalificarVendedor result,
                                   Calificaciones calificaciones) {
        this.reference = reference;
        interfaceResultadoDialogo = result;
        this.context = context;
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);//el dialogo se presenta sin el titulo
        dialog.setCancelable(false); //impedimos el cancelamiento del dialogo
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.));//le damos un color de fondo transparente
        dialog.setContentView(R.layout.cuadro_calificar_vendedor); //le asisganos el layout

        btnCalificarBienVendedor= dialog.findViewById(R.id.btnCalificarBienVendedor);
        btnCalificarMalVendedor= dialog.findViewById(R.id.btnCalificarMalVendedor);


        Map<String,Object> actualizacionCalificacion = new HashMap<>();
        actualizacionCalificacion.put("Calificaciones/"
                +calificaciones.getIdCalificaciones()
                +"/esNuevo",false);
        actualizacionCalificacion.put("Calificaciones/"
                +calificaciones.getIdCalificaciones()
                +"/idCliente_esNuevo",calificaciones.getIdCliente()+"_false");
        btnCalificarBienVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                actualizacionCalificacion.put("Calificaciones/"
                        +calificaciones.getIdCalificaciones()
                        +"/esCalificacionBuena",true);

                reference.updateChildren(actualizacionCalificacion);
                dialog.dismiss();
            }
        });

        btnCalificarMalVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizacionCalificacion.put("Calificaciones/"
                        +calificaciones.getIdCalificaciones()
                        +"/esCalificacionBuena",false);
                reference.updateChildren(actualizacionCalificacion);
                dialog.dismiss();

            }
        });


        dialog.show();
    }
}
