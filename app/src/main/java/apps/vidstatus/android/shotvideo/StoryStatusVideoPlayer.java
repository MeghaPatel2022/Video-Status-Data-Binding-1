package apps.vidstatus.android.shotvideo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

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
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import apps.vidstatus.android.shotvideo.adapter.homeadapter.InnerStoryStatusPlayerAdapter;
import apps.vidstatus.android.shotvideo.databinding.ActivityStoryStatusVideoPlayerBinding;
import apps.vidstatus.android.shotvideo.model.landscape.ItemsItem;
import apps.vidstatus.android.shotvideo.model.landscape.LandscapeResponse;
import apps.vidstatus.android.shotvideo.utils.ConnectionDetector;
import apps.vidstatus.android.shotvideo.utils.Constant;

public class StoryStatusVideoPlayer extends AppCompatActivity implements InnerStoryStatusPlayerAdapter.DownloadClickListener {

    private final ArrayList<ItemsItem> landscapeItems = new ArrayList<>();
    ActivityStoryStatusVideoPlayerBinding statusVideoPlayerBinding;
    MediaScannerConnection msConn;
    int pastVisibleItems, visibleItemCount, totalItemCount;
    InnerStoryStatusPlayerAdapter innerStoryStatusPlayerAdapter;
    ItemsItem itemsItem = new ItemsItem();
    private ArrayList<ItemsItem> landscapeItems1 = new ArrayList<>();
    private boolean loading = true;
    private String pageCount = "1";
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private AdView adView;
    private FirebaseAnalytics mFirebaseAnalytics;

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
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        statusVideoPlayerBinding = DataBindingUtil.setContentView(StoryStatusVideoPlayer.this, R.layout.activity_story_status_video_player);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(StoryStatusVideoPlayer.this);
        AndroidNetworking.initialize(StoryStatusVideoPlayer.this);
        itemsItem = (ItemsItem) getIntent().getSerializableExtra("position");

        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Step 1 - Create an AdView and set the ad unit ID on it.
                    adView = new AdView(StoryStatusVideoPlayer.this);
                    fireAnalyticsAds("admob_banner", "Ad Request send");
                    adView.setAdUnitId(getString(R.string.banner_ad));
                    statusVideoPlayerBinding.bannerContainer.addView(adView);
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

    @Override
    protected void onResume() {
        super.onResume();
        setAdapter();
    }

    private void setAdapter() {

        //get the bundle
        Bundle b = getIntent().getExtras();
        landscapeItems1 = (ArrayList<ItemsItem>) b.getSerializable("VideoList");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(StoryStatusVideoPlayer.this, RecyclerView.VERTICAL, false);
        statusVideoPlayerBinding.rvVideos.setLayoutManager(linearLayoutManager);
        innerStoryStatusPlayerAdapter = new InnerStoryStatusPlayerAdapter(landscapeItems, StoryStatusVideoPlayer.this);
        statusVideoPlayerBinding.rvVideos.setAdapter(innerStoryStatusPlayerAdapter);
        SnapHelper snapHelper = new PagerSnapHelper();
        statusVideoPlayerBinding.rvVideos.setOnFlingListener(null);
        snapHelper.attachToRecyclerView(statusVideoPlayerBinding.rvVideos);
        innerStoryStatusPlayerAdapter.downloadClickListener(this);
        innerStoryStatusPlayerAdapter.clearAll();
        getStoryStatusVideos(pageCount);

        statusVideoPlayerBinding.rvVideos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) { //check for scroll down
                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = false;
                            Log.v("...", "Last Item Wow !");
                            // Do pagination.. i.e. fetch new data
                            getStoryStatusVideos(pageCount);
                            loading = true;
                        }
                    }
                }
            }
        });
    }

    private void getStoryStatusVideos(String Count) {
        if (Constant.BOTTOM_SELECTED_ITEM.equals("Latest")) {
            Constant.pagerPosition = 0;
        } else if (Constant.BOTTOM_SELECTED_ITEM.equals("Trending")) {
            Constant.pagerPosition = 1;
        }
        landscapeItems.clear();
        AndroidNetworking.post(Constant.BASEURL + "get_new_video_portrait.php")
                .addBodyParameter("seed", "8216")
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
                            if (pageCount.equals("1")) {
                                landscapeItems.add(itemsItem);
                            }
                            pageCount = landscapeResponse.getNextPageToken();
                            landscapeItems.addAll(landscapeResponse.getItems());
                            innerStoryStatusPlayerAdapter.addAll(landscapeItems);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("LLLL_Response: ", anError.getErrorDetail());
                    }
                });
    }

    @Override
    public void onDownloadClick(int position, String imageUrl, String title, String url) {
        new LongOperation(imageUrl).execute();
        new downloadTask(landscapeItems.get(position).getTitle(), landscapeItems.get(position).getVideoUrl(), position).execute();
    }

    @Override
    public void backPress() {
        onBackPressed();
    }

    public void scanPhoto(final String imageFileName) {
        msConn = new MediaScannerConnection(StoryStatusVideoPlayer.this, new MediaScannerConnection.MediaScannerConnectionClient() {
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
    public void onBackPressed() {
        Intent intent = new Intent(StoryStatusVideoPlayer.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private final class LongOperation extends AsyncTask<Void, Void, Bitmap> {

        String downloadUrl;

        public LongOperation(String url) {
            this.downloadUrl = url;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap bitmap = null;
            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                statusVideoPlayerBinding.sProgressBar.setImageBitmap(result);
            }
        }
    }

    private final class downloadTask extends AsyncTask<Void, Void, String> {

        private final int TIMEOUT_CONNECTION = 5000;//5sec
        private final int TIMEOUT_SOCKET = 30000;//30sec

        String fileName;
        String downloadUrl;
        int position = 0;

        public downloadTask(String fileName, String downloadUrl, int position) {
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
                    statusVideoPlayerBinding.sProgressBar.showProgress(true);
                    Constant.expand(statusVideoPlayerBinding.rlProgress);
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
                                    long progress = (bytesDownloaded * 100) / totalBytes;
                                    statusVideoPlayerBinding.sProgressBar.setProgress(progress);
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
                                    statusVideoPlayerBinding.tvTile1.setText("Download Completed.");
                                    innerStoryStatusPlayerAdapter.notifyItemChanged(position);
                                    final Handler handler = new Handler(Looper.getMainLooper());
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Constant.collapse(statusVideoPlayerBinding.sProgressBar);
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

}