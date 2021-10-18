package apps.vidstatus.android.shotvideo.adapter.homeadapter;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.util.ArrayList;

import apps.vidstatus.android.shotvideo.R;
import apps.vidstatus.android.shotvideo.databinding.ListPortraitItemBinding;
import apps.vidstatus.android.shotvideo.model.landscape.ItemsItem;
import apps.vidstatus.android.shotvideo.utils.Constant;
import es.dmoral.toasty.Toasty;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class InnerStoryStatusPlayerAdapter extends RecyclerView.Adapter<InnerStoryStatusPlayerAdapter.MyClassView> {

    private final FirebaseAnalytics mFirebaseAnalytics;
    ArrayList<ItemsItem> landscapeItems;
    Activity activity;
    private DownloadClickListener mDownloadClickListener;

    public InnerStoryStatusPlayerAdapter(ArrayList<ItemsItem> landscapeItems, Activity activity) {
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

    public void downloadClickListener(DownloadClickListener listener) {
        mDownloadClickListener = listener;
    }

    @NonNull
    @Override
    public MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListPortraitItemBinding portraitItemBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_portrait_item, parent, false);
        return new MyClassView(portraitItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyClassView viewHolder, int position) {

        Glide.with(activity)
                .load(landscapeItems.get(position).getVideoThumb())
                .transition(withCrossFade())
                .transition(new DrawableTransitionOptions().crossFade(500))
                .into(viewHolder.portraitItemBinding.imageView);

        viewHolder.portraitItemBinding.rlLoading.setVisibility(View.VISIBLE);
        viewHolder.portraitItemBinding.avLoader.smoothToShow();
        viewHolder.portraitItemBinding.imageView.setVisibility(View.VISIBLE);

        viewHolder.portraitItemBinding.setLandsacp(landscapeItems.get(position));

        Uri uri = Uri.parse(landscapeItems.get(position).getVideoUrl());

        viewHolder.portraitItemBinding.videoView.setVideoURI(uri);

        File file = new File(Constant.FOLDERPATH, landscapeItems.get(position).getTitle() + ".mp4");
        if (!file.exists()) {
            viewHolder.portraitItemBinding.imgDownload.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_download));
        } else {
            viewHolder.portraitItemBinding.imgDownload.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_alredy_download));
        }

        viewHolder.portraitItemBinding.imgDownload.setOnClickListener(v -> {
            fireAnalytics("download_video", "StoryStatus");
            if (mDownloadClickListener != null) {
                if (!file.exists())
                    mDownloadClickListener.onDownloadClick(position, landscapeItems.get(position).getVideoThumb(), landscapeItems.get(position).getTitle(), landscapeItems.get(position).getVideoUrl());
                else
                    Toasty.info(activity, "Already Downloaded.", Toast.LENGTH_SHORT, true).show();
            }
        });

        viewHolder.portraitItemBinding.imgWhatsApp.setOnClickListener(v -> {
            fireAnalytics("share_to_WhatsApp", "StoryStatus");
            Constant.whatsappShareVideo(activity, landscapeItems.get(position).getVideoUrl(), file);
        });

        viewHolder.portraitItemBinding.imgShare.setOnClickListener(v -> {
            fireAnalytics("share_video_to_other_app", "StoryStatus");
            Constant.shareVideo(activity, landscapeItems.get(position).getVideoUrl(), file);
        });

        viewHolder.portraitItemBinding.videoView.setOnPreparedListener(mp -> {
            float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
            float screenRatio = viewHolder.portraitItemBinding.videoView.getWidth() / (float)
                    viewHolder.portraitItemBinding.videoView.getHeight();
            float scaleX = videoRatio / screenRatio;
            if (scaleX >= 1f) {
                viewHolder.portraitItemBinding.videoView.setScaleX(scaleX);
            } else {
                viewHolder.portraitItemBinding.videoView.setScaleY(1f / scaleX);
            }
            mp.setLooping(true);
            viewHolder.portraitItemBinding.imageView.setVisibility(View.GONE);
            viewHolder.portraitItemBinding.rlLoading.setVisibility(View.GONE);
            viewHolder.portraitItemBinding.avLoader.smoothToHide();
            viewHolder.portraitItemBinding.videoView.start();
        });

        viewHolder.portraitItemBinding.imgBack.setOnClickListener(v -> {
            if (mDownloadClickListener != null) {
                mDownloadClickListener.backPress();
            }
        });
    }

    @Override
    public int getItemCount() {
        return landscapeItems.size();
    }

    public void addAll(ArrayList<ItemsItem> itemsItems) {
        landscapeItems.addAll(itemsItems);
        notifyDataSetChanged();
//        notifyItemRangeInserted(landscapeItems.size() - itemsItems.size(), landscapeItems.size() - 1);
    }

    public void clearAll() {
        landscapeItems.clear();
        notifyDataSetChanged();
    }

    //Define your Interface method here
    public interface DownloadClickListener {
        void onDownloadClick(int position, String imageUrl, String title, String url);

        void backPress();
    }

    public class MyClassView extends RecyclerView.ViewHolder {

        private final ListPortraitItemBinding portraitItemBinding;

        public MyClassView(ListPortraitItemBinding portraitItemBinding) {
            super(portraitItemBinding.getRoot());
            this.portraitItemBinding = portraitItemBinding;
        }
    }

}
