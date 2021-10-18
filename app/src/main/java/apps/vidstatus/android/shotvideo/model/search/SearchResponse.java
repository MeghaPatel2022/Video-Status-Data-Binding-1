package apps.vidstatus.android.shotvideo.model.search;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SearchResponse {

    @SerializedName("success")
    private String success;

    @SerializedName("nextPageToken")
    private String nextPageToken;

    @SerializedName("items")
    private ArrayList<SearchItemsItem> items;

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

    public ArrayList<SearchItemsItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<SearchItemsItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return
                "SearchResponse{" +
                        "success = '" + success + '\'' +
                        ",nextPageToken = '" + nextPageToken + '\'' +
                        ",items = '" + items + '\'' +
                        "}";
    }
}