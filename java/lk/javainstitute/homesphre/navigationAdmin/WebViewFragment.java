package lk.javainstitute.homesphre.navigationAdmin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import lk.javainstitute.homesphre.R;

public class WebViewFragment extends Fragment {

    private WebView webView;
    private ImageButton btnBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_web_view, container, false);


        webView = view.findViewById(R.id.webViewContent);
        btnBack = view.findViewById(R.id.btnBack);


        String pageType = getArguments() != null ? getArguments().getString("PAGE_TYPE") : "";

        // Setup WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());


        if ("privacy".equals(pageType)) {
            webView.loadUrl("https://vimukthiprabath.github.io/Homesphere/");
        } else if ("terms".equals(pageType)) {
            webView.loadUrl("https://vimukthiprabath.github.io/Homesphere/");
        } else if ("info".equals(pageType)) {
            webView.loadUrl("https://vimukthiprabath.github.io/Homesphere/");
        }


        btnBack.setOnClickListener(v -> getActivity().onBackPressed());

        return view;
    }


    public static WebViewFragment newInstance(String pageType) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle args = new Bundle();
        args.putString("PAGE_TYPE", pageType);
        fragment.setArguments(args);
        return fragment;
    }
}
