package lk.javainstitute.homesphre.navigationAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lk.javainstitute.homesphre.OrderDetailsActivity;
import lk.javainstitute.homesphre.R;

public class OrderFragment extends Fragment {

    private RecyclerView ordersRecyclerView;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        ordersRecyclerView = view.findViewById(R.id.ordersRecycleView);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ordersRecyclerView.setAdapter(new OrderAdapter(new ArrayList<>()));

        loadOrders();

        return view;
    }

    private void loadOrders() {
        db.collection("orders")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Order> orders = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Order order = document.toObject(Order.class);
                            orders.add(order);
                        }
                        ((OrderAdapter) ordersRecyclerView.getAdapter()).updateOrders(orders);
                    }
                    // Handle errors here
                });
    }

    // Model class as inner class
    public static class Order {
        private String orderId, paymentId, status, userId, userAddress, userMobile, userName;
        private Timestamp orderDate, paymentDate;
        private double totalAmount;
        private List<OrderItem> items;

        private String deliveryStatus;  // Add this field
        private Timestamp statusUpdateTime;  // Add this field


        public Order() {}  // Needed for Firestore

        // Add getters and setters for all fields
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUserAddress() { return userAddress; }
        public void setUserAddress(String userAddress) { this.userAddress = userAddress; }
        public String getUserMobile() { return userMobile; }
        public void setUserMobile(String userMobile) { this.userMobile = userMobile; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public Timestamp getOrderDate() { return orderDate; }
        public void setOrderDate(Timestamp orderDate) { this.orderDate = orderDate; }
        public Timestamp getPaymentDate() { return paymentDate; }
        public void setPaymentDate(Timestamp paymentDate) { this.paymentDate = paymentDate; }
        public double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
        public List<OrderItem> getItems() { return items; }
        public void setItems(List<OrderItem> items) { this.items = items; }

        public String getDeliveryStatus() { return deliveryStatus; }
        public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
        public Timestamp getStatusUpdateTime() { return statusUpdateTime; }
        public void setStatusUpdateTime(Timestamp statusUpdateTime) { this.statusUpdateTime = statusUpdateTime; }
    }

    // OrderItem inner class
    public static class OrderItem {
        private String imageUrl, price, productId, productName;
        private int quantity;

        public OrderItem() {}  // Needed for Firestore

        // Getters and setters
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    // Adapter as inner class
    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

        private List<Order> orderList;

        public OrderAdapter(List<Order> orderList) {
            this.orderList = orderList;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.order_item_layout, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = orderList.get(position);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm:ss a", Locale.getDefault());

            holder.orderId.setText("Order ID: " + order.getOrderId());
            holder.customerName.setText("Customer: " + order.getUserName());
            holder.totalAmount.setText(String.format("Total: Rs.%.2f", order.getTotalAmount()));

            String status = order.getStatus();
            holder.status.setText(status);

            if (status != null) {
                switch (status.toUpperCase()) {
                    case "PAID":
                        holder.status.setBackgroundResource(R.drawable.status_paid);
                        break;
                    case "CANCELLED":
                        holder.status.setBackgroundResource(R.drawable.status_cancelled);
                        break;
                    default:
                        holder.status.setBackgroundResource(R.drawable.status_background);
                        break;
                }
            }

            if (order.getOrderDate() != null) {
                holder.orderDate.setText("Order Date: " + sdf.format(order.getOrderDate().toDate()));
            }

            holder.cardView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), OrderDetailsActivity.class);
                intent.putExtra("ORDER_ID", order.getOrderId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return orderList.size();
        }

        public void updateOrders(List<Order> newOrders) {
            orderList.clear();
            orderList.addAll(newOrders);
            notifyDataSetChanged();
        }

        // ViewHolder class
        class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView orderId, customerName, totalAmount, status, orderDate;
            CardView cardView;

            public OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.orderCard);
                orderId = itemView.findViewById(R.id.txtOrderId);
                customerName = itemView.findViewById(R.id.txtCustomerName);
                totalAmount = itemView.findViewById(R.id.txtTotalAmount);
                status = itemView.findViewById(R.id.txtStatus);
                orderDate = itemView.findViewById(R.id.txtOrderDate);

            }
        }
    }
}