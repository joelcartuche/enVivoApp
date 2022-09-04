package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Producto;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AdapterGridProductosVendedor extends RecyclerView.Adapter<AdapterGridProductosVendedor.ViewHolder> {


    private List<Producto> listProductos;
    private Context context;
    private FirebaseStorage storage;

    public AdapterGridProductosVendedor(
            Context context,
            List<Producto> listProductos,
            FirebaseStorage storage) {
        this.listProductos = listProductos;
        this.context = context;
        this.storage = storage;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View vistaMensaje = inflater.inflate(R.layout.item_lista_productos_vendedor,parent,false);
        AdapterGridProductosVendedor.ViewHolder viewHolder = new AdapterGridProductosVendedor.ViewHolder(vistaMensaje);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        TextView txtCodigoItemProductosVendedor = holder.txtCodigoItemProductosVendedor;
        TextView txtNombreProductoItemProductosVendedor= holder.txtNombreProductoItemProductosVendedor;
        TextView txtCantidadItemProductosVendedor= holder.txtCantidadItemProductosVendedor;
        TextView txtPrecioItemProductosVendedor= holder.txtPrecioItemProductosVendedor;
        TextView txtDescripcionItemProductosVendedor= holder.txtDescripcionItemProductosVendedor;
        ImageView imgItemProductosVendedor= holder.imgItemProductosVendedor;
        Button btnEditarProductoItemProductosVendedor= holder.btnEditarProductoItemProductosVendedor;
        Button btnEliminarProductoItemProductosVendedor = holder.btnEliminarProductoItemProductosVendedor;

        Producto producto = listProductos.get(position);



        txtCodigoItemProductosVendedor.setText(producto.getCodigoProducto());
        txtNombreProductoItemProductosVendedor.setText(producto.getNombreProducto());
        txtPrecioItemProductosVendedor.setText(producto.getPrecioProducto()+"");
        txtCantidadItemProductosVendedor.setText(producto.getCantidadProducto()+"");
        txtDescripcionItemProductosVendedor.setText(producto.getDescripcionProducto());

        imgItemProductosVendedor.setVisibility(View.GONE);

        //analizamos si existe una imagen del pedido
        if (producto.getImagen() == null){
            imgItemProductosVendedor.setVisibility(View.GONE);
        }else{
            imgItemProductosVendedor.setVisibility(View.VISIBLE);

            storage.getReference().child(producto.getCodigoProducto()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(context).load(uri).into(imgItemProductosVendedor);
                }
            });
        }


        btnEditarProductoItemProductosVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnEliminarProductoItemProductosVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return listProductos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCodigoItemProductosVendedor ,
                txtNombreProductoItemProductosVendedor,
                txtCantidadItemProductosVendedor,
                txtPrecioItemProductosVendedor,
                txtDescripcionItemProductosVendedor;
        ImageView imgItemProductosVendedor;
        Button btnEditarProductoItemProductosVendedor,btnEliminarProductoItemProductosVendedor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCodigoItemProductosVendedor = itemView.findViewById(R.id.txtCodigoItemProductosVendedor);
            txtNombreProductoItemProductosVendedor= itemView.findViewById(R.id.txtNombreProductoItemProductosVendedor);
            txtCantidadItemProductosVendedor= itemView.findViewById(R.id.txtCantidadItemProductosVendedor);
            txtPrecioItemProductosVendedor= itemView.findViewById(R.id.txtPrecioItemProductosVendedor);
            txtDescripcionItemProductosVendedor= itemView.findViewById(R.id.txtDescripcionItemProductosVendedor);
            imgItemProductosVendedor= itemView.findViewById(R.id.imgItemProductosVendedor);
            btnEditarProductoItemProductosVendedor= itemView.findViewById(R.id.btnEditarProductoItemProductosVendedor);
            btnEliminarProductoItemProductosVendedor = itemView.findViewById(R.id.btnEliminarProductoItemProductosVendedor);
        }
    }
}
