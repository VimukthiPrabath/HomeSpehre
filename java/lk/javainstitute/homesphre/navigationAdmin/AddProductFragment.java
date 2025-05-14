package lk.javainstitute.homesphre.navigationAdmin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import lk.javainstitute.homesphre.model.Product;
import lk.javainstitute.homesphre.R;

public class AddProductFragment extends Fragment {

    private Spinner spinnerCategory;
    private List<String> categoriesList;
    private EditText etProductName, etProductDescription, etProductPrice;
    private ImageView ivProductImage;
    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Button btnSaveProduct;

    private int productQuantity = 1;
    private TextView tvQuantity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_product, container, false);


        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        etProductName = view.findViewById(R.id.etProductName);
        etProductDescription = view.findViewById(R.id.etProductDescription);
        etProductPrice = view.findViewById(R.id.etProductPrice);
        ivProductImage = view.findViewById(R.id.ivProductImage);
        btnSaveProduct = view.findViewById(R.id.btnSaveProduct);
        tvQuantity = view.findViewById(R.id.tvQuantity);

        categoriesList = new ArrayList<>();
        loadCategoriesFromFirestore();


        Button btnSelectImage = view.findViewById(R.id.btnSelectImage);
        btnSelectImage.setOnClickListener(v -> openImageChooser());


        btnSaveProduct.setOnClickListener(v -> saveProduct());


        view.findViewById(R.id.btnIncrease).setOnClickListener(v -> {

            productQuantity++;

            tvQuantity.setText(String.valueOf(productQuantity));
        });


        view.findViewById(R.id.btnDecrease).setOnClickListener(v -> {

            if (productQuantity > 1) {
                productQuantity--;

                tvQuantity.setText(String.valueOf(productQuantity));
            }
        });

        return view;
    }

    private void loadCategoriesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        db.collection("categories")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        categoriesList.clear();


                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String categoryName = document.getString("name");
                            categoriesList.add(categoryName);
                        }


                        updateCategorySpinner();
                    } else {
                        Toast.makeText(getContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, categoriesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            ivProductImage.setImageURI(selectedImageUri);
        }
    }

    private void saveProduct() {
        String productName = etProductName.getText().toString();
        String productDescription = etProductDescription.getText().toString();
        String productPrice = etProductPrice.getText().toString();
        String category = spinnerCategory.getSelectedItem().toString();

        if (productName.isEmpty() || productDescription.isEmpty() || productPrice.isEmpty() || selectedImageUri == null) {
            Toast.makeText(getContext(), "Please fill all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadImageAndSaveProduct(productName, productDescription, productPrice, category);
    }

    private void uploadImageAndSaveProduct(String productName, String productDescription, String productPrice, String category) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();


        StorageReference imageRef = storageReference.child("product_images/" + System.currentTimeMillis() + ".jpg");


        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveProductDetailsToFirestore(productName, productDescription, productPrice, category, imageUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProductDetailsToFirestore(String productName, String productDescription, String productPrice, String category, String imageUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        Product product = new Product(productName, productDescription, productPrice, category, imageUrl, productQuantity);


        db.collection("products")
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Product added successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to add product", Toast.LENGTH_SHORT).show();
                });
    }

    private void clearFields() {

        etProductName.setText("");
        etProductDescription.setText("");
        etProductPrice.setText("");
        tvQuantity.setText("1");
        spinnerCategory.setSelection(0);


        ivProductImage.setImageResource(0);


        productQuantity = 1;
    }
}
