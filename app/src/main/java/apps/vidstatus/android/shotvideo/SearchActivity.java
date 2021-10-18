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
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import apps.vidstatus.android.shotvideo.adapter.homeadapter.SubSearchAdapter;
import apps.vidstatus.android.shotvideo.databinding.ActivitySearchBinding;
import apps.vidstatus.android.shotvideo.fragment.SearchFragment;
import apps.vidstatus.android.shotvideo.model.search.SearchItemsItem;
import apps.vidstatus.android.shotvideo.model.search.SearchResponse;
import apps.vidstatus.android.shotvideo.utils.Constant;

public class SearchActivity extends BaseActivity implements SubSearchAdapter.DownloadClickListener {

    private final ArrayList<SearchItemsItem> categoryItems = new ArrayList<>();
    ActivitySearchBinding searchBinding;
    int pastVisibleItems, visibleItemCount, totalItemCount;
    SubSearchAdapter subSearchAdapter;
    SearchItemsItem searchItemsItem = new SearchItemsItem();
    private boolean loading = true;
    private String pageCount = "1";
    private MediaScannerConnection msConn;


    @Override
    public void permissionGranted() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        searchBinding = DataBindingUtil.setContentView(SearchActivity.this, R.layout.activity_search);

        AndroidNetworking.initialize(SearchActivity.this);
        searchItemsItem = (SearchItemsItem) getIntent().getSerializableExtra("position");
    }

    @Override
    protected void onResume() {
        super.onResume();
        setAdapter();
    }

    private void setAdapter() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SearchActivity.this, RecyclerView.VERTICAL, false);
        searchBinding.rvVideos.setLayoutManager(linearLayoutManager);
        subSearchAdapter = new SubSearchAdapter(categoryItems, SearchActivity.this);
        searchBinding.rvVideos.setAdapter(subSearchAdapter);
        SnapHelper snapHelper = new PagerSnapHelper();
        searchBinding.rvVideos.setOnFlingListener(null);
        snapHelper.attachToRecyclerView(searchBinding.rvVideos);

        subSearchAdapter.downloadClickListener(this);
        subSearchAdapter.clearAll();
        getSearchData(pageCount, SearchFragment.searchStr);

        searchBinding.rvVideos.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            getSearchData(pageCount, SearchFragment.searchStr);
                            loading = true;
                        }
                    }
                }
            }
        });
    }

    private void getSearchData(String Count, String searchString) {
        categoryItems.clear();
        AndroidNetworking.get(Constant.SOCIALCATBASEURL + "mother_board_funny.php")
                .addQueryParameter("page", Count)
                .addQueryParameter("s", searchString)
                .addQueryParameter("type", "search")
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        SearchResponse playVideoResponse = new Gson().fromJson(response.toString(), SearchResponse.class);
                        if (playVideoResponse.getSuccess().equals("false")) {
                            loading = false;
                        } else {
                            if (pageCount.equals("1")) {
                                categoryItems.add(searchItemsItem);
                            }
                            pageCount = playVideoResponse.getNextPageToken();
                            categoryItems.addAll(playVideoResponse.getItems());
                            subSearchAdapter.addAll(categoryItems);
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
        new downloadTask(categoryItems.get(position).getTitle(), categoryItems.get(position).getVideoUrl(), position).execute();
    }

    @Override
    public void backPress() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void scanPhoto(final String imageFileName) {
        msConn = new MediaScannerConnection(SearchActivity.this, new MediaScannerConnection.MediaScannerConnectionClient() {
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
                searchBinding.sProgressBar.setImageBitmap(result);
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
                    searchBinding.sProgressBar.showProgress(true);
                    Constant.expand(searchBinding.rlProgress);
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
                                    searchBinding.sProgressBar.setProgress(progress);
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
                                    searchBinding.tvTile1.setText("Download Completed.");
                                    subSearchAdapter.notifyItemChanged(position);
                                    final Handler handler = new Handler(Looper.getMainLooper());
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Constant.collapse(searchBinding.rlProgress);
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