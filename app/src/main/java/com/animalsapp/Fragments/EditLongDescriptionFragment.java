package com.animalsapp.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.animalsapp.AnimalAdapter;
import com.animalsapp.AnimalModel;
import com.animalsapp.PreferencesUtil;
import com.animalsapp.R;
import com.animalsapp.databinding.FragmentAnimalDetailsBinding;
import com.animalsapp.databinding.FragmentEditLongDescriptionBinding;
import com.animalsapp.databinding.FragmentHomeBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;


public class EditLongDescriptionFragment extends Fragment {


    FragmentEditLongDescriptionBinding binding;

    PreferencesUtil preferencesUtil;
    AnimalModel animalModel = new AnimalModel();

    NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEditLongDescriptionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);


        animalModel = (AnimalModel) getArguments().getSerializable("model");

        if (animalModel!=null){

            binding.longDesEditText.setText(animalModel.getLongDescription());

        }

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("model", animalModel);
                    navController.navigate(R.id.action_editLongDescriptionFragment_to_animalDetailsFragment,bundle);

            }
        });

        binding.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap<String, Object> userHash = new HashMap<>();
                userHash.put("longDescription", binding.longDesEditText.getText().toString());

                DatabaseReference animal = FirebaseDatabase.getInstance().getReference()
                        .child("animal")
                        .child(animalModel.getaId());
                animal.updateChildren(userHash).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {



                        Toast.makeText(getActivity(), "Long Description Update  details", Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.action_editLongDescriptionFragment_to_homeUserFragment);

                    }
                });
            }
        });


        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event

                Bundle bundle = new Bundle();
                bundle.putSerializable("model", animalModel);
                navController.navigate(R.id.action_editLongDescriptionFragment_to_animalDetailsFragment,bundle);

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);




    }
}