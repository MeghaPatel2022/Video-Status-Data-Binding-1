package apps.vidstatus.android.shotvideo.model.subcategory;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SubCategoryResponse {

    @SerializedName("success")
    private String success;

    @SerializedName("nextPageToken")
    private String nextPageToken;

    @SerializedName("items")
    private List<SubCatItemsItem> items;

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public List<SubCatItemsItem> getItems() {
        return items;
    }

    public void setItems(List<SubCatItemsItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return
                "SubCategoryResponse{" +
                        "success = '" + success + '\'' +
                        ",nextPageToken = '" + nextPageToken + '\'' +
                        ",items = '" + items + '\'' +
                        "}";
    }
}