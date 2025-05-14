package lk.javainstitute.homesphre;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.javainstitute.homesphre.R;
import lk.javainstitute.homesphre.navigationAdmin.OrderFragment;

public class OrderDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String orderId;
    private FirebaseFirestore db;

    private TextView orderIdTextView, customerNameTextView, customerMobileTextView,
            addressTextView, orderDateTextView, paymentDateTextView,
            statusTextView, totalAmountTextView;
    private RecyclerView itemsRecyclerView;
    private GoogleMap mMap;

    private static final LatLng STORE_LOCATION = new LatLng(6.677959799113576, 80.39427943405586);
    private static final int MAP_ZOOM_LEVEL = 14;
    private static final int POLYLINE_WIDTH = 5;
    private static final int STORE_MARKER_COLOR = Color.BLUE;
    private static final int DELIVERY_MARKER_COLOR = Color.RED;

    private Marker storeMarker;
    private Marker deliveryMarker;
    private Polyline routePolyline;
    private TextView distanceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get order ID from intent
        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null) {
            finish();
            return;
        }

        // Initialize views
        initializeViews();

        TextView deliveryCoordinatesTextView = findViewById(R.id.deliveryCoordinatesTextView);

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Load order details
        loadOrderDetails();

        setupStatusUpdateButtons();

    }

    private void setupStatusUpdateButtons() {
        Button btnMarkPacked = findViewById(R.id.btnMarkPacked);
        Button btnMarkInTransit = findViewById(R.id.btnMarkInTransit);
        Button btnMarkDelivered = findViewById(R.id.btnMarkDelivered);

        db.collection("orders").document(orderId).get()
                .addOnSuccessListener(document -> {
                    if (!"CANCELLED".equalsIgnoreCase(document.getString("status"))) {
                        btnMarkPacked.setOnClickListener(v -> updateStatus("PACKED"));
                        btnMarkInTransit.setOnClickListener(v -> updateStatus("IN_TRANSIT"));
                        btnMarkDelivered.setOnClickListener(v -> updateStatus("DELIVERED"));
                    } else {
                        // Disable buttons for cancelled orders
                        btnMarkPacked.setEnabled(false);
                        btnMarkInTransit.setEnabled(false);
                        btnMarkDelivered.setEnabled(false);
                        btnMarkPacked.setAlpha(0.5f);
                        btnMarkInTransit.setAlpha(0.5f);
                        btnMarkDelivered.setAlpha(0.5f);
                    }
                });
    }

    private void updateStatus(String newStatus) {
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Invalid order ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("deliveryStatus", newStatus);
        updates.put("statusUpdateTime", FieldValue.serverTimestamp());

        db.collection("orders").document(orderId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Update UI
                    updateStatusUI(newStatus, Timestamp.now());
                    Toast.makeText(OrderDetailsActivity.this,
                            "Status updated to " + newStatus,
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OrderDetailsActivity.this,
                            "Failed to update status: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e("OrderDetails", "Status update failed", e);
                });
    }

    private void initializeViews() {
        orderIdTextView = findViewById(R.id.orderIdTextView);
        customerNameTextView = findViewById(R.id.customerNameTextView);
        customerMobileTextView = findViewById(R.id.customerMobileTextView);
        addressTextView = findViewById(R.id.addressTextView);
        orderDateTextView = findViewById(R.id.orderDateTextView);
        paymentDateTextView = findViewById(R.id.paymentDateTextView);
        statusTextView = findViewById(R.id.statusTextView);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
        distanceTextView = findViewById(R.id.distanceTextView);

        itemsRecyclerView = findViewById(R.id.itemsRecyclerView);
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadOrderDetails() {
        db.collection("orders").document(orderId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            displayOrderDetails(document);
                        }
                    }
                });
    }

    private void displayOrderDetails(DocumentSnapshot document) {
        // Format dates
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm:ss a", Locale.getDefault());
        TextView deliveryCoordinatesTextView = findViewById(R.id.deliveryCoordinatesTextView);

        // Set basic order info
        orderIdTextView.setText(document.getString("orderId"));
        customerNameTextView.setText(document.getString("userName"));
        customerMobileTextView.setText(document.getString("userMobile"));
        addressTextView.setText(document.getString("userAddress"));
        statusTextView.setText(document.getString("status"));
        totalAmountTextView.setText(String.format("Total: Rs.%.2f", document.getDouble("totalAmount")));

        // Set dates
        Timestamp orderDate = document.getTimestamp("orderDate");
        if (orderDate != null) {
            orderDateTextView.setText(sdf.format(orderDate.toDate()));
        }

        Timestamp paymentDate = document.getTimestamp("paymentDate");
        if (paymentDate != null) {
            paymentDateTextView.setText(sdf.format(paymentDate.toDate()));
        }

        // Set items
        List<OrderFragment.OrderItem> items = new ArrayList<>();
        List<Object> itemsList = (List<Object>) document.get("items");
        if (itemsList != null) {
            for (Object itemObj : itemsList) {
                if (itemObj instanceof java.util.Map) {
                    java.util.Map<String, Object> itemMap = (java.util.Map<String, Object>) itemObj;
                    OrderFragment.OrderItem item = new OrderFragment.OrderItem();
                    item.setImageUrl((String) itemMap.get("imageUrl"));
                    item.setPrice((String) itemMap.get("price"));
                    item.setProductId((String) itemMap.get("productId"));
                    item.setProductName((String) itemMap.get("productName"));
                    item.setQuantity(((Long) itemMap.get("quantity")).intValue());
                    items.add(item);
                }
            }
        }

        itemsRecyclerView.setAdapter(new OrderItemAdapter(items));

        // Update map if coordinates are available
        if (mMap != null) {
            Double lat = document.getDouble("deliveryLat");
            Double lng = document.getDouble("deliveryLng");
            if (lat != null && lng != null) {
                LatLng deliveryLocation = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions().position(deliveryLocation).title("Delivery Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(deliveryLocation, 15f));
                deliveryCoordinatesTextView.setText(String.format(Locale.getDefault(),
                        "Coordinates: %.6f, %.6f", lat, lng));
            }
        }

        String status = document.getString("status");
        String deliveryStatus = document.getString("deliveryStatus");
        Timestamp statusUpdateTime = document.getTimestamp("statusUpdateTime");
        updateStatusUI(deliveryStatus, statusUpdateTime);

        if ("CANCELLED".equalsIgnoreCase(status)) {
            // Disable all status update functionality
            LinearLayout statusButtonsLayout = findViewById(R.id.statusButtonsLayout);
            statusButtonsLayout.setVisibility(View.GONE);

            // Grey out the status progress
            updateStatusUI("CANCELLED", statusUpdateTime);
        } else {
            // Normal order - enable status updates
            updateStatusUI(deliveryStatus, statusUpdateTime);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        storeMarker = mMap.addMarker(new MarkerOptions()
                .position(STORE_LOCATION)
                .title("Our Store")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        // If we already have the order details, update the map
        if (orderId != null) {
            db.collection("orders").document(orderId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                updateMapWithOrderLocation(document);
                            }
                        }
                    });
        }
    }

    private void updateMapWithOrderLocation(DocumentSnapshot document) {
        Double lat = document.getDouble("deliveryLat");
        Double lng = document.getDouble("deliveryLng");

        if (lat != null && lng != null) {
            LatLng deliveryLocation = new LatLng(lat, lng);

            // Add delivery marker
            deliveryMarker = mMap.addMarker(new MarkerOptions()
                    .position(deliveryLocation)
                    .title("Delivery Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            // Draw polyline between store and delivery location
            if (routePolyline != null) routePolyline.remove();
            routePolyline = mMap.addPolyline(new PolylineOptions()
                    .add(STORE_LOCATION, deliveryLocation)
                    .width(POLYLINE_WIDTH)
                    .color(Color.GREEN));

            // Calculate and display distance
            float distance = calculateDistance(STORE_LOCATION, deliveryLocation);
            String distanceText = String.format(Locale.getDefault(),
                    "Distance: %.1f km", distance / 1000);
            distanceTextView.setText(distanceText);

            // Move camera to show both locations
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(STORE_LOCATION);
            builder.include(deliveryLocation);
            LatLngBounds bounds = builder.build();

            int padding = 150; // padding in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cu);
        }
    }

    private float calculateDistance(LatLng start, LatLng end) {
        float[] results = new float[1];
        Location.distanceBetween(
                start.latitude, start.longitude,
                end.latitude, end.longitude,
                results);
        return results[0];
    }

    // Adapter for order items
    private class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

        private List<OrderFragment.OrderItem> items;
        private Context context;

        public OrderItemAdapter(List<OrderFragment.OrderItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            context = parent.getContext();
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.order_item_detail_layout, parent, false);
            return new OrderItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
            OrderFragment.OrderItem item = items.get(position);
            holder.productName.setText(item.getProductName());
            holder.price.setText("Rs." + item.getPrice());
            holder.quantity.setText("Qty: " + item.getQuantity());

            // Load image with Glide
            Glide.with(context)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.placeholder_image) // Add a placeholder image
                    .error(R.drawable.placeholder_image) // Add an error image
                    .centerCrop()
                    .into(holder.productImage);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class OrderItemViewHolder extends RecyclerView.ViewHolder {
            TextView productName, price, quantity;
            ImageView productImage;

            public OrderItemViewHolder(@NonNull View itemView) {
                super(itemView);
                productName = itemView.findViewById(R.id.productNameTextView);
                price = itemView.findViewById(R.id.priceTextView);
                quantity = itemView.findViewById(R.id.quantityTextView);
                productImage = itemView.findViewById(R.id.productImageView);
            }
        }
    }

    private void updateStatusUI(String status, Timestamp updateTime) {
        ImageView orderedIcon = findViewById(R.id.orderedIcon);
        ImageView packedIcon = findViewById(R.id.packedIcon);
        ImageView inTransitIcon = findViewById(R.id.inTransitIcon);
        ImageView deliveredIcon = findViewById(R.id.deliveredIcon);
        LinearLayout statusButtonsLayout = findViewById(R.id.statusButtonsLayout);

        // Reset all icons
        orderedIcon.setImageResource(R.drawable.ic_radio_button_unchecked);
        packedIcon.setImageResource(R.drawable.ic_radio_button_unchecked);
        inTransitIcon.setImageResource(R.drawable.ic_radio_button_unchecked);
        deliveredIcon.setImageResource(R.drawable.ic_radio_button_unchecked);

        // Format the update time
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        String timeText = updateTime != null ?
                "Last updated: " + sdf.format(updateTime.toDate()) :
                "Status not updated yet";

        TextView statusUpdateTimeTextView = findViewById(R.id.statusUpdateTimeTextView);
        statusUpdateTimeTextView.setText(timeText);

        if ("CANCELLED".equalsIgnoreCase(status)) {
            // Disable all status UI for cancelled orders
            orderedIcon.setAlpha(0.5f);
            packedIcon.setAlpha(0.5f);
            inTransitIcon.setAlpha(0.5f);
            deliveredIcon.setAlpha(0.5f);
            statusButtonsLayout.setVisibility(View.GONE);
            return;
        }

        // Enable UI for active orders
        orderedIcon.setAlpha(1f);
        packedIcon.setAlpha(1f);
        inTransitIcon.setAlpha(1f);
        deliveredIcon.setAlpha(1f);

        // Update icons based on current status
        orderedIcon.setImageResource(R.drawable.ic_check_circle);

        if (status != null) {
            switch (status.toUpperCase()) {
                case "PACKED":
                    packedIcon.setImageResource(R.drawable.ic_check_circle);
                    break;
                case "IN_TRANSIT":
                    packedIcon.setImageResource(R.drawable.ic_check_circle);
                    inTransitIcon.setImageResource(R.drawable.ic_check_circle);
                    break;
                case "DELIVERED":
                    packedIcon.setImageResource(R.drawable.ic_check_circle);
                    inTransitIcon.setImageResource(R.drawable.ic_check_circle);
                    deliveredIcon.setImageResource(R.drawable.ic_check_circle);
                    statusButtonsLayout.setVisibility(View.GONE);
                    break;
            }
        }
    }
}