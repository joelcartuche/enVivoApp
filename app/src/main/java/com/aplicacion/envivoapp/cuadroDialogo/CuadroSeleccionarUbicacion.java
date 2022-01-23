package com.aplicacion.envivoapp.cuadroDialogo;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.activitysParaVendedores.DataLocal;
import com.aplicacion.envivoapp.activitysParaVendedores.PedidoVendedor;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Pedido;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.aplicacion.envivoapp.utilidades.MapsFragment.latLng;

public class CuadroSeleccionarUbicacion extends FragmentActivity {


    public interface resultadoDialogo {
        void resultado(Boolean seActualizoCoordena);
    }

    private CuadroSeleccionarUbicacion.resultadoDialogo interfaceResultadoDialogo;

    public CuadroSeleccionarUbicacion(Context context,
                                      LatLng latLng,
                                      DatabaseReference reference,
                                      CuadroSeleccionarUbicacion.resultadoDialogo result, Boolean esListaLocal) {
        interfaceResultadoDialogo = result;
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);//el dialogo se presenta sin el titulo
        dialog.setCancelable(true); //impedimos el cancelamiento del dialogo
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.));//le damos un color de fondo transparente

        dialog.setContentView(R.layout.cuadro_seleccionar_mapa); //le asisganos el layout
        //((MyFirebaseApp) context.getApplicationContext()).setLatLng(latLng);

        Button btnGuardar = dialog.findViewById(R.id.btnSeleccionaUbicacionCuadro);
        btnGuardar.setVisibility(View.GONE);
        Button btnCancelar = dialog.findViewById(R.id.cancelarSeleccionUbicacionCuadro);
        MapView mapView = dialog.findViewById(R.id.mapVisualizar);

        Intent intent = ((Activity) context).getIntent();
        mapView.onCreate(intent.getExtras());
        mapView.onResume();

        try {
            MapsInitializer.initialize(context.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                googleMap.setMyLocationEnabled(true);
                googleMap.addMarker(new MarkerOptions().
                        position(latLng).
                        title("Ubicación").
                        snippet("Marker Description"));
                CameraPosition cameraPosition = new CameraPosition.Builder().
                        target(latLng).
                        zoom(17).
                        build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });


        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dialog.dismiss();
                if (esListaLocal){
                    dialog.dismiss();
                }else{

                    dialog.dismiss();
                }

            }
        });

        dialog.show();
    }

}
