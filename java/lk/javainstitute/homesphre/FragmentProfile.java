package lk.javainstitute.homesphre;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import lk.javainstitute.homesphre.model.User;

public class FragmentProfile extends Fragment {

    private TextView textViewNameTop, textViewEmailTop;
    private TextView textViewName, textViewEmail, textViewMobile, textViewAddress;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ImageView logoutImageView = view.findViewById(R.id.logoutImageView);

        logoutImageView.setOnClickListener(v -> showCustomLogoutDialog());


        TextView editProfileBtn = view.findViewById(R.id.textView17);
        editProfileBtn.setOnClickListener(v -> showEditProfileDialog());


        textViewNameTop = view.findViewById(R.id.textView3);
        textViewEmailTop = view.findViewById(R.id.textView4);

        textViewName = view.findViewById(R.id.textView9);
        textViewEmail = view.findViewById(R.id.textView11);
        textViewMobile = view.findViewById(R.id.textView13);
        textViewAddress = view.findViewById(R.id.textView15);


        loadUserData();

        return view;
    }

    private void loadUserData() {

        SharedPreferences sp = requireActivity().getSharedPreferences("lk.javainstitute.homesphre.data", Context.MODE_PRIVATE);


        String userJson = sp.getString("user", null);

        if (userJson != null) {

            Gson gson = new Gson();
            User user = gson.fromJson(userJson, User.class);


            textViewNameTop.setText(user.getUsername());
            textViewEmailTop.setText(user.getEmail());

            textViewName.setText(user.getUsername());
            textViewEmail.setText(user.getEmail());
            textViewMobile.setText(user.getMobile());
            textViewAddress.setText(user.getAddress());
        }
    }


    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_edit_profile, null);
        builder.setView(dialogView);

        EditText editName = dialogView.findViewById(R.id.editTextName);
        EditText editEmail = dialogView.findViewById(R.id.editTextEmail);
        EditText editMobile = dialogView.findViewById(R.id.editTextMobile);
        EditText editAddress = dialogView.findViewById(R.id.editTextAddress);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        SharedPreferences sp = getActivity().getSharedPreferences("lk.javainstitute.homesphre.data", Context.MODE_PRIVATE);
        String userJson = sp.getString("user", null);


        final User[] user = new User[1];

        if (userJson != null) {
            Gson gson = new Gson();
            user[0] = gson.fromJson(userJson, User.class);

            editName.setText(user[0].getUsername());
            editEmail.setText(user[0].getEmail());
            editMobile.setText(user[0].getMobile());
            editAddress.setText(user[0].getAddress());
        }

        AlertDialog alertDialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String newName = editName.getText().toString().trim();
            String newEmail = editEmail.getText().toString().trim();
            String newMobile = editMobile.getText().toString().trim();
            String newAddress = editAddress.getText().toString().trim();

            if (newName.isEmpty() || newEmail.isEmpty() || newMobile.isEmpty() || newAddress.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (user[0] == null) {
                Toast.makeText(getContext(), "User not found in SharedPreferences!", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("User")
                    .whereEqualTo("mobile", user[0].getMobile())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            String docId = task.getResult().getDocuments().get(0).getId();

                            firestore.collection("User").document(docId)
                                    .update(
                                            "email", newEmail,
                                            "mobile", newMobile,
                                            "username", newName,
                                            "address", newAddress
                                    ).addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show();

                                        // Update SharedPreferences
                                        User updatedUser = new User( newEmail, newMobile,newName, newAddress);

                                        SharedPreferences.Editor editor = sp.edit();
                                        editor.putString("user", new Gson().toJson(updatedUser));
                                        editor.apply();

                                        updateProfileViews(updatedUser);

                                        alertDialog.dismiss();
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Failed to update profile!", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(getContext(), "User not found!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        alertDialog.show();
    }


    private void updateProfileViews(User updatedUser) {
        TextView nameTextView = getView().findViewById(R.id.textView9);
        TextView emailTextView = getView().findViewById(R.id.textView11);
        TextView mobileTextView = getView().findViewById(R.id.textView13);
        TextView addressTextView = getView().findViewById(R.id.textView15);

        nameTextView.setText(updatedUser.getUsername());
        emailTextView.setText(updatedUser.getEmail());
        mobileTextView.setText(updatedUser.getMobile());
        addressTextView.setText(updatedUser.getAddress());


        TextView topNameTextView = getView().findViewById(R.id.textView3);
        TextView topEmailTextView = getView().findViewById(R.id.textView4);
        topNameTextView.setText(updatedUser.getUsername());
        topEmailTextView.setText(updatedUser.getEmail());
    }


    private void showCustomLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_logout, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonLogout = dialogView.findViewById(R.id.buttonLogout);

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonLogout.setOnClickListener(v -> {

            SharedPreferences sp = getActivity().getSharedPreferences("lk.javainstitute.homesphre.data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.remove("user");
            editor.apply();


            Intent intent = new Intent(getActivity(), SignIn.class);
            startActivity(intent);


            requireActivity().finish();


            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

            dialog.dismiss();
        });

        dialog.show();
    }



}
