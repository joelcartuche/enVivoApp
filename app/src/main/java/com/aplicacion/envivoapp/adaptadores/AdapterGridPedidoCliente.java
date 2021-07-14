package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.util.Log;
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
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class AdapterGridPedidoCliente extends BaseAdapter implements CuadroCancelarPedidoCliente.resultadoDialogo {
    private Context context;
    private List<Pedido> listaPedidoCliente;
    private DatabaseReference databaseReference;

    public AdapterGridPedidoCliente(Context context,
                                    List<Pedido> listaPedidoCliente,
                                    DatabaseReference databaseReference){
        this.context = context;
        this.listaPedidoCliente = listaPedidoCliente;
        this.databaseReference = databaseReference;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater layoutInflater =(LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_list_pedido_cliente,null);
        }

        Pedido pedido = listaPedidoCliente.get(position);//para manejar que elemento estamos clickeando
        //inicializamos las variables
        TextView codigo =  convertView.findViewById(R.id.txtCantidadItemPedidoCliente);
        TextView nombre = convertView.findViewById(R.id.txtNombreProductoItemPedidoCliente);
        TextView cantidad = convertView.findViewById(R.id.txtCantidadItemPedidoCliente);
        TextView precio= convertView.findViewById(R.id.txtPrecioItemPedidoCliente);
        TextView descripcion = convertView.findViewById(R.id.txtDescripcionItemPedidoCliente);
        TextView nombreVendedor = convertView.findViewById(R.id.txtNombreVendedorItemPedidoCliente);
        Button btnCambiarPedido = convertView.findViewById(R.id.btnCambiarPedidoCliente);
        Button btnCancelarPedido = convertView.findViewById(R.id.btnCancelarItemPedidoCliente);

        databaseReference.child("Vendedor").child(pedido.getIdVendedor()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    codigo.setText(pedido.getCodigoProducto());
                    nombre.setText(pedido.getNombreProducto());
                    cantidad.setText(pedido.getCantidadProducto()+"");
                    precio.setText(pedido.getPrecioProducto()+"");
                    descripcion.setText(pedido.getDescripcionProducto());
                    nombreVendedor.setText(snapshot.getValue(Vendedor.class).getNombre());
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
