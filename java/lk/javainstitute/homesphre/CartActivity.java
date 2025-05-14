package lk.javainstitute.homesphre;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.javainstitute.homesphre.adapter.CartAdapter;
import lk.javainstitute.homesphre.model.CartItem;
import lk.javainstitute.homesphre.model.Product;
import lk.javainstitute.homesphre.model.User;

public class CartActivity extends AppCompatActivity implements CartAdapter.TotalPriceUpdateListener {

    private RecyclerView cartRecyclerView;
    private ProgressBar progressBar;
    private TextView totalPrice;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();
    private String userMobile;

    private Button PlaceOrder;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        totalPrice = findViewById(R.id.totalPrice);
        PlaceOrder = findViewById(R.id.placeOrder);

        PlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cartItems.isEmpty()) {
                    Toast.makeText(CartActivity.this, "Cart is empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(CartActivity.this, OrderActivity.class);
                intent.putExtra("cartItems", new ArrayList<>(cartItems));


                double total = Double.parseDouble(totalPrice.getText().toString().replace("Total: Rs", ""));
                intent.putExtra("total", total);

                startActivity(intent);
                finish();
            }
        });

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences sp = getSharedPreferences("lk.javainstitute.homesphre.data", MODE_PRIVATE);
        String userJson = sp.getString("user", null);

        if (userJson == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Gson gson = new Gson();
        User user = gson.fromJson(userJson, User.class);
        userMobile = user.getMobile();

        loadCartItems();
        enableSwipeToDelete();



    }

    private void enableSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                CartItem item = cartItems.get(position);
                deleteItemFromFirestore(item, position);
            }
        }).attachToRecyclerView(cartRecyclerView);
    }

    private void deleteItemFromFirestore(CartItem item, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String docId = userMobile + "_" + item.getProduct().getDocumentId();

        db.collection("cart").document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    cartItems.remove(position);
                    cartAdapter.notifyItemRemoved(position);
                    calculateTotalPrice();
                    Toast.makeText(this, "Item removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    cartAdapter.notifyItemChanged(position);
                });
    }

    @Override
    public void onTotalPriceUpdated() {
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        double total = 0.0;
        for (CartItem item : cartItems) {
            try {
                // Convert string price to double
                double price = Double.parseDouble(item.getProduct().getProductPrice());
                total += price * item.getQuantity();
            } catch (NumberFormatException e) {
                Log.e("PriceError", "Invalid price format for product: " + item.getProduct().getDocumentId());
                // Handle invalid price format (optional)
            }
        }
        totalPrice.setText(String.format("Total: Rs%.2f", total));
    }

    private void loadCartItems() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore.getInstance().collection("cart")
                .whereEqualTo("userMobile", userMobile)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> cartDocuments = task.getResult().getDocuments();
                        Log.d("CART_DEBUG", "Cart Docs: " + cartDocuments.size());

                        List<String> productIds = new ArrayList<>();
                        for (DocumentSnapshot doc : cartDocuments) {
                            String productId = doc.getString("productId");
                            productIds.add(productId);
                        }

                        if (!productIds.isEmpty()) {
                            FirebaseFirestore.getInstance().collection("products")
                                    .whereIn(FieldPath.documentId(), productIds)
                                    .get()
                                    .addOnSuccessListener(productTask -> {
                                        Map<String, Product> productMap = new HashMap<>();
                                        for (QueryDocumentSnapshot productDoc : productTask) {
                                            Product product = productDoc.toObject(Product.class);
                                            product.setDocumentId(productDoc.getId());
                                            productMap.put(productDoc.getId(), product);
                                        }

                                        cartItems.clear();
                                        for (DocumentSnapshot cartDoc : cartDocuments) {
                                            String productId = cartDoc.getString("productId");
                                            Product product = productMap.get(productId);
                                            if (product != null) {
                                                long quantity = cartDoc.getLong("quantity");
                                                cartItems.add(new CartItem(product, (int) quantity));
                                            }
                                        }

                                        cartAdapter = new CartAdapter(cartItems, this, userMobile, this);
                                        cartRecyclerView.setAdapter(cartAdapter);
                                        calculateTotalPrice();
                                    });
                        } else {
                            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error loading cart: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}