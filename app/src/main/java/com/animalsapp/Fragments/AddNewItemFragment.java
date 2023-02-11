package com.animalsapp.Fragments;

import static android.app.Activity.RESULT_OK;
import static com.animalsapp.MainActivity.hideKeyboard;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.animalsapp.AnimalModel;
import com.animalsapp.R;
import com.animalsapp.databinding.FragmentAddNewItemBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.HashMap;


public class AddNewItemFragment extends Fragment {

    FragmentAddNewItemBinding binding;


    ProgressDialog progressDialog;
    AnimalModel animalModel = new AnimalModel();

    NavController navController;
    StorageReference mstorageref;
    private Uri imageUri;
    private String id;

    boolean update;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddNewItemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialog = new ProgressDialog(getActivity(), R.style.AppCompatAlertDialogStyle); //Initialize ProgressDialog class
        progressDialog.setMessage("Please Wait....");
        progressDialog.setCancelable(false);

        mstorageref = FirebaseStorage.getInstance().getReference();


        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);

        if (getArguments() != null) {

            update = true;

            animalModel = (AnimalModel) getArguments().getSerializable("model");

            Glide.with(getActivity())
                    .load(animalModel.getImage())
                    .into((binding.imgPost));

            binding.nameEditText.setText(animalModel.getAnimalName());
            binding.disEditText.setText(animalModel.getShortDescription());
            binding.longDesEditText.setText(animalModel.getLongDescription());
            binding.addButton.setText("Update Details");


        }
        else {
            update = false;
            binding.addButton.setText("Add Details");
        }


        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!update) {
                    navController.navigate(R.id.action_addNewItemFragment_to_homeUserFragment);

                } else {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("model", animalModel);
                    navController.navigate(R.id.action_addNewItemFragment_to_animalDetailsFragment,bundle);
                }
            }
        });


        binding.imgPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] permission = {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};


                Permissions.check(getActivity(), permission, null, null, new PermissionHandler() {
                    @Override
                    public void onGranted() {

                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, 100);


                    }
                });

            }
        });


        binding.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (!update) {
                    if (imageUri == null) {
                        Toast.makeText(getActivity(), "Please add product image", Toast.LENGTH_SHORT).show();

                    } else if (binding.nameEditText.getText().toString().isEmpty()
                            || binding.disEditText.getText().toString().isEmpty()
                            || binding.longDesEditText.getText().toString().isEmpty()) {
                        Toast.makeText(getActivity(), "All text fields required", Toast.LENGTH_SHORT).show();
                    } else {
                        hideKeyboard(getActivity());
                        progressDialog.show();
                        final StorageReference reference = mstorageref.child("Image/-" + System.currentTimeMillis());
                        reference.putFile(imageUri)
                                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NotNull Task<UploadTask.TaskSnapshot> task) {
                                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri productImage) {
                                                if (task.isSuccessful()) {

                                                    id = String.valueOf(System.currentTimeMillis());
                                                    HashMap<String, Object> userHash = new HashMap<>();
                                                    userHash.put("shortDescription", binding.disEditText.getText().toString());
                                                    userHash.put("image", productImage.toString());
                                                    userHash.put("animalName", binding.nameEditText.getText().toString());
                                                    userHash.put("longDescription", binding.longDesEditText.getText().toString());
                                                    userHash.put("orderBy", id);

                                                    DatabaseReference users = FirebaseDatabase.getInstance().getReference()
                                                            .child("animal")
                                                            .child(id);
                                                    users.updateChildren(userHash)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    progressDialog.dismiss();
                                                                    binding.longDesEditText.setText("");
                                                                    binding.nameEditText.setText("");
                                                                    binding.disEditText.setText("");
                                                                    binding.imgPost.setImageURI(null);
                                                                    Toast.makeText(getActivity(), "Animal details add successful", Toast.LENGTH_SHORT).show();

                                                                }
                                                            });


                                                } else {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(getActivity(), "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }
                } else {
                    if (binding.nameEditText.getText().toString().isEmpty()
                            || binding.disEditText.getText().toString().isEmpty()
                            || binding.longDesEditText.getText().toString().isEmpty()) {
                        Toast.makeText(getActivity(), "All text fields required", Toast.LENGTH_SHORT).show();
                    } else if (imageUri == null) {
                        progressDialog.show();
                        id = animalModel.getaId();
                        HashMap<String, Object> userHash = new HashMap<>();
                        userHash.put("shortDescription", binding.disEditText.getText().toString());
                        userHash.put("animalName", binding.nameEditText.getText().toString());
                        userHash.put("longDescription", binding.longDesEditText.getText().toString());
                        userHash.put("orderBy", id);

                        DatabaseReference users = FirebaseDatabase.getInstance().getReference()
                                .child("animal")
                                .child(id);
                        users.updateChildren(userHash)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressDialog.dismiss();
                                        binding.longDesEditText.setText("");
                                        binding.nameEditText.setText("");
                                        binding.disEditText.setText("");
                                        binding.imgPost.setImageURI(null);
                                        Toast.makeText(getActivity(), "Animal details update successful", Toast.LENGTH_SHORT).show();
                                        navController.navigate(R.id.action_addNewItemFragment_to_homeUserFragment);
                                    }
                                });
                    } else {
                        hideKeyboard(getActivity());
                        progressDialog.show();
                        final StorageReference reference = mstorageref.child("Image/-" + System.currentTimeMillis());
                        reference.putFile(imageUri)
                                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NotNull Task<UploadTask.TaskSnapshot> task) {
                                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri productImage) {
                                                if (task.isSuccessful()) {

                                                    id = animalModel.getaId();
                                                    HashMap<String, Object> userHash = new HashMap<>();
                                                    userHash.put("shortDescription", binding.disEditText.getText().toString());
                                                    userHash.put("image", productImage.toString());
                                                    userHash.put("animalName", binding.nameEditText.getText().toString());
                                                    userHash.put("longDescription", binding.longDesEditText.getText().toString());
                                                    userHash.put("orderBy", id);

                                                    DatabaseReference users = FirebaseDatabase.getInstance().getReference()
                                                            .child("animal")
                                                            .child(id);
                                                    users.updateChildren(userHash)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    progressDialog.dismiss();
                                                                    binding.longDesEditText.setText("");
                                                                    binding.nameEditText.setText("");
                                                                    binding.disEditText.setText("");
                                                                    binding.imgPost.setImageURI(null);
                                                                    Toast.makeText(getActivity(), "Animal details update successful", Toast.LENGTH_SHORT).show();
                                                                    navController.navigate(R.id.action_addNewItemFragment_to_homeUserFragment);
                                                                }
                                                            });


                                                } else {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(getActivity(), "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }

                }


            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                if (!update) {
                    navController.navigate(R.id.action_addNewItemFragment_to_homeUserFragment);

                } else {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("model", animalModel);
                    navController.navigate(R.id.action_addNewItemFragment_to_animalDetailsFragment,bundle);
                }

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            imageUri = data.getData();
            binding.imgPost.setImageURI(imageUri);
        }

    }
}