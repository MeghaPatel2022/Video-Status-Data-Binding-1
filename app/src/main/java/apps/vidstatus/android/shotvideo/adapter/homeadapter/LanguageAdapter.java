package apps.vidstatus.android.shotvideo.adapter.homeadapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

import apps.vidstatus.android.shotvideo.CatVideoPlayer;
import apps.vidstatus.android.shotvideo.R;
import apps.vidstatus.android.shotvideo.databinding.ListLangugeBinding;
import apps.vidstatus.android.shotvideo.model.category.LanguageItem;
import apps.vidstatus.android.shotvideo.utils.Constant;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.MyClassView> {

    private final FirebaseAnalytics mFirebaseAnalytics;
    ArrayList<LanguageItem> languageItems;
    Activity activity;

    public LanguageAdapter(ArrayList<LanguageItem> languageItems, Activity activity) {
        this.languageItems = languageItems;
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
        ListLangugeBinding langugeBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_languge, parent, false);
        return new MyClassView(langugeBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyClassView holder, int position) {

        holder.langugeBinding.setCategory(languageItems.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constant.isCategory = true;
                if (v.isSelected()) {
                    v.setSelected(false);
                    holder.langugeBinding.tvLanguage.setTextColor(activity.getResources().getColor(R.color.yellow));
                } else {
                    v.setSelected(true);
                    holder.langugeBinding.tvLanguage.setTextColor(activity.getResources().getColor(R.color.black));
                }
            }
        });

        holder.langugeBinding.getRoot().setOnClickListener(v -> {
            fireAnalytics("select_category", languageItems.get(position).getCatName());
            Intent intent = new Intent(activity, CatVideoPlayer.class);
            intent.putExtra("catId", languageItems.get(position).getId());
            activity.startActivity(intent);
            activity.finish();
        });
    }

    public void addAll(ArrayList<LanguageItem> itemsItems) {
        languageItems.clear();
        languageItems.addAll(itemsItems);
        notifyDataSetChanged();
    }

    public void clearAll() {
        languageItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return languageItems.size();
    }

    public class MyClassView extends RecyclerView.ViewHolder {

        private final ListLangugeBinding langugeBinding;

        public MyClassView(ListLangugeBinding langugeBinding) {
            super(langugeBinding.getRoot());
            this.langugeBinding = langugeBinding;
        }
    }
}
