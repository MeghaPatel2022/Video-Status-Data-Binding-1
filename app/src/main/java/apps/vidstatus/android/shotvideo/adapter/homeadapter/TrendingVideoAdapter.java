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

import apps.vidstatus.android.shotvideo.LandscapeVideoPlayer;
import apps.vidstatus.android.shotvideo.R;
import apps.vidstatus.android.shotvideo.databinding.ListLandscapeBinding;
import apps.vidstatus.android.shotvideo.model.landscape.ItemsItem;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class TrendingVideoAdapter extends RecyclerView.Adapter<TrendingVideoAdapter.VideoViewHolder> {

    ArrayList<ItemsItem> landscapeItems;
    Activity activity;

    public TrendingVideoAdapter(ArrayList<ItemsItem> landscapeItems, Activity activity) {
        this.landscapeItems = landscapeItems;
        this.activity = activity;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ListLandscapeBinding landscapeBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_landscape, parent, false);

        return new VideoViewHolder(landscapeBinding);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {

        holder.landscapeBinding.mImage.setClipToOutline(true);

        ItemsItem itemsItem = landscapeItems.get(position);
        holder.landscapeBinding.setItems(itemsItem);
        Glide
                .with(activity)
                .load(itemsItem.getVideoThumb())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.landscapeBinding.sProgressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.landscapeBinding.sProgressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .transition(withCrossFade())
                .transition(new DrawableTransitionOptions().crossFade(500))
                .into(holder.landscapeBinding.mImage);

        holder.landscapeBinding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // intialize Bundle instance
                Bundle bundle = new Bundle();
                bundle.putSerializable("VideoList", landscapeItems);
                Intent intent = new Intent(activity, LandscapeVideoPlayer.class);
                intent.putExtras(bundle);
                intent.putExtra("position", itemsItem);
                activity.startActivity(intent);
                activity.finish();
            }
        });
    }

    public void addAll(ArrayList<ItemsItem> itemsItems) {
        landscapeItems.addAll(itemsItems);
        notifyItemRangeInserted(landscapeItems.size() - itemsItems.size(), landscapeItems.size() - 1);
    }

    public void clearAll() {
        landscapeItems.clear();
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return landscapeItems.size();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {

        private final ListLandscapeBinding landscapeBinding;


        public VideoViewHolder(ListLandscapeBinding landscapeBinding) {
            super(landscapeBinding.getRoot());
            this.landscapeBinding = landscapeBinding;
        }
    }

}
