package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activityParaClientes.ListarStreamingsVendedor;
import com.aplicacion.envivoapp.activityParaClientes.ListarVendedores;
import com.aplicacion.envivoapp.activityParaClientes.MensajeriaGlobal;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroCancelarPedidoCliente;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Usuario;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class AdapterGridPedidoCliente extends BaseAdapter implements CuadroCancelarPedidoCliente.resultadoDialogo {
    private Context context;
    private List<Pedido> listaPedidoCliente;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();

    public AdapterGridPedidoCliente(Context context,
                                    List<Pedido> listaPedidoCliente,
                                    DatabaseReference databaseReference,
                                    FirebaseStorage storage){
        this.context = context;
        this.listaPedidoCliente = listaPedidoCliente;
        this.databaseReference = databaseReference;
        this.storage=storage;
    }

    @Override
    public int getCount() {
        return listaPedidoCliente.size();
    }

    @Override
    public Object getItem(int position) {
        return listaPedidoCliente.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater layoutInflater =(LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_list_pedido_cliente,null);
        }

        Pedido pedido = listaPedidoCliente.get(position);//para manejar que elemento estamos clickeando
        //inicializamos las variables
        TextView codigo =  convertView.findViewById(R.id.txtCodigoItemPedidoCliente);
        TextView nombre = convertView.findViewById(R.id.txtNombreProductoItemPedidoCliente);
        TextView cantidad = convertView.findViewById(R.id.txtCantidadItemPedidoCliente);
        TextView precio= convertView.findViewById(R.id.txtPrecioItemPedidoCliente);
        TextView descripcion = convertView.findViewById(R.id.txtDescripcionItemPedidoCliente);
        TextView nombreVendedor = convertView.findViewById(R.id.txtNombreVendedorItemPedidoCliente);
        TextView fechaPedido = convertView.findViewById(R.id.txtFechaItemPedidoCliente);
        TextView fechaFinalPedido = convertView.findViewById(R.id.txtFechaFinalItemPedidoCliente);
        TextView colorFecha = convertView.findViewById(R.id.txtColorFechaVendedorItemPedidoCliente);
        ImageView imagenPedido = convertView.findViewById(R.id.imgPedidoCliente);
        Button btnCancelarPedido = convertView.findViewById(R.id.btnCancelarItemPedidoCliente);
        Button btnConversarVendedor = convertView.findViewById(R.id.btnConvesarVendedor);
        ImageView imgPerfilVendedor = convertView.findViewById(R.id.imgPerfilVendedorPedidoCliente);

        btnConversarVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle parametros = new Bundle();
                parametros.putString("vendedor", pedido.getIdVendedor());
                Intent streamingsIntent = new Intent(context, MensajeriaGlobal.class);
                streamingsIntent.putExtras(parametros);
                context.startActivity(streamingsIntent);
            }
        });

        databaseReference.child("Vendedor").child(pedido.getIdVendedor()).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Vendedor vendedor = snapshot.getValue(Vendedor.class);
                    codigo.setText(pedido.getCodigoProducto());
                    nombre.setText(pedido.getNombreProducto());
                    cantidad.setText(pedido.getCantidadProducto()+"");
                    precio.setText(pedido.getPrecioProducto()+"");
                    descripcion.setText(pedido.getDescripcionProducto());
                    try {
                        nombreVendedor.setText(encriptacionDatos.desencriptar(vendedor.getNombre()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    fechaPedido.setText(pedido.getFechaPedido().getDate() +"/"+pedido.getFechaPedido().getMonth()+"/"+pedido.getFechaPedido().getYear());
                    fechaFinalPedido.setText(pedido.getFechaFinalPedido().getDate() +"/"+pedido.getFechaFinalPedido().getMonth()+"/"+pedido.getFechaFinalPedido().getYear());
                    storage.getReference().child(pedido.getImagen()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.with(context).load(uri).into(imagenPedido);
                        }
                    });
                    //Cargamos la imagen del usuario
                    Query query = databaseReference.child("Usuario").orderByChild("uidUser").equalTo(vendedor.getUidUsuario());
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Usuario usuario = null;
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    usuario = ds.getValue(Usuario.class);

                                }
                                if (usuario != null) {
                                    Uri uri = Uri.parse(usuario.getImagen());
                                    Picasso.with(context).
                                            load(uri).
                                            resize(100, 100).
                                            into(imgPerfilVendedor);
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Le damos funcionalidad a los botones
        btnCancelarPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CuadroCancelarPedidoCliente(context,
                        pedido,
                        databaseReference,
                       AdapterGridPedidoCliente.this);//inciamos el cuadro de dialogo cancelar
            }
        });

        return convertView;

    }


    @Override
    public void resultado(Boolean isAcepatado, Boolean isCancelado) {

    }
}
