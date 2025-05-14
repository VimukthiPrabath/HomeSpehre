package lk.javainstitute.homesphre;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

import lk.javainstitute.homesphre.adapter.AdapterViewPager;

public class HomeActivity extends AppCompatActivity {

    ViewPager2 pagerMain;
    ArrayList<Fragment> fragmentArrayList = new ArrayList<>();
    BottomNavigationView bottomNav;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        pagerMain = findViewById(R.id.pagerMain);
        bottomNav = findViewById(R.id.bottomNav);


        fragmentArrayList.add(new FragmentHome());
        fragmentArrayList.add(new FragmentProduct());
        fragmentArrayList.add(new FragmentProfile());

        AdapterViewPager adapterViewPager = new AdapterViewPager(this, fragmentArrayList);

        pagerMain.setAdapter(adapterViewPager);
        pagerMain.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNav.setSelectedItemId(R.id.itemHome);
                        break;
                    case 1:
                        bottomNav.setSelectedItemId(R.id.itemProduct);
                        break;
                    case 2:
                        bottomNav.setSelectedItemId(R.id.itemProfile);
                        break;
                }
                super.onPageSelected(position);
            }


        });

        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.itemHome) {
                    pagerMain.setCurrentItem(0);
                    return true;
                } else if (itemId == R.id.itemProduct) {
                    pagerMain.setCurrentItem(1);
                    return true;
                } else if (itemId == R.id.itemProfile) {
                    pagerMain.setCurrentItem(2);
                    return true;
                }

                return false;
            }
        });



    }
}