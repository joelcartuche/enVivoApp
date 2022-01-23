package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Usuario;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterMensajeriaCliente extends RecyclerView.Adapter<AdapterMensajeriaCliente.ViewHolder> {
    private List<Mensaje> mList;
    private Context mContext;
    private int resourceLayout;
    private DatabaseReference databaseReference;

    public AdapterMensajeriaCliente( Context context,int resource,  List<Mensaje> objects,DatabaseReference reference) {
        this.mList = objects;
        this.mContext = context;
        this.resourceLayout = resource;
        this.databaseReference = reference;
    }
    @Override
    public AdapterMensajeriaCliente.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View vistaMensaje = inflater.inflate(R.layout.item_list_mensajeria_cliente,parent,false);
        AdapterMensajeriaCliente.ViewHolder viewHolder = new AdapterMensajeriaCliente.ViewHolder(vistaMensaje);
        return viewHolder;

    }





    @Override
    public int getItemCount() {
        return mList.size();
    }


    @Override
    public void onBindViewHolder( ViewHolder holder, int position) {

        Mensaje mensaje = mList.get(position);//para manejar que elemento estamos clickeando
        //inicializamos las variables
        TextView nombreClienteMensaje = holder.nombreClienteMensaje;
        TextView fechaClienteMensaje  = holder.fechaClienteMensaje;
        TextView mensajeClienteMensaje = holder.mensajeClienteMensaje;

        if (mensaje.getEsVededor()){//En caso de que el mensaje sea departe del vendedor

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
        }else{//En caso de que elmensaje sea departe del cliente

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
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView nombreClienteMensaje ;
        public TextView fechaClienteMensaje ;
        public TextView mensajeClienteMensaje ;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreClienteMensaje = itemView.findViewById(R.id.txtItemNombreMensajeCliente);
            fechaClienteMensaje  = itemView.findViewById(R.id.txtItemFechaMensajeCliente);
            mensajeClienteMensaje = itemView.findViewById(R.id.txtItemMensajeCliente);

        }
    }
}
