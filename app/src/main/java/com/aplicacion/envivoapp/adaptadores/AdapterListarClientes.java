package com.aplicacion.envivoapp.adaptadores;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activitysParaVendedores.MensajeriaGlobalVendedor;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Usuario;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterListarClientes extends BaseAdapter {
    private Context context;
    private List<Cliente> listaCliente;
    private DatabaseReference databaseReference;
    private String esMensajeGlobal;
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();
    public AdapterListarClientes(Context context,
                                             List<Cliente> listaCliente,
                                             DatabaseReference databaseReference,
                                 String esMensajeGlobal){
        this.context = context;
        this.listaCliente = listaCliente;
        this.databaseReference = databaseReference;
        this.esMensajeGlobal = esMensajeGlobal;
    }

    @Override
    public int getCount() {
        return listaCliente.size();
    }

    @Override
    public Object getItem(int position) {
        return listaCliente.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater layoutInflater =(LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_list_clientes,null);
        }

        Cliente cliente = listaCliente.get(position);//para manejar que elemento estamos clickeando
        //inicializamos las variables
        TextView nombreCliente= convertView.findViewById(R.id.txtINombreClienteItemListClientes);
        TextView telefonoCliente = convertView.findViewById(R.id.txtTelefonoClienteItemListClientes);
        TextView celularCliente = convertView.findViewById(R.id.txtCelularClienteItemListClientes);
        Button btnbloquerCliente = convertView.findViewById(R.id.btnBloquearClienteItemListClientes);
        Button btnDesbloquearCliente = convertView.findViewById(R.id.btnDesbloquearCliente);
        TextView lblTelelfono = convertView.findViewById(R.id.lblTelefono);
        TextView lblCelular = convertView.findViewById(R.id.lblCelular);
        ImageView imgPerfil = convertView.findViewById(R.id.imgPerfilCliente);

        nombreCliente.setText(cliente.getNombre());

        if (cliente.getTelefono()==null || cliente.getTelefono().equals("")){
            lblTelelfono.setVisibility(View.GONE);
            telefonoCliente.setVisibility(View.GONE);
        }else{
            telefonoCliente.setText(cliente.getTelefono());
        }

        if (cliente.getCelular() == null || cliente.getCelular().equals("") ){
            lblCelular.setVisibility(View.GONE);
            celularCliente.setVisibility(View.GONE);
        }else{
            celularCliente.setText(cliente.getCelular());
        }

        //Cargamos la imagen del usuario
        Query queryUsuario = databaseReference.child("Usuario").orderByChild("uidUser").equalTo(cliente.getUidUsuario());
        queryUsuario.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Usuario usuario = null;
                    for (DataSnapshot ds: snapshot.getChildren()){
                        usuario = ds.getValue(Usuario.class);
                    }
                    if (usuario != null){
                        if (usuario.getImagen() != null ){
                            try {

                                if (!usuario.getImagen().equals("")) {
                                    Uri uri = Uri.parse(usuario.getImagen());
                                    Picasso.with(context).
                                            load(uri).
                                            resize(300, 300).
                                            into(imgPerfil);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();

                            }

                        }else{
                            imgPerfil.setImageResource(R.drawable.ic_persona);
                        }

                    }

                }
            }
        });



        if (esMensajeGlobal.equals("1")){
            //btnbloquerCliente.setVisibility(View.GONE);
            btnDesbloquearCliente.setVisibility(View.VISIBLE);
            btnbloquerCliente.setVisibility(View.VISIBLE);
            btnbloquerCliente.setText("Eliminar mensajes");//cambiamos a eliminar mensajes
            btnDesbloquearCliente.setText("Iniciar conversación");

            //cambiamos la funcionalidad del botón para que se eliminen  los mensajes
            btnbloquerCliente.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogInterface.OnClickListener dialogDesbloqueo = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            actualizarMensajes(databaseReference,cliente);
                        }
                    };
                    new Utilidades().cuadroDialogo(context,dialogDesbloqueo,"Eliminar Mensajes","¿Desea eliminar los mensajes del cliente?");
                }
            });

            btnDesbloquearCliente.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ingresarMensajeria(cliente);
                }
            });




        }
        if (esMensajeGlobal.equals("0")){//en caso de que se desee listar al los clientes bloqueados
            if (!cliente.getBloqueado()) {//seteamos el mensje y color del boton
                btnDesbloquearCliente.setVisibility(View.GONE);
                btnbloquerCliente.setVisibility(View.VISIBLE);

                btnbloquerCliente.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogInterface.OnClickListener dialogDesbloqueo = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Map<String,Object> bloqueo = new HashMap<>();
                                bloqueo.put("bloqueado",true);//actualizamos el estado bloqueado del cliente
                                databaseReference.child("Cliente").child(cliente.getIdCliente()).updateChildren(bloqueo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context,"El cliente fue bloqueado con éxito",Toast.LENGTH_SHORT).show();

                                        dialog.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context,"Error al bloquear el cliente",Toast.LENGTH_SHORT).show();

                                        dialog.dismiss();
                                    }
                                });

                            }
                        };
                        new Utilidades().cuadroDialogo(context,dialogDesbloqueo,"Bloquear cliente","¿Desea bloquearlo al cliente?");
                    }
                });

            }else{
                btnDesbloquearCliente.setVisibility(View.VISIBLE);
                btnbloquerCliente.setVisibility(View.GONE);
                btnDesbloquearCliente.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                DialogInterface.OnClickListener dialogBloqueo = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Map<String,Object> bloqueo = new HashMap<>();
                        bloqueo.put("bloqueado",false);//actualizamos el estado bloqueado del cliente
                        databaseReference.child("Cliente").child(cliente.getIdCliente()).updateChildren(bloqueo).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context,"El cliente fue desbloqueado con éxito",Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context,"Error al Desbloquear el usuario",Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        });

                    }
                };
                new Utilidades().cuadroDialogo(context,dialogBloqueo,"Desbloquear cliente","¿Desea desbloquearlo al cliente?");

                    }
                });

            }

        }

        return convertView;
    }

    public void actualizarActividad(){//actualiza el activity actual para evitar la dupicacion de los datos
        Bundle parametros = new Bundle();
        parametros.putString("global", "0");
        Intent actualizar = new Intent(context, context.getClass());
        actualizar.putExtras(parametros);
        context.startActivity(actualizar);
        ((Activity) context).finish();

    }


    public void actualizarMensajes(DatabaseReference databaseReference,Cliente cliente){

        Query queryMensaje = databaseReference.child("Mensaje").orderByChild("idcliente").equalTo(cliente.getIdCliente());
        queryMensaje.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                if (snapshot.exists()){
                    //inicio de creacion de cuadro de dialogo
                    Dialog cargar = cargar();
                    cargar.show();
                    //fin de cuadro de dialogo
                    Map<String, Object> map = new HashMap<>();

                    for (DataSnapshot ds:snapshot.getChildren()){
                        Mensaje mensaje = ds.getValue(Mensaje.class);
                        if(mensaje != null
                                && mensaje.getEsEliminado() == false){
                            map.put(mensaje.getIdMensaje()+"/esEliminado",true);//cargamos los datos de manera masiva
                        }
                    }


                    databaseReference.child("Mensaje").updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            cargar.dismiss();
                            Toast.makeText(context,"Los mensajes han sido eliminados satisfactoriamente",Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            cargar.dismiss();
                            Toast.makeText(context,"Error al eliminar mensajes intentelo de nuevo",Toast.LENGTH_LONG).show();
                        }
                    });


                }
            }
        });

    }


    public void ingresarMensajeria(Cliente cliente){
        Bundle parametros = new Bundle();
        parametros.putString("cliente", cliente.getIdCliente());
        Intent streamingsIntent = new Intent(context,
                MensajeriaGlobalVendedor.class);
        streamingsIntent.putExtras(parametros);
        ((Activity) context).startActivity(streamingsIntent);
    }

    public Dialog cargar(){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);//el dialogo se presenta sin el titulo
        dialog.setCancelable(false); //impedimos el cancelamiento del dialogo
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.));//le damos un color de fondo transparente
        dialog.setContentView(R.layout.cuadro_cargando); //le asisganos el layout
        return dialog;
    }
}
