package apps.vidstatus.android.shotvideo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.ViewPager;

import apps.vidstatus.android.shotvideo.adapter.pageradapters.IntroViewerPagersAdapter;
import apps.vidstatus.android.shotvideo.databinding.ActivityIntroBinding;
import apps.vidstatus.android.shotvideo.fragment.introfragment.IntroFirstFragment;
import apps.vidstatus.android.shotvideo.fragment.introfragment.IntroSecondFragment;
import apps.vidstatus.android.shotvideo.fragment.introfragment.IntroThirdFragment;
import apps.vidstatus.android.shotvideo.sharedpreference.SharedPreference;

public class IntroActivity extends AppCompatActivity implements View.OnClickListener {

    ActivityIntroBinding introBinding;

    @Override
    protected void onStart() {
        super.onStart();
        if (SharedPreference.getLogin(IntroActivity.this)) {
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }
        introBinding = DataBindingUtil.setContentView(IntroActivity.this, R.layout.activity_intro);
        introBinding.imgFirst.setOnClickListener(this);
        introBinding.imgSecond.setOnClickListener(this);
        introBinding.imgThird.setOnClickListener(this);

        setViewPager();
    }

    private void setViewPager() {
        IntroViewerPagersAdapter adapter = new IntroViewerPagersAdapter(getSupportFragmentManager());
        adapter.addFragment(new IntroFirstFragment(), "FIRST_FRAGMENT");
        adapter.addFragment(new IntroSecondFragment(), "SECOND_FRAGMENT");
        adapter.addFragment(new IntroThirdFragment(), "THIRD_FRAGMENT");
        introBinding.viewPager.setAdapter(adapter);

        introBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    introBinding.llFirst.setVisibility(View.VISIBLE);
                    introBinding.llSecond.setVisibility(View.GONE);
                    introBinding.llThird.setVisibility(View.GONE);
                } else if (position == 1) {
                    introBinding.llFirst.setVisibility(View.GONE);
                    introBinding.llSecond.setVisibility(View.VISIBLE);
                    introBinding.llThird.setVisibility(View.GONE);
                } else if (position == 2) {
                    introBinding.llFirst.setVisibility(View.GONE);
                    introBinding.llSecond.setVisibility(View.GONE);
                    introBinding.llThird.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (introBinding.viewPager.getCurrentItem() == 2) {
            introBinding.viewPager.setCurrentItem(1);
        } else if (introBinding.viewPager.getCurrentItem() == 1) {
            introBinding.viewPager.setCurrentItem(0);
        } else {
            super.onBackPressed();
            finishAffinity();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgFirst:
                introBinding.viewPager.setCurrentItem(1);
                break;
            case R.id.imgSecond:
                introBinding.viewPager.setCurrentItem(2);
                break;
            case R.id.imgThird:
                SharedPreference.setLogin(IntroActivity.this, true);
                Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                startActivity(intent);
                break;
        }
    }
}