package com.aplicacion.envivoapp.adaptadores;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activityParaClientes.ListarStreamingsVendedor;
import com.aplicacion.envivoapp.activityParaClientes.ListarVendedores;
import com.aplicacion.envivoapp.activitysParaVendedores.ListarClientes;
import com.aplicacion.envivoapp.activitysParaVendedores.MensajeriaGlobalVendedor;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroCambiarPedido;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroCancelarPedidoCliente;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroEditarLocal;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroSeleccionarUbicacion;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.common.api.Api;
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

public class AdapterGridPedidoVendedor extends BaseAdapter implements CuadroCancelarPedidoCliente.resultadoDialogo,CuadroSeleccionarUbicacion.resultadoDialogo{

    private Context context;
    private List<Pedido> listaPedidoVendedor;
    private DatabaseReference databaseReference;
    private  Boolean eliminado;
    private  FirebaseStorage storage;
    private EncriptacionDatos encriptacionDatos= new EncriptacionDatos();

    public AdapterGridPedidoVendedor(Context context,
                                     List<Pedido> listaPedidoVendedor,
                                     DatabaseReference databaseReference,
                                     FirebaseStorage storage){
        this.context = context;
        this.listaPedidoVendedor = listaPedidoVendedor;
        this.databaseReference = databaseReference;
        this.eliminado = eliminado;
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
        Button btnPagado = convertView.findViewById(R.id.btnPagadoPedidoVendedor);
        Button btnHabilitarPedido = convertView.findViewById(R.id.btnHabilitarPedidoVendedor);
        Button btnEliminarPedido = convertView.findViewById(R.id.btnEliminarItemPedidoVendedor);
        ImageView imagenPedido = convertView.findViewById(R.id.imgListPedidoVendedor);
        Button btnConversarComprador = convertView.findViewById(R.id.btnConversarVendedorListPedidoVendedor);
        Button btnVerUbicacion = convertView.findViewById(R.id.btnDireccionClienteListPedidoV);
        CardView cardView = convertView.findViewById(R.id.cardItemPedidoVendor);
        ScrollView scrollView = convertView.findViewById(R.id.scrollListPedidoVendedor);

        //hacemos que el scroll baje para que muestre los nuevos datos
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        //damos funcion al boton conversar comprador
        conversarComprador(btnConversarComprador,position,context);

        //funcionalidad para el boton ver ubicación
        btnVerUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child("Cliente").child(pedido.getIdCliente()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Cliente cliente = snapshot.getValue(Cliente.class);

                            if (cliente!= null){
                                LatLng aux = new LatLng(cliente.getLatitud(),cliente.getLongitud());
                                try {
                                    cliente.setNombre(encriptacionDatos.desencriptar(cliente.getNombre()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                new CuadroSeleccionarUbicacion(context,aux,true,"Nombre: "+cliente.getNombre(),AdapterGridPedidoVendedor.this::resultado,false);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });




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

        btnPagado.setVisibility(View.GONE);//escondemos el boton de pagado
        btnHabilitarPedido.setVisibility(View.GONE);
        btnCambiarPedido.setVisibility(View.GONE);
        btnEliminarPedido.setVisibility(View.GONE);
        btnCancelarPedido.setVisibility(View.GONE);
        imagenPedido.setVisibility(View.GONE);

        if (pedido.getAceptado()&& !pedido.getPagado() && !pedido.getCancelado()) {//en caso de ser aceptado el pedido mostramos el boton de pagado
            btnCambiarPedido.setVisibility(View.VISIBLE);
            btnPagado.setVisibility(View.VISIBLE);
            btnCancelarPedido.setVisibility(View.VISIBLE);
            btnHabilitarPedido.setVisibility(View.GONE);
            btnEliminarPedido.setVisibility(View.GONE);

            btnCancelarPedido.setText("Cancelar pedido");
            //fucion de pedido aceptado
            pedidoAceptado(btnPagado,btnCancelarPedido,btnCambiarPedido,pedido);

        }
        if(pedido.getPagado() && !pedido.getCancelado() && !pedido.getAceptado()){ //en caso de que el pedido ya este pagado
            btnCambiarPedido.setVisibility(View.GONE);
            btnPagado.setVisibility(View.GONE);
            btnHabilitarPedido.setVisibility(View.VISIBLE);
            btnCancelarPedido.setVisibility(View.VISIBLE);
            btnEliminarPedido.setVisibility(View.GONE);

            btnCancelarPedido.setText("Eliminar por completo");//cambiuamos el mensaje del boton a eliminado
            //funcionalidad de un pedido pagado

            pedidoPagado(btnCancelarPedido,btnHabilitarPedido,pedido);


        }
        if (pedido.getCancelado() && !pedido.getPagado() && !pedido.getAceptado()){

            btnCambiarPedido.setVisibility(View.GONE);
            btnPagado.setVisibility(View.GONE);
            btnCancelarPedido.setVisibility(View.GONE);
            btnHabilitarPedido.setVisibility(View.VISIBLE);
            btnEliminarPedido.setVisibility(View.VISIBLE);

            btnCambiarPedido.setVisibility(View.GONE);
            btnPagado.setVisibility(View.GONE);


            //funcionalidad de pedido cancelado

            pedidoCancelado(btnEliminarPedido,btnHabilitarPedido,pedido);

        }


        return convertView;

    }


    @Override
    public void resultado(Boolean isAcepatado, Boolean isCancelado) {

    }

    //funcionalidad del boton pagado cuando el pedido esta aceptado

    private void pedidoAceptado(Button btnPagado,Button btnCancelarPedido,Button btnCambiarPedido,Pedido pedido){

        //damos funcionalidad al boton y cambiamos a pagado el pedido
        btnPagado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pedido.getAceptado()) {
                    Map<String, Object> pedidoActualizacion = new HashMap<>();
                    pedidoActualizacion.put("pagado", true);
                    pedidoActualizacion.put("cancelado", false);
                    pedidoActualizacion.put("aceptado", false);
                    pedidoActualizacion.put("eliminado", false);
                    databaseReference.child("Pedido").child(pedido.getIdPedido()).updateChildren(pedidoActualizacion).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "El pedido a sido cambiado a pagado exitosamente", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(context, "A ocurrido un error al cambiar a pagado  el pedido", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });


        //Le damos funcionalidad a los botones
        btnCancelarPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pedido.getAceptado()) {
                    new CuadroCancelarPedidoCliente(context,
                            pedido,
                            databaseReference,
                            AdapterGridPedidoVendedor.this);//inciamos el cuadro de dialogo cancelar
                }
            }
        });

        btnCambiarPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pedido.getAceptado()) {
                    Bundle parametros = new Bundle();
                    parametros.putString("idPedido",pedido.getIdPedido());
                    Intent streamingsIntent = new Intent(context.getApplicationContext(), CuadroCambiarPedido.class);
                    streamingsIntent.putExtras(parametros);
                    context.startActivity(streamingsIntent);


                    //new CuadroCambiarPedido(context, pedido, databaseReference,storage);
                }
            }
        });

        //fin
    }

    //funcionalida parar cuando un pedido ya ha sido pagado

    private  void pedidoPagado(Button btnCancelarPedido,Button btnHabilitarPedido,Pedido pedido){

        btnHabilitarPedido.setText("Pedido no pagado");
        btnHabilitarPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pedido.getPagado()) {
                    DialogInterface.OnClickListener confirmar = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Map<String, Object> pedidoActualizacion = new HashMap<>();
                            pedidoActualizacion.put("eliminado", false);
                            pedidoActualizacion.put("aceptado", true);
                            pedidoActualizacion.put("cancelado", false);
                            pedidoActualizacion.put("pagado", false);
                            databaseReference.child("Pedido").child(pedido.getIdPedido()).updateChildren(pedidoActualizacion).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "El pedido a sido actualizado correctamente", Toast.LENGTH_LONG).show();

                                    } else {
                                        Toast.makeText(context, "A ocurrido un error al actualizar el pedido", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    };
                    new Utilidades().cuadroDialogo(context, confirmar, "Habilitar pedido aceptado", "¿Desea habilitar pedido?");
                }
            }
        });

        btnCancelarPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pedido.getPagado()) {
                    DialogInterface.OnClickListener confirmar = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Map<String, Object> pedidoActualizacion = new HashMap<>();
                             //cambiamos la bandera de eliminado a true
                            pedidoActualizacion.put("pagado", false);
                            pedidoActualizacion.put("cancelado", false);
                            pedidoActualizacion.put("aceptado", false);
                            pedidoActualizacion.put("eliminado", true);
                            databaseReference.child("Pedido").child(pedido.getIdPedido()).updateChildren(pedidoActualizacion).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) { //en caso de que el update sea exitoso
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "El pedido a sido eliminado exitosamente", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(context, "A ocurrido un error al eliminar el pedido", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    };
                    new Utilidades().cuadroDialogo(context, confirmar, "Eliminar pedido", "¿Desea eliminar el pedido?");
                }
            }
        });

    }

    //funcionalidad pedido cancelado
    private  void pedidoCancelado(Button btnCancelarPedido,Button btnHabilitarPedido,Pedido pedido){
        btnCancelarPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pedido.getCancelado()) {
                    DialogInterface.OnClickListener confirmar = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Map<String, Object> pedidoActualizacion = new HashMap<>();
                            pedidoActualizacion.put("pagado", false);
                            pedidoActualizacion.put("cancelado", false);
                            pedidoActualizacion.put("aceptado", false);
                            pedidoActualizacion.put("eliminado", true);
                            databaseReference.child("Pedido").child(pedido.getIdPedido()).updateChildren(pedidoActualizacion).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "El pedido a sido eliminado exitosamente", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(context, "A ocurrido un error al eliminar el pedido", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    };
                    new Utilidades().cuadroDialogo(context, confirmar, "Eliminar pedido", "¿Desea eliminar el pedido?");
                }
            }
        });

        btnHabilitarPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pedido.getCancelado()) {
                    DialogInterface.OnClickListener confirmar = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Map<String, Object> pedidoActualizacion = new HashMap<>();
                            pedidoActualizacion.put("eliminado", false);
                            pedidoActualizacion.put("aceptado", true);
                            pedidoActualizacion.put("cancelado", false);
                            databaseReference.child("Pedido").child(pedido.getIdPedido()).updateChildren(pedidoActualizacion).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "El pedido a sido actualizado correctamente", Toast.LENGTH_LONG).show();

                                    } else {
                                        Toast.makeText(context, "A ocurrido un error al actualizar el pedido", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    };
                    new Utilidades().cuadroDialogo(context, confirmar, "Habilitar pedido aceptado", "¿Desea habilitar pedido?");
                }
            }
        });
    }

    //funcionalidad para conversar con el comprador
    public void conversarComprador(Button mensajear,int position,Context context){
        mensajear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle parametros = new Bundle();
                parametros.putString("cliente", listaPedidoVendedor.get(position).getIdCliente());//enviamos el id para el activity
                Intent streamingsIntent = new Intent(context,
                        MensajeriaGlobalVendedor.class);
                streamingsIntent.putExtras(parametros);
                context.startActivity(streamingsIntent);
            }
        });
    }


    @Override
    public void resultado(Boolean seActualizoCoordena) {
        //resultado del cuadro de dialogo para mostrar el mapa
    }
}
