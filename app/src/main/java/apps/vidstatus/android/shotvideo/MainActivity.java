package apps.vidstatus.android.shotvideo;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.ViewPager;

import com.facebook.ads.Ad;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.NativeAdBase;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import apps.vidstatus.android.shotvideo.adapter.DrawerAdapter;
import apps.vidstatus.android.shotvideo.adapter.pageradapters.ViewPagerAdapter;
import apps.vidstatus.android.shotvideo.databinding.ActivityMainBinding;
import apps.vidstatus.android.shotvideo.utils.ConnectionDetector;
import apps.vidstatus.android.shotvideo.utils.Constant;
import es.dmoral.toasty.Toasty;

public class MainActivity extends BaseActivity {

    private final ArrayList<String> menuItems = new ArrayList<>();
    private final ArrayList<Integer> menuImages = new ArrayList<>();
    public NativeAdLayout nativeAdLayout;
    ActivityMainBinding mainBinding;
    MyClickHandlers myClickHandlers;
    ViewPagerAdapter viewPagerAdapter;

    Dialog dial;

    InterstitialAdListener interstitialAdListener;
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AdView adView;
    private com.facebook.ads.NativeAd nativeAd;
    private com.facebook.ads.InterstitialAd interstitialAd;

    @Override
    public void permissionGranted() {

    }

