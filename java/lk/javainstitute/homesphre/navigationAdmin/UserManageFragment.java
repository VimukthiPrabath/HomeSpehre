package lk.javainstitute.homesphre.navigationAdmin;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.javainstitute.homesphre.R;
import lk.javainstitute.homesphre.model.User;
import lk.javainstitute.homesphre.adapter.UserAdapter;

public class UserManageFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private EditText etSearchUser;
    private FirebaseFirestore db;
    private Query baseQuery;

    PieChart pieChart;
    int activeCount = 0;
    int deactiveCount = 0;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_manage, container, false);


        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        etSearchUser = view.findViewById(R.id.etSearchUser);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        baseQuery = db.collection("User");


        updateRecyclerView("");


        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String searchText = charSequence.toString();
                updateRecyclerView(searchText);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        pieChart = view.findViewById(R.id.userPieChart);
        db = FirebaseFirestore.getInstance();

        loadUserData();


        return view;
    }

    private void updateRecyclerView(String searchText) {
        Query query;
        if (searchText.isEmpty()) {
            query = baseQuery;
        } else {
            query = baseQuery.orderBy("username")
                    .startAt(searchText)
                    .endAt(searchText + "\uf8ff");
        }


        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        if (userAdapter == null) {
            userAdapter = new UserAdapter(options,this);
            recyclerView.setAdapter(userAdapter);
        } else {
            userAdapter.updateOptions(options);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        userAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        userAdapter.stopListening();
    }




    public void loadUserData() {
        db.collection("User")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activeCount = 0;
                    deactiveCount = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Boolean isActive = doc.getBoolean("active");
                        if (isActive != null && isActive) {
                            activeCount++;
                        } else {
                            deactiveCount++;
                        }
                    }

                    showPieChart(activeCount, deactiveCount);
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
                });
    }

    private void showPieChart(int activeCount, int deactiveCount) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(activeCount, "Active"));
        entries.add(new PieEntry(deactiveCount, "Deactive"));

        PieDataSet dataSet = new PieDataSet(entries, "User Status");


        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#1E8693")); // Active color
        colors.add(Color.parseColor("#FF6F61")); // Deactive color
        dataSet.setColors(colors);

        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(14f);

        PieData pieData = new PieData(dataSet);

        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setCenterText("Users");
        pieChart.setCenterTextSize(18f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setTransparentCircleAlpha(0);

        pieChart.animateY(1000, Easing.EaseInOutQuad);
        pieChart.invalidate();
    }


}