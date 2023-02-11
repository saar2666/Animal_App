package com.animalsapp.Fragments;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.animalsapp.AnimalAdapter;
import com.animalsapp.AnimalModel;
import com.animalsapp.PreferencesUtil;
import com.animalsapp.R;
import com.animalsapp.databinding.FragmentHomeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;


    PreferencesUtil preferencesUtil;
    ProgressDialog progressDialog;
    ArrayList<AnimalModel> animalModelArrayList = new ArrayList<>();
    AnimalModel animalModel = new AnimalModel();


    AnimalAdapter adapter;

    NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //Data load message
        super.onViewCreated(view, savedInstanceState);
        progressDialog = new ProgressDialog(getActivity(), R.style.AppCompatAlertDialogStyle); //Initialize ProgressDialog class
        progressDialog.setMessage("Please Wait....");
        progressDialog.setCancelable(false);
        preferencesUtil = new PreferencesUtil(getContext());

        //Display add button if logged in as admin
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        if (preferencesUtil.getString("userType").equalsIgnoreCase("admin")) {
            binding.addImageView.setVisibility(View.VISIBLE);
        }

        // Navigate to Add new item fragment
        binding.addImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_homeUserFragment_to_addNewItemFragment);


            }
        });


        adapter = new AnimalAdapter(getActivity(), getActivity(), R.layout.item_animal, animalModelArrayList);
        binding.lisView.setAdapter(adapter);

        getAnimalList();


        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                ArrayList<AnimalModel> temp = new ArrayList();
                for (AnimalModel a : animalModelArrayList) {
                    String text = a.getAnimalName().toLowerCase();
                    if (text.contains(editable.toString().toLowerCase())) {
                        temp.add(a);

                    }
                }
                adapter = new AnimalAdapter(getActivity(), getActivity(), R.layout.item_animal, temp);
                binding.lisView.setAdapter(adapter);


            }
        });

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event

                getActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);


    }

    //Get data from Firebase

    private void getAnimalList() {
        progressDialog.show();
        Query animal = FirebaseDatabase.getInstance().getReference()
                .child("animal")
                .orderByKey();

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null) {
                    animalModelArrayList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        animalModel = dataSnapshot.getValue(AnimalModel.class);
                        animalModel.setaId(dataSnapshot.getKey());
                        animalModelArrayList.add(animalModel);


                    }
                    adapter.notifyDataSetChanged();
                    progressDialog.dismiss();

                    Log.e("animalList", "" + animalModelArrayList);


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
            }
        };


        animal.addValueEventListener(valueEventListener);


    }

}