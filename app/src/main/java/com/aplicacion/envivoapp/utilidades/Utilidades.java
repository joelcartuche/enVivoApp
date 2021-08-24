package com.aplicacion.envivoapp.utilidades;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.aplicacion.envivoapp.MainActivity;
import com.aplicacion.envivoapp.Reportes.ReporteVendedor;
import com.aplicacion.envivoapp.activityParaClientes.DataCliente;
import com.aplicacion.envivoapp.activityParaClientes.HomeCliente;
import com.aplicacion.envivoapp.activityParaClientes.ListarVendedores;
import com.aplicacion.envivoapp.activityParaClientes.MensajeriaGlobal;
import com.aplicacion.envivoapp.activityParaClientes.PedidoCliente;
import com.aplicacion.envivoapp.activitysParaVendedores.DataLocal;
import com.aplicacion.envivoapp.activitysParaVendedores.DataVendedor;
import com.aplicacion.envivoapp.activitysParaVendedores.GestionVideos;
import com.aplicacion.envivoapp.activitysParaVendedores.HomeVendedor;
import com.aplicacion.envivoapp.activitysParaVendedores.ListarClientes;
import com.aplicacion.envivoapp.activitysParaVendedores.PedidoVendedor;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Utilidades {

    private String claveYoutube;

    public Utilidades() {
    }

    public String getClaveYoutube() {
        return claveYoutube = "AIzaSyDzjngQCozr7u7xkbUAIKPZDKkDBzfXq-0";
    }

    //mostramos el calendario en el edit text
    public void abrirCalendario(View v, Context context, EditText fechaStreaming) {
        Calendar calendar = Calendar.getInstance(); //Instanciamos el calendario
        int anio = calendar.get(Calendar.YEAR); //alamacena el año del calendario
        int mes = calendar.get(Calendar.MONTH); //almacena el mes del calendario
        int dia = calendar.get(Calendar.DAY_OF_MONTH); //almacena el dia del calendario

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String fecha = dayOfMonth + "/" + (month+1) + "/" + (year-1);
                fechaStreaming.setText(fecha);
            }
        }, dia, mes, anio);//obtenemos en el formato de dia mes anio
        datePickerDialog.show();
    }

    //mostramos la hora en el edit text de hora
    public void abrirCalendarioHora(View v, Context context, EditText horaStreaming) {
        Calendar calendar = Calendar.getInstance();
        int hora = calendar.get(Calendar.HOUR_OF_DAY);
        int minuto = calendar.get(Calendar.MINUTE);


        TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String hora = hourOfDay + ":" + minute;
                horaStreaming.setText(hora);
            }
        }, hora, minuto, true);
        timePickerDialog.show();
    }

    public void cargarToolbar(Button home,
                              Button listarVendedores,
                              Button perfil,
                              Button pedido,
                              Button salir,
                              Button mensaje,
                              Context context,
                              FirebaseAuth firebaseAuth,
                              DatabaseReference reference) {

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent streamingsIntent = new Intent(context, HomeCliente.class);
                context.startActivity(streamingsIntent);
            }
        });

        //Damos funcionalidad al menu
        Button btnListarVendedore = listarVendedores;
        btnListarVendedore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle parametros = new Bundle();
                parametros.putString("global", "0");
                Intent mensajeriGlobalIntent = new Intent(context, ListarVendedores.class);
                mensajeriGlobalIntent.putExtras(parametros);
                context.startActivity(mensajeriGlobalIntent);

            }
        });
        Button btnPerfil = perfil;
        btnPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent streamingsIntent = new Intent(context, DataCliente.class);
                context.startActivity(streamingsIntent);

            }
        });
        Button btnPedido = pedido;
        btnPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent streamingsIntent = new Intent(context, PedidoCliente.class);
                context.startActivity(streamingsIntent);

            }
        });
        Button btnSalir = salir;
        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogInterface.OnClickListener confirmar = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        LoginManager.getInstance().logOut();
                        Intent streamingsIntent = new Intent(context, MainActivity.class);
                        context.startActivity(streamingsIntent);
                        ((Activity) context).finish();
                    }
                };
                cuadroDialogo(context, confirmar, "Alerta", "¿Desea cerrar sesión?");
            }
        });
        Button btnMensaje = mensaje;
        btnMensaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle parametros = new Bundle();
                parametros.putString("global", "1");
                Intent mensajeriGlobalIntent = new Intent(context, ListarVendedores.class);
                mensajeriGlobalIntent.putExtras(parametros);
                context.startActivity(mensajeriGlobalIntent);
            }
        });
    }
    public void cargarToolbarVendedor(Button home,
                                      Button local,
                              Button perfil,
                              Button pedido,
                              Button mensaje,
                              Button salir,
                              Button videos,
                              Button clientes,
                              Button  reporte,
                              Context context,
                              FirebaseAuth firebaseAuth){

        Button btnHome = home;
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reporteIntent = new Intent(context, HomeVendedor.class);
                context.startActivity(reporteIntent);
                ((Activity) context).finish();
            }
        });

        //Damos funcionalidad al menu
        Button btnListarLocal = local;
        btnListarLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent listarLocalIntent = new Intent(context, DataLocal.class);
                context.startActivity(listarLocalIntent);
                ((Activity) context).finish();

            }
        });
        Button btnVideos = videos;
        btnVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent streamingsIntent = new Intent(context, GestionVideos.class);
                context.startActivity(streamingsIntent);
                ((Activity) context).finish();

            }
        });
        Button btnPerfil = perfil;
        btnPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent streamingsIntent = new Intent(context, DataVendedor.class);
                context.startActivity(streamingsIntent);
                ((Activity) context).finish();

            }
        });
        Button btnPedido = pedido;
        btnPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pedidoIntent = new Intent(context, PedidoVendedor.class);
                context.startActivity(pedidoIntent);
                ((Activity) context).finish();

            }
        });
        Button btnMensaje = mensaje;
        btnMensaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle parametros = new Bundle();
                parametros.putString("global", "1");
                Intent mensajeriGlobalIntent = new Intent(context, ListarClientes.class);
                mensajeriGlobalIntent.putExtras(parametros);
                context.startActivity(mensajeriGlobalIntent);
                ((Activity) context).finish();

            }
        });

        Button btnReporte = reporte;
        btnReporte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reporteIntent = new Intent(context, ReporteVendedor.class);
                context.startActivity(reporteIntent);
                ((Activity) context).finish();
            }
        });

        Button btnSalir = salir;
        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener confirmar = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        LoginManager.getInstance().logOut();
                        Intent streamingsIntent = new Intent(context, MainActivity.class);
                        context.startActivity(streamingsIntent);
                        ((Activity) context).finish();
                    }
                };
                cuadroDialogo(context,confirmar,"Alerta","¿Desea cerrar sesión?");
            }
        });
        Button btnClientes = clientes;
        btnClientes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle parametros = new Bundle();
                parametros.putString("global", "0");
                Intent mensajeriGlobalIntent = new Intent(context, ListarClientes.class);
                mensajeriGlobalIntent.putExtras(parametros);
                context.startActivity(mensajeriGlobalIntent);
                ((Activity) context).finish();
            }
        });
    }

    public  void cuadroDialogo(Context context,
                               DialogInterface.OnClickListener confirmar,
                               String titulo,
                               String mensaje){
        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(context);//creamos el mensaje de aceptacion o cancelacion
        dialogo1.setTitle(titulo);
        dialogo1.setMessage(mensaje);
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Confirmar", confirmar);
        dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                dialogo1.dismiss();
            }
        });
        dialogo1.show();
    }


}
