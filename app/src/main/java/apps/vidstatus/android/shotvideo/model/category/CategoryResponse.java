package apps.vidstatus.android.shotvideo.model.category;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CategoryResponse {

    @SerializedName("total")
    private int total;

    @SerializedName("success")
    private String success;

    @SerializedName("language")
    private ArrayList<LanguageItem> language;

    @SerializedName("category")
    private ArrayList<CategoryItem> category;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public ArrayList<LanguageItem> getLanguage() {
        return language;
    }

    public void setLanguage(ArrayList<LanguageItem> language) {
        this.language = language;
    }

    public ArrayList<CategoryItem> getCategory() {
        return category;
    }

    public void setCategory(ArrayList<CategoryItem> category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return
                "CategoryResponse{" +
                        "total = '" + total + '\'' +
                        ",success = '" + success + '\'' +
                        ",language = '" + language + '\'' +
                        ",category = '" + category + '\'' +
                        "}";
    }
}