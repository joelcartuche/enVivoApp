package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Usuario;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterMensajeriaGlobalVendedores extends RecyclerView.Adapter<AdapterMensajeriaGlobalVendedores.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView nombreMensaje;
        TextView fechaMensaje;
        TextView mensajeGlobal;
        ConstraintLayout contenedorMensajeria;
        ImageView imgMensajeriaGlobal;
        ImageView imgUsuario;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreMensaje = itemView.findViewById(R.id.txtItemNombreMensajeGlobbal);
            fechaMensaje  = itemView.findViewById(R.id.txtItemFechaMensajeCliente);
            mensajeGlobal = itemView.findViewById(R.id.txtItemMensajeGlobal);
            contenedorMensajeria = itemView.findViewById(R.id.cardMensajeriaGlobal);
            imgMensajeriaGlobal = itemView.findViewById(R.id.imgMensajeriaGlobal);
            imgUsuario = itemView.findViewById(R.id.imgItemMensajeGlobal);
        }
    }

    private Context context;
    private List<Mensaje> listaMensajeGlobal;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private FirebaseAuth firebaseAuth;

    public AdapterMensajeriaGlobalVendedores(Context context,
                                             List<Mensaje> listaMensajeGlobal,
                                             DatabaseReference databaseReference,
                                             FirebaseStorage storage,
                                             FirebaseAuth firebaseAuth){
        this.context = context;
        this.listaMensajeGlobal = listaMensajeGlobal;
        this.databaseReference = databaseReference;
        this.storage = storage;
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public int getItemCount() {
        return listaMensajeGlobal.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public AdapterMensajeriaGlobalVendedores.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View vistaMensaje = inflater.inflate(R.layout.item_list_mensajeria_global,parent,false);
        AdapterMensajeriaGlobalVendedores.ViewHolder viewHolder = new AdapterMensajeriaGlobalVendedores.ViewHolder(vistaMensaje);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(AdapterMensajeriaGlobalVendedores.ViewHolder convertView, int position) {


        Mensaje mensaje = listaMensajeGlobal.get(position);//para manejar que elemento estamos clickeando
        //inicializamos las variables
        TextView nombreMensaje = convertView.nombreMensaje;
        TextView fechaMensaje  = convertView.fechaMensaje;
        TextView mensajeGlobal = convertView.mensajeGlobal;
        ConstraintLayout contenedorMensajeria = convertView.contenedorMensajeria;
        ImageView imgMensajeriaGlobal = convertView.imgMensajeriaGlobal;
        ImageView imgUsuario = convertView.imgUsuario;



        if (mensaje.getImagen() == null){
         imgMensajeriaGlobal.setVisibility(View.GONE);
        }else {
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
                        contenedorMensajeria.setBackgroundColor(Color.parseColor("#556BFF"));

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
                                        Picasso.with(context).load(uri).into(imgUsuario);
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
                            fechaMensaje.setText(mensaje.getFecha().getDate() + "/" +
                                    mensaje.getFecha().getMonth() + "/" + mensaje.getFecha().getYear() + " " +
                                    mensaje.getFecha().getHours() + ":" + mensaje.getFecha().getMinutes() + ":" +
                                    mensaje.getFecha().getSeconds());
                            mensajeGlobal.setText(mensaje.getTexto());
                            mensajeGlobal.setTextDirection(View.TEXT_DIRECTION_RTL);
                            contenedorMensajeria.setBackgroundColor(Color.parseColor("#47C1FF"));

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
                                        Picasso.with(context).load(uri).into(imgUsuario);
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
}
