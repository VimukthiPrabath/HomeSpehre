package lk.javainstitute.homesphre.navigationAdmin;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import lk.javainstitute.homesphre.R;
import lk.javainstitute.homesphre.helpers.Admin;
import lk.javainstitute.homesphre.helpers.AdminDbHelper;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class AdminProfileFragment extends Fragment {

    private Button btnEditAdminProfile;
    private TextView tvAdminName, tvAdminEmail, tvAdminMobile;
    private AdminDbHelper dbHelper;
    private Admin currentAdmin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_profile, container, false);

        btnEditAdminProfile = view.findViewById(R.id.btnEditProfile);
        tvAdminName = view.findViewById(R.id.tvAdminName);
        tvAdminEmail = view.findViewById(R.id.tvAdminEmail);
        tvAdminMobile = view.findViewById(R.id.tvAdminMobile);

        dbHelper = new AdminDbHelper(getContext());

        loadAdminProfile();

        btnEditAdminProfile.setOnClickListener(v -> showEditProfilePopup());

        return view;
    }

    private void loadAdminProfile() {
        currentAdmin = dbHelper.getAdmin();

        if (currentAdmin != null) {
            tvAdminName.setText(currentAdmin.getName());
            tvAdminEmail.setText(currentAdmin.getEmail());
            tvAdminMobile.setText(currentAdmin.getMobile());
        } else {
            Toast.makeText(getContext(), "No admin data found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditProfilePopup() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View popupView = inflater.inflate(R.layout.popup_edit_admin_profile, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(popupView);

        AlertDialog dialog = builder.create();
        dialog.show();

        EditText etPopupAdminName = popupView.findViewById(R.id.etPopupAdminName);
        EditText etPopupAdminEmail = popupView.findViewById(R.id.etPopupAdminEmail);
        EditText etPopupAdminMobile = popupView.findViewById(R.id.etPopupAdminMobile);
        EditText etPopupAdminPassword = popupView.findViewById(R.id.etPopupAdminPassword);

        Button btnCancelPopup = popupView.findViewById(R.id.btnCancelPopup);
        Button btnSavePopup = popupView.findViewById(R.id.btnSavePopup);


        if (currentAdmin != null) {
            etPopupAdminName.setText(currentAdmin.getName());
            etPopupAdminEmail.setText(currentAdmin.getEmail());
            etPopupAdminMobile.setText(currentAdmin.getMobile());
            etPopupAdminPassword.setText(currentAdmin.getPassword());
        }

        btnCancelPopup.setOnClickListener(v -> dialog.dismiss());

        btnSavePopup.setOnClickListener(v -> {
            String updatedName = etPopupAdminName.getText().toString().trim();
            String updatedEmail = etPopupAdminEmail.getText().toString().trim();
            String updatedMobile = etPopupAdminMobile.getText().toString().trim();
            String updatedPassword = etPopupAdminPassword.getText().toString().trim();


            if (updatedName.isEmpty() || updatedEmail.isEmpty() || updatedMobile.isEmpty() || updatedPassword.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            AdminDbHelper dbHelper = new AdminDbHelper(getContext());


            Admin existingAdmin = dbHelper.getAdmin();
            if (existingAdmin == null) {
                Toast.makeText(getContext(), "Admin not found in SQLite", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean sqliteUpdated = dbHelper.updateAdmin(existingAdmin.getEmail(), updatedName, updatedEmail, updatedMobile, updatedPassword);

            if (sqliteUpdated) {
                Toast.makeText(getContext(), "SQLite updated!", Toast.LENGTH_SHORT).show();


                updateAdminInFirestore(existingAdmin.getEmail(), updatedName, updatedEmail, updatedMobile, updatedPassword);
            } else {
                Toast.makeText(getContext(), "Failed to update SQLite", Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
        });

    }

    private void updateAdminInFirestore(String email, String name, String mobile, String password) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", name);
        updatedData.put("email", email);
        updatedData.put("mobile", mobile);
        updatedData.put("password", password);

        firestore.collection("Admin")
                .document(email)
                .set(updatedData)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Firestore updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Firestore update failed", Toast.LENGTH_SHORT).show());
    }

    private void updateAdminInFirestore(String oldEmail, String newName, String newEmail, String newMobile, String newPassword) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        db.collection("Admin")
                .whereEqualTo("email", oldEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String documentId = document.getId();

                            db.collection("Admin").document(documentId)
                                    .update(
                                            "name", newName,
                                            "email", newEmail,
                                            "mobile", newMobile,
                                            "password", newPassword
                                    )
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Firebase updated!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Firebase update failed!", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(getContext(), "Admin not found in Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
