package com.aplicacion.envivoapp.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroEditarLocal;
import com.aplicacion.envivoapp.cuadroDialogo.CuadroSeleccionarUbicacion;
import com.aplicacion.envivoapp.modelos.Local;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.List;

public class AdapterListarLocal extends BaseAdapter implements CuadroSeleccionarUbicacion.resultadoDialogo,CuadroEditarLocal.resultadoDialogo{
    private Context context;
    private List<Local> listaLocal;
    private DatabaseReference databaseReference;
    private GoogleMap mMap;
    private  FirebaseAuth firebaseAuth;


    public AdapterListarLocal(Context context,
                              List<Local> listarLocal,
                              FirebaseAuth firebaseAuth,
                              DatabaseReference databaseReference){
        this.context = context;
        this.listaLocal = listarLocal;
        this.databaseReference = databaseReference;
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public int getCount() {
        return listaLocal.size();
    }

    @Override
    public Object getItem(int position) {
        return listaLocal.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater layoutInflater =(LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_list_local,null);
        }

        Local local = listaLocal.get(position);//para manejar que elemento estamos clickeando
        //inicializamos las variables
        TextView nombre= convertView.findViewById(R.id.txtNombreLocal);
//        TextView direccion = convertView.findViewById(R.id.txtDireccionLocal);
        TextView telefono = convertView.findViewById(R.id.txtTelefonoLocal);
        TextView celular = convertView.findViewById(R.id.txtCelularLocal);
        TextView callePrincipal = convertView.findViewById(R.id.txtCallePrincipalLocal);
        TextView calleSecundaria = convertView.findViewById(R.id.txtCalleSecundariaLocal);
        TextView referencia = convertView.findViewById(R.id.txtReferenciaLocal);
        Button visualizarMapa = convertView.findViewById(R.id.btnVizualizarMapa);
        Button editarLocal =convertView.findViewById(R.id.btnItemListLocal);
        LatLng ubicacion = new LatLng(local.getLatitud(),local.getLongitud());



        //LatLng latLng = ((MyFirebaseApp) context.getApplicationContext()).getLatLng();

        nombre.setText(local.getNombre());
        //direccion.setText(local.getDireccion());
        telefono.setText(local.getTelefono());
        celular.setText(local.getCelular());
        callePrincipal.setText(local.getCallePrincipal());
        if (local.getCalleSecundaria() !=null || !local.getCalleSecundaria().equals("")){
            calleSecundaria.setText(local.getCalleSecundaria());
        }
        referencia.setText(local.getReferencia());
        visualizarMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CuadroSeleccionarUbicacion(context,ubicacion,false,local.getNombre(),AdapterListarLocal.this::resultado,true);
            }
        });

        editarLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirCuadroDialogo(local,firebaseAuth,context);
            }
        });
        return convertView;
    }


    @Override
    public void resultado(Boolean seActualizoCoordena) {

    }



    public void abrirCuadroDialogo(Local local, FirebaseAuth firebaseAuth,Context context){
        Vendedor vendedorGlobal = ((MyFirebaseApp) context.getApplicationContext()).getVendedor();
        if (vendedorGlobal!=null){
            new CuadroEditarLocal(context,local,vendedorGlobal,false,databaseReference,AdapterListarLocal.this::resultado);
        }


    }

    @Override
    public void resultado() {//resultado del cuadro de dialogo editar

    }
}
