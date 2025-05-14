package lk.javainstitute.homesphre.navigationAdmin;

import android.app.Dialog;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import lk.javainstitute.homesphre.R;
import lk.javainstitute.homesphre.model.User;

public class UserDetailsDialog extends Dialog {

    private TextView tvUsername, tvEmail, tvStatus;
    private Button btnClose;

    public UserDetailsDialog(AppCompatActivity activity, User user) {
        super(activity);
        setContentView(R.layout.dialog_user_details);
        setCancelable(true);


        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvStatus = findViewById(R.id.tvStatus);
        btnClose = findViewById(R.id.btnClose);


        tvUsername.setText(user.getUsername());
        tvEmail.setText(user.getEmail());
        tvStatus.setText(user.isActive() ? "Active" : "Inactive");


        btnClose.setOnClickListener(v -> dismiss());
    }
}
