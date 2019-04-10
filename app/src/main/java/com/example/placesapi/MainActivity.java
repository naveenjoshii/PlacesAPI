package com.example.placesapi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    PlacesClient placesClient;
    Button currentPlaces;
    EditText current_address,likelihood_addresses;
    List<Place.Field> plFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);
    AutocompleteSupportFragment place_fragment;
    private String placeID="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        current_address=(EditText)findViewById(R.id.edt_address);
        likelihood_addresses = (EditText)findViewById(R.id.edt_address_likelihood);
        currentPlaces = (Button) findViewById(R.id.btn_get_current_place);
        currentPlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentPlace();
            }
        });
        initPlaces();
        setupPlaceAutoComplete();
    }

    private void requestPermission() {
        Dexter.withActivity(this)
                .withPermissions(Arrays.asList(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION))
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        Toast.makeText(MainActivity.this,"You Must Enable the Location",Toast.LENGTH_SHORT).show();

                    }
                }).check();
    }

    private void getCurrentPlace() {
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(plFields).build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           return;
        }
        Task<FindCurrentPlaceResponse> placeRequestTask = placesClient.findCurrentPlace(request);
            placeRequestTask.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    FindCurrentPlaceResponse response = task.getResult();
                    Collections.sort(response.getPlaceLikelihoods(), new Comparator<PlaceLikelihood>() {
                        @Override
                        public int compare(PlaceLikelihood o1, PlaceLikelihood o2) {
                            return new Double(o1.getLikelihood()).compareTo(o2.getLikelihood());
                        }
                    });
                       Collections.reverse(response.getPlaceLikelihoods());
                       placeID = response.getPlaceLikelihoods().get(0).getPlace().getId();
                       current_address.setText(new StringBuilder(response.getPlaceLikelihoods().get(0).getPlace().getAddress()));
                       StringBuilder stringBuilder = new StringBuilder();
                       for (PlaceLikelihood place :response.getPlaceLikelihoods()){
                           stringBuilder.append(place.getPlace().getName())
                                   .append("--LikeliHood Value: ")
                                   .append(place.getLikelihood())
                                   .append("\n");
                       }
                       likelihood_addresses.setText(stringBuilder);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
    }

    private void setupPlaceAutoComplete() {
        place_fragment = (AutocompleteSupportFragment)getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragement);
        place_fragment.setPlaceFields(plFields);
        place_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Toast.makeText(MainActivity.this,""+place.getName(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
    }

    private void initPlaces() {
        Places.initialize(this,getString(R.string.places_api_key));
        placesClient = Places.createClient(this);
    }
}
