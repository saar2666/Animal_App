package com.animalsapp.Fragments;

import android.Manifest;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.animalsapp.GPSClass;
import com.animalsapp.PreferencesUtil;
import com.animalsapp.R;
import com.animalsapp.databinding.FragmentSignupBinding;
import com.blongho.country_data.World;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class SignupFragment extends Fragment {

    FragmentSignupBinding binding;


    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    ProgressDialog progressDialog;
    PreferencesUtil preferencesUtil;
    NavController navController;
    SupportMapFragment mapFragment;
    GPSClass gpsClass;


    GoogleMap map;
    LatLng latLng;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        gpsClass = new GPSClass(getActivity());
        progressDialog = new ProgressDialog(getActivity(), R.style.AppCompatAlertDialogStyle); //Initialize ProgressDialog class
        progressDialog.setMessage("Please Wait....");
        progressDialog.setCancelable(false);


//         mapFragment =
//                (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.maps);

        mapFragment = ((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.maps));

        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        preferencesUtil = new PreferencesUtil(getActivity());
        binding.passwordShowHideImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.passwordEditText.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                    binding.passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    binding.passwordShowHideImageView.setImageResource(R.drawable.hide);

                } else {
                    binding.passwordShowHideImageView.setImageResource(R.drawable.show);
                    binding.passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                binding.passwordEditText.setSelection(binding.passwordEditText.length());
            }
        });
        binding.confirmPasswordShowHideImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.confirmPasswordEditText.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                    binding.confirmPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    binding.confirmPasswordShowHideImageView.setImageResource(R.drawable.hide);

                } else {
                    binding.confirmPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    binding.confirmPasswordShowHideImageView.setImageResource(R.drawable.show);
                }
                binding.confirmPasswordEditText.setSelection(binding.confirmPasswordEditText.length());
            }
        });

        binding.alreadyHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_signupFragment_to_blankFragment);
            }
        });

        binding.signUpImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String FirstName = binding.firstNameEditText.getText().toString();
                String LastName = binding.lastNameEditText.getText().toString();
                String Email = binding.emailEditText.getText().toString();
                String Password = binding.passwordEditText.getText().toString();
                String confirmPassword = binding.confirmPasswordEditText.getText().toString();


                if (FirstName.isEmpty() || LastName.isEmpty() || Email.isEmpty() || Password.isEmpty() || confirmPassword.isEmpty()) {

                    Toast.makeText(getActivity(), "All Fields Required", Toast.LENGTH_SHORT).show();
                } else if (!Email.matches(emailPattern)) {
                    Toast.makeText(getActivity(), "Please Enter Valid Email", Toast.LENGTH_SHORT).show();

                } else {

                    // Create Account in firebase

                    progressDialog.show();
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(Email, Password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("firstName", FirstName);
                                        hashMap.put("lastName", LastName);
                                        hashMap.put("email", Email);
                                        DatabaseReference users = FirebaseDatabase.getInstance().getReference()
                                                .child("users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        users.updateChildren(hashMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        progressDialog.dismiss();
                                                        FirebaseAuth.getInstance().signOut();
                                                        navController.navigate(R.id.action_signupFragment_to_blankFragment);
                                                    }
                                                });


                                    }
                                }


                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull @NotNull Exception e) {
                                    // Error in account create

                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    //Display error message from Firebase
                                }
                            });


                }
            }
        });

        World.init(getActivity());
        String[] permission = {
                Manifest.permission.ACCESS_FINE_LOCATION};

        Permissions.check(getActivity(), permission, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {

                if (gpsClass.isLocationEnabled()) {

                    if (gpsClass.getLatitude()!=0) {
                        Geocoder geocoder = new Geocoder(getActivity(), Locale.ENGLISH);
                        try {

                            List<Address> addresses = geocoder.getFromLocation(gpsClass.getLatitude(), gpsClass.getLongitude(), 5);
                            latLng = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                            binding.counteryName.setText(addresses.get(0).getCountryName());
                            final int flag = World.getFlagOf(addresses.get(0).getCountryName());
                            binding.flagImageView.setImageResource(flag);


                            if (mapFragment != null) {
                                mapFragment.getMapAsync(new OnMapReadyCallback() {
                                    @Override
                                    public void onMapReady(@NonNull GoogleMap googleMap) {

                                        map = googleMap;
                                        map.addMarker(new MarkerOptions().position(latLng));
                                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 4.0f));
                                    }
                                });
                            }



                        } catch (IOException e) {
                            //e.printStackTrace();
                            Log.e("", "Impossible to connect to Geocoder", e);
                        }
                    } else {
                        Toast.makeText(getActivity(), "Please restart the app to get location", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }




            }


            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                Permissions.check(getActivity(), permission, null, null, new PermissionHandler() {
                    @Override
                    public void onGranted() {

                        if (gpsClass.isLocationEnabled()) {

                            if (gpsClass.getLatitude()!=00) {
                                Geocoder geocoder = new Geocoder(getActivity(), Locale.ENGLISH);
                                try {
                                    /**
                                     * Geocoder.getFromLocation - Returns an array of Addresses
                                     * that are known to describe the area immediately surrounding the given latitude and longitude.
                                     */
                                    List<Address> addresses = geocoder.getFromLocation(gpsClass.getLatitude(), gpsClass.getLongitude(), 5);
                                    binding.counteryName.setText(addresses.get(0).getCountryName());
                                    final int flag = World.getFlagOf(addresses.get(0).getCountryName());
                                    binding.flagImageView.setImageResource(flag);

                                    latLng = new LatLng(gpsClass.getLatitude(),gpsClass.getLongitude() );

                                } catch (IOException e) {
                                    //e.printStackTrace();
                                    Log.e("", "Impossible to connect to Geocoder", e);
                                }
                            } else {
                                Toast.makeText(getActivity(), "Please restart the app to get location", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }




                    }


                    @Override
                    public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                        super.onDenied(context, deniedPermissions);
                    }
                });
                super.onDenied(context, deniedPermissions);


            }
        });



    }


}