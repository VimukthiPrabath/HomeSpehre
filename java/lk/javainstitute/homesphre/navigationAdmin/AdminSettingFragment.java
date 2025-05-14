package lk.javainstitute.homesphre.navigationAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import lk.javainstitute.homesphre.R;
import lk.javainstitute.homesphre.SignIn;

public class AdminSettingFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_setting, container, false);


        View btnPrivacyPolicy = view.findViewById(R.id.btnPrivacyPolicy);
        View btnTerms = view.findViewById(R.id.btnTerms);
        View btnAppInfo = view.findViewById(R.id.btnAppInfo);
        View logout = view.findViewById(R.id.btnLogout);

        btnPrivacyPolicy.setOnClickListener(v -> openWebViewFragment("privacy"));
        btnTerms.setOnClickListener(v -> openWebViewFragment("terms"));
        btnAppInfo.setOnClickListener(v -> openWebViewFragment("info"));
        logout.setOnClickListener(v-> {
                    Intent intent = new Intent(getActivity(), SignIn.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                }
                );

        return view;
    }

    private void openWebViewFragment(String pageType) {

        WebViewFragment webViewFragment = WebViewFragment.newInstance(pageType);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainerViewAdmin, webViewFragment);
        transaction.addToBackStack(null); // Add to back stack so user can press back to return
        transaction.commit();
    }
}
