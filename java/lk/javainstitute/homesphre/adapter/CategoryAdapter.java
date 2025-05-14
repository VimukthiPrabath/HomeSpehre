package lk.javainstitute.homesphre.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.javainstitute.homesphre.model.Category;
import lk.javainstitute.homesphre.R;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<Category> categoryList;
    private ProductAdapter.ImageSelectionListener imageSelectionListener;

    public CategoryAdapter(Context context, List<Category> categoryList,
                           ProductAdapter.ImageSelectionListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.imageSelectionListener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.tvCategoryName.setText(category.getCategoryName());

        ProductAdapter productAdapter = new ProductAdapter(
                context,
                category.getProductList(),
                categoryList,
                imageSelectionListener
        );

        holder.recyclerViewProducts.setLayoutManager(
                new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerViewProducts.setAdapter(productAdapter);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        RecyclerView recyclerViewProducts;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            recyclerViewProducts = itemView.findViewById(R.id.recyclerViewProducts);
        }
    }
}