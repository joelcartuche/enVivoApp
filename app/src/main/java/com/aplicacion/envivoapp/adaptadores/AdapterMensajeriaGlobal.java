package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Usuario;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterMensajeriaGlobal extends RecyclerView.Adapter<AdapterMensajeriaGlobal.ViewHolder>  {


    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();
    public class ViewHolder extends RecyclerView.ViewHolder {

        public  TextView nombreMensaje;
        public TextView fechaMensaje;
        public TextView mensajeGlobal;
        public ImageView imgMensajeriaGlobal;
        public ConstraintLayout contenedorMensajeria;
        public ImageView imgUsuario;


        public ViewHolder(View itemView) {
            super(itemView);
            nombreMensaje = itemView.findViewById(R.id.txtItemNombreMensajeGlobbal);
            fechaMensaje  = itemView.findViewById(R.id.txtItemFechaMensajeGlobal);
            mensajeGlobal = itemView.findViewById(R.id.txtItemMensajeGlobal);
            imgMensajeriaGlobal = itemView.findViewById(R.id.imgMensajeriaGlobal);
            contenedorMensajeria = itemView.findViewById(R.id.cardMensajeriaGlobal);
            imgUsuario = itemView.findViewById(R.id.imgItemMensajeGlobal);
        }
    }


    private Context context;
    private List<Mensaje> listaMensajeGlobal;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;

    public AdapterMensajeriaGlobal(Context context, List<Mensaje> listaMensajeGlobal, DatabaseReference databaseReference, FirebaseStorage storage){
        this.context = context;
        this.listaMensajeGlobal = listaMensajeGlobal;
        this.databaseReference = databaseReference;
        this.storage=storage;
    }

    @Override
    public AdapterMensajeriaGlobal.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View vistaMensaje = inflater.inflate(R.layout.item_list_mensajeria_global,parent,false);
        ViewHolder viewHolder = new ViewHolder(vistaMensaje);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(AdapterMensajeriaGlobal.ViewHolder convertView, int position) {
        Mensaje mensaje = listaMensajeGlobal.get(position);//para manejar que elemento estamos clickeando
        //inicializamos las variables
        TextView nombreMensaje = convertView.nombreMensaje;
        TextView fechaMensaje  = convertView.fechaMensaje;
        TextView mensajeGlobal = convertView.mensajeGlobal;
        ImageView imgMensajeriaGlobal = convertView.imgMensajeriaGlobal;
        ConstraintLayout contenedorMensajeria = convertView.contenedorMensajeria;
        ImageView imgUsuario = convertView.imgUsuario;

        if (mensaje.getImagen() == null){
            imgMensajeriaGlobal.setVisibility(View.GONE);
        }else{
            imgMensajeriaGlobal.setVisibility(View.VISIBLE);
//            mensaje.getImagen()
            storage.getReference().child(mensaje.getIdMensaje()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(context).load(uri).into(imgMensajeriaGlobal);
                }
            });
        }

        if (mensaje.getEsVededor()){//En caso de que el mensaje sea departe del vendedor

            Vendedor vendedor = mensaje.getVendedor();
            try {
                nombreMensaje.setText(encriptacionDatos.desencriptar(vendedor.getNombre()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            fechaMensaje.setText(mensaje.getFecha().getDate() +"/"+
                    mensaje.getFecha().getMonth()+"/"+mensaje.getFecha().getYear()+" "+
                    mensaje.getFecha().getHours()+":"+mensaje.getFecha().getMinutes()+":"+
                    mensaje.getFecha().getSeconds());
            mensajeGlobal.setText(mensaje.getTexto());

            contenedorMensajeria.setBackgroundColor(Color.parseColor("#47C1FF"));
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
                                Picasso.with(context).load(uri).into(imgUsuario);
                            }
                        }

                    }
                }
            });
        }else{//En caso de que elmensaje sea departe del cliente

            Cliente cliente = mensaje.getCliente(); //instanciamos el cliente
            try {
                nombreMensaje.setText(encriptacionDatos.desencriptar(cliente.getNombre()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            fechaMensaje.setText(mensaje.getFecha().getDate() +"/"+
                    mensaje.getFecha().getMonth()+"/"+mensaje.getFecha().getYear()+" "+
                    mensaje.getFecha().getHours()+":"+mensaje.getFecha().getMinutes()+":"+
                    mensaje.getFecha().getSeconds());
            mensajeGlobal.setText(mensaje.getTexto());
            mensajeGlobal.setTextDirection(View.TEXT_DIRECTION_RTL);

            contenedorMensajeria.setBackgroundColor(Color.parseColor("#556BFF"));

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
                            Uri uri = Uri.parse(usuario.getImagen());
                            Picasso.with(context).load(uri).resize(100,100).into(imgUsuario);
                        }

                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listaMensajeGlobal.size();
    }



}
