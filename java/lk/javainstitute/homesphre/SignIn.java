package lk.javainstitute.homesphre;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import lk.javainstitute.homesphre.helpers.AdminDbHelper;
import lk.javainstitute.homesphre.model.User;

public class SignIn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView textViewAdminLogin = findViewById(R.id.textView6);
        textViewAdminLogin.setOnClickListener(v -> showAdminLoginPopup());
        fetchAdminDataFromFirestore();


        TextView textView = findViewById(R.id.textView2);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
                finish();
            }
        });

        TextView buttonSignIn = findViewById(R.id.buttonSignin);
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextInputEditText inputTextMobile = findViewById(R.id.textInputEditText);
                TextInputEditText inputTextPassword = findViewById(R.id.textInputEditText2);

                String mobile = String.valueOf(inputTextMobile.getText());
                String password = String.valueOf(inputTextPassword.getText());

                FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                firestore.collection("User")
                        .whereEqualTo("mobile", mobile)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                boolean userFound = false;

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String Password = document.getString("password");

                                    if (Password != null && Password.equals(password)) {
                                        userFound = true;
                                        String name = document.getString("name");

                                        User user = new User(
                                                document.getString("email"),
                                                document.getString("mobile"),
                                                document.getString("username"),
                                                document.getString("address")
                                        );

                                        Gson gson = new Gson();
                                        String userJson = gson.toJson(user);

                                        SharedPreferences sp = getSharedPreferences("lk.javainstitute.homesphre.data", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sp.edit();
                                        editor.putString("user", userJson);
                                        editor.apply();

                                        Intent intent = new Intent(SignIn.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                        break;
                                    }
                                }
                                if (!userFound) {
                                    Toast.makeText(SignIn.this, "userNotFound", Toast.LENGTH_SHORT).show();
                                }
                            } else {

                            }
                        });
            }
        });
    }

    private void fetchAdminDataFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Admin")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AdminDbHelper dbHelper = new AdminDbHelper(this);

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String email = document.getString("email");
                            String mobile = document.getString("mobile");
                            String password = document.getString("password");

                            dbHelper.insertAdmin(name, email, mobile, password);
                        }


                    } else {
                        Toast.makeText(this, "Error fetching admin data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAdminLoginPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.admin_login_popup, null);

        EditText emailEditText = view.findViewById(R.id.emailEditText);
        EditText passwordEditText = view.findViewById(R.id.passwordEditText);
        Button loginButton = view.findViewById(R.id.loginButton);

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        loginButton.setOnClickListener(v -> {
//            String email = emailEditText.getText().toString().trim();
//            String password = passwordEditText.getText().toString().trim();
//
//            AdminDbHelper dbHelper = new AdminDbHelper(this);
//
//            if (dbHelper.validateAdmin(email, password)) {
//                Toast.makeText(this, "Admin login successful!", Toast.LENGTH_SHORT).show();
//                dialog.dismiss();

            Intent intent = new Intent(this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
//            } else {
//                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
//            }
        });
    }


}