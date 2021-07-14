package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroCancelarPedidoCliente;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class AdapterGridPedidoVendedor extends BaseAdapter implements CuadroCancelarPedidoCliente.resultadoDialogo{

    private Context context;
    private List<Pedido> listaPedidoVendedor;
    private DatabaseReference databaseReference;

    public AdapterGridPedidoVendedor(Context context,
                                    List<Pedido> listaPedidoVendedor,
                                    DatabaseReference databaseReference){
        this.context = context;
        this.listaPedidoVendedor = listaPedidoVendedor;
        this.databaseReference = databaseReference;
    }

    @Override
    public int getCount() {
        return listaPedidoVendedor.size();
    }

    @Override
    public Object getItem(int position) {
        return listaPedidoVendedor.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater layoutInflater =(LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_list_pedido_vendedor,null);
        }

        Pedido pedido = listaPedidoVendedor.get(position);//para manejar que elemento estamos clickeando
        //inicializamos las variables
        TextView codigo =  convertView.findViewById(R.id.txtCodigoItemPedidoVendedor);
        TextView nombre = convertView.findViewById(R.id.txtNombreProductoItemPedidoVendedor);
        TextView cantidad = convertView.findViewById(R.id.txtCantidadItemPedidoVendedor);
        TextView precio= convertView.findViewById(R.id.txtPrecioItemPedidoVendedor);
        TextView descripcion = convertView.findViewById(R.id.txtDescripcionItemPedidoVendedor);
        TextView nombreCliente = convertView.findViewById(R.id.txtNombreClienteItemPedidoVendedor);
        Button btnCambiarPedido = convertView.findViewById(R.id.btnCambiarPedidoVendedor);
        Button btnCancelarPedido = convertView.findViewById(R.id.btnCancelarItemPedidoVendedor);

        databaseReference.child("Cliente").child(pedido.getIdCliente()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    codigo.setText(pedido.getCodigoProducto());
                    nombre.setText(pedido.getNombreProducto());
                    cantidad.setText(pedido.getCantidadProducto()+"");
                    precio.setText(pedido.getPrecioProducto()+"");
                    descripcion.setText(pedido.getDescripcionProducto());
                    nombreCliente.setText(snapshot.getValue(Cliente.class).getNombre());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Le damos funcionalidad a los botones
        btnCancelarPedido.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {/*
                new CuadroCancelarPedidoCliente(context,
                        pedido,
                        databaseReference,
                        AdapterGridPedidoVendedor.this);//inciamos el cuadro de dialogo cancelar
                        */
            }
        });

        return convertView;

    }


    @Override
    public void resultado(Boolean isAcepatado, Boolean isCancelado) {

    }
}
