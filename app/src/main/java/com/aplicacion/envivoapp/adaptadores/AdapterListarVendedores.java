package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.modelos.VideoStreaming;

import java.util.List;

public class AdapterListarVendedores extends ArrayAdapter<Vendedor> {
    private List<Vendedor> mList;
    private Context mContext;
    private int resourceLayout;

    public AdapterListarVendedores(@NonNull Context context, int resource, @NonNull List<Vendedor> objects) {
        super(context, resource, objects);
        this.mList = objects;
        this.mContext = context;
        this.resourceLayout = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if(view == null){ //adaptamos y pasamos el inflate
            view = LayoutInflater.from(mContext).inflate(R.layout.item_list_vendedores,null); //null como view grup
        }


        Vendedor vendedor = mList.get(position);//para manejar que elemento estamos clickeando
        //inicializamos las variables
        TextView nombreVendedor = view.findViewById(R.id.txtItemListVendedoresNombreVendedor);
        TextView telefonoVendedor = view.findViewById(R.id.txtItemListarVendedoresTelefonoVendedor);
        TextView celularVendedor = view.findViewById(R.id.txtItemListarVendedoresCelularVendedor);
        TextView tineLocalVendedor = view.findViewById(R.id.txtItemListarVendedoresTieneLocalVendedor);


        nombreVendedor.setText(vendedor.getNombre());
        telefonoVendedor.setText(vendedor.getTelefono());
        celularVendedor.setText(vendedor.getCelular());

        if(vendedor.isTieneTienda()){
            tineLocalVendedor.setText("Si");
        }else{
            tineLocalVendedor.setText("No");
        }


        return view; //retornamos la vista
    }
}
