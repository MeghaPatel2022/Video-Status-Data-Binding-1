package apps.vidstatus.android.shotvideo.adapter.homeadapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

import apps.vidstatus.android.shotvideo.CatVideoPlayer;
import apps.vidstatus.android.shotvideo.R;
import apps.vidstatus.android.shotvideo.databinding.ListCategoryBinding;
import apps.vidstatus.android.shotvideo.model.category.CategoryItem;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyClassView> {

    private final FirebaseAnalytics mFirebaseAnalytics;
    ArrayList<CategoryItem> categoryItems = new ArrayList<>();
    Activity activity;

    public CategoryAdapter(ArrayList<CategoryItem> categoryItems, Activity activity) {
        this.categoryItems = categoryItems;
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
    public MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListCategoryBinding categoryBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_category, parent, false);
        return new MyClassView(categoryBinding);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull MyClassView holder, int position) {
        holder.categoryBinding.mImage.setClipToOutline(true);

        holder.categoryBinding.setCatItems(categoryItems.get(position));

        Glide
                .with(activity)
                .load(categoryItems.get(position).getCatImage())
                .transition(withCrossFade())
                .transition(new DrawableTransitionOptions().crossFade(500))
                .into(holder.categoryBinding.mImage);


        holder.categoryBinding.getRoot().setOnClickListener(v -> {
            fireAnalytics("select_category", categoryItems.get(position).getCatName());
            Intent intent = new Intent(activity, CatVideoPlayer.class);
            intent.putExtra("catId", categoryItems.get(position).getId());
            activity.startActivity(intent);
            activity.finish();
        });
    }

    @Override
    public int getItemCount() {
        return categoryItems.size();
    }

    public void addAll(ArrayList<CategoryItem> itemsItems) {
        categoryItems.clear();
        categoryItems.addAll(itemsItems);
        notifyDataSetChanged();
    }

    public void clearAll() {
        categoryItems.clear();
        notifyDataSetChanged();
    }


    public class MyClassView extends RecyclerView.ViewHolder {

        private final ListCategoryBinding categoryBinding;

        public MyClassView(ListCategoryBinding categoryBinding) {
            super(categoryBinding.getRoot());
            this.categoryBinding = categoryBinding;
        }
    }
}
