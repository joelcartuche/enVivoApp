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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

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

public class AdapterMensajeriaGlobal extends BaseAdapter {

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
    public int getCount() {
        return listaMensajeGlobal.size();
    }

    @Override
    public Object getItem(int position) {
        return listaMensajeGlobal.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater layoutInflater =(LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_list_mensajeria_global,null);
        }

        Mensaje mensaje = listaMensajeGlobal.get(position);//para manejar que elemento estamos clickeando
        //inicializamos las variables
        TextView nombreMensaje = convertView.findViewById(R.id.txtItemNombreMensajeGlobbal);
        TextView fechaMensaje  = convertView.findViewById(R.id.txtItemFechaMensajeCliente);
        TextView mensajeGlobal = convertView.findViewById(R.id.txtItemMensajeGlobal);
        ImageView imgMensajeriaGlobal = convertView.findViewById(R.id.imgMensajeriaGlobal);
        ConstraintLayout contenedorMensajeria = convertView.findViewById(R.id.cardMensajeriaGlobal);
        ImageView imgUsuario = convertView.findViewById(R.id.imgItemMensajeGlobal);

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

        return convertView;
    }
}
