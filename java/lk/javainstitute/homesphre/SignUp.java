package lk.javainstitute.homesphre;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        TextView textView3 = findViewById(R.id.gotoSignIn);

        textView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUp.this, SignIn.class);
                startActivity(intent);
                finish();
            }
        });


        TextView buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextInputEditText editText1 = findViewById(R.id.textInputEditTextUser);
                TextInputEditText editText2 = findViewById(R.id.textInputEditTextEmail);
                TextInputEditText editText3 = findViewById(R.id.textInputEditTextMobile);
                TextInputEditText editText4 = findViewById(R.id.textInputEditTextPassword);

                String userName = String.valueOf(editText1.getText());
                String email = String.valueOf(editText2.getText());
                String mobile = String.valueOf(editText3.getText());
                String password = String.valueOf(editText4.getText());

                String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

                if (userName.isEmpty()) {
                    Toast.makeText(SignUp.this, "Please Enter Your Username", Toast.LENGTH_SHORT).show();
                } else if (userName.length() > 20) {
                    Toast.makeText(SignUp.this, "Username too long (max 20 characters)", Toast.LENGTH_SHORT).show();
                } else if (email.isEmpty()) {
                    Toast.makeText(SignUp.this, "Please Enter Your Email", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(SignUp.this, "Invalid Email format", Toast.LENGTH_SHORT).show();
                } else if (mobile.isEmpty()) {
                    Toast.makeText(SignUp.this, "Please Enter Your Mobile Number", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(SignUp.this, "Please Enter Your Password", Toast.LENGTH_SHORT).show();
                } else if (!password.matches(passwordPattern)) {
                    Toast.makeText(SignUp.this,
                            "Password must include:\\n- 8+ characters\\n- 1 uppercase\\n- 1 lowercase\\n- 1 digit\\n- 1 special symbol (@$!%*?&)",
                            Toast.LENGTH_SHORT).show();
                }else {

                    HashMap<String, Object> userMap = new HashMap<>();
                    userMap.put("username", userName);
                    userMap.put("email", email);
                    userMap.put("mobile", mobile);
                    userMap.put("password", password);

                    firestore.collection("User").add(userMap)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(SignUp.this, "User Registration Successfully", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SignUp.this, "Somthing Went Wrong!", Toast.LENGTH_SHORT).show();
                                }
                            });

                    editText1.setText("");
                    editText2.setText("");
                    editText3.setText("");
                    editText4.setText("");
                }

            }
        });


    }
}