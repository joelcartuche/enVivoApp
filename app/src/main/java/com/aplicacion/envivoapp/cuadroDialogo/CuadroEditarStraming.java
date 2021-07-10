package com.aplicacion.envivoapp.cuadroDialogo;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.VideoStreaming;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CuadroEditarStraming {
    public interface resultadoDialogo{
        void resultado(Boolean isEliminado);
    }
    private resultadoDialogo interfaceResultadoDialogo;

    public CuadroEditarStraming(Context context,
                                VideoStreaming videoStreaming,
                                String fecha,
                                String hora,
                                FirebaseDatabase firebaseDatabase,
                                resultadoDialogo result){
        interfaceResultadoDialogo = result;
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);//el dialogo se presenta sin el titulo
        dialog.setCancelable(false); //impedimos el cancelamiento del dialogo
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.));//le damos un color de fondo transparente
        dialog.setContentView(R.layout.editar_video_streaming); //le asisganos el layout

        Utilidades utilidades = new Utilidades();
        EditText txtEditarUrlVideo = dialog.findViewById(R.id.txtUrlCuadroStreaming);
        EditText txtEditarFechaVideo = dialog.findViewById(R.id.txtFechaCuadroStreaming);
        EditText txtEditarHoraVideo = dialog.findViewById(R.id.txtHoraCuadroStreaming);
        Button btnGuardarVideo = dialog.findViewById(R.id.btnGuardarEdicionStreaming);
        Button btnEliminarVideo = dialog.findViewById(R.id.btnIrStreamingCuadroStreraming);
        Button btnCancelarVideo = dialog.findViewById(R.id.btnCancelarCuadroStreamings);

        txtEditarUrlVideo.setText(videoStreaming.getUrlVideoStreaming());
        txtEditarFechaVideo.setText(fecha);
        txtEditarHoraVideo.setText(hora);

        btnGuardarVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] fecha = txtEditarFechaVideo.getText().toString().split("/");//separamos la fecha en un arreglo
                if(Integer.parseInt(fecha[0])<10){
                    fecha[0]="0"+fecha[0]; //añadimos un cero en caso de que la fecha se una sola unidad
                }
                if(Integer.parseInt(fecha[1])<10){
                    fecha[1]="0"+fecha[1]; //añadimos un cero en la fecha en caso de que sea una solo unidad
                }

                String fechaFormat = fecha[0]+"-"+fecha[1]+"-"+fecha[2]+" "+txtEditarHoraVideo.getText().toString(); //le damos formato a la fecha
                Date fechaTransmision = null;//creamos una variable de tipo date para luego almacenarla en  la clase videoStreaming
                try {
                    fechaTransmision = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(fechaFormat); //almacenamos la fecha Date
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                fechaTransmision.setMonth(Integer.parseInt(fecha[1]));//editamos el mes ya que presenta error en la base
                fechaTransmision.setYear(Integer.parseInt(fecha[2])); //editamos el año por error en la base

                Map<String,Object> videoStreamingMap = new HashMap<>(); //almacena los datos que van a ser editados

                videoStreamingMap.put("urlVideoStreaming",txtEditarUrlVideo.getText().toString());//setiamos el url del video
                videoStreamingMap.put("fechaTransmision",fechaTransmision);//seteamos la fecha de trasmision
                videoStreamingMap.put("idVideoStreaming",videoStreaming.getIdVideoStreaming());//seteamos el id
                firebaseDatabase.getReference().child("VideoStreaming").child(videoStreaming.getIdVideoStreaming()).updateChildren(videoStreamingMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Datos actualizados con exito", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "No se pudo actualizar los datos intentelo de nuevo", Toast.LENGTH_LONG).show();
                    }
                });

            }
        });
        btnEliminarVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseDatabase.getReference().child("VideoStreaming").child(videoStreaming.getIdVideoStreaming()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Datos eliminados con exito", Toast.LENGTH_LONG).show();
                        interfaceResultadoDialogo.resultado(true);
                        dialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "No se pudo eliminar el dato intentelo de nuevo", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        txtEditarFechaVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                utilidades.abrirCalendario(v,dialog.getContext(),txtEditarFechaVideo);
            }
        });
        txtEditarHoraVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                utilidades.abrirCalendarioHora(v,dialog.getContext(),txtEditarHoraVideo);
            }
        });
        btnCancelarVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }
}
