package lk.javainstitute.homesphre;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import lk.javainstitute.homesphre.model.Product;
import lk.javainstitute.homesphre.model.User;

public class FragmentSingleProduct extends Fragment {

    private ImageView productImage;
    private TextView productName, productPrice, productDescription , productQuantity, productId;
    private Button addToCartButton;
    private Product product;

    public static FragmentSingleProduct newInstance(Product product) {
        FragmentSingleProduct fragment = new FragmentSingleProduct();
        Bundle args = new Bundle();
        args.putSerializable("product", product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_single_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        productImage = view.findViewById(R.id.productImage);
        productName = view.findViewById(R.id.productName);
        productPrice = view.findViewById(R.id.productPrice);
        productDescription = view.findViewById(R.id.productDescription);
        productQuantity = view.findViewById(R.id.productQuantity);
        productId = view.findViewById(R.id.productId);
        addToCartButton = view.findViewById(R.id.addToCartButton);

        if (getArguments() != null) {
            product = (Product) getArguments().getSerializable("product");

            if (product != null) {
                productName.setText("Product : " + product.getProductName());
                productId.setText("ID : " + product.getDocumentId());
                productPrice.setText("PRICE : Rs" + product.getProductPrice());
                productDescription.setText("Description : "+product.getProductDescription());
                productQuantity.setText("Quantity : "+String.valueOf(product.getProductQuantity()));

                Glide.with(requireContext()).load(product.getImageUrl()).into(productImage);
            }
        }

        addToCartButton.setOnClickListener(v -> {
            // Implement add-to-cart functionality here

            SharedPreferences sp = requireContext().getSharedPreferences("lk.javainstitute.homesphre.data", Context.MODE_PRIVATE);
            String userJson = sp.getString("user", null);

            if (userJson == null) {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            Gson gson = new Gson();
            User user = gson.fromJson(userJson, User.class);
            String userMobile = user.getMobile();
            String productId = product.getDocumentId();

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            String cartDocId = userMobile + "_" + productId;
            DocumentReference docRef = firestore.collection("cart").document(cartDocId);


            firestore.runTransaction(transaction -> {
                DocumentSnapshot snapshot = transaction.get(docRef);
                if (snapshot.exists()) {

                    double currentQty = snapshot.getDouble("quantity");
                    transaction.update(docRef, "quantity", currentQty + 1);
                } else {

                    Map<String, Object> cartItem = new HashMap<>();
                    cartItem.put("productId", productId);
                    cartItem.put("quantity", 1);
                    cartItem.put("userMobile", userMobile);
                    cartItem.put("productName", product.getProductName());
                    cartItem.put("productPrice", product.getProductPrice());
                    cartItem.put("imageUrl", product.getImageUrl());
                    transaction.set(docRef, cartItem);
                }
                return null;
            }).addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Added to cart!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });
    }
}