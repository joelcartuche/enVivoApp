package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activitysParaVendedores.fragmentos.FragmentoPedidoVendedor;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroEditarProducto;
import com.aplicacion.envivoapp.modelos.Producto;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterGridProductosVendedor extends RecyclerView.Adapter<AdapterGridProductosVendedor.ViewHolder> {


    private List<Producto> listProductos;
    private Context context;
    private FirebaseStorage storage;
    private FragmentActivity fragmentActivity;
    private String actividadDeLaQueViene;
    private DatabaseReference databaseReference;

    public AdapterGridProductosVendedor(
            Context context,
            List<Producto> listProductos,
            FirebaseStorage storage,
            FragmentActivity fragmentActivity,
            String actividadDeLaQueViene,
            DatabaseReference databaseReference) {
        this.listProductos = listProductos;
        this.context = context;
        this.storage = storage;
        this.fragmentActivity = fragmentActivity;
        this.actividadDeLaQueViene = actividadDeLaQueViene;
        this.databaseReference = databaseReference;
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
                ((MyFirebaseApp) context.getApplicationContext()).setIdProducto(producto.getIdProducto());
                ((MyFirebaseApp) context.getApplicationContext()).setActividadDeLaQueViene(actividadDeLaQueViene);

                Fragment fragment = new CuadroEditarProducto();
                fragmentActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                        .replace(R.id.home_content_vendedor, fragment)
                        .commit();
            }
        });

        btnEliminarProductoItemProductosVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Utilidades util = new Utilidades();
                util.cuadroDialogo(context, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eliminarProducto(producto.getIdProducto());
                    }
                },"ELIMINAR","¿Esta seguro de eliminar el producto?");

            }
        });

    }

    private void eliminarProducto(String idProducto) {
        Map<String,Object> productoActualizacion= new HashMap<>();
        try {
            productoActualizacion.put("esEliminado",true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        databaseReference.child("Producto").child(idProducto).updateChildren(productoActualizacion).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context,"Producto eliminado con éxito",Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,"Error al eliminar producto",Toast.LENGTH_LONG).show();
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
