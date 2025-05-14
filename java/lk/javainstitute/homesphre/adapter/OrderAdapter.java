package lk.javainstitute.homesphre.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import lk.javainstitute.homesphre.R;
import lk.javainstitute.homesphre.model.CartItem;
import lk.javainstitute.homesphre.model.Product;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private List<CartItem> items;

    public OrderAdapter(List<CartItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = items.get(position);
        if (item == null) return;

        Product product = item.getProduct();
        if (product == null) return;

        try {
            // Product Name
            holder.productName.setText(product.getProductName() != null ?
                    product.getProductName() : "Unknown Product");

            // Quantity
            holder.quantity.setText(item.getQuantity() > 0 ?
                    "Qty: " + item.getQuantity() : "Qty: N/A");

            // Price Handling
            if (product.getProductPrice() != null) {
                try {
                    double price = Double.parseDouble(product.getProductPrice());
                    holder.price.setText(String.format("Rs%.2f", price * item.getQuantity()));
                } catch (NumberFormatException e) {
                    holder.price.setText("Price: N/A");
                }
            } else {
                holder.price.setText("Price: N/A");
            }

            // Image Loading
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(holder.productImage);
            } else {
                holder.productImage.setImageResource(R.drawable.placeholder_image);
            }
        } catch (Exception e) {
            Log.e("OrderAdapter", "Error binding product", e);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, quantity, price;

        ViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            quantity = itemView.findViewById(R.id.quantity);
            price = itemView.findViewById(R.id.price);
        }
    }
}