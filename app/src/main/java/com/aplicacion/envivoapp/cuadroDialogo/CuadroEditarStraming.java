package com.aplicacion.envivoapp.cuadroDialogo;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.modelos.VideoStreaming;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CuadroEditarStraming {

    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();
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
        dialog.setContentView(R.layout.cuadro_editar_video_streaming); //le asisganos el layout

        Utilidades utilidades = new Utilidades();
        EditText txtEditarUrlVideo = dialog.findViewById(R.id.txtUrlCuadroStreaming);
        EditText txtEditarFechaVideo = dialog.findViewById(R.id.txtFechaCuadroStreaming);
        EditText txtEditarHoraVideo = dialog.findViewById(R.id.txtHoraCuadroStreaming);
        RadioButton activarVideo = dialog.findViewById(R.id.radioActivarReunion);
        RadioButton desactivarVideo = dialog.findViewById(R.id.radioDesactivarReunion);
        Button btnGuardarVideo = dialog.findViewById(R.id.btnGuardarCuadroEditarStreaming);
        Button btnActualizarVideo = dialog.findViewById(R.id.btnAgregarStreaming);
        Button btnEliminarVideo = dialog.findViewById(R.id.btnEliminarStreamingCuadroStreraming);
        Button btnCancelarVideo = dialog.findViewById(R.id.btnCancelarCuadroStreamings);
        Button btnIrVideo = dialog.findViewById(R.id.btnIrStreamingCuadroStreraming);

        txtEditarUrlVideo.setHint("https://youtu.be/8wV32B34N_I");

        if (videoStreaming != null) {
            if (videoStreaming.getIniciado()) {
                activarVideo.setChecked(true);
                desactivarVideo.setChecked(false);
            } else {
                activarVideo.setChecked(false);
                desactivarVideo.setChecked(true);
            }
        }
        if (esNuevo ){
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

                    if(txtEditarUrlVideo.getText().toString().contains("https://youtu.be/") ||
                            txtEditarUrlVideo.getText().toString().contains("https://youtube.com/")){
                        //creamos el query para buscar el vendedor
                        Query query = reference.child("Vendedor").orderByChild("uidUsuario").equalTo(firebaseAuth.getCurrentUser().getUid());
                        query.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                            @Override
                            public void onSuccess(DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    Vendedor vendedor = null;
                                    for(DataSnapshot ds:snapshot.getChildren()) {
                                        vendedor = ds.getValue(Vendedor.class);
                                    }
                                    if (vendedor != null){
                                        VideoStreaming videoStreaming = new VideoStreaming();//inicializamos la variable que va a contener a la clase VideoStreaming

                                        try {
                                            videoStreaming.setUrlVideoStreaming(encriptacionDatos.encriptar(txtEditarUrlVideo.getText().toString())); //setiamos el url del video


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
                                            videoStreaming.setIniciado(activarVideo.isChecked());
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
                                                    Log.w("Error guardar","A ocurrido un error",e);
                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }
                            }
                        });

                    }else{
                        Toast.makeText(context, "El url del video no es correcto", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(context, "Asegurese de aver ingresado todos los datos", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnActualizarVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!txtEditarUrlVideo.getText().toString().equals("")
                        && !txtEditarFechaVideo.getText().toString().equals("")
                        && !txtEditarHoraVideo.getText().toString().equals("")) {

                    if (txtEditarUrlVideo.getText().toString().contains("https://youtu.be/") ||
                            txtEditarUrlVideo.getText().toString().contains("https://youtube.com/")) {



                        String[] fecha = txtEditarFechaVideo.getText().toString().split("/");//separamos la fecha en un arreglo
                        if (Integer.parseInt(fecha[0]) < 10) {
                            fecha[0] = "0" + fecha[0]; //añadimos un cero en caso de que la fecha se una sola unidad
                        }
                        if (Integer.parseInt(fecha[1]) < 10) {
                            fecha[1] = "0" + fecha[1]; //añadimos un cero en la fecha en caso de que sea una solo unidad
                        }

                        String fechaFormat = fecha[0] + "-" + fecha[1] + "-" + fecha[2] + " " + txtEditarHoraVideo.getText().toString(); //le damos formato a la fecha
                        Date fechaTransmision = null;//creamos una variable de tipo date para luego almacenarla en  la clase videoStreaming
                        try {
                            fechaTransmision = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(fechaFormat); //almacenamos la fecha Date
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        fechaTransmision.setMonth(Integer.parseInt(fecha[1]));//editamos el mes ya que presenta error en la base
                        fechaTransmision.setYear(Integer.parseInt(fecha[2])); //editamos el año por error en la base

                        Map<String, Object> videoStreamingMap = new HashMap<>(); //almacena los datos que van a ser editados

                        try {
                            videoStreamingMap.put("urlVideoStreaming", encriptacionDatos.encriptar(txtEditarUrlVideo.getText().toString()));//setiamos el url del video
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        videoStreamingMap.put("fechaTransmision", fechaTransmision);//seteamos la fecha de trasmision
                        videoStreamingMap.put("idVideoStreaming", videoStreaming.getIdVideoStreaming());//seteamos el id
                        videoStreamingMap.put("iniciado", activarVideo.isChecked());


                        firebaseDatabase.getReference().child("VideoStreaming").child(videoStreaming.getIdVideoStreaming()).updateChildren(videoStreamingMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Datos actualizados con exito", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "No se pudo actualizar los datos intentelo de nuevo", Toast.LENGTH_LONG).show();
                            }
                        });

                    }else{
                        Toast.makeText(context, "El url del video no es correcto", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        if (videoStreaming != null) {
            if (!videoStreaming.getEliminado()) {//en caso de que el video no este eliminado
                btnGuardarVideo.setVisibility(View.GONE);
                btnActualizarVideo.setVisibility(View.VISIBLE);
                btnEliminarVideo.setVisibility(View.VISIBLE);
                btnIrVideo.setVisibility(View.VISIBLE);

                btnEliminarVideo.setText("Eliminar");
                btnEliminarVideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Map<String, Object> actualizacionEstado = new HashMap<>();
                        actualizacionEstado.put("eliminado", true);
                        actualizacionEstado.put("iniciado", false);
                        firebaseDatabase.getReference().child("VideoStreaming").child(videoStreaming.getIdVideoStreaming()).updateChildren(actualizacionEstado).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Datos eliminados con exito", Toast.LENGTH_LONG).show();
                                interfaceResultadoDialogo.resultado(true, false, null);
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

            } else if(videoStreaming.getEliminado()) { //en caso de que el video este eliminado

                btnGuardarVideo.setVisibility(View.VISIBLE);
                btnActualizarVideo.setVisibility(View.GONE);
                btnEliminarVideo.setVisibility(View.VISIBLE);
                btnIrVideo.setVisibility(View.GONE);


                //no dejamos que se editen los datos si el video esta eliminado
                txtEditarUrlVideo.setFocusable(false);
                txtEditarUrlVideo.setEnabled(false);
                txtEditarFechaVideo.setFocusable(false);
                txtEditarFechaVideo.setEnabled(false);
                txtEditarHoraVideo.setFocusable(false);
                txtEditarHoraVideo.setEnabled(false);
                if (videoStreaming.getIniciado()){
                    activarVideo.setChecked(true);
                    desactivarVideo.setChecked(false);
                }else{
                    activarVideo.setChecked(false);
                    desactivarVideo.setChecked(true);
                }

                btnEliminarVideo.setText("Eliminar por completo");
                btnGuardarVideo.setText("Recuperar video");
                btnEliminarVideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogInterface.OnClickListener confirmar = new DialogInterface.OnClickListener() {//creamos la accion de confirmacion para la eliminacion
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                firebaseDatabase.getReference().child("VideoStreaming").child(videoStreaming.getIdVideoStreaming()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //inicio de bloqueo de mensajes
                                        reference.child("Mensaje").addValueEventListener(new ValueEventListener() {//bloqueamos los mensajes del cliente
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()){
                                                    for (DataSnapshot ds: snapshot.getChildren()){
                                                        Mensaje mensaje= ds.getValue(Mensaje.class);
                                                        if (mensaje.getIdStreaming().equals(videoStreaming.getIdVideoStreaming())){
                                                            Map<String,Object> bloqueoCliente = new HashMap<>();
                                                            bloqueoCliente.put("esClienteBloqueado",true);//actualizamos el estado bloqueado de los mensajes del cliente
                                                            reference.child("Mensaje").child(mensaje.getIdMensaje()).updateChildren(bloqueoCliente).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Toast.makeText(context,"Bloqueando mensajes...",Toast.LENGTH_LONG).show();
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(context,"Error al Bloquer los mensajes",Toast.LENGTH_LONG).show();

                                                                }
                                                            });
                                                        }
                                                    }
                                                    Toast.makeText(context, "Datos borrados con exito", Toast.LENGTH_LONG).show();
                                                    interfaceResultadoDialogo.resultado(true, false, null);
                                                    dialog.dismiss();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.w("Error bloqueo","Error al bloquear los mensajes",error.toException());
                                            }
                                        });

                                        //fin de bloqueo de mensajes

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "No se pudo eliminar el dato intentelo de nuevo", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        };
                        new Utilidades().cuadroDialogo(context, confirmar, "Eliminación total", "¿Desea eliminar por completo los datos?\nLos mensajes del streaming no se podran volver a visualizar pero los pedidos aceptados no seran alterados");
                    }
                });
                //le damos funcionalidad a guardar video para regresar el video
                btnGuardarVideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Map<String, Object> actualizacionEstado = new HashMap<>();
                        actualizacionEstado.put("iniciado", false);
                        actualizacionEstado.put("eliminado", false);
                        firebaseDatabase.getReference().child("VideoStreaming").child(videoStreaming.getIdVideoStreaming()).updateChildren(actualizacionEstado).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Datos recuperados con exito", Toast.LENGTH_LONG).show();
                                interfaceResultadoDialogo.resultado(true, false, null);
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
            }
        }
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
