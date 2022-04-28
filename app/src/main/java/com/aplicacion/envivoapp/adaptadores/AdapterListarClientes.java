package com.aplicacion.envivoapp.adaptadores;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activitysParaVendedores.fragmentos.FragmentoMensajeriaGlobalVendedor;
import com.aplicacion.envivoapp.activitysParaVendedores.fragmentos.FragmentoMensajeriaVendedor;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Mensaje_Cliente_Vendedor;
import com.aplicacion.envivoapp.modelos.Usuario;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterListarClientes extends BaseAdapter {
    private Context context;
    private List<Cliente> listaCliente;
    private DatabaseReference databaseReference;
    private Boolean esMensajeGlobal;
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();
    private FragmentActivity fragmentActivity;
    private Vendedor vendedorGlobal;
    public AdapterListarClientes(Context context,
                                             List<Cliente> listaCliente,
                                             DatabaseReference databaseReference,
                                 Boolean esMensajeGlobal,
                                 Vendedor vendedorGlobal,
                                 FragmentActivity fragmentActivity){
        this.context = context;
        this.listaCliente = listaCliente;
        this.databaseReference = databaseReference;
        this.esMensajeGlobal = esMensajeGlobal;
        this.fragmentActivity = fragmentActivity;
        this.vendedorGlobal = vendedorGlobal;
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
        cargarUsuario(cliente,imgPerfil);


        if (esMensajeGlobal){
            //llamamos la funcionalidad de la mensajeria global
            mensajeriaGlobal(btnDesbloquearCliente,btnbloquerCliente,cliente);
        }
        if (!esMensajeGlobal){//en caso de que se desee listar al los clientes bloqueados
            Log.d("Cliente",cliente.getIdCliente()+"_" +cliente.getBloqueado());
            if (cliente.getBloqueado()){
                btnDesbloquearCliente.setVisibility(View.VISIBLE);
                btnbloquerCliente.setVisibility(View.GONE);
                btnDesbloquearCliente.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DialogInterface.OnClickListener dialogBloqueo = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                bloquerDesbloquearCliente(false,dialog,cliente);
                            }
                        };
                        new Utilidades().cuadroDialogo(context,dialogBloqueo,"Desbloquear cliente","¿Desea desbloquearlo al cliente?");
                    }
                });
            }

            if (!cliente.getBloqueado()) {//seteamos el mensaje y color del boton
                btnDesbloquearCliente.setVisibility(View.GONE);
                btnbloquerCliente.setVisibility(View.VISIBLE);
                btnbloquerCliente.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DialogInterface.OnClickListener dialogDesbloqueo = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                bloquerDesbloquearCliente(true,dialog,cliente);
                            }
                        };
                        new Utilidades().cuadroDialogo(context,dialogDesbloqueo,"Bloquear cliente","¿Desea bloquearlo al cliente?");
                    }
                });
            }
        }

        return convertView;
    }


    //funcionalidad de la mensajeria global
    private void mensajeriaGlobal(Button btnDesbloquearCliente,
                                  Button btnbloquerCliente,
                                  Cliente cliente) {
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
                        actualizarMensajes(databaseReference,cliente,vendedorGlobal);
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

    private void cargarUsuario(Cliente cliente,ImageView imgPerfil) {
        Query queryUsuario = databaseReference.child("Usuario").orderByChild("uidUser").equalTo(cliente.getUidUsuario());
        queryUsuario.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Usuario usuario = null;
                    for (DataSnapshot ds: snapshot.getChildren()){
                        Usuario usuarioAux =ds.getValue(Usuario.class);
                        if (usuarioAux.getUidUser().equals(cliente.getUidUsuario())){
                            usuario = usuarioAux;
                        };
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
    }

    public void actualizarActividad(){//actualiza el activity actual para evitar la dupicacion de los datos
        Bundle parametros = new Bundle();
        parametros.putString("global", "0");
        Intent actualizar = new Intent(context, context.getClass());
        actualizar.putExtras(parametros);
        context.startActivity(actualizar);
        ((Activity) context).finish();

    }


    public void actualizarMensajes(DatabaseReference databaseReference,Cliente cliente,Vendedor vendedor){

        Query queryMensaje = databaseReference.
                child("Mensaje").
                orderByChild("idCliente_idVendedor").
                equalTo(cliente.getIdCliente()+"_"+vendedor.getIdVendedor());
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
                            map.put("Mensaje/"+mensaje.getIdMensaje()+"/esEliminado",true);//cargamos los datos de manera masiva
                        }
                    }


                    databaseReference.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
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

        ((MyFirebaseApp) fragmentActivity.getApplicationContext()).setCliente(cliente); //recogemos los datos del vendedor

        Fragment fragment = new FragmentoMensajeriaGlobalVendedor();

        fragmentActivity.getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                .replace(R.id.home_content_vendedor, fragment)
                .commit();

    }

    public Dialog cargar(){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);//el dialogo se presenta sin el titulo
        dialog.setCancelable(false); //impedimos el cancelamiento del dialogo
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.));//le damos un color de fondo transparente
        dialog.setContentView(R.layout.cuadro_cargando); //le asisganos el layout
        return dialog;
    }

    public void bloquerDesbloquearCliente(Boolean estadoBloqueado,
                                DialogInterface dialog,
                                Cliente cliente){
        Dialog cargando = new Utilidades().dialogCargar(context);
        cargando.show();

        Map<String,Object> bloqueoCli = new HashMap<>();
        String idClient_idVendedor = cliente.getIdCliente() +"_"+vendedorGlobal.getIdVendedor();
        Log.d("actualizar",cliente.getIdCliente()+"");
        bloqueoCli.put("Cliente/"+cliente.getIdCliente()+"/bloqueado",estadoBloqueado);
        bloqueoCli.put("Mensaje_Cliente_Vendedor/"+
                idClient_idVendedor+
                "/cliente/bloqueado",estadoBloqueado);
        bloqueoCli.put("Mensaje_Cliente_Vendedor/"+
                idClient_idVendedor+
                "/elVendedorBloqueoCliente",estadoBloqueado);
        bloquearCliente(cargando,bloqueoCli,estadoBloqueado);
    }

    private void bloquearCliente(Dialog cargando,Map<String,Object> bloqueoCli,Boolean estadoBloqueado){
        databaseReference.
                updateChildren(bloqueoCli).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        String mensaje = "";
                        if (estadoBloqueado){
                            mensaje = "Cliente bloqueado con éxito";
                        }else{
                            mensaje = "Cliente desbloqueado con éxito";
                        }
                        Toast.makeText(context,mensaje,Toast.LENGTH_LONG).show();
                        cargando.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,"A ocurrido un error al boquear el cliente",Toast.LENGTH_LONG).show();
            }
        });
    }
}
