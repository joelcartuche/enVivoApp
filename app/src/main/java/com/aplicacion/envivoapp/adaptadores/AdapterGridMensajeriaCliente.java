package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Usuario;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AdapterGridMensajeriaCliente extends RecyclerView.Adapter<AdapterGridMensajeriaCliente.ViewHolder>  {

    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nombreClienteMensaje;
        public TextView fechaClienteMensaje ;
        public TextView mensajeClienteMensaje;
        public ImageView imagenPedido;
        public ImageView imagenUsuario;
        public CardView contenedorMensajeria;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreClienteMensaje = itemView.findViewById(R.id.txtItemNombreMensajeCliente);
            fechaClienteMensaje  = itemView.findViewById(R.id.txtItemFechaMensajeCliente);
            mensajeClienteMensaje = itemView.findViewById(R.id.txtItemMensajeCliente);
            imagenPedido = itemView.findViewById(R.id.bitmapCapturaPantallaMensajeriaVendedor);
            imagenUsuario = itemView.findViewById(R.id.imgItemMensajeCliente);
            contenedorMensajeria = itemView.findViewById(R.id.cardContenedorMensaje);
        }
    }

    private Context context;
    private List<Mensaje> listaMensajeCliente;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage ; //para la insersion de archivos

    public AdapterGridMensajeriaCliente(Context context,
                                        List<Mensaje> listaMensajeCliente,
                                        DatabaseReference databaseReference,
                                        FirebaseStorage storage ){
        this.context = context;
        this.listaMensajeCliente = listaMensajeCliente;
        this.databaseReference = databaseReference;
        this.storage = storage;
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return listaMensajeCliente.size();
    }

    @Override
    public AdapterGridMensajeriaCliente.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View vistaMensaje = inflater.inflate(R.layout.item_list_mensajeria_cliente,parent,false);
        AdapterGridMensajeriaCliente.ViewHolder viewHolder = new AdapterGridMensajeriaCliente.ViewHolder(vistaMensaje);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(AdapterGridMensajeriaCliente.ViewHolder view, int position) {

        Mensaje mensaje = listaMensajeCliente.get(position);//para manejar que elemento estamos clickeando
        //inicializamos las variables
        TextView nombreClienteMensaje = view.nombreClienteMensaje;
        TextView fechaClienteMensaje  = view.fechaClienteMensaje;
        TextView mensajeClienteMensaje = view.mensajeClienteMensaje;
        ImageView imagenPedido = view.imagenPedido;
        ImageView imagenUsuario = view.imagenUsuario;
        CardView contenedorMensajeria = view.contenedorMensajeria;

        imagenPedido.setVisibility(View.GONE);
        if (mensaje.getEsVededor()){//En caso de que el mensaje sea departe del vendedor
            imagenPedido.setVisibility(View.GONE);
            Vendedor vendedor = mensaje.getVendedor(); //instanciamos el cliente
            try {
                nombreClienteMensaje.setText(encriptacionDatos.desencriptar(vendedor.getNombre()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            fechaClienteMensaje.setText(mensaje.getFecha().getDate() +"/"+
                    mensaje.getFecha().getMonth()+"/"+mensaje.getFecha().getYear()+" "+
                    mensaje.getFecha().getHours()+":"+mensaje.getFecha().getMinutes()+":"+
                    mensaje.getFecha().getSeconds());
            mensajeClienteMensaje.setText(mensaje.getTexto());

            contenedorMensajeria.setCardBackgroundColor(Color.parseColor("#47C1FF"));


            //CArgamos la imagen del usuario
            Query query = databaseReference.child("Usuario").orderByChild("uidUser").equalTo(vendedor.getUidUsuario());
            query.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        Usuario usuario = null;
                        for (DataSnapshot ds: snapshot.getChildren()){
                            usuario = ds.getValue(Usuario.class);

                        }
                        if (usuario != null){
                            if (usuario.getImagen()!= null) {
                                Uri uri = Uri.parse(usuario.getImagen());
                                Picasso.with(context).load(uri).into(imagenUsuario);
                            }
                        }

                    }
                }
            });
        }else{//En caso de que elmensaje sea departe del cliente
            imagenPedido.setVisibility(View.GONE);
            Cliente cliente = mensaje.getCliente(); //instanciamos el cliente
            try {
                nombreClienteMensaje.setText(encriptacionDatos.desencriptar(cliente.getNombre()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            fechaClienteMensaje.setText(mensaje.getFecha().getDate() +"/"+
                    mensaje.getFecha().getMonth()+"/"+mensaje.getFecha().getYear()+" "+
                    mensaje.getFecha().getHours()+":"+mensaje.getFecha().getMinutes()+":"+
                    mensaje.getFecha().getSeconds());
            mensajeClienteMensaje.setText(mensaje.getTexto());
            //contenedorMensajeria.setBackgroundColor();
            contenedorMensajeria.setCardBackgroundColor(Color.parseColor("#556BFF"));

                        /*
                        if (mensaje.getImagen()!=null) {
                            storage.getReference().child(mensaje.getIdMensaje()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imagenPedido.setVisibility(View.VISIBLE);
                                    Picasso.with(context).load(uri).into(imagenPedido);
                                }
                            });
                        }
                        */

            //CArgamos la imagen del usuario
            Query query = databaseReference.child("Usuario").orderByChild("uidUser").equalTo(cliente.getUidUsuario());
            query.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        Usuario usuario = null;
                        for (DataSnapshot ds: snapshot.getChildren()){
                            usuario = ds.getValue(Usuario.class);

                        }
                        if (usuario != null){
                            if (usuario.getImagen()!= null) {
                                Uri uri = Uri.parse(usuario.getImagen());
                                Picasso.with(context).load(uri).into(imagenUsuario);
                            }
                        }

                    }
                }
            });

        }

    }


}
