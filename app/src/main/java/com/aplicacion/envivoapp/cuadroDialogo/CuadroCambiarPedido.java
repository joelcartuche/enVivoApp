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
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CuadroCambiarPedido {


    public CuadroCambiarPedido(Context context,
                               Pedido pedido,
                               DatabaseReference reference) {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);//el dialogo se presenta sin el titulo
        dialog.setCancelable(false); //impedimos el cancelamiento del dialogo
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.));//le damos un color de fondo transparente
        dialog.setContentView(R.layout.cuadro_aceptar_pedido_mensaje_cliente); //le asisganos el layout

        //incializamos las variables
        EditText nombreProducto = dialog.findViewById(R.id.txtNombreProductoCuadroAceptar);
        EditText cantidadProducto = dialog.findViewById(R.id.txtCantidadPedidoCuadroAceptar);
        EditText codigoProducto = dialog.findViewById(R.id.txtCodigoPedidoCuadroAceptar);
        EditText precioProducto = dialog.findViewById(R.id.txtPrecioCuadroAceptarPedido);
        EditText descripcionProducto = dialog.findViewById(R.id.txtDescripcionCuadroPedido);
        Button cambiarPedido = dialog.findViewById(R.id.btnAceptarPedidoCuadroAceptarPedido);
        Button cancelarProducto = dialog.findViewById(R.id.btnCancelarCuadroAceptarPedido);

        nombreProducto.setText(pedido.getNombreProducto());
        cantidadProducto.setText(pedido.getCantidadProducto()+"");
        codigoProducto.setText(pedido.getCodigoProducto());
        precioProducto.setText(pedido.getPrecioProducto()+"");
        descripcionProducto.setText(pedido.getDescripcionProducto());

        cambiarPedido.setText("Guardar cambio");
        cambiarPedido.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                Map<String,Object> pedidoActualizacion= new HashMap<>();
                pedidoActualizacion.put("nombreProducto",nombreProducto.getText().toString());
                pedidoActualizacion.put("cantidadProducto",Integer.parseInt(cantidadProducto.getText().toString()));
                pedidoActualizacion.put("codigoProducto",codigoProducto.getText().toString());
                pedidoActualizacion.put("precioProducto",Double.parseDouble(precioProducto.getText().toString()));
                pedidoActualizacion.put("descripcionProducto",descripcionProducto.getText().toString());



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
                mensaje.setPedidoCancelado(false);
                mensaje.setTexto("Su pedido ha sido cambiado porfavor revise su carrito de compra");

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
                reference.child("Mensaje").child(idMensaje).setValue(mensaje);

                reference.child("Pedido").child(pedido.getIdPedido()).updateChildren(pedidoActualizacion).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(context,"Pedido actualizado con éxito",Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(context,"No se pudo actualizar el pedido",Toast.LENGTH_LONG).show();
                        }
                    }
                });


                dialog.dismiss();
            }
        });

        cancelarProducto.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
