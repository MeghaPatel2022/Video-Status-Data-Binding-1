package apps.vidstatus.android.shotvideo.model.landscape;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class LandscapeResponse {

    @SerializedName("success")
    private String success;

    @SerializedName("nextPageToken")
    private String nextPageToken;

    @SerializedName("items")
    private ArrayList<ItemsItem> items;

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

    public ArrayList<ItemsItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<ItemsItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return
                "LandscapeResponse{" +
                        "success = '" + success + '\'' +
                        ",nextPageToken = '" + nextPageToken + '\'' +
                        ",items = '" + items + '\'' +
                        "}";
    }
}