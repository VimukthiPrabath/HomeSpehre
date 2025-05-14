package lk.javainstitute.homesphre;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyOrderActivity extends AppCompatActivity {

    private RecyclerView ordersRecyclerView;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_order);

        // Get logged-in user from SharedPreferences
        SharedPreferences sp = getSharedPreferences("lk.javainstitute.homesphre.data", MODE_PRIVATE);
        String userJson = sp.getString("user", "");
        User user = new Gson().fromJson(userJson, User.class);
        currentUserId = user.getMobile();

        db = FirebaseFirestore.getInstance();
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadUserOrders();
    }

    private void loadUserOrders() {
        db.collection("orders")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Order> orders = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Order order = document.toObject(Order.class);
                            orders.add(order);
                        }
                        ordersRecyclerView.setAdapter(new OrderAdapter(orders));
                    }
                    // Handle errors
                });
    }

    public static class User {
        private String email, mobile, username, address;

        public User() {
        }

        public User(String email, String mobile, String username, String address) {
            this.email = email;
            this.mobile = mobile;
            this.username = username;
            this.address = address;
        }

        // Getters
        public String getEmail() {
            return email;
        }

        public String getMobile() {
            return mobile;
        }

        public String getUsername() {
            return username;
        }

        public String getAddress() {
            return address;
        }

        // Setters
        public void setEmail(String email) {
            this.email = email;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    public static class Order {
        private String orderId, paymentId, status, userId, userAddress, userMobile, userName;
        private Timestamp orderDate, paymentDate;
        private double totalAmount;
        private List<OrderItem> items;

        public Order() {
        }

        // Getters
        public String getOrderId() {
            return orderId;
        }

        public String getPaymentId() {
            return paymentId;
        }

        public String getStatus() {
            return status;
        }

        public String getUserId() {
            return userId;
        }

        public String getUserAddress() {
            return userAddress;
        }

        public String getUserMobile() {
            return userMobile;
        }

        public String getUserName() {
            return userName;
        }

        public Timestamp getOrderDate() {
            return orderDate;
        }

        public Timestamp getPaymentDate() {
            return paymentDate;
        }

        public double getTotalAmount() {
            return totalAmount;
        }

        public List<OrderItem> getItems() {
            return items;
        }

        // Setters
        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public void setPaymentId(String paymentId) {
            this.paymentId = paymentId;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public void setUserAddress(String userAddress) {
            this.userAddress = userAddress;
        }

        public void setUserMobile(String userMobile) {
            this.userMobile = userMobile;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public void setOrderDate(Timestamp orderDate) {
            this.orderDate = orderDate;
        }

        public void setPaymentDate(Timestamp paymentDate) {
            this.paymentDate = paymentDate;
        }

        public void setTotalAmount(double totalAmount) {
            this.totalAmount = totalAmount;
        }

        public void setItems(List<OrderItem> items) {
            this.items = items;
        }
    }

    public static class OrderItem {
        private String imageUrl, price, productId, productName;
        private int quantity;

        public OrderItem() {
        }

        // Getters
        public String getImageUrl() {
            return imageUrl;
        }

        public String getPrice() {
            return price;
        }

        public String getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public int getQuantity() {
            return quantity;
        }

        // Setters
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    // Adapter classes
    private class OrderAdapter extends RecyclerView.Adapter<OrderViewHolder> {
        private List<Order> orders;

        public OrderAdapter(List<Order> orders) {
            this.orders = orders;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new OrderViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.myorder_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = orders.get(position);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

            holder.orderId.setText("Order #" + order.getOrderId());
            holder.orderDate.setText(sdf.format(order.getOrderDate().toDate()));
            holder.totalAmount.setText(String.format("Rs.%.2f", order.getTotalAmount()));

            // Status color
            int bgRes = order.getStatus().equalsIgnoreCase("PAID") ?
                    R.drawable.status_paid : R.drawable.status_cancelled;
            holder.status.setBackgroundResource(bgRes);
            holder.status.setText(order.getStatus());

            // Setup items recycler view
            holder.itemsRecycler.setAdapter(new OrderItemAdapter(order.getItems()));

        }

        @Override
        public int getItemCount() {
            return orders.size();
        }
    }

    private class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, orderDate, totalAmount, status;
        RecyclerView itemsRecycler;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.txtOrderId);
            orderDate = itemView.findViewById(R.id.txtOrderDate);
            totalAmount = itemView.findViewById(R.id.txtTotalAmount);
            status = itemView.findViewById(R.id.txtStatus);
            itemsRecycler = itemView.findViewById(R.id.itemsRecycler);
            itemsRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }
    }

    private class OrderItemAdapter extends RecyclerView.Adapter<ItemViewHolder> {
        private List<OrderItem> items;

        public OrderItemAdapter(List<OrderItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ItemViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.order_product_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            OrderItem item = items.get(position);
            holder.productName.setText(item.getProductName());
            holder.price.setText("Rs." + item.getPrice());
            holder.quantity.setText("Qty: " + item.getQuantity());

            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .into(holder.productImage);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, price, quantity;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.imgProduct);
            productName = itemView.findViewById(R.id.txtProductName);
            price = itemView.findViewById(R.id.txtPrice);
            quantity = itemView.findViewById(R.id.txtQuantity);
        }
    }
}