package com.aplicacion.envivoapp.cuadroDialogo;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Mensaje_Cliente_Vendedor;
import com.aplicacion.envivoapp.modelos.Notificacion;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
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

public class CuadroAceptarPedidoVendedor {
    EncriptacionDatos encrypt  = new EncriptacionDatos();
    DatabaseReference reference ;
    Mensaje mensajeDado;
    public interface resultadoCuadroAceptarPedidoVendedor{
        void resultadoCuadroAceptarPedidoVendedor(Boolean isAcepatado,Boolean isCancelado, int position);
    }
    private CuadroAceptarPedidoVendedor.resultadoCuadroAceptarPedidoVendedor interfaceResultadoDialogo;



    public CuadroAceptarPedidoVendedor(Context context,
                                       Integer position,
                                       DatabaseReference reference,
                                       CuadroAceptarPedidoVendedor.resultadoCuadroAceptarPedidoVendedor result,
                                       Mensaje mensajeDado) {
        this.reference = reference;
        this.mensajeDado = mensajeDado;
        interfaceResultadoDialogo = result;
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);//el dialogo se presenta sin el titulo
        dialog.setCancelable(false); //impedimos el cancelamiento del dialogo
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.));//le damos un color de fondo transparente
        dialog.setContentView(R.layout.cuadro_aceptar_pedido_vendedor_mensajeria_vendedor); //le asisganos el layout

        //incializamos las variables
        EditText nombreProducto = dialog.findViewById(R.id.txtNombreProductoCuadroAceptar);
        EditText cantidadProducto = dialog.findViewById(R.id.txtCantidadCuadroAceptarPedido);
        EditText codigoProducto = dialog.findViewById(R.id.txtCodigoPedidoCuadroAceptar);
        EditText precioProducto = dialog.findViewById(R.id.txtPrecioCuadroAceptarPedido);
        EditText descripcionProducto = dialog.findViewById(R.id.txtDescripcionCuadroAceptarPedido);
        Button aceptarProducto = dialog.findViewById(R.id.btnAceptarPedidoCuadroAceptarPedido);
        Button cancelarProducto = dialog.findViewById(R.id.btnCancelarCuadroAceptarPedido);
        if(nombreProducto == null){
            nombreProducto.setText("");
        }
        if(cantidadProducto == null){
            cantidadProducto.setText("");
        }
        if(codigoProducto == null){
            codigoProducto.setText("");
        }
        if(precioProducto == null){
            precioProducto.setText("");
        }
        if(descripcionProducto == null){
            precioProducto.setText("");
        }

        aceptarProducto.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                //obtenemos el numero de dias de espera para la cancelacion del pedido

                if (codigoProducto.getText().toString().equals("")&&nombreProducto.getText().toString().equals("")){
                    Toast.makeText(context,"Ingrese al menos el código y nombre del producto",Toast.LENGTH_LONG).show();
                    if (codigoProducto.getText().toString().equals("")){
                        codigoProducto.setError("Ingrese código");
                    }
                    if(nombreProducto.getText().toString().equals("")){
                        nombreProducto.setError("Ingrese nombre");
                    }

                }else {
                    //creamos el pedido
                    Pedido pedido = new Pedido();
                    String idPedido = reference.push().getKey();
                    pedido.setIdPedido(idPedido);

                    try {
                        pedido.setCodigoProducto(encrypt.encriptar(codigoProducto.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        pedido.setNombreProducto(encrypt.encriptar(nombreProducto.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (cantidadProducto.getText().toString().equals("")){
                        pedido.setCantidadProducto(0);
                    }else{
                        pedido.setCantidadProducto(Integer.parseInt(cantidadProducto.getText().toString()));
                    }
                    if (precioProducto.getText().toString().equals("")){
                        pedido.setPrecioProducto(0.0);
                    }else{
                        pedido.setPrecioProducto(Double.parseDouble(precioProducto.getText().toString()));
                    }

                    try {
                        pedido.setDescripcionProducto(encrypt.encriptar(descripcionProducto.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    pedido.setIdCliente(mensajeDado.getCliente().getIdCliente());
                    pedido.setIdVendedor(mensajeDado.getVendedor().getIdVendedor());
                    pedido.setIdStreaming(mensajeDado.getIdStreaming());
                    pedido.setCancelado(false);
                    pedido.setAceptado(true);
                    pedido.setImagen(null);
                    //fin de la creacion del pedido

                    //creamos el mensaje
                    Mensaje mensaje = new Mensaje();
                    String idMensaje = reference.push().getKey();
                    mensaje.setIdMensaje(idMensaje);
                    mensaje.setIdStreaming(mensajeDado.getIdStreaming());
                    mensaje.setVendedor(mensajeDado.getVendedor());
                    mensaje.setCliente(mensajeDado.getCliente());
                    mensaje.setPedidoAceptado(true);
                    mensaje.setPedidoCancelado(false);
                    mensaje.setEsVededor(true);
                    mensaje.setPedidoCancelado(false);
                    mensaje.setIdCliente_idVendedor(mensajeDado.getCliente().getIdCliente()+"_"+mensajeDado.getVendedor().getIdVendedor());
                    mensaje.setIdVendedor_idStreaming(mensajeDado.getVendedor().getIdVendedor()+"_"+mensajeDado.getIdStreaming());

                    try {
                        mensaje.setTexto(encrypt.encriptar("Su pedido a sido aceptado porfavor revise su carrito de compra"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    LocalDateTime tiempoActual = LocalDateTime.now();//obtenemos la fecha actual
                    Date fecha = new Date();
                    fecha.setDate(tiempoActual.getDayOfMonth());
                    fecha.setMonth(tiempoActual.getMonth().getValue());
                    fecha.setYear(tiempoActual.getYear());
                    fecha.setHours(tiempoActual.getHour());
                    fecha.setMinutes(tiempoActual.getMinute());
                    fecha.setSeconds(tiempoActual.getSecond());


                    mensaje.setFecha(fecha);//seteamos la fecha del mensaje
                    pedido.setFechaPedido(fecha);//seteamos la fecha del pedido


                    //creamos el date en base la hora actual
                    LocalDateTime tiempoActualFinal = LocalDateTime.now();//obtenemos la fecha actual
                    tiempoActualFinal = tiempoActualFinal.plusDays(mensajeDado.getVendedor().getDiasEperaCancelacion()); //sumamos los dias de cancelacion al tiempo actual
                    Date fechaFinal = new Date(); //creamos la fecha date
                    fechaFinal.setDate(tiempoActualFinal.getDayOfMonth());
                    fechaFinal.setMonth(tiempoActualFinal.getMonth().getValue());
                    fechaFinal.setYear(tiempoActualFinal.getYear());
                    fechaFinal.setHours(tiempoActualFinal.getHour());
                    fechaFinal.setMinutes(tiempoActualFinal.getMinute());
                    fechaFinal.setSeconds(tiempoActualFinal.getSecond());

                    pedido.setFechaFinalPedido(fechaFinal);/// asiganamos la fecha final al pedido

                    reference.child("Pedido").child(idPedido).setValue(pedido).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Map<String,Object> mensajeActualizacion= new HashMap<>();
                            mensajeActualizacion.put("pedidoAceptado",true);//cargamos la variable pedido aceptado como true
                            mensajeActualizacion.put("pedidoCancelado",false);//cargamos la variable pedido cancelado como true
                            reference.child("Mensaje").child(mensajeDado.getIdMensaje()).updateChildren(mensajeActualizacion).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    reference.child("Mensaje").child(idMensaje).setValue(mensaje);//creamos el mensaje que sera enviado al cliente
                                    Toast.makeText(context, "Pedido guardado", Toast.LENGTH_LONG).show();
                                    interfaceResultadoDialogo.resultadoCuadroAceptarPedidoVendedor(true, false, position);
                                }
                            });//actualizamo el estado del mensaje
                            //creamos la notificacion correspondiente
                            crearNotificacionPedidoCreado(pedido,1);
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "Error al guardar el pedido revise sus pedidos", Toast.LENGTH_LONG).show();
                        }
                    });//creamos el pedido para el vendedor y el clietne
                    dialog.dismiss();
                }
            }
        });

        cancelarProducto.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                interfaceResultadoDialogo.resultadoCuadroAceptarPedidoVendedor(false, true, position);
                dialog.dismiss();
            }
        });

        dialog.show();
    }




    private void crearNotificacionPedidoCreado( Pedido pedido,int codigoNotificacion){

        reference.child("Notificacion").orderByChild("idVendedor").equalTo(mensajeDado.getVendedor().getIdVendedor()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                Notificacion notificacion = new Notificacion();
                if (snapshot.exists()){

                    for (DataSnapshot ds:snapshot.getChildren()){

                        Notificacion aux = ds.getValue(Notificacion.class);

                        if (aux.getIdCliente().equals(mensajeDado.getCliente().getIdCliente())) {//buscamos que la notificacion sea del cliente

                            if (aux.getCodigoNotificacion() == codigoNotificacion) { //buscamos que el codigo sea 1 que corresponde a creacion de pedido

                                aux.setEsNuevo(true);
                                Map<String,Object> map = new HashMap<>();
                                map.put("esNuevo",true);
                                map.put("idPedido",pedido.getIdPedido());
                                notificacion = aux;
                                reference.child("Notificacion").child(notificacion.getIdNotificacion()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("Creada1","Notificacion actualizada");
                                    }
                                });
                            }
                        }
                    }
                    if (notificacion==null){//

                        notificacionCrearPedido(1,pedido.getIdPedido());
                    }

                }else{
                    //notificacion pedido aceptado

                    notificacionCrearPedido(1,pedido.getIdPedido()); //codigo 1 codigo de pedido aceptado
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("ENTREEE","entreNotificacion8");
                Log.e("ERROR no notificacion","No se ha encontrado notificacion");
                notificacionCrearPedido(1,pedido.getIdPedido());
            }
        });
    }

    private  void notificacionCrearPedido(int codigo, String idPedido){
        Notificacion notificacion = new Notificacion();
        notificacion.setIdNotificacion(reference.push().getKey());
        notificacion.setIdCliente(mensajeDado.getCliente().getIdCliente());
        notificacion.setIdVendedor(mensajeDado.getVendedor().getIdVendedor());
        notificacion.setIdPedido(idPedido);
        notificacion.setEsNuevo(true);
        notificacion.setCodigoNotificacion(codigo); //codigo 1 codigo de pedido aceptado
        reference.child("Notificacion").child(notificacion.getIdNotificacion()).setValue(notificacion);
    }




}
