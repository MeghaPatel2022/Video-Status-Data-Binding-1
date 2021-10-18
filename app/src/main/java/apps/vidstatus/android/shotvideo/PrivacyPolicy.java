package apps.vidstatus.android.shotvideo;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import apps.vidstatus.android.shotvideo.databinding.ActivityPrivacyPolicyBinding;

public class PrivacyPolicy extends AppCompatActivity {

    ActivityPrivacyPolicyBinding privacyPolicyBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        privacyPolicyBinding = DataBindingUtil.setContentView(PrivacyPolicy.this, R.layout.activity_privacy_policy);

        privacyPolicyBinding.ivWebView.setWebViewClient(new MyWebViewClient());
        openURL();

        privacyPolicyBinding.imgBack.setOnClickListener(v -> onBackPressed());
    }

    private void openURL() {
        privacyPolicyBinding.ivWebView.loadUrl(getResources().getString(R.string.privacy_policy));
        privacyPolicyBinding.ivWebView.requestFocus();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}