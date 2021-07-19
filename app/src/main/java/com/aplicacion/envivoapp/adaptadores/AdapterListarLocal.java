package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Local;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class AdapterListarLocal extends BaseAdapter {
    private Context context;
    private List<Local> listaLocal;
    private DatabaseReference databaseReference;

    public AdapterListarLocal(Context context,
                                 List<Local> listarLocal,
                                 DatabaseReference databaseReference){
        this.context = context;
        this.listaLocal = listarLocal;
        this.databaseReference = databaseReference;
    }

    @Override
    public int getCount() {
        return listaLocal.size();
    }

    @Override
    public Object getItem(int position) {
        return listaLocal.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater layoutInflater =(LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_list_local,null);
        }

        Local local = listaLocal.get(position);//para manejar que elemento estamos clickeando
        //inicializamos las variables
        TextView nombre= convertView.findViewById(R.id.txtNombreLocal);
        TextView direccion = convertView.findViewById(R.id.txtDireccionLocal);
        TextView telefono = convertView.findViewById(R.id.txtTelefonoLocal);
        TextView celular = convertView.findViewById(R.id.txtCelularLocal);

        nombre.setText(local.getNombre());
        direccion.setText(local.getDireccion());
        telefono.setText(local.getTelefono());
        celular.setText(local.getCelular());

        return convertView;
    }
}
