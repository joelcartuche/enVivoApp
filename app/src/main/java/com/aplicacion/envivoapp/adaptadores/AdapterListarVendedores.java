package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activityParaClientes.ListarVendedores;
import com.aplicacion.envivoapp.activityParaClientes.MensajeriaGlobal;
import com.aplicacion.envivoapp.modelos.Local;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdapterListarVendedores extends BaseAdapter {
    private List<Vendedor> listaVendedor;
    private Context mContext;
    private DatabaseReference reference;

    private List<Local> listLocal = new ArrayList<>();
    private AdapterListarLocal gridAdapterLocal;

    public AdapterListarVendedores(Context context, List<Vendedor> objects, DatabaseReference reference) {
        this.listaVendedor = objects;
        this.mContext = context;
        this.reference = reference;

    }


    @Override
    public int getCount() {
        return listaVendedor.size();
    }

    @Override
    public Object getItem(int position) {
        return listaVendedor.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position,  View convertView,  ViewGroup parent) {
        View view = convertView;
        if(view == null){ //adaptamos y pasamos el inflate
            view = LayoutInflater.from(mContext).inflate(R.layout.item_list_vendedores,null); //null como view grup
        }


        Vendedor vendedor = listaVendedor.get(position);//para manejar que elemento estamos clickeando
        //inicializamos las variables
        TextView nombreVendedor = view.findViewById(R.id.txtItemListVendedoresNombreVendedor);
        TextView telefonoVendedor = view.findViewById(R.id.txtItemListarVendedoresTelefonoVendedor);
        TextView celularVendedor = view.findViewById(R.id.txtItemListarVendedoresCelularVendedor);


        nombreVendedor.setText(vendedor.getNombre());
        telefonoVendedor.setText(vendedor.getTelefono());
        celularVendedor.setText(vendedor.getCelular());

        return view; //retornamos la vista
    }
}
