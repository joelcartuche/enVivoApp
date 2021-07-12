package com.aplicacion.envivoapp.adaptadores;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activitysParaVendedores.MensajeriaVendedor;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroAceptarPedidoMensajeCliente;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterGridMensajeriaVendedor extends BaseAdapter implements CuadroAceptarPedidoMensajeCliente.resultadoDialogo {

    private Context context;
    private List<Mensaje> listaMensajeVendedor;
    private DatabaseReference databaseReference;

    private  Button aceptar,cancelar;
    private Boolean filtrarTodos;

    public AdapterGridMensajeriaVendedor(Context context,
                                         List<Mensaje> listaMensajeVendedor,
                                         DatabaseReference databaseReference,
                                         Boolean filtrarTodos){
        this.context = context;
        this.listaMensajeVendedor = listaMensajeVendedor;
        this.databaseReference = databaseReference;
        this.filtrarTodos = filtrarTodos;
    }


    @Override
    public int getCount() {
        return listaMensajeVendedor.size();
    }

    @Override
    public Object getItem(int position) {
        return listaMensajeVendedor.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater layoutInflater =(LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_list_mensajeria_vendedor,null);

        }

        Mensaje mensaje = listaMensajeVendedor.get(position);//para manejar que elemento estamos clickeando
        //inicializamos las variables
        TextView nombreClienteMensaje = convertView.findViewById(R.id.txtItemNombreMensajeVendedor);
        TextView fechaClienteMensaje  = convertView.findViewById(R.id.txtItemFechaMensajeVendedor);
        TextView mensajeClienteMensaje = convertView.findViewById(R.id.txtItemMensajeVendedor);
        aceptar = convertView.findViewById(R.id.btnAceptarMensajeVendedor);
        cancelar = convertView.findViewById(R.id.btnCancelarMensajeVendedor);


        //en caso de que el usuario tenga ya un pedido cancelado o aceptado

        if(mensaje.getPedidoAceptado()||mensaje.getPedidoCancelado() || filtrarTodos){
            aceptar.setVisibility(View.INVISIBLE);
            cancelar.setVisibility(View.INVISIBLE);
        }

        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CuadroAceptarPedidoMensajeCliente(context,
                        mensaje.getIdvendedor(),
                        mensaje.getIdcliente(),
                        mensaje.getIdStreaming(),
                        position,
                        databaseReference,
                        AdapterGridMensajeriaVendedor.this,
                        mensaje);
            }
        });
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder dialogo1 = new AlertDialog.Builder(context);//creamos el mensaje de aceptacion o cancelacion
                dialogo1.setTitle("Importante");
                dialogo1.setMessage("¿ Desea cancelar el pedido del cliente ?");
                dialogo1.setCancelable(false);
                dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    public void onClick(DialogInterface dialogo1, int id) {
                        Mensaje  mensajeAux = new Mensaje();
                        String idMensaje = databaseReference.push().getKey();
                        mensajeAux.setIdMensaje(idMensaje);
                        mensajeAux.setIdStreaming(mensaje.getIdStreaming());
                        mensajeAux.setIdvendedor(mensaje.getIdvendedor());
                        mensajeAux.setIdcliente(mensaje.getIdcliente());
                        mensajeAux.setPedidoAceptado(false);
                        mensajeAux.setPedidoCancelado(true);
                        mensajeAux.setEsVededor(true);
                        mensajeAux.setTexto("Su pedido no ha sido aceptado");

                        LocalDateTime tiempoActual = LocalDateTime.now();//obtenemos la fecha actual
                        Date fecha = new Date();
                        fecha.setDate(tiempoActual.getDayOfMonth());
                        fecha.setMonth(tiempoActual.getMonth().getValue());
                        fecha.setYear(tiempoActual.getYear());
                        fecha.setHours(tiempoActual.getHour());
                        fecha.setMinutes(tiempoActual.getMinute());
                        fecha.setSeconds(tiempoActual.getSecond());

                        mensajeAux.setFecha(fecha);
                        databaseReference.child("Mensaje").child(idMensaje).setValue(mensajeAux);

                        //Actualizamos el estado del mensaje
                        Map<String,Object> mensajeCliente = new HashMap<>(); //almacena los datos que van a ser editados
                        mensajeCliente.put("pedidoCancelado",true);
                        databaseReference.child("Mensaje").child(mensaje.getIdMensaje()).updateChildren(mensajeCliente).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Pedido Aceptado", Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "A ocurrido un error al actualizar los datos", Toast.LENGTH_LONG).show();
                            }
                        });
                        dialogo1.dismiss();
                    }
                });
                dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {
                        dialogo1.dismiss();
                    }
                });
                dialogo1.show();


            }
        });

        if (mensaje.getEsVededor()){//en caso de que el usuario es el vendedor
            databaseReference.child("Vendedor").child(mensaje.getIdvendedor()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Vendedor vendedor = snapshot.getValue(Vendedor.class); //instanciamos el cliente
                        nombreClienteMensaje.setText(vendedor.getNombre());
                        fechaClienteMensaje.setText(mensaje.getFecha().getDate() +"/"+
                                mensaje.getFecha().getMonth()+"/"+mensaje.getFecha().getYear()+" "+
                                mensaje.getFecha().getHours()+":"+mensaje.getFecha().getMinutes()+":"+
                                mensaje.getFecha().getSeconds());
                        mensajeClienteMensaje.setText(mensaje.getTexto());
                    }else{
                        Log.d("ERROR","error en encontrar el vendedor para AdapterMensajeriaCliente");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }else{//en caso de que el usuario es  el cliente
            databaseReference.child("Cliente").child(mensaje.getIdcliente()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){

                        Cliente cliente = snapshot.getValue(Cliente.class); //instanciamos el cliente
                        nombreClienteMensaje.setText(cliente.getNombre());
                        fechaClienteMensaje.setText(mensaje.getFecha().getDate() +"/"+
                                mensaje.getFecha().getMonth()+"/"+mensaje.getFecha().getYear()+" "+
                                mensaje.getFecha().getHours()+":"+mensaje.getFecha().getMinutes()+":"+
                                mensaje.getFecha().getSeconds());
                        mensajeClienteMensaje.setText(mensaje.getTexto());


                    }else{
                        Log.d("ERROR","error en encontrar el cliente para AdapterMensajeriaCliente");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        return convertView;
    }


    @Override
    public void resultado(Boolean isAcepatado, Boolean isCancelado, int position) {

    }
}
