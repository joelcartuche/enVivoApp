package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Comentario;
import com.aplicacion.envivoapp.modelos.Producto;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class AdapteGridComentariosVendedor extends RecyclerView.Adapter<AdapteGridComentariosVendedor.ViewHolder> {

    private List<Comentario> listComentario;
    private Context context;
    private EncriptacionDatos encrypt = new EncriptacionDatos();

    public AdapteGridComentariosVendedor(
            Context context,
            List<Comentario> listComentario) {
        this.listComentario = listComentario;
        this.context = context;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View vistaMensaje = inflater.inflate(R.layout.item_lista_comentarios_vendedor,parent,false);
        AdapteGridComentariosVendedor.ViewHolder viewHolder = new AdapteGridComentariosVendedor.ViewHolder(vistaMensaje);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView tvNombreClienteComentarioVendedor = holder.tvNombreClienteComentarioVendedor;
        TextView mensajeComentariosVendedor = holder.mensajeComentariosVendedor;
        Comentario comentario = listComentario.get(position);

        try {
            tvNombreClienteComentarioVendedor.setText(encrypt.desencriptar(comentario.getCliente().getNombre()));
            mensajeComentariosVendedor.setText(comentario.getMensaje());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public int getItemCount() {
        return listComentario.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView tvNombreClienteComentarioVendedor,mensajeComentariosVendedor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreClienteComentarioVendedor = itemView.findViewById(R.id.tvNombreClienteComentarioVendedor);
            mensajeComentariosVendedor = itemView.findViewById(R.id.mensajeComentariosVendedor);
        }
    }
}
