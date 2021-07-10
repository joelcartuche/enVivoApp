package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class AdapterMensajeriaCliente extends ArrayAdapter<Mensaje> {
    private List<Mensaje> mList;
    private Context mContext;
    private int resourceLayout;
    private DatabaseReference databaseReference;

    public AdapterMensajeriaCliente(@NonNull Context context,int resource, @NonNull List<Mensaje> objects,DatabaseReference reference) {
        super(context, resource, objects);
        this.mList = objects;
        this.mContext = context;
        this.resourceLayout = resource;
        this.databaseReference = reference;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if(view == null){ //adaptamos y pasamos el inflate
            view = LayoutInflater.from(mContext).inflate(R.layout.item_list_mensajeria_cliente,null); //null como view grup
        }


        Mensaje mensaje = mList.get(position);//para manejar que elemento estamos clickeando
        //inicializamos las variables
        TextView nombreClienteMensaje = view.findViewById(R.id.txtItemNombreMensajeCliente);
        TextView fechaClienteMensaje  = view.findViewById(R.id.txtItemFechaMensajeCliente);
        TextView mensajeClienteMensaje = view.findViewById(R.id.txtItemMensajeCliente);


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
        return view; //retornamos la vista
    }
}
