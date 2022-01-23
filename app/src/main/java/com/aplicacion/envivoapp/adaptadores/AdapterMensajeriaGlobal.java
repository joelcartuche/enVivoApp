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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterMensajeriaGlobal extends RecyclerView.Adapter<AdapterMensajeriaGlobal.ViewHolder>  {

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
            fechaMensaje  = itemView.findViewById(R.id.txtItemFechaMensajeCliente);
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
            storage.getReference().child(mensaje.getIdMensaje()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(context).load(uri).into(imgMensajeriaGlobal);
                }
            });
        }

        if (mensaje.getEsVededor()){//En caso de que el mensaje sea departe del vendedor
            databaseReference.child("Vendedor").child(mensaje.getIdvendedor()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){

                        Vendedor vendedor = snapshot.getValue(Vendedor.class); //instanciamos el cliente
                        nombreMensaje.setText(vendedor.getNombre());
                        fechaMensaje.setText(mensaje.getFecha().getDate() +"/"+
                                mensaje.getFecha().getMonth()+"/"+mensaje.getFecha().getYear()+" "+
                                mensaje.getFecha().getHours()+":"+mensaje.getFecha().getMinutes()+":"+
                                mensaje.getFecha().getSeconds());
                        mensajeGlobal.setText(mensaje.getTexto());

                        contenedorMensajeria.setBackgroundColor(Color.parseColor("#47C1FF"));

                        databaseReference.child("Usuario").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    Usuario usuario = null;
                                    for (DataSnapshot ds: snapshot.getChildren()){
                                        Usuario usuarioAux = ds.getValue(Usuario.class);
                                        if (usuarioAux.getUidUser().equals(vendedor.getUidUsuario())){
                                            usuario = usuarioAux;
                                            break;
                                        }
                                    }
                                    if (usuario != null){
                                        Uri uri = Uri.parse(usuario.getImagen());
                                        Picasso.with(context).load(uri).resize(100,100).into(imgUsuario);
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }else{
                        Log.d("ERROR","error en encontrar el vendedor para AdapterMensajeriaCliente");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{//En caso de que elmensaje sea departe del cliente
            databaseReference.child("Cliente").child(mensaje.getIdcliente()).addValueEventListener(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Cliente cliente = snapshot.getValue(Cliente.class); //instanciamos el cliente
                        nombreMensaje.setText(cliente.getNombre());
                        fechaMensaje.setText(mensaje.getFecha().getDate() +"/"+
                                mensaje.getFecha().getMonth()+"/"+mensaje.getFecha().getYear()+" "+
                                mensaje.getFecha().getHours()+":"+mensaje.getFecha().getMinutes()+":"+
                                mensaje.getFecha().getSeconds());
                        mensajeGlobal.setText(mensaje.getTexto());
                        mensajeGlobal.setTextDirection(View.TEXT_DIRECTION_RTL);

                        contenedorMensajeria.setBackgroundColor(Color.parseColor("#556BFF"));

                        databaseReference.child("Usuario").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    Usuario usuario = null;
                                    for (DataSnapshot ds: snapshot.getChildren()){
                                        Usuario usuarioAux = ds.getValue(Usuario.class);
                                        if (usuarioAux.getUidUser().equals(cliente.getUidUsuario())){
                                            usuario = usuarioAux;
                                        }
                                    }
                                    if (usuario != null){
                                        Uri uri = Uri.parse(usuario.getImagen());
                                        Picasso.with(context).load(uri).resize(100,100).into(imgUsuario);
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }else{
                        Log.d("ERROR","error en encontrar el cliente para AdapterMensajeriaCliente");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listaMensajeGlobal.size();
    }



    /*
    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
     */

}