    private void fireAnalytics(String arg1, String arg2) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, arg1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg2);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
    }

    private void fireAnalyticsAds(String arg1, String arg2) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, arg1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg2);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.WHITE);
        }
        mainBinding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);
        myClickHandlers = new MyClickHandlers(MainActivity.this);
        mainBinding.setOnClick(myClickHandlers);

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(MainActivity.this, initializationStatus -> {
        });
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(MainActivity.this);

        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Step 1 - Create an AdView and set the ad unit ID on it.
                    adView = new AdView(MainActivity.this);
                    fireAnalyticsAds("admob_banner", "Ad Request send");
                    adView.setAdUnitId(getString(R.string.banner_ad));
                    mainBinding.bannerContainer.addView(adView);
                    adView.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                            fireAnalyticsAds("admob_banner", "loaded");
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                            if (loadAdError.getMessage() != null)
                                fireAnalyticsAds("admob_banner_Error", loadAdError.getMessage());
                        }
                    });
                    loadBanner();
                }
            }, 2000);
        }
        interstitialAd = new com.facebook.ads.InterstitialAd(this, getString(R.string.fb_inter_placementID));

        // load the ad
        fireAnalyticsAds("fb_interstitial", "Ad Request send");
        interstitialAd.loadAd();

        getDeviceID();
        setNavigation();

        if (!isInternetPresent) {
            mainBinding.rlMain.setVisibility(View.GONE);
            Toasty.error(MainActivity.this, "Please Check your internet connection.", Toasty.LENGTH_LONG).show();
        } else {
            mainBinding.rlMain.setVisibility(View.VISIBLE);
            setViewPager();
            exitApp();
        }
    }

    private void exitApp() {
        dial = new Dialog(MainActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dial.requestWindowFeature(1);
        dial.setContentView(R.layout.dialog_exit);
        dial.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dial.setCanceledOnTouchOutside(true);
        nativeAdLayout = dial.findViewById(R.id.native_ad_container);

        loadNativeAd(MainActivity.this);

        dial.findViewById(R.id.delete_yes).setOnClickListener(view -> {
            dial.dismiss();
            finishAffinity();
        });
        dial.findViewById(R.id.delete_no).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dial.dismiss();
            }
        });
    }

    private void loadNativeAd(Context context) {

        nativeAd = new com.facebook.ads.NativeAd(context, "521075155719800_521075932386389");
        // creating  NativeAdListener
        NativeAdListener nativeAdListener = new NativeAdListener() {

            @Override
            public void onMediaDownloaded(Ad ad) {
            }

            @Override
            public void onError(Ad ad, com.facebook.ads.AdError adError) {
                // showing Toast message
                fireAnalyticsAds("fb_native", "Error to load");
                Log.e("Native error:", adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                fireAnalyticsAds("fb_native", "loaded");
                Log.e("LLL_Native: ", "Loded");
                if (nativeAdLayout != null)
                    inflateAd(nativeAd, nativeAdLayout);

            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {
            }
        };
        fireAnalyticsAds("fb_native", "Ad Request send");
        // Load an ad
        nativeAd.loadAd(
                nativeAd.buildLoadAdConfig()
                        .withAdListener(nativeAdListener)
                        .withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL)
                        .build());
    }

    void inflateAd(com.facebook.ads.NativeAd nativeAd, NativeAdLayout nativeAdLayout) {

        Log.e("LLL_Native: ", "Come");
        // Add the Ad view into the ad container.

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);

        // Inflate the Ad view.
        LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.ad_unified_facebook, nativeAdLayout, false);

        // adding view
        nativeAdLayout.addView(adView);

        // Add the AdOptionsView
        LinearLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(MainActivity.this, nativeAd, nativeAdLayout);

        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        // Create native UI using the ad metadata.
        com.facebook.ads.MediaView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
        com.facebook.ads.MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);
        TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
        TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
        Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

        // Setting  the Text.
        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

        // Create a list of clickable views
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);

        // Register the Title and  button to listen for clicks.
        nativeAd.registerViewForInteraction(adView, nativeAdMedia, nativeAdIcon, clickableViews);
    }

    private void getDeviceID() {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String token = null;
                AdvertisingIdClient.Info adInfo = null;
                try {
                    adInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
                } catch (IOException e) {
                    // ...
                } catch (GooglePlayServicesRepairableException e) {
                    // ...
                } catch (GooglePlayServicesNotAvailableException e) {
                    // ...
                }
                String android_id = adInfo.getId();
                Log.d("LLLLL_DeviceID", android_id);

                return android_id;
            }

            @Override
            protected void onPostExecute(String token) {
                Log.i("LLLLL_DeviceID", "DEVICE_ID Access token retrieved:" + token);
            }

        };
        task.execute();
    }

    private void loadBanner() {
        AdRequest adRequest =
                new AdRequest.Builder().build();
        AdSize adSize = AdSize.BANNER;
        // Step 4 - Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize);

        // Step 5 - Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    private void setViewPager() {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        final ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(viewPagerAdapter);

        viewPager.setCurrentItem(Constant.pagerPosition);

        mainBinding.tvTitle.setText(viewPagerAdapter.getPageTitle(Constant.pagerPosition));
        mainBinding.topNavigationConstraint.setCurrentActiveItem(Constant.pagerPosition);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                mainBinding.tvTitle.setText(viewPagerAdapter.getPageTitle(i));
                mainBinding.topNavigationConstraint.setCurrentActiveItem(i);
                fireAnalytics("selected_bottom_tab", viewPagerAdapter.getPageTitle(i).toString());
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        mainBinding.topNavigationConstraint.setNavigationChangeListener((view, position) -> {
            if (position == 0) {
                Constant.BOTTOM_SELECTED_ITEM = "Latest";
            } else if (position == 1) {
                Constant.BOTTOM_SELECTED_ITEM = "Trending";
            }
            Constant.pagerPosition = position;
            fireAnalytics("selected_bottom_tab", viewPagerAdapter.getPageTitle(position).toString());
            mainBinding.tvTitle.setText(viewPagerAdapter.getPageTitle(position));
            viewPager.setCurrentItem(position, true);
        });
        viewPager.setOffscreenPageLimit(4);
    }

    private void setNavigation() {

        menuItems.add("Story Status");
        menuItems.add("Landscape Status");
        menuItems.add("Social Video");
        menuItems.add("About Us");
        menuItems.add("Privacy Policy");
        menuItems.add("Rate US");

        menuImages.add(R.drawable.ic_story_status);
        menuImages.add(R.drawable.ic_land_status);
        menuImages.add(R.drawable.ic_social_vid);
        menuImages.add(R.drawable.ic_about_us);
        menuImages.add(R.drawable.ic_privacy);
        menuImages.add(R.drawable.ic_rate_us);

        mainBinding.navListDesign.navList.setAdapter(new DrawerAdapter(this, menuItems, menuImages));
        mainBinding.navListDesign.navList.setOnItemClickListener((parent, view, position, id) -> {
            mainBinding.drawerLayout.closeDrawers();
            fireAnalytics("selected_menu_navigation", menuItems.get(position));
            switch (position) {
                case 0:
                case 1:
                    interstitialAdListener = new InterstitialAdListener() {
                        @Override
                        public void onError(Ad ad, com.facebook.ads.AdError adError) {
                            fireAnalyticsAds("fb_interstitial", "Error to load");
                            Log.e("LLLL_ErrFB: ", adError.getErrorMessage());
                            if (Constant.NAV_SELECTED_ITEM.equals(menuItems.get(position))) {
                                if (mainBinding.drawerLayout.isDrawerOpen(mainBinding.navigationView)) {
                                    mainBinding.drawerLayout.closeDrawers();
                                }
                            } else {
                                Constant.pagerPosition = 0;
                                if (mainBinding.drawerLayout.isDrawerOpen(mainBinding.navigationView)) {
                                    mainBinding.drawerLayout.closeDrawers();
                                }
                                Constant.NAV_SELECTED_ITEM = menuItems.get(position);
                                runOnUiThread(MainActivity.this::setViewPager);
                            }
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            fireAnalyticsAds("fb_interstitial", "loaded");
                        }

                        @Override
                        public void onAdClicked(Ad ad) {

                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {

                        }

                        @Override
                        public void onInterstitialDisplayed(Ad ad) {

                        }

                        @Override
                        public void onInterstitialDismissed(Ad ad) {

                            // load the ad
                            interstitialAd.loadAd();

                            if (Constant.NAV_SELECTED_ITEM.equals(menuItems.get(position))) {
                                if (mainBinding.drawerLayout.isDrawerOpen(mainBinding.navigationView)) {
                                    mainBinding.drawerLayout.closeDrawers();
                                }
                            } else {
                                Constant.pagerPosition = 0;
                                if (mainBinding.drawerLayout.isDrawerOpen(mainBinding.navigationView)) {
                                    mainBinding.drawerLayout.closeDrawers();
                                }
                                Constant.NAV_SELECTED_ITEM = menuItems.get(position);
                                runOnUiThread(MainActivity.this::setViewPager);
                            }
                        }
                    };
                    interstitialAd.buildLoadAdConfig()
                            .withAdListener(interstitialAdListener)
                            .build();
                    if (interstitialAd.isAdLoaded())
                        interstitialAd.show();
                    else {
                        // load the ad
                        interstitialAd.loadAd();

                        if (Constant.NAV_SELECTED_ITEM.equals(menuItems.get(position))) {
                            if (mainBinding.drawerLayout.isDrawerOpen(mainBinding.navigationView)) {
                                mainBinding.drawerLayout.closeDrawers();
                            }
                        } else {
                            Constant.pagerPosition = 0;
                            if (mainBinding.drawerLayout.isDrawerOpen(mainBinding.navigationView)) {
                                mainBinding.drawerLayout.closeDrawers();
                            }
                            Constant.NAV_SELECTED_ITEM = menuItems.get(position);
                            runOnUiThread(MainActivity.this::setViewPager);
                        }
                    }

                    break;
                case 2:
                    interstitialAdListener = new InterstitialAdListener() {
                        @Override
                        public void onError(Ad ad, com.facebook.ads.AdError adError) {
                            fireAnalyticsAds("fb_interstitial", "Error to load");
                            Log.e("LLLL_ErrFB: ", adError.getErrorMessage());
                            if (mainBinding.drawerLayout.isDrawerOpen(mainBinding.navigationView)) {
                                mainBinding.drawerLayout.closeDrawers();
                            }
                            Constant.NAV_SELECTED_ITEM = menuItems.get(position);
                            runOnUiThread(MainActivity.this::setViewPager);
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            fireAnalyticsAds("fb_interstitial", "loaded");
                        }

                        @Override
                        public void onAdClicked(Ad ad) {

                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {

                        }

                        @Override
                        public void onInterstitialDisplayed(Ad ad) {

                        }

                        @Override
                        public void onInterstitialDismissed(Ad ad) {

                            // load the ad
                            interstitialAd.loadAd();

                            if (mainBinding.drawerLayout.isDrawerOpen(mainBinding.navigationView)) {
                                mainBinding.drawerLayout.closeDrawers();
                            }
                            Constant.NAV_SELECTED_ITEM = menuItems.get(position);
                            runOnUiThread(MainActivity.this::setViewPager);
                        }
                    };
                    interstitialAd.buildLoadAdConfig()
                            .withAdListener(interstitialAdListener)
                            .build();
                    if (interstitialAd.isAdLoaded())
                        interstitialAd.show();
                    else {
                        interstitialAd.loadAd();

                        if (mainBinding.drawerLayout.isDrawerOpen(mainBinding.navigationView)) {
                            mainBinding.drawerLayout.closeDrawers();
                        }
                        Constant.NAV_SELECTED_ITEM = menuItems.get(position);
                        runOnUiThread(MainActivity.this::setViewPager);
                    }

                    break;
                case 3:
                    Intent intent = new Intent(MainActivity.this, AboutUs.class);
                    startActivity(intent);
                    break;
                case 4:
                    Intent intent1 = new Intent(MainActivity.this, PrivacyPolicy.class);
                    startActivity(intent1);
                    break;
                case 5:
                    /* This code assumes you are inside an activity */
                    final Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
//                    final Uri uri = Uri.parse("market://details?id=com.bbotdev.weather");
                    final Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);

                    if (getPackageManager().queryIntentActivities(rateAppIntent, 0).size() > 0) {
                        startActivity(rateAppIntent);
                    } else {
                        /* handle your error case: the device has no way to handle market urls */
                    }
                    break;
            }

            if (mainBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                mainBinding.drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
    }

    public class MyClickHandlers {
        Context context;

        public MyClickHandlers(Context context) {
            this.context = context;
        }

        public void onMenuClick(View view) {
            mainBinding.drawerLayout.openDrawer(GravityCompat.START);
        }
    }
}