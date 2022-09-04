package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activitysParaVendedores.fragmentos.FragmentoMensajeriaGlobalVendedor;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroCambiarPedido;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroCancelarPedidoCliente;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroSeleccionarUbicacion;
import com.aplicacion.envivoapp.modelos.Calificaciones;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterGridPedidosCliente extends BaseAdapter implements CuadroCancelarPedidoCliente.resultadoDialogo,CuadroSeleccionarUbicacion.resultadoDialogo{

    private Context context;
    private List<Pedido> listaPedidoVendedor;

    private DatabaseReference databaseReference;

    private  FirebaseStorage storage;
    private EncriptacionDatos encriptacionDatos= new EncriptacionDatos();



    public AdapterGridPedidosCliente(Context context,
                                     List<Pedido> listaPedidoVendedor,
                                     DatabaseReference databaseReference,
                                     FirebaseStorage storage){
        this.context = context;
        this.listaPedidoVendedor = listaPedidoVendedor;
        this.databaseReference = databaseReference;

        this.storage = storage;

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
            convertView = layoutInflater.inflate(R.layout.item_list_pedidos_cliente,null);
        }

        Pedido pedido = listaPedidoVendedor.get(position);//para manejar que elemento estamos clickeando
        //inicializamos las variables
        TextView codigo =  convertView.findViewById(R.id.txtCodigoPedidosCliente);
        TextView nombre = convertView.findViewById(R.id.txtNombreProductoPedidosCliente);
        TextView cantidad = convertView.findViewById(R.id.txtCantidadPedidosCliente);
        TextView precio= convertView.findViewById(R.id.txtPrecioPedidosCliente);
        TextView descripcion = convertView.findViewById(R.id.txtDescripcionPedidosCliente);
        TextView nombreCliente = convertView.findViewById(R.id.txtNombreClientePedidosCliente);
        ImageView imagenPedido = convertView.findViewById(R.id.imgListPedidosCliente);
        CardView cardView = convertView.findViewById(R.id.cardItemPedidoVendor);
        ScrollView scrollView = convertView.findViewById(R.id.scrollListPedidoVendedor);
        imagenPedido.setVisibility(View.GONE);
        //hacemos que el scroll baje para que muestre los nuevos datos
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        //llenamos los textView con los datos correspondientes
        databaseReference.child("Cliente").child(pedido.getIdCliente()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Cliente cliente = snapshot.getValue(Cliente.class);
                    if (!cliente.getBloqueado()){
                        codigo.setText(pedido.getCodigoProducto());
                        nombre.setText(pedido.getNombreProducto());
                        cantidad.setText(pedido.getCantidadProducto() + "");
                        precio.setText(pedido.getPrecioProducto() + "");
                        descripcion.setText(pedido.getDescripcionProducto());
                        try {
                            nombreCliente.setText(encriptacionDatos.desencriptar(cliente.getNombre()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(pedido.getImagen()!=null) {
                            imagenPedido.setVisibility(View.VISIBLE);
                            storage.getReference().child(pedido.getImagen()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.with(context).load(uri).into(imagenPedido);
                                }
                            });
                        }
                    }else{
                        cardView.setVisibility(View.GONE);//para no mostrar los pedidos de clientes bloqueados
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return convertView;

    }


    @Override
    public void resultado(Boolean isAcepatado, Boolean isCancelado) {

    }



    @Override
    public void resultado(Boolean seActualizoCoordena) {
        //resultado del cuadro de dialogo para mostrar el mapa
    }
}
