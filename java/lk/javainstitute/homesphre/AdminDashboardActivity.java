package lk.javainstitute.homesphre;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

import lk.javainstitute.homesphre.helpers.Admin;
import lk.javainstitute.homesphre.helpers.AdminDbHelper;
import lk.javainstitute.homesphre.navigationAdmin.AddProductFragment;
import lk.javainstitute.homesphre.navigationAdmin.AdminProfileFragment;
import lk.javainstitute.homesphre.navigationAdmin.AdminSettingFragment;
import lk.javainstitute.homesphre.navigationAdmin.DashboardFragment;
import lk.javainstitute.homesphre.navigationAdmin.OrderFragment;
import lk.javainstitute.homesphre.navigationAdmin.ProductManageFragment;
import lk.javainstitute.homesphre.navigationAdmin.UpdateProductFragment;
import lk.javainstitute.homesphre.navigationAdmin.UserManageFragment;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayoutadmin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        DrawerLayout drawerLayout1 = findViewById(R.id.drawerLayoutadmin);
        Toolbar toolbaradmin = findViewById(R.id.toolbarAdmin);
        NavigationView navigationViewadmin = findViewById(R.id.navigationViewAdmin);

        View headerView = navigationViewadmin.getHeaderView(0);
        TextView adminNameTextView = headerView.findViewById(R.id.textView7);
        TextView adminEmailTextView = headerView.findViewById(R.id.textView16);

        AdminDbHelper dbHelper = new AdminDbHelper(this);
        Admin admin = dbHelper.getAdmin();

        if (admin != null) {
            adminNameTextView.setText(admin.getName());
            adminEmailTextView.setText(admin.getEmail());
        } else {
            adminNameTextView.setText("No Admin");
            adminEmailTextView.setText("No Email");
        }

        navigationViewadmin.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId() == R.id.nav_menu_dashboard) {

                    loadFragment(new DashboardFragment());

                } else if (item.getItemId() == R.id.nav_menu_addProduct) {

                    loadFragment(new AddProductFragment());

                } else if (item.getItemId() == R.id.nav_menu_profile) {

                    loadFragment(new AdminProfileFragment());

                } else if (item.getItemId() == R.id.nav_menu_setting) {

                    loadFragment(new AdminSettingFragment());

                }else if (item.getItemId() == R.id.nav_menu_User) {

                    loadFragment(new UserManageFragment());
                }else if (item.getItemId() == R.id.nav_menu_manageProduct) {

                    loadFragment(new ProductManageFragment());
                }else if (item.getItemId() == R.id.nav_menu_updateProduct) {

                    loadFragment(new UpdateProductFragment());
                }else if (item.getItemId() == R.id.nav_menu_Order) {

                    loadFragment(new OrderFragment());
                }

                toolbaradmin.setTitle(item.getTitle());
                drawerLayout1.closeDrawers();

                return true;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerViewAdmin, fragment, null)
                .setReorderingAllowed(true)
                .commit();
    }
}