package apps.vidstatus.android.shotvideo.adapter.homeadapter;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.AnalyticsListener;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.DownloadProgressListener;
import com.bumptech.glide.Glide;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.util.ArrayList;

import apps.vidstatus.android.shotvideo.LandscapeVideoPlayer;
import apps.vidstatus.android.shotvideo.R;
import apps.vidstatus.android.shotvideo.databinding.InnerListLandscapeBinding;
import apps.vidstatus.android.shotvideo.model.landscape.ItemsItem;
import apps.vidstatus.android.shotvideo.utils.Constant;
import es.dmoral.toasty.Toasty;

public class InnerLandscapeAdapter extends RecyclerView.Adapter<InnerLandscapeAdapter.VideoViewHolder> {

    private final FirebaseAnalytics mFirebaseAnalytics;
    ArrayList<ItemsItem> landscapeItems;
    Activity activity;
    private MediaScannerConnection msConn;

    public InnerLandscapeAdapter(ArrayList<ItemsItem> landscapeItems, Activity activity) {
        this.landscapeItems = landscapeItems;
        this.activity = activity;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
    }

    private void fireAnalytics(String arg1, String arg2) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, arg1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg2);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        InnerListLandscapeBinding listLandscapeBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.inner_list_landscape, parent, false);

        return new VideoViewHolder(listLandscapeBinding);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {

        holder.listLandscapeBinding.mImage.setClipToOutline(true);

        ItemsItem itemsItem = landscapeItems.get(position);

        Glide
                .with(activity)
                .load(itemsItem.getVideoThumb())
                .into(holder.listLandscapeBinding.mImage);

        holder.listLandscapeBinding.tvTile.setText(itemsItem.getTitle());

        File file = new File(Constant.FOLDERPATH, itemsItem.getTitle() + ".mp4");
        if (!file.exists()) {
            holder.listLandscapeBinding.imgDownload.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_download));
        } else {
            holder.listLandscapeBinding.imgDownload.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_alredy_download));
        }

        holder.listLandscapeBinding.imgDownload.setOnClickListener(v -> {
            fireAnalytics("download_video", "landscapeVideo");
            if (!file.exists())
                new downloadAdapterTask(itemsItem.getTitle(), itemsItem.getVideoUrl(), position, holder).execute();
            else
                Toasty.info(activity, "Already Downloaded.", Toast.LENGTH_SHORT, true).show();
        });

        holder.listLandscapeBinding.imgWhatsApp.setOnClickListener(v -> {
            fireAnalytics("share_to_WhatsApp", "landscapeVideo");
            Constant.whatsappShareVideo(activity, itemsItem.getVideoUrl(), file);
        });

        holder.listLandscapeBinding.imgShare.setOnClickListener(v -> {
            fireAnalytics("share_video_to_other_app", "landscapeVideo");
            Constant.shareVideo(activity, itemsItem.getVideoUrl(), file);
        });

        holder.listLandscapeBinding.getRoot().setOnClickListener(v -> {
            // intialize Bundle instance
            Bundle bundle = new Bundle();
            bundle.putSerializable("VideoList", landscapeItems);
            Intent intent = new Intent(activity, LandscapeVideoPlayer.class);
            intent.putExtras(bundle);
            intent.putExtra("position", itemsItem);
            activity.startActivity(intent);
            activity.finish();
        });
    }

    @Override
    public int getItemCount() {
        return landscapeItems.size();
    }


    public void addAll(ArrayList<ItemsItem> itemsItems) {
        landscapeItems.addAll(itemsItems);
        notifyItemRangeInserted(landscapeItems.size() - itemsItems.size(), landscapeItems.size() - 1);
    }

    public void clearAll() {
        landscapeItems.clear();
        notifyDataSetChanged();
    }

    public void scanPhoto(final String imageFileName) {
        msConn = new MediaScannerConnection(activity, new MediaScannerConnection.MediaScannerConnectionClient() {
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

    //Define your Interface method here
    public interface DownloadClickListener {
        void onDownloadClick(int position, String imageUrl, String title, String url);
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {

        private final InnerListLandscapeBinding listLandscapeBinding;

        public VideoViewHolder(InnerListLandscapeBinding listLandscapeBinding) {
            super(listLandscapeBinding.getRoot());
            this.listLandscapeBinding = listLandscapeBinding;
        }
    }

    private final class downloadAdapterTask extends AsyncTask<Void, Void, String> {

        private final int TIMEOUT_CONNECTION = 5000;//5sec
        private final int TIMEOUT_SOCKET = 30000;//30sec

        String fileName;
        String downloadUrl;
        int position;
        VideoViewHolder myClassView;

        public downloadAdapterTask(String fileName, String downloadUrl, int position, VideoViewHolder myClassView) {
            this.fileName = fileName;
            this.downloadUrl = downloadUrl;
            this.position = position;
            this.myClassView = myClassView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    myClassView.listLandscapeBinding.llProgress.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        protected String doInBackground(Void... params) {
            AndroidNetworking.download(downloadUrl, Constant.FOLDERPATH, fileName + ".mp4")
                    .setTag("downloadTest")
                    .setPriority(com.androidnetworking.common.Priority.IMMEDIATE)
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
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    int progress = (int) ((bytesDownloaded * 100) / totalBytes);
                                    myClassView.listLandscapeBinding.downloadProgress.setProgress(progress);
                                    Log.e("LLLLL_Progress: ", ((bytesDownloaded * 100) / totalBytes) + " Total: " + (totalBytes / 1000));
                                }
                            });

                        }
                    })
                    .startDownload(new DownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            // do anything after completion
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("LLLLL_Progress: ", "Download Completed.");

                                    File file = new File(Constant.FOLDERPATH, fileName + ".mp4");
                                    scanPhoto(file.toString());
                                    myClassView.listLandscapeBinding.imgDownload.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_alredy_download));
                                    final Handler handler = new Handler(Looper.getMainLooper());
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            myClassView.listLandscapeBinding.llProgress.setVisibility(View.GONE);
                                        }
                                    }, 2000);

                                }
                            });
                        }

                        @Override
                        public void onError(ANError error) {
                            // handle error
                            activity.runOnUiThread(new Runnable() {
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
