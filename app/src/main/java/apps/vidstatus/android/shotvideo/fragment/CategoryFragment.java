package apps.vidstatus.android.shotvideo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;

import apps.vidstatus.android.shotvideo.R;
import apps.vidstatus.android.shotvideo.adapter.homeadapter.CategoryAdapter;
import apps.vidstatus.android.shotvideo.adapter.homeadapter.LanguageAdapter;
import apps.vidstatus.android.shotvideo.databinding.FragmentCategoryBinding;
import apps.vidstatus.android.shotvideo.model.category.CategoryResponse;
import apps.vidstatus.android.shotvideo.utils.Constant;

public class CategoryFragment extends Fragment {

    FragmentCategoryBinding categoryBinding;

    LanguageAdapter languageAdapter;
    CategoryAdapter categoryAdapter;

    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        categoryBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_category, container, false);

        categoryBinding.rvLanguage.setLayoutManager(new GridLayoutManager(getContext(), 2));
        languageAdapter = new LanguageAdapter(new ArrayList<>(), getActivity());
        categoryBinding.rvLanguage.setAdapter(languageAdapter);

        categoryBinding.rvCategory.setLayoutManager(new GridLayoutManager(getContext(), 2));
        categoryAdapter = new CategoryAdapter(new ArrayList<>(), getActivity());
        categoryBinding.rvCategory.setAdapter(categoryAdapter);
        categoryBinding.rvCategory.setNestedScrollingEnabled(false);

        getLanguageList();

        return categoryBinding.getRoot();
    }

    private void getLanguageList() {
        languageAdapter.clearAll();
        categoryAdapter.clearAll();
        AndroidNetworking.post(Constant.SOCIALCATBASEURL + "category_android.php")
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        CategoryResponse categoryResponse = new Gson().fromJson(response.toString(), CategoryResponse.class);
                        if (categoryResponse.getSuccess().equals("true")) {
                            languageAdapter.addAll(categoryResponse.getLanguage());
                            categoryAdapter.addAll(categoryResponse.getCategory());
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

}