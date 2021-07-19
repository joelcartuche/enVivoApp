package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class AdapterMensajeriaGlobal extends BaseAdapter {

    private Context context;
    private List<Mensaje> listaMensajeGlobal;
    private DatabaseReference databaseReference;

    public AdapterMensajeriaGlobal(Context context, List<Mensaje> listaMensajeGlobal, DatabaseReference databaseReference){
        this.context = context;
        this.listaMensajeGlobal = listaMensajeGlobal;
        this.databaseReference = databaseReference;
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
