package lk.javainstitute.homesphre.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.javainstitute.homesphre.model.Category;
import lk.javainstitute.homesphre.model.Product;
import lk.javainstitute.homesphre.R;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface ImageSelectionListener {
        void onImageSelectionRequested();
    }

    private Context context;
    private List<Product> productList;
    private List<Category> categoryList;
    private Uri selectedImageUri;
    private StorageReference storageReference;
    private ImageSelectionListener imageSelectionListener;

    public ProductAdapter(Context context, List<Product> productList,
                          List<Category> categoryList, ImageSelectionListener listener) {
        this.context = context;
        this.productList = productList;
        this.categoryList = categoryList != null ? categoryList : new ArrayList<>();
        this.imageSelectionListener = listener;
        this.storageReference = FirebaseStorage.getInstance().getReference("product_images");
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvProductName.setText(product.getProductName());
        holder.tvProductPrice.setText("Price: Rs. " + product.getProductPrice());
        holder.tvProductStock.setText("Stock: " + product.getProductQuantity());

        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.ivProductImage);

        holder.itemView.setOnClickListener(v -> showUpdateDialog(product));
    }

    private void showUpdateDialog(Product product) {
        List<String> categories = new ArrayList<>();
        for (Category category : categoryList) {
            categories.add(category.getCategoryName());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_update_product, null);
        builder.setView(dialogView);

        ImageView ivProductImage = dialogView.findViewById(R.id.ivProductImage);
        Button btnChangeImage = dialogView.findViewById(R.id.btnChangeImage);
        EditText etProductName = dialogView.findViewById(R.id.etProductName);
        EditText etProductDescription = dialogView.findViewById(R.id.etProductDescription);
        EditText etProductPrice = dialogView.findViewById(R.id.etProductPrice);
        EditText etProductQuantity = dialogView.findViewById(R.id.etProductQuantity);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);

        // Initialize fields
        Glide.with(context).load(product.getImageUrl()).into(ivProductImage);
        etProductName.setText(product.getProductName());
        etProductDescription.setText(product.getProductDescription());
        etProductPrice.setText(product.getProductPrice());
        etProductQuantity.setText(String.valueOf(product.getProductQuantity()));

        // Setup spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        int categoryPosition = categories.indexOf(product.getCategory());
        if (categoryPosition >= 0) {
            spinnerCategory.setSelection(categoryPosition);
        }

        AlertDialog dialog = builder.create();

        btnChangeImage.setOnClickListener(v -> {
            if (imageSelectionListener != null) {
                imageSelectionListener.onImageSelectionRequested();
            }
        });

        dialogView.findViewById(R.id.btnUpdate).setOnClickListener(v -> {
            String newName = etProductName.getText().toString().trim();
            String newDesc = etProductDescription.getText().toString().trim();
            String newPrice = etProductPrice.getText().toString().trim();
            String newQuantity = etProductQuantity.getText().toString().trim();
            String newCategory = spinnerCategory.getSelectedItem().toString();

            if (validateInputs(newName, newDesc, newPrice, newQuantity)) {
                updateProduct(product, newName, newDesc, newPrice, newQuantity, newCategory, dialog);
            }
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private boolean validateInputs(String... inputs) {
        for (String input : inputs) {
            if (input.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void updateProduct(Product product, String name, String desc,
                               String price, String quantity, String category,
                               AlertDialog dialog) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("productName", name);
        updates.put("productDescription", desc);
        updates.put("productPrice", price);
        updates.put("productQuantity", Integer.parseInt(quantity));
        updates.put("category", category);

        if (selectedImageUri != null) {
            uploadImageAndUpdate(product, updates, dialog);
        } else {
            performFirestoreUpdate(product.getDocumentId(), updates, dialog);
        }
    }

    private void uploadImageAndUpdate(Product product, Map<String, Object> updates, AlertDialog dialog) {
        StorageReference fileRef = storageReference.child(System.currentTimeMillis() + ".jpg");
        fileRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            updates.put("imageUrl", uri.toString());
                            performFirestoreUpdate(product.getDocumentId(), updates, dialog);
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
    }

    private void performFirestoreUpdate(String docId, Map<String, Object> updates, AlertDialog dialog) {
        FirebaseFirestore.getInstance()
                .collection("products")
                .document(docId)
                .update(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Product updated", Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();
                    } else {
                        Toast.makeText(context, "Update failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void setSelectedImageUri(Uri uri) {
        this.selectedImageUri = uri;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductPrice, tvProductStock;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductStock = itemView.findViewById(R.id.tvProductStock);
        }
    }


}