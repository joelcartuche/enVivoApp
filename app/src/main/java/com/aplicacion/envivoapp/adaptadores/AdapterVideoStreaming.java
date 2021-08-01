package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.VideoStreaming;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class AdapterVideoStreaming extends BaseAdapter  {

    private List<VideoStreaming> listaVideoStreamingOriginal;
    private List<VideoStreaming> listaVideoStreamingDisplay;
    private Context context;
    private DatabaseReference databaseReference;


    public AdapterVideoStreaming(Context context,
                                    List<VideoStreaming> listaVideoStreamingOriginal,
                                    DatabaseReference databaseReference){
        this.context = context;
        this.listaVideoStreamingOriginal = listaVideoStreamingOriginal;
        this.listaVideoStreamingDisplay = listaVideoStreamingOriginal;
        this.databaseReference = databaseReference;
    }
    @Override
    public int getCount() {
        return listaVideoStreamingOriginal.size();
    }

    @Override
    public Object getItem(int position) {
        return listaVideoStreamingOriginal.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) { //adaptamos y pasamos el inflate
            view = LayoutInflater.from(context).inflate(R.layout.item_list_video_streaming, null); //null como view grup
        }

        VideoStreaming videoStreaming = listaVideoStreamingOriginal.get(position);//para manejar que elemento estamos clickeando
        TextView urlStreaming = view.findViewById(R.id.txtItemUrlVideoStreaming); //damos el valor del text view de la vista
        TextView fechaStreaming = view.findViewById(R.id.txtItemFechaVideoStreaming);
        TextView horaStreaming = view.findViewById(R.id.txtItemHoraVideoStreaming);
        TextView estado = view.findViewById(R.id.txtEstadoStreamingVendedor);
        //TextView idStreaming = view.findViewById(R.id.txtItemIdVideoStreaming);

        urlStreaming.setText(videoStreaming.getUrlVideoStreaming());
        fechaStreaming.setText(videoStreaming.getFechaTransmision().getDate() + "/" +
                videoStreaming.getFechaTransmision().getMonth() + "/" + videoStreaming.getFechaTransmision().getYear());
        horaStreaming.setText(videoStreaming.getFechaTransmision().getHours() + ":" + videoStreaming.getFechaTransmision().getMinutes());
        if (videoStreaming.getIniciado()){
            estado.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_conectado,0,0,0);
        }else{
            estado.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_desconectado,0,0,0);
        }
        //idStreaming.setText(videoStreaming.getIdVideoStreaming());

        return view; //retornamos la vista
    }

}

