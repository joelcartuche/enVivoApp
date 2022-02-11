package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class AdapterGridReporteVendedor extends RecyclerView.Adapter<AdapterGridReporteVendedor.ViewHolder> {

    private Context context;
    private ArrayList<Pedido> listaData;
    private DatabaseReference databaseReference;


    public AdapterGridReporteVendedor(Context context,
                                      ArrayList<Pedido> listaData){
        this.context = context;
        this.listaData = listaData;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View vistaMensaje = inflater.inflate(R.layout.item_list_reportes,parent,false);
        AdapterGridReporteVendedor.ViewHolder viewHolder = new AdapterGridReporteVendedor.ViewHolder(vistaMensaje);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(AdapterGridReporteVendedor.ViewHolder holder, int position) {
        TextView codigo = holder.codigo;
        TextView nombre = holder.nombre;
        TextView precio= holder.precio;
        TextView cantidad= holder.cantidad;
        TextView descripcion = holder.descripcion;
        TextView fecha= holder.fecha;
        TextView hora= holder.hora;
        LinearLayout horizontalScrollView = holder.horizontalScrollView;

        Pedido pedido = listaData.get(position);

        if (position >0){
            horizontalScrollView.setVisibility(View.GONE);
        }

        Log.w("Pedido {",pedido.getNombreProducto()+" "+ pedido.getCodigoProducto());
        codigo.setText(pedido.getCodigoProducto());
        nombre.setText(pedido.getNombreProducto());
        precio.setText(""+pedido.getPrecioProducto());
        cantidad.setText(pedido.getCantidadProducto()+"");

        descripcion.setText(pedido.getDescripcionProducto());
        fecha.setText(pedido.getFechaPedido().getDate()+"/"+pedido.getFechaPedido().getMonth()+"/"+pedido.getFechaPedido().getYear());
        hora.setText(pedido.getFechaPedido().getHours()+":"+pedido.getFechaPedido().getMinutes());

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return listaData.size();
    }




    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView codigo ;
        TextView nombre ;
        TextView precio;
        TextView cantidad;
        TextView descripcion;
        TextView fecha;
        TextView hora;
        LinearLayout horizontalScrollView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
             codigo = itemView.findViewById(R.id.txtCodigoProductoReporteVendedor);
             nombre = itemView.findViewById(R.id.txtNombreProductoReporteVendedor);
             precio= itemView.findViewById(R.id.txtPrecioProductoReporteVendedor);
             cantidad= itemView.findViewById(R.id.txtCantidadProductoReporteVendedor);
             descripcion = itemView.findViewById(R.id.txtDescripcionProductoReporteVendedor);
             fecha= itemView.findViewById(R.id.txtFechaProductoReporteVendedor);
             hora= itemView.findViewById(R.id.txtHoraProductoReporteVendedor);
             horizontalScrollView = itemView.findViewById(R.id.scrollHorizontalTituloReporte);
        }
    }
}
