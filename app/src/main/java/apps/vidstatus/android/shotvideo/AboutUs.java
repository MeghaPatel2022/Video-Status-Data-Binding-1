package apps.vidstatus.android.shotvideo;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import apps.vidstatus.android.shotvideo.databinding.ActivityAboutUsBinding;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class AboutUs extends AppCompatActivity {

    ActivityAboutUsBinding aboutUsBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        aboutUsBinding = DataBindingUtil.setContentView(AboutUs.this, R.layout.activity_about_us);

        Glide
                .with(AboutUs.this)
                .load(R.drawable.logo)
                .transition(withCrossFade())
                .transition(new DrawableTransitionOptions().crossFade(500))
                .into(aboutUsBinding.imgLogo);

        aboutUsBinding.tvAppName.setText(getResources().getString(R.string.app_name));
        aboutUsBinding.tvVersion.setText("Version: " + BuildConfig.VERSION_NAME);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}