package com.aplicacion.envivoapp.cuadroDialogo;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.util.Log;
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
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CuadroCalificarVendedor {

    EncriptacionDatos encrypt  = new EncriptacionDatos();
    DatabaseReference reference ;
    Context context;
    Vendedor vendedorGlobal;
    Calificaciones calificaciones;
    ImageButton btnCalificarBienVendedor,
            btnCalificarMalVendedor;
    Dialog dialogoGlobal ;



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
        dialogoGlobal= new Dialog(context);
        dialogoGlobal.requestWindowFeature(Window.FEATURE_NO_TITLE);//el dialogo se presenta sin el titulo
        dialogoGlobal.setCancelable(false); //impedimos el cancelamiento del dialogo
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.));//le damos un color de fondo transparente
        dialogoGlobal.setContentView(R.layout.cuadro_calificar_vendedor); //le asisganos el layout


        this.calificaciones = calificaciones;
        btnCalificarBienVendedor= dialogoGlobal.findViewById(R.id.btnCalificarBienVendedor);
        btnCalificarMalVendedor= dialogoGlobal.findViewById(R.id.btnCalificarMalVendedor);


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
                actualizacionCalificacion.put("Vendedor/"
                        +vendedorGlobal.getIdVendedor()
                        +"/numCalificacionesBuenas",vendedorGlobal.getNumCalificacionesBuenas()+1);

                reference.updateChildren(actualizacionCalificacion);
                dialogoGlobal.dismiss();
            }
        });

        btnCalificarMalVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizacionCalificacion.put("Calificaciones/"
                        +calificaciones.getIdCalificaciones()
                        +"/esCalificacionBuena",false);
                actualizacionCalificacion.put("Vendedor/"
                        +vendedorGlobal.getIdVendedor()
                        +"/numCalificacionesMalas",vendedorGlobal.getNumCalificacionesMalas()+1);

                reference.updateChildren(actualizacionCalificacion);
                dialogoGlobal.dismiss();

            }
        });

        obtenerVendedorGlobal();

    }



    /**
     * ***Suma el numero de calificaciones en la clase vendedor
     * */
    private void obtenerVendedorGlobal() {
        reference.child("Vendedor").child(calificaciones.getVendedor().getIdVendedor()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Vendedor vendedor = snapshot.getValue(Vendedor.class);
                    if (vendedor != null){
                        vendedorGlobal =vendedor;
                        dialogoGlobal.show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ERROR","Error al encontrar vendedor",error.toException());
            }
        });
    }


}
