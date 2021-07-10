package com.aplicacion.envivoapp.utilidades;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.aplicacion.envivoapp.activitysParaVendedores.GestionVideos;

import java.util.Calendar;

public class Utilidades {
    public Utilidades() {
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
        }, dia, mes, anio);
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
}
