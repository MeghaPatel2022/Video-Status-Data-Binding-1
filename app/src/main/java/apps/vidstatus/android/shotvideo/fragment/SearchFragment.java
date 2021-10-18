package apps.vidstatus.android.shotvideo.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;

import apps.vidstatus.android.shotvideo.R;
import apps.vidstatus.android.shotvideo.adapter.homeadapter.SearchAdapter;
import apps.vidstatus.android.shotvideo.databinding.FragmentSearchBinding;
import apps.vidstatus.android.shotvideo.model.search.SearchItemsItem;
import apps.vidstatus.android.shotvideo.model.search.SearchResponse;
import apps.vidstatus.android.shotvideo.utils.Constant;

public class SearchFragment extends Fragment {

    public static String searchStr = "";
    private final ArrayList<SearchItemsItem> searchItemsItems = new ArrayList<>();
    FragmentSearchBinding searchBinding;
    SearchAdapter searchAdapter;

    int pastVisibleItems, visibleItemCount, totalItemCount;
    Animation animFadeOut, animFadeIn;
    private boolean loading = true;
    private int pageCount = 0;

    private GridLayoutManager searchGridLayoutManager;

    public SearchFragment() {
        // Required empty public constructor
    }

    private void avLoaderVisible() {
        searchBinding.rlLoading.setVisibility(View.VISIBLE);
        searchBinding.avLoader.smoothToShow();
    }

    private void avLoaderGone() {
        searchBinding.avLoader.smoothToHide();
        searchBinding.rlLoading.setVisibility(View.GONE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        searchBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);

        AndroidNetworking.initialize(getContext());

        animFadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fadeout);
        animFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fadein);
/*
        searchBinding.imgFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBinding.rvVideos.smoothScrollToPosition(0);
            }
        });*/

        searchBinding.autoCompleteEditText.setText(searchStr);

        searchBinding.autoCompleteEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchStr = s.toString();
                setAdapter();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /*imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchStr = auto_complete_edit_text.getText().toString();
                setAdapter();
            }
        });*/

        return searchBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        setAdapter();
    }

    private void setAdapter() {
        searchGridLayoutManager = new GridLayoutManager(getContext(), 2);
        searchAdapter = new SearchAdapter(new ArrayList<>(), getActivity());
        searchBinding.rvVideos.setLayoutManager(searchGridLayoutManager);
        searchBinding.rvVideos.setAdapter(searchAdapter);

        searchAdapter.clearAll();
        getSearchVideos(String.valueOf(pageCount), searchStr);

        searchBinding.rvVideos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) { //check for scroll down
                    visibleItemCount = searchGridLayoutManager.getChildCount();
                    totalItemCount = searchGridLayoutManager.getItemCount();
                    pastVisibleItems = searchGridLayoutManager.findFirstVisibleItemPosition();

                    searchBinding.imgFirst.setVisibility(View.VISIBLE);
                    animFadeIn.reset();
                    searchBinding.imgFirst.clearAnimation();
                    searchBinding.imgFirst.startAnimation(animFadeIn);

                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            searchBinding.imgFirst.startAnimation(animFadeOut);
                            searchBinding.imgFirst.setVisibility(View.GONE);
                        }
                    }, 3000);

                    if (loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = false;
                            Log.v("...", "Last Item Wow !");
                            // Do pagination.. i.e. fetch new data
                            getSearchVideos(String.valueOf(pageCount), searchStr);
                            loading = true;
                        }
                    }
                } else {
                    visibleItemCount = searchGridLayoutManager.getChildCount();
                    totalItemCount = searchGridLayoutManager.getItemCount();
                    pastVisibleItems = searchGridLayoutManager.findFirstVisibleItemPosition();

                    if (totalItemCount >= 10 && pastVisibleItems >= 10) {

                        searchBinding.imgFirst.setVisibility(View.VISIBLE);
                        animFadeIn.reset();
                        searchBinding.imgFirst.clearAnimation();
                        searchBinding.imgFirst.startAnimation(animFadeIn);

                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                searchBinding.imgFirst.startAnimation(animFadeOut);
                                searchBinding.imgFirst.setVisibility(View.GONE);
                            }
                        }, 3000);
                    }
                }
            }
        });

    }

    private void getSearchVideos(String Count, String searchString) {
        avLoaderVisible();
        Log.e("LLL_Url: ", Constant.SOCIALCATBASEURL + "mother_board_funny.php");
        Log.e("LLL_Url: ", Count);
        AndroidNetworking.get(Constant.SOCIALCATBASEURL + "mother_board_funny.php")
                .addQueryParameter("page", Count)
                .addQueryParameter("s", searchString)
                .addQueryParameter("type", "search")
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        SearchResponse searchResponse = new Gson().fromJson(response.toString(), SearchResponse.class);
                        if (searchResponse.getSuccess().equals("false")) {
                            loading = false;
                        } else {
                            Log.e("LLLL_Count: ", searchResponse.getNextPageToken());
                            pageCount += 1;
                            searchAdapter.addAll(searchResponse.getItems());
                        }

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    avLoaderGone();
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("LLLL_Response: ", anError.getErrorDetail());
                    }
                });
    }
}