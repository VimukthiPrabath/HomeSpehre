package lk.javainstitute.homesphre.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import lk.javainstitute.homesphre.model.Product;

import lk.javainstitute.homesphre.R;

public class SimilarProductAdapter extends RecyclerView.Adapter<SimilarProductAdapter.SimilarProductViewHolder> {

    private List<Product> productList;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public SimilarProductAdapter(List<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    @NonNull
    @Override
    public SimilarProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_similar_product, parent, false);
        return new SimilarProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SimilarProductViewHolder holder, int position) {
        Product product = productList.get(position);

        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.productImage);

        holder.productName.setText(product.getProductName());
        holder.productPrice.setText(String.format("Rs %s", product.getProductPrice()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateProducts(List<Product> newProducts) {
        productList = newProducts;
        notifyDataSetChanged();
    }

    public Product getProductAt(int position) {
        return productList.get(position);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class SimilarProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice;

        public SimilarProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
        }
    }
}