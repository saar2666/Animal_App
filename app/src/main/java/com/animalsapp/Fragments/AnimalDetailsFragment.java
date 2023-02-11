package com.animalsapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.animalsapp.AnimalModel;
import com.animalsapp.PreferencesUtil;
import com.animalsapp.R;
import com.animalsapp.databinding.FragmentAnimalDetailsBinding;
import com.bumptech.glide.Glide;


public class AnimalDetailsFragment extends Fragment {


    FragmentAnimalDetailsBinding binding;

    PreferencesUtil preferencesUtil;
    AnimalModel animalModel = new AnimalModel();

    NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAnimalDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        preferencesUtil = new PreferencesUtil(getActivity());

        if (getArguments()!=null){
            animalModel = (AnimalModel) getArguments().getSerializable("model");

        }

        if (animalModel != null) {
            binding.animalNameTextView.setText(animalModel.getAnimalName());
            binding.shortDescriptionTextView.setText(animalModel.getShortDescription());
            binding.longDesTextVext.setText(animalModel.getLongDescription());

            //An image loading and caching library for Android focused on smooth scrolling
            Glide.with(getActivity())
                    .load(animalModel.getImage())
                    .circleCrop()
                    .into((binding.profileImageViewdrawer));

        }

        if (preferencesUtil.getString("userType").equalsIgnoreCase("admin")) {
            binding.addButton.setText("Update Details");

        } else {
            binding.addButton.setText("Edit Long Description");
        }

        binding.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (preferencesUtil.getString("userType").equalsIgnoreCase("admin")) {

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("model", animalModel);
                    navController.navigate(R.id.action_animalDetailsFragment_to_addNewItemFragment, bundle);


                } else {

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("model", animalModel);
                    navController.navigate(R.id.action_animalDetailsFragment_to_editLongDescriptionFragment, bundle);


                }
            }
        });

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                navController.navigate(R.id.action_animalDetailsFragment_to_homeUserFragment);

            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                navController.navigate(R.id.action_animalDetailsFragment_to_homeUserFragment);

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);


    }
}