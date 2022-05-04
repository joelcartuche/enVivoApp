package com.aplicacion.envivoapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.aplicacion.envivoapp.activityParaClientes.HomeClienteMain;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Notificacion;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Servicio extends Service {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private Cliente cliente;
    private EncriptacionDatos encrypt = new EncriptacionDatos();

    public Servicio() {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        if (firebaseAuth.getCurrentUser() != null) {
            databaseReference.child("Cliente").orderByChild("uidUsuario").equalTo(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            cliente = ds.getValue(Cliente.class);
                        }
                        if (cliente != null) {
                            if (firebaseAuth.getCurrentUser() != null) {
                                notificaciones();
                            }

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    String notificacionPrimaria = "";
    String notificacionCopia = "";

    public void notificaciones() {


        Query queryNotificacion = databaseReference.child("Notificacion").orderByChild("idCliente").equalTo(cliente.getIdCliente());
        queryNotificacion.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    notificacionPrimaria = String.valueOf(snapshot.getValue());

                    if (notificacionPrimaria == null) { //si la notificacionPrimaria es nula
                        notificacionPrimaria = "";
                    }

                    if (notificacionCopia.isEmpty()) {//en caso de que la copia este vacia
                        notificacionCopia = notificacionPrimaria;
                    }

                    if (!notificacionPrimaria.equals(notificacionCopia)) { //en caso de existir algun cambio con las notificaiones
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Notificacion notificacion = ds.getValue(Notificacion.class);
                            if (notificacion != null) {
                                if (notificacion.getEsNuevo()) {
                                    Query queryVendedor = databaseReference.child("Vendedor").child(notificacion.getIdVendedor());

                                    //enviamos el query
                                    queryVendedor.get().addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("ERROR ", e.toString());
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                        @Override
                                        public void onSuccess(DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                Vendedor vendedor = snapshot.getValue(Vendedor.class);
                                                if (vendedor != null) {
                                                    //actualizamos la notificacion
                                                    Map<String, Object> map = new HashMap<>();
                                                    map.put("esNuevo", false);
                                                    databaseReference.child("Notificacion").child(notificacion.getIdNotificacion()).updateChildren(map).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.e("ERROR", e.toString());
                                                        }
                                                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("Success", "notificacion actualizada con exito");
                                                            String nombre = "";
                                                            notificacion.setEsNuevo(false);//se cambia a false ya que no es nueva la notificacion
                                                            if (notificacion.getCodigoNotificacion() == 1) {
                                                                try {
                                                                    nombre = encrypt.desencriptar(vendedor.getNombre());
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                                crearNotificacion(nombre, "A creado un pedido", 1, notificacion);
                                                            } else if (notificacion.getCodigoNotificacion() == 2) {
                                                                try {
                                                                    nombre = encrypt.desencriptar(vendedor.getNombre());
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                                crearNotificacion(nombre, "A actualizado un pedido", 2, notificacion);
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                        notificacionCopia = notificacionPrimaria;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void crearNotificacion(String titulo, String mensaje, int codigo, Notificacion notificacionData) {

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, HomeClienteMain.class);
        intent.putExtra("codigo", codigo);
        intent.putExtra("idPedido", notificacionData.getIdPedido());
        Log.d("estadoServicio", ((MyFirebaseApp) getApplicationContext()).getCodigo() + "");
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder creador;
        String canalID = "MiCanal01";
        Context contexto = getApplicationContext();
        NotificationManager notificador = (NotificationManager) getSystemService(contexto.NOTIFICATION_SERVICE);
        creador = new NotificationCompat.Builder(contexto, canalID);
        // Si nuestro dispositivo tiene Android 8 (API 26, Oreo) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String canalNombre = "Mensajes";
            String canalDescribe = "Canal de mensajes";
            int importancia = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel miCanal = new NotificationChannel(canalID, canalNombre, importancia);
            miCanal.setDescription(canalDescribe);
            miCanal.enableLights(true);
            miCanal.setLightColor(Color.BLUE); // Esto no lo soportan todos los dispositivos
            miCanal.enableVibration(true);
            notificador.createNotificationChannel(miCanal);
            creador = new NotificationCompat.Builder(contexto, canalID);
        }
        Bitmap iconoNotifica = BitmapFactory.decodeResource(contexto.getResources(), R.drawable.com_facebook_auth_dialog_cancel_background);
        int iconoSmall = R.drawable.ic_carrito_compra;
        creador.setSmallIcon(iconoSmall);
        creador.setLargeIcon(iconoNotifica);
        creador.setContentTitle(titulo);
        creador.setContentText(mensaje);
        creador.setContentIntent(pendingIntent);
        creador.setAutoCancel(true);
        creador.setLights(Color.YELLOW, 1000, 1000);
        creador.addAction(R.drawable.ic_ir_aplicacion, "Ir", irIntent(codigo, notificacionData.getIdPedido()));
        creador.setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje));
        creador.setChannelId(canalID);

        int id = ((MyFirebaseApp) getApplicationContext()).getIdNotificacion();
        ((MyFirebaseApp) getApplicationContext()).setIdNotificacion(id + 1);
        id = ((MyFirebaseApp) getApplicationContext()).getIdNotificacion();

        notificador.notify(id, creador.build());
        //startForeground(id,creador.build());

    }

    private PendingIntent irIntent(int codigo, String idPedido) {
        PendingIntent pendingIntent;
        Intent intent = new Intent(this, HomeClienteMain.class);
        intent.putExtra("codigo", codigo);
        intent.putExtra("idPedido", idPedido);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(HomeClienteMain.class);
        stackBuilder.addNextIntent(intent);
        pendingIntent = stackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }
}