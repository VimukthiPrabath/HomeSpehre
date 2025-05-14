package lk.javainstitute.homesphre.navigationAdmin;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.javainstitute.homesphre.model.Category;
import lk.javainstitute.homesphre.model.Product;
import lk.javainstitute.homesphre.R;
import lk.javainstitute.homesphre.adapter.CategoryAdapter;
import lk.javainstitute.homesphre.adapter.ProductAdapter;

public class UpdateProductFragment extends Fragment implements ProductAdapter.ImageSelectionListener {

    private RecyclerView recyclerViewCategories;
    private CategoryAdapter categoryAdapter;
    private final List<Category> categoryList = new ArrayList<>();
    private FirebaseFirestore db;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri selectedImageUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_product, container, false);

        initializeImagePicker();
        setupRecyclerView(view);
        loadCategoriesAndProducts();

        return view;
    }

    private void initializeImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        updateAdaptersWithNewImage();
                    }
                }
        );
    }

    private void setupRecyclerView(View view) {
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryAdapter = new CategoryAdapter(requireContext(), categoryList, this);
        recyclerViewCategories.setAdapter(categoryAdapter);
    }

    private void updateAdaptersWithNewImage() {
        for (int i = 0; i < categoryList.size(); i++) {
            RecyclerView.ViewHolder viewHolder = recyclerViewCategories.findViewHolderForAdapterPosition(i);
            if (viewHolder != null) {
                RecyclerView productsRecycler = viewHolder.itemView.findViewById(R.id.recyclerViewProducts);
                ProductAdapter adapter = (ProductAdapter) productsRecycler.getAdapter();
                if (adapter != null) {
                    adapter.setSelectedImageUri(selectedImageUri);
                }
            }
        }
    }

    @Override
    public void onImageSelectionRequested() {
        imagePickerLauncher.launch("image/*");
    }

    private void loadCategoriesAndProducts() {
        db = FirebaseFirestore.getInstance();
        db.collection("categories").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> categoryDocs = task.getResult().getDocuments();
                List<Task<QuerySnapshot>> productTasks = new ArrayList<>();

                for (DocumentSnapshot categoryDoc : categoryDocs) {
                    String categoryName = categoryDoc.getString("name");
                    if (categoryName != null) {
                        productTasks.add(
                                db.collection("products")
                                        .whereEqualTo("category", categoryName)
                                        .get()
                        );
                    }
                }

                Tasks.whenAllComplete(productTasks).addOnCompleteListener(combinedTask -> {
                    categoryList.clear();
                    for (int i = 0; i < categoryDocs.size(); i++) {
                        String categoryName = categoryDocs.get(i).getString("name");
                        QuerySnapshot productSnapshot = (QuerySnapshot) productTasks.get(i).getResult();
                        if (productSnapshot != null) {
                            List<Product> products = productSnapshot.toObjects(Product.class);
                            for (int j = 0; j < productSnapshot.size(); j++) {
                                products.get(j).setDocumentId(
                                        productSnapshot.getDocuments().get(j).getId()
                                );
                            }
                            categoryList.add(new Category(categoryName, products));
                        }
                    }
                    categoryAdapter.notifyDataSetChanged();
                });
            }
        });
    }
}