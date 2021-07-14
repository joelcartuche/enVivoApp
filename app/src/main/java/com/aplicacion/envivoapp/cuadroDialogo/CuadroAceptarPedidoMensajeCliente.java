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

public class CuadroAceptarPedidoMensajeCliente {
    public interface resultadoDialogo{
        void resultado(Boolean isAcepatado,Boolean isCancelado, int position);
    }
    private CuadroAceptarPedidoMensajeCliente.resultadoDialogo interfaceResultadoDialogo;

    public CuadroAceptarPedidoMensajeCliente(Context context,
                                             String idVendedor,
                                             String idCliente,
                                             String idStreaming,
                                             Integer position,
                                             DatabaseReference reference,
                                             CuadroAceptarPedidoMensajeCliente.resultadoDialogo result,
                                             Mensaje mensajeDado) {
        interfaceResultadoDialogo = result;
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
        Button aceptarProducto = dialog.findViewById(R.id.btnAceptarPedidoCuadroAceptarPedido);
        Button cancelarProducto = dialog.findViewById(R.id.btnCancelarCuadroAceptarPedido);

        aceptarProducto.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                //creamos el pedido
                Pedido pedido = new Pedido();
                String idPedido = reference.push().getKey();
                pedido.setIdPedido(idPedido);
                pedido.setNombreProducto(nombreProducto.getText().toString());
                pedido.setCantidadProducto(Integer.parseInt(cantidadProducto.getText().toString()));
                pedido.setCodigoProducto(codigoProducto.getText().toString());
                pedido.setPrecioProducto(Double.parseDouble(precioProducto.getText().toString()));
                pedido.setDescripcionProducto(descripcionProducto.getText().toString());
                pedido.setIdCliente(idCliente);
                pedido.setIdVendedor(idVendedor);
                pedido.setIdStreaming(idStreaming);
                pedido.setCancelado(false);
                pedido.setAceptado(true);


                //creamos el mensaje
                Mensaje  mensaje = new Mensaje();
                String idMensaje = reference.push().getKey();
                mensaje.setIdMensaje(idMensaje);
                mensaje.setIdStreaming(idStreaming);
                mensaje.setIdvendedor(idVendedor);
                mensaje.setIdcliente(idCliente);
                mensaje.setPedidoAceptado(true);
                mensaje.setPedidoCancelado(false);
                mensaje.setEsVededor(true);
                mensaje.setPedidoCancelado(false);
                mensaje.setPedidoAceptado(true);
                mensaje.setTexto("Su pedido a sido aceptado porfavor revise su carrito de compra");

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
                reference.child("Pedido").child(idPedido).setValue(pedido);
                Toast.makeText(context,"Pedido guardado",Toast.LENGTH_LONG).show();
                interfaceResultadoDialogo.resultado(true,false,position);

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
