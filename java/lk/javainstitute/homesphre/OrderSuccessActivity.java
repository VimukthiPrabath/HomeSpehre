package lk.javainstitute.homesphre;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class OrderSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        // Get order details from intent
        Intent intent = getIntent();
        String orderId = intent.getStringExtra("orderId");
        double amount = intent.getDoubleExtra("amount", 0.0);

        // Initialize views
        TextView tvOrderId = findViewById(R.id.tvOrderId);
        TextView tvAmount = findViewById(R.id.tvAmount);
        Button btnContinue = findViewById(R.id.btnContinue);

        // Set order details
        tvOrderId.setText("Order ID: " + orderId);
        tvAmount.setText(String.format("Total Paid: Rs%.2f", amount));

        // Handle continue shopping button
        btnContinue.setOnClickListener(v -> {
            Intent homeIntent = new Intent(this, HomeActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to payment screen
        Intent homeIntent = new Intent(this, HomeActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        finish();
    }
}