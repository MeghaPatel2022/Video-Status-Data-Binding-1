package apps.vidstatus.android.shotvideo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.AnalyticsListener;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.DownloadProgressListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import apps.vidstatus.android.shotvideo.adapter.homeadapter.InnerLandscapeAdapter;
import apps.vidstatus.android.shotvideo.databinding.ActivityLandscapeVideoPlayerBinding;
import apps.vidstatus.android.shotvideo.model.landscape.ItemsItem;
import apps.vidstatus.android.shotvideo.model.landscape.LandscapeResponse;
import apps.vidstatus.android.shotvideo.utils.ConnectionDetector;
import apps.vidstatus.android.shotvideo.utils.Constant;
import es.dmoral.toasty.Toasty;
import tcking.github.com.giraffeplayer.GiraffePlayer;

public class LandscapeVideoPlayer extends BaseActivity implements InnerLandscapeAdapter.DownloadClickListener {

    ActivityLandscapeVideoPlayerBinding landscapeVideoPlayerBinding;
    MyClickHandlers myClickHandlers;

    AdRequest adRequest;
    InnerLandscapeAdapter landscapeStatusAdapter;
    int pastVisibleItems, visibleItemCount, totalItemCount;
    GiraffePlayer player;
    Animation animFadeOut, animFadeIn;
    String url = "";
    ItemsItem itemsItem = new ItemsItem();
    File file;
    private Boolean isInternetPresent = false;
    private boolean loading = true;
    private String pageCount = "1";
    private ArrayList<ItemsItem> landscapeItems = new ArrayList<>();
    private ArrayList<ItemsItem> landscapeItems1 = new ArrayList<>();
    private MediaScannerConnection msConn;
    private ConnectionDetector cd;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AdView adView;

