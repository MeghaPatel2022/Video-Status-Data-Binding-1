package apps.vidstatus.android.shotvideo.adapter.homeadapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import apps.vidstatus.android.shotvideo.R;
import apps.vidstatus.android.shotvideo.StoryStatusVideoPlayer;
import apps.vidstatus.android.shotvideo.databinding.ImageViewBinding;
import apps.vidstatus.android.shotvideo.model.landscape.ItemsItem;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class StoryStatusAdapter extends RecyclerView.Adapter<StoryStatusAdapter.VideoViewHolder> {


    ArrayList<ItemsItem> storyStatusItems;
    Activity activity;

    public StoryStatusAdapter(ArrayList<ItemsItem> storyStatusItems, Activity activity) {
        this.storyStatusItems = storyStatusItems;
        this.activity = activity;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ImageViewBinding imageViewBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.image_view, parent, false);

        return new VideoViewHolder(imageViewBinding);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {

        holder.imageViewBinding.mImage.setClipToOutline(true);

        ItemsItem itemsItem = storyStatusItems.get(position);

        Glide
                .with(activity)
                .load(itemsItem.getVideoThumb())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.imageViewBinding.sProgressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.imageViewBinding.sProgressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .transition(withCrossFade())
                .transition(new DrawableTransitionOptions().crossFade(500))
                .into(holder.imageViewBinding.mImage);

        holder.imageViewBinding.getRoot().setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("VideoList", storyStatusItems);
            Intent intent = new Intent(activity, StoryStatusVideoPlayer.class);
            intent.putExtras(bundle);
            intent.putExtra("position", itemsItem);
            activity.startActivity(intent);
            activity.finish();
        });
    }

    @Override
    public int getItemCount() {
        return storyStatusItems.size();
    }

    public void addAll(ArrayList<ItemsItem> itemsItems) {
        storyStatusItems.addAll(itemsItems);
        notifyItemRangeInserted(storyStatusItems.size() - itemsItems.size(), storyStatusItems.size() - 1);
    }

    public void clearAll() {
        storyStatusItems.clear();
        notifyDataSetChanged();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {

        private final ImageViewBinding imageViewBinding;

        public VideoViewHolder(ImageViewBinding imageViewBinding) {
            super(imageViewBinding.getRoot());
            this.imageViewBinding = imageViewBinding;
        }
    }

}
