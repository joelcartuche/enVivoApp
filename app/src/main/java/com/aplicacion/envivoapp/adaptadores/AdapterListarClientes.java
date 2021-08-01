package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterListarClientes extends BaseAdapter {
    private Context context;
    private List<Cliente> listaCliente;
    private DatabaseReference databaseReference;
    private String esMensajeGlobal;
    public AdapterListarClientes(Context context,
                                             List<Cliente> listaCliente,
                                             DatabaseReference databaseReference,
                                 String esMensajeGlobal){
        this.context = context;
        this.listaCliente = listaCliente;
        this.databaseReference = databaseReference;
        this.esMensajeGlobal = esMensajeGlobal;
    }

    @Override
    public int getCount() {
        return listaCliente.size();
    }

    @Override
    public Object getItem(int position) {
        return listaCliente.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater layoutInflater =(LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_list_clientes,null);
        }

        Cliente cliente = listaCliente.get(position);//para manejar que elemento estamos clickeando
        //inicializamos las variables
        TextView nombreCliente= convertView.findViewById(R.id.txtINombreClienteItemListClientes);
        TextView telefonoCliente = convertView.findViewById(R.id.txtTelefonoClienteItemListClientes);
        TextView celularCliente = convertView.findViewById(R.id.txtCelularClienteItemListClientes);
        Button btnbloquerCliente = convertView.findViewById(R.id.btnBloquearClienteItemListClientes);
        Button btnDesbloquearCliente = convertView.findViewById(R.id.btnDesbloquearCliente);
        nombreCliente.setText(cliente.getNombre());
        telefonoCliente.setText(cliente.getTelefono());
        celularCliente.setText(cliente.getCelular());
        if (esMensajeGlobal.equals("1")){
            btnbloquerCliente.setVisibility(View.GONE);
            btnDesbloquearCliente.setVisibility(View.GONE);
        }
        if (esMensajeGlobal.equals("0")){//en caso de que se desee listar al los clientes bloqueados
            if (!cliente.getBloqueado()) {//seteamos el mensje y color del boton
                btnDesbloquearCliente.setVisibility(View.GONE);
                btnbloquerCliente.setVisibility(View.VISIBLE);

                btnbloquerCliente.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogInterface.OnClickListener dialogDesbloqueo = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Map<String,Object> bloqueo = new HashMap<>();
                                bloqueo.put("bloqueado",true);//actualizamos el estado bloqueado del cliente
                                databaseReference.child("Cliente").child(cliente.getIdCliente()).updateChildren(bloqueo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context,"El cliente fue bloqueado con éxito ya puede ser vizualizado",Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context,"Error al bloquear el cliente",Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                });

                            }
                        };
                        new Utilidades().cuadroDialogo(context,dialogDesbloqueo,"Bbloquear cliente","¿Desea bloquearlo al cliente?");
                    }
                });

            }else{
                btnDesbloquearCliente.setVisibility(View.VISIBLE);
                btnbloquerCliente.setVisibility(View.GONE);
                btnDesbloquearCliente.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                DialogInterface.OnClickListener dialogBloqueo = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Map<String,Object> bloqueo = new HashMap<>();
                        bloqueo.put("bloqueado",false);//actualizamos el estado bloqueado del cliente
                        databaseReference.child("Cliente").child(cliente.getIdCliente()).updateChildren(bloqueo).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context,"El cliente fue desbloqueado con exito ya no se podra vizualisar",Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context,"Error al Desbloquear el usuario",Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        });

                    }
                };
                new Utilidades().cuadroDialogo(context,dialogBloqueo,"Desbloquear cliente","¿Desea desbloquearlo al cliente?");

                    }
                });

            }

        }

        return convertView;
    }
}
