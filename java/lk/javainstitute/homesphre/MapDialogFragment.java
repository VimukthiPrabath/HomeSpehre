package lk.javainstitute.homesphre;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapDialogFragment extends DialogFragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private LatLng selectedLocation;
    private MapLocationListener locationListener;

    interface MapLocationListener {
        void onLocationSelected(LatLng location);
    }

    public void setLocationListener(MapLocationListener listener) {
        this.locationListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_map, null);
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(view);
        dialog.setCancelable(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.map, mapFragment)
                    .commit();
        }
        mapFragment.getMapAsync(this);

        Button btnConfirm = view.findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(v -> {
            if (selectedLocation != null && locationListener != null) {
                locationListener.onLocationSelected(selectedLocation);
                dismiss();
            }
        });

        return dialog;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        selectedLocation = new LatLng(6.680583932427651, 80.40201334327476);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15));
        googleMap.addMarker(new MarkerOptions().position(selectedLocation));

        googleMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng));
        });

        Button btnConfirm = getDialog().findViewById(R.id.btnConfirm);
        btnConfirm.setEnabled(false);
        googleMap.setOnCameraMoveListener(() -> {
            btnConfirm.setEnabled(true);
            googleMap.setOnCameraMoveListener(null);
        });
    }
}