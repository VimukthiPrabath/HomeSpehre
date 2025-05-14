package lk.javainstitute.homesphre.navigationAdmin;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
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

public class ProductManageFragment extends Fragment implements ProductAdapter.ImageSelectionListener {

    private RecyclerView recyclerViewCategories;
    private PieChart pieChart;
    private ProgressBar progressBar;
    private CategoryAdapter categoryAdapter;
    private final List<Category> categoryList = new ArrayList<>();
    private FirebaseFirestore db;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri selectedImageUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_manage, container, false);


        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        pieChart = view.findViewById(R.id.pieChart);
        progressBar = view.findViewById(R.id.progressBar);


        initializeImagePicker();


        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryAdapter = new CategoryAdapter(requireContext(), categoryList, this);
        recyclerViewCategories.setAdapter(categoryAdapter);


        db = FirebaseFirestore.getInstance();


        configurePieChart();


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

    private void configurePieChart() {
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(58f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setDrawEntryLabels(true);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.animateY(1000);
    }

    private void updatePieChart() {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        int colorIndex = 0;
        int[] chartColors = getChartColors();

        for (Category category : categoryList) {
            int productCount = category.getProductList().size();
            if (productCount > 0) {
                entries.add(new PieEntry(productCount, category.getCategoryName()));
                colors.add(chartColors[colorIndex % chartColors.length]);
                colorIndex++;
            }
        }

        if (entries.isEmpty()) {
            pieChart.setNoDataText("No products available");
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Product Distribution");
        dataSet.setColors(colors);
        dataSet.setValueLinePart1OffsetPercentage(80f);
        dataSet.setValueLinePart1Length(0.5f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.invalidate();
    }

    private int[] getChartColors() {
        return new int[] {
                Color.rgb(30, 134, 147),   // Primary teal
                Color.rgb(255, 193, 7),    // Amber
                Color.rgb(76, 175, 80),    // Green
                Color.rgb(156, 39, 176),   // Purple
                Color.rgb(244, 67, 54),    // Red
                Color.rgb(33, 150, 243)    // Blue
        };
    }

    private void loadCategoriesAndProducts() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("categories")
                .get()
                .addOnCompleteListener(categoryTask -> {
                    if (categoryTask.isSuccessful()) {
                        List<DocumentSnapshot> categoryDocs = categoryTask.getResult().getDocuments();
                        List<com.google.android.gms.tasks.Task<QuerySnapshot>> productTasks = new ArrayList<>();


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


                        Tasks.whenAllComplete(productTasks)
                                .addOnCompleteListener(combinedTask -> {
                                    progressBar.setVisibility(View.GONE);

                                    if (combinedTask.isSuccessful()) {
                                        categoryList.clear();

                                        for (int i = 0; i < categoryDocs.size(); i++) {
                                            String categoryName = categoryDocs.get(i).getString("name");
                                            QuerySnapshot productSnapshot = (QuerySnapshot) productTasks.get(i).getResult();

                                            if (productSnapshot != null && categoryName != null) {
                                                List<Product> products = productSnapshot.toObjects(Product.class);
                                                categoryList.add(new Category(categoryName, products));
                                            }
                                        }

                                        categoryAdapter.notifyDataSetChanged();
                                        updatePieChart();
                                    } else {
                                        Log.e("Firestore", "Error loading products", combinedTask.getException());
                                    }
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Log.e("Firestore", "Error loading categories", categoryTask.getException());
                    }
                });
    }
}