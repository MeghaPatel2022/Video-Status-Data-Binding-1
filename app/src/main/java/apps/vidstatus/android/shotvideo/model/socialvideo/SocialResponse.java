package apps.vidstatus.android.shotvideo.model.socialvideo;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SocialResponse {

    @SerializedName("success")
    private String success;

    @SerializedName("nextPageToken")
    private String nextPageToken;

    @SerializedName("items")
    private ArrayList<SocialItemsItem> items;

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

    public ArrayList<SocialItemsItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<SocialItemsItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return
                "SocialResponse{" +
                        "success = '" + success + '\'' +
                        ",nextPageToken = '" + nextPageToken + '\'' +
                        ",items = '" + items + '\'' +
                        "}";
    }
}