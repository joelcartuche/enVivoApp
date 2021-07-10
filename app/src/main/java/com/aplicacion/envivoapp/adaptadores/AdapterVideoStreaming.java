package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.VideoStreaming;

import java.util.List;

public class AdapterVideoStreaming extends ArrayAdapter<VideoStreaming> {

    private List<VideoStreaming> mList;
    private Context mContext;
    private int resourceLayout;

    public AdapterVideoStreaming(@NonNull Context context, int resource, @NonNull List<VideoStreaming> objects) {
        super(context, resource, objects);
        this.mList = objects;
        this.mContext = context;
        this.resourceLayout = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if(view == null){ //adaptamos y pasamos el inflate
            view = LayoutInflater.from(mContext).inflate(R.layout.item_list_video_streaming,null); //null como view grup
        }

        VideoStreaming videoStreaming = mList.get(position);//para manejar que elemento estamos clickeando
        TextView urlStreaming = view.findViewById(R.id.txtItemUrlVideoStreaming); //damos el valor del text view de la vista
        TextView fechaStreaming = view.findViewById(R.id.txtItemFechaVideoStreaming);
        TextView horaStreaming = view.findViewById(R.id.txtItemHoraVideoStreaming);
        //TextView idStreaming = view.findViewById(R.id.txtItemIdVideoStreaming);

        urlStreaming.setText(videoStreaming.getUrlVideoStreaming());
        fechaStreaming.setText(videoStreaming.getFechaTransmision().getDate()+"/"+
                videoStreaming.getFechaTransmision().getMonth()+"/"+videoStreaming.getFechaTransmision().getYear());
        horaStreaming.setText(videoStreaming.getFechaTransmision().getHours()+":"+videoStreaming.getFechaTransmision().getMinutes());
        //idStreaming.setText(videoStreaming.getIdVideoStreaming());

        return view; //retornamos la vista
    }
}

