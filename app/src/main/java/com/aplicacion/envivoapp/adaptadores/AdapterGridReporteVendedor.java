package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class AdapterGridReporteVendedor extends BaseAdapter {

    private Context context;
    private ArrayList<String> listaData;
    private DatabaseReference databaseReference;


    public AdapterGridReporteVendedor(Context context,
                                      ArrayList<String> listaData){
        this.context = context;
        this.listaData = listaData;
    }


    @Override
    public int getCount() {
        return listaData.size();
    }

    @Override
    public Object getItem(int position) {
        return listaData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater layoutInflater =(LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_list_reportes,null);
        }

        TextView textView = convertView.findViewById(R.id.txtReporteVendedor);
        textView.setText(listaData.get(position));
        return convertView;
    }


}
