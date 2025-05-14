package lk.javainstitute.homesphre;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.javainstitute.homesphre.adapter.OrderAdapter;
import lk.javainstitute.homesphre.model.CartItem;
import lk.javainstitute.homesphre.model.Order;
import lk.javainstitute.homesphre.model.User;
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.StatusResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;

public class OrderActivity extends AppCompatActivity implements MapDialogFragment.MapLocationListener {

    private Order currentOrder;
    private User currentUser;
    private OrderAdapter adapter;
    private static final String TAG = "PaymentGateway";
    private int paymentAttempts = 0;
    private static final int MAX_PAYMENT_ATTEMPTS = 3;
    private LatLng selectedDeliveryLocation;

    private Button payNowButton;

    private final ActivityResultLauncher<Intent> payHereLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                int resultCode = result.getResultCode();
                Intent data = result.getData();
                handlePaymentResult(resultCode, data);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        initializeUserData();
        setupUI();
        setupPaymentButton();
        setupDeliveryLocationButton();
    }

    private void setupDeliveryLocationButton() {
        Button locationButton = findViewById(R.id.deliveryLocationButton);
        locationButton.setOnClickListener(v -> {
            MapDialogFragment dialog = new MapDialogFragment();
            dialog.setLocationListener(this);
            dialog.show(getSupportFragmentManager(), "map_dialog");
        });
    }

    @Override
    public void onLocationSelected(LatLng location) {
        selectedDeliveryLocation = location;
        updateDeliveryLocation(location);
    }

    private void updateDeliveryLocation(LatLng location) {
        TextView addressView = findViewById(R.id.userAddress);
        String addressText = String.format("Delivery Location: %.6f, %.6f",
                location.latitude,
                location.longitude);
        addressView.setText(addressText);

        currentOrder.setUserAddress(addressText);
        currentOrder.setDeliveryLat(location.latitude);
        currentOrder.setDeliveryLng(location.longitude);
        updatePaymentButtonState();
    }

    private void initializeUserData() {
        SharedPreferences sp = getSharedPreferences("lk.javainstitute.homesphre.data", MODE_PRIVATE);
        Gson gson = new Gson();
        currentUser = gson.fromJson(sp.getString("user", null), User.class);

        ArrayList<CartItem> cartItems = (ArrayList<CartItem>) getIntent().getSerializableExtra("cartItems");
        double total = getIntent().getDoubleExtra("total", 0.0);

        currentOrder = new Order(
                generateOrderId(),
                currentUser.getMobile(),
                currentUser.getUsername(),
                currentUser.getMobile(),
                currentUser.getAddress(),
                cartItems,
                total
        );
    }

    private void setupUI() {
        TextView orderIdView = findViewById(R.id.orderId);
        TextView orderDate = findViewById(R.id.orderDate);
        TextView userName = findViewById(R.id.userName);
        TextView userMobile = findViewById(R.id.userMobile);
        TextView userAddress = findViewById(R.id.userAddress);
        TextView orderTotal = findViewById(R.id.orderTotal);
        RecyclerView itemsRecycler = findViewById(R.id.orderItemsRecycler);

        orderIdView.setText("Order ID: " + currentOrder.getOrderId());
        orderDate.setText("Date: " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()));
        userName.setText("Name: " + currentOrder.getUserName());
        userMobile.setText("Mobile: " + currentOrder.getUserMobile());
        userAddress.setText("Address: " + currentOrder.getUserAddress());
        orderTotal.setText(String.format("Total: Rs%.2f", currentOrder.getTotalAmount()));

        if (currentOrder.getItems() != null && !currentOrder.getItems().isEmpty()) {
            adapter = new OrderAdapter(currentOrder.getItems());
            itemsRecycler.setLayoutManager(new LinearLayoutManager(this));
            itemsRecycler.setAdapter(adapter);
        } else {
            Toast.makeText(this, "No items in order", Toast.LENGTH_SHORT).show();
        }

        payNowButton = findViewById(R.id.payNowButton);
        updatePaymentButtonState();
    }

    private void updatePaymentButtonState() {
        boolean hasLocation = selectedDeliveryLocation != null;
        payNowButton.setEnabled(hasLocation);
        payNowButton.setBackgroundTintList(ColorStateList.valueOf(
                hasLocation ?
                        ContextCompat.getColor(this, R.color.colorPrimary) :
                        ContextCompat.getColor(this, R.color.disabled_grey)
        ));
    }

    private void setupPaymentButton() {
        payNowButton.setOnClickListener(v -> {
            if (paymentAttempts >= MAX_PAYMENT_ATTEMPTS) {
                Toast.makeText(this, "Maximum payment attempts reached", Toast.LENGTH_SHORT).show();
                return;
            }
            if(selectedDeliveryLocation == null) {
                Toast.makeText(this, "Please select delivery location first", Toast.LENGTH_SHORT).show();
                return;
            }
            saveOrderToFirestore(this::initiatePayment);
        });
    }

    private void saveOrderToFirestore(Runnable onSuccess) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> orderData = createOrderData();

        db.collection("orders").document(currentOrder.getOrderId())
                .set(orderData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Order saved successfully", Toast.LENGTH_SHORT).show();
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save order", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error saving order", e);
                });
    }

    private Map<String, Object> createOrderData() {
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", currentOrder.getOrderId());
        orderData.put("userId", currentOrder.getUserId());
        orderData.put("userName", currentOrder.getUserName());
        orderData.put("userMobile", currentOrder.getUserMobile());
        orderData.put("userAddress", currentOrder.getUserAddress());
        orderData.put("totalAmount", currentOrder.getTotalAmount());
        orderData.put("orderDate", new Date());
        orderData.put("status", "PENDING");
        orderData.put("deliveryLat", currentOrder.getDeliveryLat());
        orderData.put("deliveryLng", currentOrder.getDeliveryLng());

        List<Map<String, Object>> itemsList = new ArrayList<>();
        for (CartItem item : currentOrder.getItems()) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("productId", item.getProduct().getDocumentId());
            itemMap.put("productName", item.getProduct().getProductName());
            itemMap.put("price", item.getProduct().getProductPrice());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("imageUrl", item.getProduct().getImageUrl());
            itemsList.add(itemMap);
        }
        orderData.put("items", itemsList);

        return orderData;
    }

    // Rest of the class remains the same as your original code
    // [Keep all other methods unchanged below this point]
    // initiatePayment(), handlePaymentResult(), handlePaymentResponse(),
    // handlePaymentSuccess(), handlePaymentFailure(), handlePaymentCancellation(),
    // splitName(), updateOrderStatus(), clearCartAfterOrder(),
    // navigateToSuccessScreen(), generateOrderId()

    private void initiatePayment() {
        try {
            paymentAttempts++;

            InitRequest request = new InitRequest();
            Intent intent = new Intent(this, PHMainActivity.class);

            request.setMerchantId("1221394");
            request.setMerchantSecret("MTUwMTMwOTA3OTM1NDUyNTE0OTAzMDU2MjIzNjY2MTcxNzQ2NzYxOQ==");
            request.setCurrency("LKR");
            request.setAmount(currentOrder.getTotalAmount());
            request.setOrderId(currentOrder.getOrderId());
            request.setItemsDescription("HomeSphere Order - " + currentOrder.getOrderId());

            String[] names = splitName(currentUser.getUsername());
            request.getCustomer().setEmail(currentUser.getEmail());
            request.getCustomer().setFirstName(names[0]);
            request.getCustomer().setLastName(names[1]);
            request.getCustomer().setPhone(currentUser.getMobile());
            request.getCustomer().getAddress().setAddress(currentOrder.getUserAddress());
            request.getCustomer().getAddress().setCity("Colombo");
            request.getCustomer().getAddress().setCountry("Sri Lanka");

            List<Item> payHereItems = new ArrayList<>();
            for (CartItem cartItem : currentOrder.getItems()) {
                double price = Double.parseDouble(cartItem.getProduct().getProductPrice());
                payHereItems.add(new Item(
                        cartItem.getProduct().getDocumentId(),
                        cartItem.getProduct().getProductName(),
                        cartItem.getQuantity(),
                        price
                ));
            }
            request.setItems(payHereItems);

            intent.putExtra(PHConstants.INTENT_EXTRA_DATA, request);
            PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
            payHereLauncher.launch(intent);

        } catch (Exception e) {
            Log.e(TAG, "Payment initialization failed", e);
            Toast.makeText(this, "Error initializing payment", Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePaymentResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            handlePaymentResponse(data);
        } else if (resultCode == Activity.RESULT_CANCELED) {
            handlePaymentCancellation();
        }
    }

    private void handlePaymentResponse(Intent data) {
        try {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>)
                    data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

            if (response != null) {
                if (response.isSuccess()) {
                    handlePaymentSuccess(response.getData());
                } else {
                    handlePaymentFailure(response);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Payment processing error", e);
            Toast.makeText(this, "Payment processing failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePaymentSuccess(StatusResponse statusResponse) {
        String paymentId = String.valueOf(statusResponse.getPaymentNo());
        String message = statusResponse.getMessage();

        updateOrderStatus("PAID", paymentId);
        clearCartAfterOrder();
        navigateToSuccessScreen();

        Toast.makeText(this, "Payment Successful: " + message, Toast.LENGTH_LONG).show();
    }

    private void handlePaymentFailure(PHResponse<StatusResponse> response) {
        String errorMessage = "Payment Failed: ";
        if (response != null) {
            errorMessage += response.getData().getMessage();
            if (response.getData() != null) {
                errorMessage += " - " + response.getData().getMessage();
            }
        }
        updateOrderStatus("FAILED", null);
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void handlePaymentCancellation() {
        updateOrderStatus("CANCELLED", null);
        Toast.makeText(this, "Payment cancelled by user", Toast.LENGTH_SHORT).show();
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return new String[]{"Customer", ""};
        }
        String[] names = fullName.split(" ");
        if (names.length == 1) {
            return new String[]{names[0], ""};
        }
        return new String[]{names[0], names[names.length - 1]};
    }

    private void updateOrderStatus(String status, String paymentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("paymentDate", new Date());

        if (paymentId != null) {
            updates.put("paymentId", paymentId);
        }

        db.collection("orders").document(currentOrder.getOrderId())
                .update(updates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Order status updated"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating order status", e));
    }

    private void clearCartAfterOrder() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("cart")
                .whereEqualTo("userMobile", currentUser.getMobile())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                        }
                    }
                });
    }

    private void navigateToSuccessScreen() {
        Intent intent = new Intent(this, OrderSuccessActivity.class);
        intent.putExtra("orderId", currentOrder.getOrderId());
        intent.putExtra("amount", currentOrder.getTotalAmount());
        startActivity(intent);
        finish();
    }

    private String generateOrderId() {
        return "ORD" + System.currentTimeMillis();
    }
}