package apps.vidstatus.android.shotvideo.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;

import apps.vidstatus.android.shotvideo.R;
import apps.vidstatus.android.shotvideo.adapter.homeadapter.SocialVideoAdapter;
import apps.vidstatus.android.shotvideo.adapter.homeadapter.StoryStatusAdapter;
import apps.vidstatus.android.shotvideo.adapter.homeadapter.TrendingVideoAdapter;
import apps.vidstatus.android.shotvideo.databinding.FragmentTrendingBinding;
import apps.vidstatus.android.shotvideo.model.landscape.ItemsItem;
import apps.vidstatus.android.shotvideo.model.landscape.LandscapeResponse;
import apps.vidstatus.android.shotvideo.model.socialvideo.SocialItemsItem;
import apps.vidstatus.android.shotvideo.model.socialvideo.SocialResponse;
import apps.vidstatus.android.shotvideo.utils.Constant;


public class TrendingFragment extends Fragment {

    private final ArrayList<ItemsItem> landscapeItems = new ArrayList<>();
    private final ArrayList<ItemsItem> storyStatusVideoes = new ArrayList<>();
    private final ArrayList<SocialItemsItem> socialItemsItems = new ArrayList<>();
    FragmentTrendingBinding trendingBinding;
    TrendingVideoAdapter trendingVideoAdapter;

    StoryStatusAdapter storyStatusAdapter;
    SocialVideoAdapter socialVideoAdapter;
    int pastVisibleItems, visibleItemCount, totalItemCount;
    Animation animFadeOut, animFadeIn;
    private boolean loading = true;
    private String pageCount = "1";
    private GridLayoutManager storyGridLayoutManager;

    public TrendingFragment() {
        // Required empty public constructor
    }

    private void avLoaderVisible() {
        trendingBinding.rlLoading.setVisibility(View.VISIBLE);
        trendingBinding.avLoader.smoothToShow();
    }

