package lk.javainstitute.homesphre;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.javainstitute.homesphre.adapter.ProductAdapterUser;
import lk.javainstitute.homesphre.model.Product;


public class FragmentProduct extends Fragment {


    private RecyclerView recyclerView;
    private ProductAdapterUser productAdapter;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        initializeRecyclerView(view);
        setupCategoryClickListeners(view);
        loadInitialProducts();

        ImageView cartIcon = view.findViewById(R.id.cartIcon);

        cartIcon.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), CartActivity.class));
        });

    }

    private void initializeRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewproductload);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setHasFixedSize(true);



        productAdapter = new ProductAdapterUser(new ArrayList<>(), getContext());
        recyclerView.setAdapter(productAdapter);
    }

    private void setupCategoryClickListeners(View view) {
        Map<Integer, String> categoryMap = new HashMap<>();
        categoryMap.put(R.id.imageView32, "Furniture");
        categoryMap.put(R.id.imageView33, "Kitchenware");
        categoryMap.put(R.id.imageView34, "Home Appliances");
        categoryMap.put(R.id.imageView35, "Home Decor");
        categoryMap.put(R.id.imageView36, "Electronics");
        categoryMap.put(R.id.imageView37, "Cleaning & Laundry");

        for (Map.Entry<Integer, String> entry : categoryMap.entrySet()) {
            view.findViewById(entry.getKey()).setOnClickListener(v -> {
                loadProductsByCategory(entry.getValue());
                highlightSelectedCategory(v);
            });
        }
    }

    private void loadInitialProducts() {

        loadProductsByCategory("Furniture");
    }

    private void loadProductsByCategory(String category) {
        db.collection("products")
                .whereEqualTo("category", category)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Product> products = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.setDocumentId(document.getId());
                            products.add(product);
                        }
                        productAdapter.updateProducts(products);
                    } else {
                        Toast.makeText(getContext(), "Error loading products", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void highlightSelectedCategory(View selectedView) {

        int[] categoryViews = {
                R.id.imageView32, R.id.imageView33, R.id.imageView34,
                R.id.imageView35, R.id.imageView36, R.id.imageView37
        };

        for (int viewId : categoryViews) {
            View view = getView().findViewById(viewId);

        }

    }
}