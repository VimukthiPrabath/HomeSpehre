package lk.javainstitute.homesphre.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import lk.javainstitute.homesphre.R;
import lk.javainstitute.homesphre.model.CartItem;
import lk.javainstitute.homesphre.model.Product;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private Context context;
    private String userMobile;
    private TotalPriceUpdateListener totalPriceUpdateListener;

    public interface TotalPriceUpdateListener {
        void onTotalPriceUpdated();
    }

    public CartAdapter(List<CartItem> cartItems, Context context, String userMobile, TotalPriceUpdateListener listener) {
        this.cartItems = cartItems;
        this.context = context;
        this.userMobile = userMobile;
        this.totalPriceUpdateListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        Product product = item.getProduct();

        holder.productName.setText(product.getProductName());
        holder.productPrice.setText("Price: Rs" + product.getProductPrice());
        holder.quantityText.setText(String.valueOf(item.getQuantity()));

        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .into(holder.productImage);

        holder.btnIncrease.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            updateQuantityInFirestore(item, newQuantity, position);
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                int newQuantity = item.getQuantity() - 1;
                updateQuantityInFirestore(item, newQuantity, position);
            }
        });
    }

    private void updateQuantityInFirestore(CartItem item, int newQuantity, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String docId = userMobile + "_" + item.getProduct().getDocumentId();

        db.collection("cart").document(docId)
                .update("quantity", newQuantity)
                .addOnSuccessListener(aVoid -> {
                    item.setQuantity(newQuantity);
                    notifyItemChanged(position);
                    if (totalPriceUpdateListener != null) {
                        totalPriceUpdateListener.onTotalPriceUpdated();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, quantityText;
        Button btnIncrease, btnDecrease;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            quantityText = itemView.findViewById(R.id.quantityText);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
        }
    }
}