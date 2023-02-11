package com.animalsapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class AnimalAdapter extends ArrayAdapter<AnimalModel> {

    private int resourceLayout;
    private Context mContext;
    PreferencesUtil preferencesUtil;
    NavController navController;

    public AnimalAdapter(Activity activity, Context context, int resource, ArrayList<AnimalModel> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;

        preferencesUtil = new PreferencesUtil(context);
        navController = Navigation.findNavController(activity, R.id.nav_host_fragment);

    }

    @Override
    //Instantiates a layout XML file into its corresponding View objects.
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }

        AnimalModel animalModel = getItem(position);

        if (animalModel != null) {

            ImageView deleteImageView = v.findViewById(R.id.deleteImageView);
            ImageView animalImageView = v.findViewById(R.id.animalImageView);
            TextView nameTextView = v.findViewById(R.id.nameTextView);
            TextView shortDescriptionTextView = v.findViewById(R.id.shortDescriptionTextView);

            CardView constraintLayout = (CardView) v.findViewById(R.id.itemLayout);

            nameTextView.setText(animalModel.getAnimalName());
            shortDescriptionTextView.setText(animalModel.getShortDescription());
            //Visibility of trash icon if User/Admin
            if (preferencesUtil.getString("userType").equalsIgnoreCase("admin")) {
                deleteImageView.setVisibility(View.VISIBLE);
            } else {
                deleteImageView.setVisibility(View.GONE);

            }
            //An image loading and caching library for Android focused on smooth scrolling
            Glide.with(mContext)
                    .load(animalModel.getImage())
                    .circleCrop()
                    .into((animalImageView));


            constraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                //Pass selected animal data to fragment
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("model", animalModel);
                    navController.navigate(R.id.action_homeUserFragment_to_animalDetailsFragment, bundle);

                }
            });
            //delete object from firebase
            deleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatabaseReference animal = FirebaseDatabase.getInstance().getReference()
                            .child("animal")
                            .child(animalModel.getaId());
                    animal.removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            Toast.makeText(mContext, "Animal details deleted successful", Toast.LENGTH_SHORT).show();

                        }
                    });

                }
            });


        }

        return v;
    }

}