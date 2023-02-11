package com.animalsapp.Fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.animalsapp.GPSClass;
import com.animalsapp.PreferencesUtil;
import com.animalsapp.R;
import com.animalsapp.databinding.FragmentLoginBinding;
import com.blongho.country_data.World;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class LoginFragment extends Fragment {

    FragmentLoginBinding binding;


    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    ProgressDialog progressDialog;
    PreferencesUtil preferencesUtil;
    NavController navController;

    String userType = "user";

    SupportMapFragment mapFragment;
    GPSClass gpsClass;


    GoogleMap map;
    LatLng latLng;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        gpsClass=new GPSClass(getActivity());

        progressDialog = new ProgressDialog(getActivity(), R.style.AppCompatAlertDialogStyle); //Initialize ProgressDialog class
        progressDialog.setMessage("Please Wait....");
        progressDialog.setCancelable(false);



        mapFragment = ((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.maps));


        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        preferencesUtil = new PreferencesUtil(getActivity());


        preferencesUtil.setString("userType", userType);

        binding.patientRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b) {
                    userType = "user";
                    preferencesUtil.setString("userType", userType);
                }
            }
        });
        binding.dentistRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    userType = "admin";
                    preferencesUtil.setString("userType", userType);
                }
            }
        });
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

        binding.donotHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_blankFragment_to_signupFragment);
            }
        });
        //Validate Email & password
        binding.loginImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Email = binding.emailEditText.getText().toString();
                String Password = binding.passwordEditText.getText().toString();
                if (Email.isEmpty() || Password.isEmpty()) {

                    Toast.makeText(getActivity(), "All Fields Required", Toast.LENGTH_SHORT).show();
                } else if (!Email.matches(emailPattern)) {
                    Toast.makeText(getActivity(), "Please Enter Valid Email", Toast.LENGTH_SHORT).show();
                    //Authenticate in Firebase
                } else {
                    if (preferencesUtil.getString("userType").equalsIgnoreCase("user")) {
                        progressDialog.show();
                        FirebaseAuth.getInstance()
                                .signInWithEmailAndPassword(Email, Password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            progressDialog.dismiss();
                                            FirebaseAuth.getInstance().signOut();
                                            navController.navigate(R.id.action_blankFragment_to_homeUserFragment);
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        progressDialog.show();
                        DatabaseReference data = FirebaseDatabase.getInstance().getReference("admin");
                        data.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                Log.e("snapshot", "" + snapshot);
                                Log.e("snapshot", "" + snapshot.child("email").getValue().toString());
                                Log.e("snapshot", "" + snapshot.child("password").getValue().toString());
                                Log.e("snapshot", "" + Email);
                                Log.e("snapshot", "" + Password);


                                if (snapshot.child("email").getValue().toString().equals(Email) &&
                                        snapshot.child("password").getValue().toString().equals(Password)) {
                                    progressDialog.dismiss();
                                    navController.navigate(R.id.action_blankFragment_to_homeUserFragment);
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Login Failed", Toast.LENGTH_SHORT).show();

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                    }

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

                    Geocoder geocoder = new Geocoder(getActivity(), Locale.ENGLISH);

                    try {
                        /**
                         * Geocoder.getFromLocation - Returns an array of Addresses
                         * that are known to describe the area immediately surrounding the given latitude and longitude.
                         */
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

                    }
                    catch (IOException e) {
                        //e.printStackTrace();
                        Log.e("", "Impossible to connect to Geocoder", e);
                    }
                }




            }
        });

    }
}