    private void fireAnalyticsAds(String arg1, String arg2) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, arg1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg2);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
    }

    @Override
    public void permissionGranted() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }
        landscapeVideoPlayerBinding = DataBindingUtil.setContentView(LandscapeVideoPlayer.this, R.layout.activity_landscape_video_player);
        myClickHandlers = new MyClickHandlers(LandscapeVideoPlayer.this);
        landscapeVideoPlayerBinding.setOnClick(myClickHandlers);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(LandscapeVideoPlayer.this);

        //get the bundle
        Bundle b = getIntent().getExtras();
        landscapeItems1 = (ArrayList<ItemsItem>) b.getSerializable("VideoList");
        itemsItem = (ItemsItem) getIntent().getSerializableExtra("position");
        landscapeVideoPlayerBinding.tvTile.setText(itemsItem.getTitle());

        player = new GiraffePlayer(LandscapeVideoPlayer.this);
        url = itemsItem.getVideoUrl();
        Constant.PlayURL = url;
        player.play(url);
        player.onComplete(new Runnable() {
            @Override
            public void run() {
                player.play(url);
            }
        });
        player.playInFullScreen(false);
        player.setFullScreenOnly(false);
        player.setShowNavIcon(false);
        player.setScaleType(GiraffePlayer.SCALETYPE_WRAPCONTENT);


        setRecyclerview();

        animFadeOut = AnimationUtils.loadAnimation(LandscapeVideoPlayer.this, R.anim.fadeout);
        animFadeIn = AnimationUtils.loadAnimation(LandscapeVideoPlayer.this, R.anim.fadein);


        file = new File(Constant.FOLDERPATH, itemsItem.getTitle() + ".mp4");

        if (!file.exists()) {
            landscapeVideoPlayerBinding.imgDownload.setImageDrawable(getResources().getDrawable(R.drawable.ic_download));
        } else {
            landscapeVideoPlayerBinding.imgDownload.setImageDrawable(getResources().getDrawable(R.drawable.ic_alredy_download));
        }

        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Step 1 - Create an AdView and set the ad unit ID on it.
                    adView = new AdView(LandscapeVideoPlayer.this);
                    fireAnalyticsAds("admob_banner", "Ad Request send");
                    adView.setAdUnitId(getString(R.string.banner_ad));
                    landscapeVideoPlayerBinding.bannerContainer.addView(adView);
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

    private void setRecyclerview() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(LandscapeVideoPlayer.this, RecyclerView.VERTICAL, false);
        landscapeVideoPlayerBinding.rvVideos.setLayoutManager(linearLayoutManager);
        landscapeStatusAdapter = new InnerLandscapeAdapter(landscapeItems, LandscapeVideoPlayer.this);
        landscapeVideoPlayerBinding.rvVideos.setAdapter(landscapeStatusAdapter);

        landscapeItems.clear();
        landscapeStatusAdapter.clearAll();
        getLandscapeVideos(pageCount);

        landscapeVideoPlayerBinding.rvVideos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) { //check for scroll down
                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                    landscapeVideoPlayerBinding.imgFirst.setVisibility(View.VISIBLE);
                    animFadeIn.reset();
                    landscapeVideoPlayerBinding.imgFirst.clearAnimation();
                    landscapeVideoPlayerBinding.imgFirst.startAnimation(animFadeIn);

                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            landscapeVideoPlayerBinding.imgFirst.startAnimation(animFadeOut);
                            landscapeVideoPlayerBinding.imgFirst.setVisibility(View.GONE);
                        }
                    }, 3000);

                    if (loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = false;
                            Log.v("...", "Last Item Wow !");
                            // Do pagination.. i.e. fetch new data
                            getLandscapeVideos(pageCount);
                            loading = true;
                        }
                    }
                } else {
                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                    if (totalItemCount >= 10 && pastVisibleItems >= 10) {

                        landscapeVideoPlayerBinding.imgFirst.setVisibility(View.VISIBLE);
                        animFadeIn.reset();
                        landscapeVideoPlayerBinding.imgFirst.clearAnimation();
                        landscapeVideoPlayerBinding.imgFirst.startAnimation(animFadeIn);

                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                landscapeVideoPlayerBinding.imgFirst.startAnimation(animFadeOut);
                                landscapeVideoPlayerBinding.imgFirst.setVisibility(View.GONE);
                            }
                        }, 3000);
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.onDestroy();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (player != null) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                landscapeVideoPlayerBinding.imgBack.setVisibility(View.GONE);
            } else {
                landscapeVideoPlayerBinding.imgBack.setVisibility(View.VISIBLE);
            }
            player.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onBackPressed() {
        if (player != null && player.onBackPressed()) {
            return;
        }
        Intent intent = new Intent(LandscapeVideoPlayer.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void getLandscapeVideos(String Count) {
        AndroidNetworking.post(Constant.BASEURL + "get_new_video_landscape.php")
                .addBodyParameter("seed", "9958")
                .addBodyParameter("page", Count)
                .addBodyParameter("type", Constant.BOTTOM_SELECTED_ITEM)
                .addBodyParameter("langauge", "")
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        LandscapeResponse landscapeResponse = new Gson().fromJson(response.toString(), LandscapeResponse.class);
                        if (landscapeResponse.getSuccess().equals("false")) {
                            loading = false;
                        } else {
                            pageCount = landscapeResponse.getNextPageToken();
                            landscapeItems = landscapeResponse.getItems();

                            for (int i = 0; i < landscapeResponse.getItems().size(); i++) {
                                if (landscapeResponse.getItems().get(i).getVideoUrl().equals(url)) {
                                    ItemsItem itemsItem = landscapeResponse.getItems().get(i);
                                    landscapeItems.remove(itemsItem);
                                }
                            }

                            landscapeStatusAdapter.addAll(landscapeItems);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("LLLL_Response: ", anError.getErrorDetail());
                    }
                });
    }

    public void scanPhoto(final String imageFileName) {
        msConn = new MediaScannerConnection(LandscapeVideoPlayer.this, new MediaScannerConnection.MediaScannerConnectionClient() {
            public void onMediaScannerConnected() {
                msConn.scanFile(imageFileName, null);
                Log.i("msClient obj", "connection established");
            }

            public void onScanCompleted(String path, Uri uri) {
                msConn.disconnect();
                Log.i("msClient obj", "scan completed");
            }
        });
        this.msConn.connect();
    }

    @Override
    public void onDownloadClick(int position, String imageUrl, String title, String url) {
        new downloadAdapterTask(title, url, position).execute();
    }

    private final class downloadTask extends AsyncTask<Void, Void, String> {

        private final int TIMEOUT_CONNECTION = 5000;//5sec
        private final int TIMEOUT_SOCKET = 30000;//30sec

        String fileName;
        String downloadUrl;

        public downloadTask(String fileName, String downloadUrl) {
            this.fileName = fileName;
            this.downloadUrl = downloadUrl;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    landscapeVideoPlayerBinding.llProgress.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        protected String doInBackground(Void... params) {
            AndroidNetworking.download(downloadUrl, Constant.FOLDERPATH, fileName + ".mp4")
                    .setTag("downloadTest")
                    .setPriority(Priority.IMMEDIATE)
                    .build()
                    .setAnalyticsListener(new AnalyticsListener() {
                        @Override
                        public void onReceived(long timeTakenInMillis, long bytesSent, long bytesReceived, boolean isFromCache) {
                            Log.e("LLLLL_Progress: ", (timeTakenInMillis / 1000) + " Received: " + (bytesReceived / 1000) + "  Sent: " + (bytesSent / 1000));
                        }
                    })
                    .setDownloadProgressListener(new DownloadProgressListener() {
                        @Override
                        public void onProgress(long bytesDownloaded, long totalBytes) {
                            // do anything with progress
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    int progress = (int) ((bytesDownloaded * 100) / totalBytes);
                                    landscapeVideoPlayerBinding.downloadProgress.setProgress(progress);
                                    Log.e("LLLLL_Progress: ", ((bytesDownloaded * 100) / totalBytes) + " Total: " + (totalBytes / 1000));
                                }
                            });

                        }
                    })
                    .startDownload(new DownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            // do anything after completion
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("LLLLL_Progress: ", "Download Completed.");

                                    File file = new File(Constant.FOLDERPATH, fileName + ".mp4");
                                    scanPhoto(file.toString());
                                    landscapeVideoPlayerBinding.imgDownload.setImageDrawable(getResources().getDrawable(R.drawable.ic_alredy_download));
                                    final Handler handler = new Handler(Looper.getMainLooper());
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            landscapeVideoPlayerBinding.llProgress.setVisibility(View.GONE);
                                        }
                                    }, 2000);

                                }
                            });
                        }

                        @Override
                        public void onError(ANError error) {
                            // handle error
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("LLLLL_Progress_Err: ", error.getErrorDetail());
                                }
                            });
                        }
                    });
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    private final class downloadAdapterTask extends AsyncTask<Void, Void, String> {

        private final int TIMEOUT_CONNECTION = 5000;//5sec
        private final int TIMEOUT_SOCKET = 30000;//30sec

        String fileName;
        String downloadUrl;
        int position;

        public downloadAdapterTask(String fileName, String downloadUrl, int position) {
            this.fileName = fileName;
            this.downloadUrl = downloadUrl;
            this.position = position;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    landscapeVideoPlayerBinding.llProgress.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        protected String doInBackground(Void... params) {
            AndroidNetworking.download(downloadUrl, Constant.FOLDERPATH, fileName + ".mp4")
                    .setTag("downloadTest")
                    .setPriority(Priority.IMMEDIATE)
                    .build()
                    .setAnalyticsListener(new AnalyticsListener() {
                        @Override
                        public void onReceived(long timeTakenInMillis, long bytesSent, long bytesReceived, boolean isFromCache) {
                            Log.e("LLLLL_Progress: ", (timeTakenInMillis / 1000) + " Received: " + (bytesReceived / 1000) + "  Sent: " + (bytesSent / 1000));
                        }
                    })
                    .setDownloadProgressListener(new DownloadProgressListener() {
                        @Override
                        public void onProgress(long bytesDownloaded, long totalBytes) {
                            // do anything with progress
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    int progress = (int) ((bytesDownloaded * 100) / totalBytes);
                                    landscapeVideoPlayerBinding.downloadProgress.setProgress(progress);
                                    Log.e("LLLLL_Progress: ", ((bytesDownloaded * 100) / totalBytes) + " Total: " + (totalBytes / 1000));
                                }
                            });

                        }
                    })
                    .startDownload(new DownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            // do anything after completion
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("LLLLL_Progress: ", "Download Completed.");

                                    File file = new File(Constant.FOLDERPATH, fileName + ".mp4");
                                    scanPhoto(file.toString());
                                    landscapeVideoPlayerBinding.imgDownload.setImageDrawable(getResources().getDrawable(R.drawable.ic_alredy_download));
                                    final Handler handler = new Handler(Looper.getMainLooper());
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            landscapeVideoPlayerBinding.llProgress.setVisibility(View.GONE);
                                        }
                                    }, 2000);

                                }
                            });
                        }

                        @Override
                        public void onError(ANError error) {
                            // handle error
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("LLLLL_Progress_Err: ", error.getErrorDetail());
                                }
                            });
                        }
                    });
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    public class MyClickHandlers {
        Context context;

        public MyClickHandlers(Context context) {
            this.context = context;
        }

        public void onFirstClick(View view) {
            landscapeVideoPlayerBinding.rvVideos.smoothScrollToPosition(0);
        }

        public void onDownloadClick(View view) {
            if (!file.exists())
                new downloadTask(itemsItem.getTitle(), itemsItem.getVideoUrl()).execute();
            else
                Toasty.success(LandscapeVideoPlayer.this, "Already Downloaded.", Toast.LENGTH_SHORT, true).show();

        }

        public void onWhatsappClick(View view) {
            Constant.whatsappShareVideo(LandscapeVideoPlayer.this, itemsItem.getVideoUrl(), file);
        }

        public void onShareClick(View view) {
            Constant.shareVideo(LandscapeVideoPlayer.this, itemsItem.getVideoUrl(), file);
        }

        public void onBackClick(View view) {
            onBackPressed();
        }
    }

}