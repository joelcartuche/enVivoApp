package com.aplicacion.envivoapp.adaptadores;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroAceptarPedidoVendedor;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Mensaje_Cliente_Vendedor;
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
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterGridMensajeriaVendedor extends BaseAdapter implements CuadroAceptarPedidoVendedor.resultadoCuadroAceptarPedidoVendedor {

    private Context context;
    private List<Mensaje> listaMensajeVendedor;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage ; //para la insersion de archivos
    private EncriptacionDatos encriptacionDatos= new EncriptacionDatos();
    private  Button aceptar,cancelar,bloquearCliente;
    private Boolean filtrarTodos;
    private  Mensaje mensaje;

    public AdapterGridMensajeriaVendedor(Context context,
                                         List<Mensaje> listaMensajeVendedor,
                                         DatabaseReference databaseReference,
                                         Boolean filtrarTodos,
                                         FirebaseStorage storage){
        this.context = context;
        this.listaMensajeVendedor = listaMensajeVendedor;
        this.databaseReference = databaseReference;
        this.filtrarTodos = filtrarTodos;
        this.storage = storage;
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

        CardView card = convertView.findViewById(R.id.cardMensajeria);
        mensaje = listaMensajeVendedor.get(position);


        //inicializamos las variables
        TextView nombreClienteMensaje = convertView.findViewById(R.id.txtItemNombreMensajeVendedor);
        TextView fechaClienteMensaje  = convertView.findViewById(R.id.txtItemFechaMensajeVendedor);
        TextView mensajeClienteMensaje = convertView.findViewById(R.id.txtItemMensajeVendedor);
        ImageView imagenPedido = convertView.findViewById(R.id.imagenVendedorPedido);
        aceptar = convertView.findViewById(R.id.btnAceptarMensajeVendedor);
        cancelar = convertView.findViewById(R.id.btnCancelarMensajeVendedor);
        bloquearCliente = convertView.findViewById(R.id.btnBloquearClienteMensajeriaVendedor);
        imagenPedido.setVisibility(View.GONE);



        //en caso de que el usuario tenga ya un pedido cancelado o aceptado

        if(mensaje.getPedidoAceptado()||mensaje.getPedidoCancelado() || filtrarTodos){
            aceptar.setVisibility(View.GONE);
            cancelar.setVisibility(View.GONE);
        }


        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mostramos el cuadro para la acepatacion del pedido

                new CuadroAceptarPedidoVendedor(context,
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
                        mensajeAux.setVendedor(mensaje.getVendedor());
                        mensajeAux.setCliente(mensaje.getCliente());
                        mensajeAux.setPedidoAceptado(false);
                        mensajeAux.setPedidoCancelado(true);
                        mensajeAux.setEsVededor(true);
                        try {
                            mensajeAux.setTexto(encriptacionDatos.encriptar("Su pedido no ha sido aceptado"));
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

                        mensajeAux.setFecha(fecha);


                        databaseReference.child("Mensaje").child(idMensaje).setValue(mensajeAux);

                        //Actualizamos el estado del mensaje
                        Map<String,Object> mensajeCliente = new HashMap<>(); //almacena los datos que van a ser editados
                        mensajeCliente.put("pedidoCancelado",true);
                        mensajeCliente.put("pedidoAceptado",false);
                        databaseReference.child("Mensaje").child(mensaje.getIdMensaje()).updateChildren(mensajeCliente).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Pedido Cancelado", Toast.LENGTH_LONG).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "A ocurrido un error al cancelar el pedido", Toast.LENGTH_LONG).show();
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

        //bloqueo del cliente
        bloquearCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titulo = "Bloqueo de cliente";
                String textoMensaje="¿Desea bloquear al cliente?";
                DialogInterface.OnClickListener confirmar= new DialogInterface.OnClickListener() {//creamos la funcion para la confirmacion del vendedor
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String idCliente_idVendedor =mensaje.getCliente().getIdCliente()+"_"+mensaje.getVendedor().getIdVendedor();
                        Map<String,Object> bloqueo = new HashMap<>();
                        bloqueo.put("Cliente/"+mensaje.getCliente().getIdCliente()+"/bloqueado",true);//actualizamos el estado bloqueado del cliente
                        bloqueo.put("Mensaje_Cliente_Vendedor/"
                                + idCliente_idVendedor
                                +"/cliente/bloqueado",true);
                        bloqueo.put("Mensaje_Cliente_Vendedor/"
                                + idCliente_idVendedor
                                +"/elVendedorBloqueoCliente",true);

                        databaseReference.updateChildren(bloqueo).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context,"El cliente que realizo el mensaje fue bloqueado con exito",Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context,"A ocurrido un error al bloquear el clinte",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                };

                new Utilidades().cuadroDialogo(context,confirmar,titulo,textoMensaje);
            }
        });

        if (mensaje.getEsVededor()){//en caso de que el usuario es el vendedor
            imagenPedido.setVisibility(View.GONE);

            try {
                nombreClienteMensaje.setText(encriptacionDatos.desencriptar(mensaje.getCliente().getNombre()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            fechaClienteMensaje.setText(mensaje.getFecha().getDate() +"/"+
                    mensaje.getFecha().getMonth()+"/"+mensaje.getFecha().getYear()+" "+
                    mensaje.getFecha().getHours()+":"+mensaje.getFecha().getMinutes()+":"+
                    mensaje.getFecha().getSeconds());
            mensajeClienteMensaje.setText(mensaje.getTexto());

        }else{//en caso de que el usuario es  el cliente
            imagenPedido.setVisibility(View.GONE);
            try {
                nombreClienteMensaje.setText(encriptacionDatos.desencriptar(mensaje.getCliente().getNombre()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            fechaClienteMensaje.setText(mensaje.getFecha().getDate() + "/" +
                    mensaje.getFecha().getMonth() + "/" + mensaje.getFecha().getYear() + " " +
                    mensaje.getFecha().getHours() + ":" + mensaje.getFecha().getMinutes() + ":" +
                    mensaje.getFecha().getSeconds());
            mensajeClienteMensaje.setText(mensaje.getTexto());
            if(mensaje.getImagen()!=null || mensaje.getImagen()!="") {
                storage.getReference().child(mensaje.getIdMensaje()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imagenPedido.setVisibility(View.VISIBLE);
                        Picasso.with(context).load(uri).into(imagenPedido);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        imagenPedido.setVisibility(View.GONE);
                    }
                });
            }else{
                imagenPedido.setVisibility(View.GONE);
            }

        }
        return convertView;
    }


    @Override
    public void resultadoCuadroAceptarPedidoVendedor(Boolean isAcepatado, Boolean isCancelado, int position) {
        if (isAcepatado){

        }
    }






}
