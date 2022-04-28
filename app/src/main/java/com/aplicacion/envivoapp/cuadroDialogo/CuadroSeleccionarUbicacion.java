package com.aplicacion.envivoapp.cuadroDialogo;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class CuadroSeleccionarUbicacion extends FragmentActivity {


    public interface resultadoDialogo {
        void resultado(Boolean seActualizoCoordena);
    }

    private CuadroSeleccionarUbicacion.resultadoDialogo interfaceResultadoDialogo;

    public CuadroSeleccionarUbicacion(Context context,
                                      LatLng latLng,
                                      Boolean esCliente,
                                      String nombre,
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
                Bitmap icon = null;
                if (esCliente){
                    icon = new Utilidades().cargarIconocliente();
                }else{
                    icon = new Utilidades().cargarIconoTienda();
                }

                googleMap.setMyLocationEnabled(true);
                googleMap.addMarker(new MarkerOptions().
                        position(latLng).
                        title("Ubicación").
                        icon(BitmapDescriptorFactory.fromBitmap(icon)).
                        snippet(nombre));
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
