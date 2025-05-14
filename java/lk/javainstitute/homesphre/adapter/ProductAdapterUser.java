package lk.javainstitute.homesphre.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import lk.javainstitute.homesphre.FragmentSingleProduct;
import lk.javainstitute.homesphre.R;
import lk.javainstitute.homesphre.model.Product;

public class ProductAdapterUser extends RecyclerView.Adapter<ProductAdapterUser.ProductViewHolder> {

    private List<Product> productList;
    private Context context;

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ProductAdapterUser(List<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product1, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.productImage);

        holder.productName.setText(product.getProductName());
        holder.productPrice.setText(String.format("Rs %s", product.getProductPrice()));

        holder.addToCartButton.setOnClickListener(v -> addToCart(product));

        holder.itemView.setOnClickListener(v -> {
            if (context instanceof androidx.fragment.app.FragmentActivity) {
                FragmentSingleProduct fragment = FragmentSingleProduct.newInstance(product);
                ((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainproduct, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void addToCart(Product product) {
        Toast.makeText(context, product.getProductName() + " added to cart", Toast.LENGTH_SHORT).show();

    }

    public void updateProducts(List<Product> newProducts) {
        productList = newProducts;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice;
        Button addToCartButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
        }
    }
}