package com.aplicacion.envivoapp.utilidades;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapListaEditarLocal extends Fragment implements OnMapReadyCallback{


    public static LatLng latLng;
    private GoogleMap mMap;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_lista_editar_local, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapListLocalEditar);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        latLng = ((MyFirebaseApp) getActivity().getApplicationContext()).getLatLng();

        if (latLng == null){

            getLocalizacion();
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);

            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

            Location myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            latLng = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());

        }

        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Loja")
                .draggable(true));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));


        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(@NonNull Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                latLng = marker.getPosition();
                ((MyFirebaseApp) getActivity().getApplicationContext()).setLatLng(marker.getPosition());




            }

            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {

            }
        });


    }

    private void getLocalizacion() {
        int permiso = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);
        if(permiso == PackageManager.PERMISSION_DENIED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
            }else{
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

}