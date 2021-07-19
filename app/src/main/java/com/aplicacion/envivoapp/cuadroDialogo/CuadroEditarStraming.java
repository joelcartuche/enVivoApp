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
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.modelos.VideoStreaming;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CuadroEditarStraming {
    public interface resultadoDialogo{
        void resultado(Boolean isEliminado,Boolean isIrStreaming,VideoStreaming videoStreaming);
    }
    private resultadoDialogo interfaceResultadoDialogo;

    public CuadroEditarStraming(Context context,
                                VideoStreaming videoStreaming,
                                Boolean esNuevo,
                                FirebaseAuth firebaseAuth,
                                FirebaseDatabase firebaseDatabase,
                                DatabaseReference reference,
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
        Button btnGuardarVideo = dialog.findViewById(R.id.btnGuardarCuadroEditarStreaming);
        Button btnActualizarVideo = dialog.findViewById(R.id.btnAgregarStreaming);
        Button btnEliminarVideo = dialog.findViewById(R.id.btnEliminarStreamingCuadroStreraming);
        Button btnCancelarVideo = dialog.findViewById(R.id.btnCancelarCuadroStreamings);
        Button btnIrVideo = dialog.findViewById(R.id.btnIrStreamingCuadroStreraming);


        if (esNuevo){
            btnGuardarVideo.setVisibility(View.VISIBLE);
            btnActualizarVideo.setVisibility(View.GONE);
            btnEliminarVideo.setVisibility(View.GONE);
            btnIrVideo.setVisibility(View.GONE);
        }else{
            btnGuardarVideo.setVisibility(View.GONE);
            btnActualizarVideo.setVisibility(View.VISIBLE);
            btnEliminarVideo.setVisibility(View.VISIBLE);
            btnIrVideo.setVisibility(View.VISIBLE);
            txtEditarUrlVideo.setText(videoStreaming.getUrlVideoStreaming());
            txtEditarFechaVideo.setText(videoStreaming.getFechaTransmision().getDate()+
                    "/"+videoStreaming.getFechaTransmision().getMonth() +
                    "/"+videoStreaming.getFechaTransmision().getYear());
            txtEditarHoraVideo.setText(videoStreaming.getFechaTransmision().getHours()+":"+videoStreaming.getFechaTransmision().getMinutes());
        }

        btnGuardarVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txtEditarUrlVideo.getText().toString().equals("")
                        && !txtEditarFechaVideo.getText().toString().equals("")
                        && !txtEditarHoraVideo.getText().toString().equals("")) {

                    reference.child("Vendedor").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                for(DataSnapshot ds:snapshot.getChildren()){
                                    Vendedor vendedor = ds.getValue(Vendedor.class);
                                    if (vendedor.getUidUsuario().equals(firebaseAuth.getCurrentUser().getUid())){
                                        VideoStreaming videoStreaming = new VideoStreaming();//inicializamos la variable que va a contener a la clase VideoStreaming
                                        videoStreaming.setUrlVideoStreaming(txtEditarUrlVideo.getText().toString()); //setiamos el url del video

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
                                        videoStreaming.setFechaTransmision(fechaTransmision);//seteamos la fecha de trasmision
                                        videoStreaming.setIdVideoStreaming(UUID.randomUUID().toString()); //seteamos el id
                                        videoStreaming.setIdVendedor(vendedor.getIdVendedor()); //seteamos el uid del vendedor
                                        reference.child("VideoStreaming").child(videoStreaming.getIdVideoStreaming()).setValue(videoStreaming).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(context, "Datos guardados con exito", Toast.LENGTH_LONG).show();
                                                dialog.dismiss();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context, "A ocurrido un error al guardar los datos", Toast.LENGTH_LONG).show();
                                            }
                                        });

                                    }

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } else {
                    Toast.makeText(context, "Asegurese de aver ingresado todos los datos", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnActualizarVideo.setOnClickListener(new View.OnClickListener() {
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
                        interfaceResultadoDialogo.resultado(true,false,null);
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

        btnIrVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interfaceResultadoDialogo.resultado(false,true,videoStreaming);
                dialog.dismiss();
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
