package com.aplicacion.envivoapp.cuadroDialogo;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CuadroCancelarPedidoCliente {

    public interface resultadoDialogo{
        void resultado(Boolean isAcepatado,Boolean isCancelado);
    }
    private CuadroCancelarPedidoCliente.resultadoDialogo interfaceResultadoDialogo;

    public CuadroCancelarPedidoCliente(Context context,
                                       Pedido pedido,
                                       DatabaseReference reference,
                                       CuadroCancelarPedidoCliente.resultadoDialogo result) {
        interfaceResultadoDialogo = result;
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);//el dialogo se presenta sin el titulo
        dialog.setCancelable(false); //impedimos el cancelamiento del dialogo
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.));//le damos un color de fondo transparente
        dialog.setContentView(R.layout.cuadro_cancelar_pedido); //le asisganos el layout

        //incializamos las variables
        EditText mensajeCancelacion = dialog.findViewById(R.id.txtMensajeCacelarPedido);
        Button enviarPedido = dialog.findViewById(R.id.btnAtrasCuadroCancelarPedido);
        Button cancelarPedido = dialog.findViewById(R.id.btnAtrasCuadroCancelarPedido);

        enviarPedido.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                //creamos el pedido
                reference.child(pedido.getIdPedido()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            //creamos el mensaje
                            Mensaje  mensaje = new Mensaje();
                            String idMensaje = reference.push().getKey();
                            mensaje.setIdMensaje(idMensaje);
                            mensaje.setIdStreaming(pedido.getIdStreaming());
                            mensaje.setIdvendedor(pedido.getIdVendedor());
                            mensaje.setIdcliente(pedido.getIdCliente());
                            mensaje.setPedidoAceptado(true);
                            mensaje.setPedidoCancelado(false);
                            mensaje.setEsVededor(true);
                            mensaje.setPedidoCancelado(true);
                            mensaje.setPedidoAceptado(false);

                            //creamos el date en base la hora actual
                            LocalDateTime tiempoActual = LocalDateTime.now();//obtenemos la fecha actual
                            Date fecha = new Date();
                            fecha.setDate(tiempoActual.getDayOfMonth());
                            fecha.setMonth(tiempoActual.getMonth().getValue());
                            fecha.setYear(tiempoActual.getYear());
                            fecha.setHours(tiempoActual.getHour());
                            fecha.setMinutes(tiempoActual.getMinute());
                            fecha.setSeconds(tiempoActual.getSecond());

                            mensaje.setFecha(fecha);
                            if(mensajeCancelacion.getText().toString().equals("")){ //en caso de que el cliente no ingrese un mensasje
                                mensaje.setTexto("El cliente a eliminado el pedido");
                            }else{
                                mensaje.setTexto(mensajeCancelacion.getText().toString());
                            }
                            reference.child("Mensaje").child(idMensaje).setValue(mensaje);//enviamos el mensaje

                            //Actualizamos el estado del mensaje
                            Map<String,Object> mensajeCliente = new HashMap<>(); //almacena los datos que van a ser editados
                            mensajeCliente.put("pedidoAceptado",true);//actualizamos el pedido aceptado
                            mensajeCliente.put("pedidoCancelado",false);//actualizamos el pedido cancelado
                            reference.child("Pedido").child(pedido.getIdPedido()).updateChildren(mensajeCliente).addOnSuccessListener(new OnSuccessListener<Void>() {//enviamos la cancelacion
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context,"El pedido a sido cancelado con éxito",Toast.LENGTH_LONG).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "A ocurrido un error al cancelar el pedido", Toast.LENGTH_LONG).show();
                                }
                            });

                        }else{
                            Toast.makeText(context,"No se a podido eliminar el pedido",Toast.LENGTH_LONG).show();
                        }

                    }
                });
                interfaceResultadoDialogo.resultado(true,false);
                dialog.dismiss();
            }
        });

        cancelarPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}
