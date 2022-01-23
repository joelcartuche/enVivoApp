package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Usuario;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AdapterGridMensajeriaCliente extends RecyclerView.Adapter<AdapterGridMensajeriaCliente.ViewHolder>  {

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nombreClienteMensaje;
        public TextView fechaClienteMensaje ;
        public TextView mensajeClienteMensaje;
        public ImageView imagenPedido;
        public ImageView imagenUsuario;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreClienteMensaje = itemView.findViewById(R.id.txtItemNombreMensajeCliente);
            fechaClienteMensaje  = itemView.findViewById(R.id.txtItemFechaMensajeCliente);
            mensajeClienteMensaje = itemView.findViewById(R.id.txtItemMensajeCliente);
            imagenPedido = itemView.findViewById(R.id.bitmapCapturaPantallaMensajeriaVendedor);
            imagenUsuario = itemView.findViewById(R.id.imgItemMensajeCliente);
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
        imagenPedido.setVisibility(View.GONE);

        if (mensaje.getEsVededor()){//En caso de que el mensaje sea departe del vendedor
            databaseReference.child("Vendedor").child(mensaje.getIdvendedor()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        imagenPedido.setVisibility(View.GONE);
                        Vendedor vendedor = snapshot.getValue(Vendedor.class); //instanciamos el cliente
                        nombreClienteMensaje.setText(vendedor.getNombre());
                        fechaClienteMensaje.setText(mensaje.getFecha().getDate() +"/"+
                                mensaje.getFecha().getMonth()+"/"+mensaje.getFecha().getYear()+" "+
                                mensaje.getFecha().getHours()+":"+mensaje.getFecha().getMinutes()+":"+
                                mensaje.getFecha().getSeconds());
                        mensajeClienteMensaje.setText(mensaje.getTexto());

                        //CArgamos la imagen del usuario
                        databaseReference.child("Usuario").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    Usuario usuario = null;
                                    for (DataSnapshot ds: snapshot.getChildren()){
                                        Usuario usuarioAux = ds.getValue(Usuario.class);
                                        if (usuarioAux.getUidUser().equals(vendedor.getUidUsuario())){
                                            usuario = usuarioAux;
                                        }
                                    }
                                    if (usuario != null){
                                        if (usuario.getImagen()!= null) {
                                            Uri uri = Uri.parse(usuario.getImagen());
                                            Picasso.with(context).load(uri).into(imagenUsuario);
                                        }
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
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        imagenPedido.setVisibility(View.GONE);
                        Cliente cliente = snapshot.getValue(Cliente.class); //instanciamos el cliente
                        nombreClienteMensaje.setText(cliente.getNombre());
                        fechaClienteMensaje.setText(mensaje.getFecha().getDate() +"/"+
                                mensaje.getFecha().getMonth()+"/"+mensaje.getFecha().getYear()+" "+
                                mensaje.getFecha().getHours()+":"+mensaje.getFecha().getMinutes()+":"+
                                mensaje.getFecha().getSeconds());
                        mensajeClienteMensaje.setText(mensaje.getTexto());

                        storage.getReference().child(mensaje.getIdMensaje()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                imagenPedido.setVisibility(View.VISIBLE);
                                Picasso.with(context).load(uri).into(imagenPedido);
                            }
                        });


                        //CArgamos la imagen del usuario
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
                                        Picasso.with(context).load(uri).into(imagenUsuario);
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
