package com.aplicacion.envivoapp.cuadroDialogo;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Comentario;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Notificacion;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CuadroRealizarComentarioAlVendedor {

    EncriptacionDatos encrypt  = new EncriptacionDatos();
    DatabaseReference reference ;
    Vendedor vendedorGlobal;
    Cliente clienteGlobal;
    Context context;

    public interface resultadoCuadroRealizarComentarioAlVendedor{
        void resultadoCuadroRealizarComentarioAlVendedor(Boolean isAcepatado,Boolean isCancelado, int position);
    }
    private CuadroRealizarComentarioAlVendedor.resultadoCuadroRealizarComentarioAlVendedor interfaceResultadoDialogo;



    public CuadroRealizarComentarioAlVendedor(Context context,
                                       DatabaseReference reference,
                                              Cliente cliente,
                                              Vendedor vendedor,
                                              CuadroRealizarComentarioAlVendedor.resultadoCuadroRealizarComentarioAlVendedor result) {
        this.reference = reference;
        interfaceResultadoDialogo = result;
        this.clienteGlobal = cliente;
        this.vendedorGlobal = vendedor;
        this.context = context;

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);//el dialogo se presenta sin el titulo
        dialog.setCancelable(false); //impedimos el cancelamiento del dialogo
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.));//le damos un color de fondo transparente
        dialog.setContentView(R.layout.cuadro_realizar_comentario_al_vendedor); //le asisganos el layout


        EditText txtComentarioAlVendedor = dialog.findViewById(R.id.txtComentarioAlVendedor);
        Button btnCancelarComentarioAlVendedor = dialog.findViewById(R.id.btnCancelarComentarioAlVendedor);
        Button btnEnviarComentarioAlVendedor = dialog.findViewById(R.id.btnEnviarComentarioAlVendedor);

        btnCancelarComentarioAlVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnEnviarComentarioAlVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtComentarioAlVendedor.getText().toString().equals("")){
                    txtComentarioAlVendedor.setError("Ingrese mensaje");
                }else{
                    enviarComentario(txtComentarioAlVendedor.getText().toString(),dialog);
                }
            }
        });


        dialog.show();
    }

    private void enviarComentario(String mensaje,Dialog dialog) {
        Comentario comentario= new Comentario();
        comentario.setCliente(clienteGlobal);
        comentario.setEsDenuncia(false);
        comentario.setIdComentario(reference.push().getKey());
        comentario.setMensaje(mensaje);
        comentario.setVendedor(vendedorGlobal);
        reference.child("Comentario").child(comentario.getIdComentario()).setValue(comentario).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,"A ocurrido un error, intentelo de nuevo",Toast.LENGTH_LONG).show();

            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context,"Se ha subido el comentario exitosamente",Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
    }

}