    private void avLoaderGone() {
        trendingBinding.avLoader.smoothToHide();
        trendingBinding.rlLoading.setVisibility(View.GONE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        trendingBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_trending, container, false);


        trendingVideoAdapter = new TrendingVideoAdapter(landscapeItems, getActivity());
        trendingBinding.rvVideos.setLayoutManager(new GridLayoutManager(getContext(), 2));
        trendingBinding.rvVideos.setAdapter(trendingVideoAdapter);

        AndroidNetworking.initialize(getContext());

        animFadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fadeout);
        animFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fadein);

        return trendingBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        setAdapter();
    }

    private void setAdapter() {
        switch (Constant.NAV_SELECTED_ITEM) {
            case "Story Status":
                storyGridLayoutManager = new GridLayoutManager(getContext(), 2);
                trendingBinding.rvVideos.setLayoutManager(storyGridLayoutManager);
                storyStatusAdapter = new StoryStatusAdapter(storyStatusVideoes, getActivity());
                trendingBinding.rvVideos.setAdapter(storyStatusAdapter);

                storyStatusAdapter.clearAll();
                getStoryStatusVideos(pageCount);

                trendingBinding.rvVideos.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if (dy > 0) { //check for scroll down
                            visibleItemCount = storyGridLayoutManager.getChildCount();
                            totalItemCount = storyGridLayoutManager.getItemCount();
                            pastVisibleItems = storyGridLayoutManager.findFirstVisibleItemPosition();

                            trendingBinding.imgFirst.setVisibility(View.VISIBLE);
                            animFadeIn.reset();
                            trendingBinding.imgFirst.clearAnimation();
                            trendingBinding.imgFirst.startAnimation(animFadeIn);

                            final Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    trendingBinding.imgFirst.startAnimation(animFadeOut);
                                    trendingBinding.imgFirst.setVisibility(View.GONE);
                                }
                            }, 3000);

                            if (loading) {
                                if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                                    loading = false;
                                    Log.v("...", "Last Item Wow !");
                                    // Do pagination.. i.e. fetch new data
                                    getStoryStatusVideos(pageCount);
                                    loading = true;
                                }
                            }
                        } else {
                            visibleItemCount = storyGridLayoutManager.getChildCount();
                            totalItemCount = storyGridLayoutManager.getItemCount();
                            pastVisibleItems = storyGridLayoutManager.findFirstVisibleItemPosition();

                            if (totalItemCount >= 10 && pastVisibleItems >= 10) {

                                trendingBinding.imgFirst.setVisibility(View.VISIBLE);
                                animFadeIn.reset();
                                trendingBinding.imgFirst.clearAnimation();
                                trendingBinding.imgFirst.startAnimation(animFadeIn);

                                final Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        trendingBinding.imgFirst.startAnimation(animFadeOut);
                                        trendingBinding.imgFirst.setVisibility(View.GONE);
                                    }
                                }, 3000);
                            }
                        }
                    }
                });

                break;
            case "Landscape Status":
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
                trendingBinding.rvVideos.setLayoutManager(linearLayoutManager);
                trendingVideoAdapter = new TrendingVideoAdapter(landscapeItems, getActivity());
                trendingBinding.rvVideos.setAdapter(trendingVideoAdapter);
                trendingVideoAdapter.clearAll();
                getLandscapeVideos(pageCount);

                trendingBinding.rvVideos.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if (dy > 0) { //check for scroll down
                            visibleItemCount = linearLayoutManager.getChildCount();
                            totalItemCount = linearLayoutManager.getItemCount();
                            pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                            trendingBinding.imgFirst.setVisibility(View.VISIBLE);
                            animFadeIn.reset();
                            trendingBinding.imgFirst.clearAnimation();
                            trendingBinding.imgFirst.startAnimation(animFadeIn);

                            final Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    trendingBinding.imgFirst.startAnimation(animFadeOut);
                                    trendingBinding.imgFirst.setVisibility(View.GONE);
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

                                trendingBinding.imgFirst.setVisibility(View.VISIBLE);
                                animFadeIn.reset();
                                trendingBinding.imgFirst.clearAnimation();
                                trendingBinding.imgFirst.startAnimation(animFadeIn);

                                final Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        trendingBinding.imgFirst.startAnimation(animFadeOut);
                                        trendingBinding.imgFirst.setVisibility(View.GONE);
                                    }
                                }, 3000);
                            }
                        }
                    }
                });


                break;
            case "Social Video":
                GridLayoutManager gridLayoutManager1 = new GridLayoutManager(getContext(), 2);
                trendingBinding.rvVideos.setLayoutManager(gridLayoutManager1);
                socialVideoAdapter = new SocialVideoAdapter(socialItemsItems, getActivity());
                trendingBinding.rvVideos.setAdapter(socialVideoAdapter);

                socialVideoAdapter.clearAll();
                getSocialVideos(pageCount);

                trendingBinding.rvVideos.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if (dy > 0) { //check for scroll down
                            visibleItemCount = gridLayoutManager1.getChildCount();
                            totalItemCount = gridLayoutManager1.getItemCount();
                            pastVisibleItems = gridLayoutManager1.findFirstVisibleItemPosition();

                            trendingBinding.imgFirst.setVisibility(View.VISIBLE);
                            animFadeIn.reset();
                            trendingBinding.imgFirst.clearAnimation();
                            trendingBinding.imgFirst.startAnimation(animFadeIn);

                            final Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    trendingBinding.imgFirst.startAnimation(animFadeOut);
                                    trendingBinding.imgFirst.setVisibility(View.GONE);
                                }
                            }, 3000);

                            if (loading) {
                                if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                                    loading = false;
                                    Log.v("...", "Last Item Wow !");
                                    // Do pagination.. i.e. fetch new data
                                    getSocialVideos(pageCount);
                                    loading = true;
                                }
                            }
                        } else {
                            visibleItemCount = gridLayoutManager1.getChildCount();
                            totalItemCount = gridLayoutManager1.getItemCount();
                            pastVisibleItems = gridLayoutManager1.findFirstVisibleItemPosition();

                            if (totalItemCount >= 10 && pastVisibleItems >= 10) {

                                trendingBinding.imgFirst.setVisibility(View.VISIBLE);
                                animFadeIn.reset();
                                trendingBinding.imgFirst.clearAnimation();
                                trendingBinding.imgFirst.startAnimation(animFadeIn);

                                final Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        trendingBinding.imgFirst.startAnimation(animFadeOut);
                                        trendingBinding.imgFirst.setVisibility(View.GONE);
                                    }
                                }, 3000);
                            }
                        }
                    }
                });

                break;
        }
    }

    private void getLandscapeVideos(String Count) {
        avLoaderVisible();
        AndroidNetworking.post(Constant.BASEURL + "get_new_video_landscape.php")
                .addBodyParameter("seed", "9958")
                .addBodyParameter("page", Count)
                .addBodyParameter("type", "Trending")
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
                            trendingVideoAdapter.addAll(landscapeResponse.getItems());
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                avLoaderGone();
                            }
                        });
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("LLLL_Response: ", anError.getErrorDetail());
                    }
                });
    }

    private void getStoryStatusVideos(String Count) {
        avLoaderVisible();
        AndroidNetworking.post(Constant.BASEURL + "get_new_video_portrait.php")
                .addBodyParameter("seed", "8216")
                .addBodyParameter("page", Count)
                .addBodyParameter("type", "Trending")
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
                            storyStatusAdapter.addAll(landscapeResponse.getItems());
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

    private void getSocialVideos(String Count) {
        avLoaderVisible();
        Log.e("LLL_Url: ", Constant.SOCIALCATBASEURL + "mother_board_funny.php");
        Log.e("LLL_Url: ", Count);
        AndroidNetworking.get(Constant.SOCIALCATBASEURL + "mother_board_funny.php")
                .addQueryParameter("page", Count)
                .addQueryParameter("type", "popular")
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        SocialResponse socialResponse = new Gson().fromJson(response.toString(), SocialResponse.class);
                        if (socialResponse.getSuccess().equals("false")) {
                            loading = false;
                        } else {
                            pageCount = socialResponse.getNextPageToken();
                            socialVideoAdapter.addAll(socialResponse.getItems());
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                avLoaderGone();
                            }
                        });
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("LLLL_Response: ", anError.getErrorDetail());
                    }
                });
    }

}