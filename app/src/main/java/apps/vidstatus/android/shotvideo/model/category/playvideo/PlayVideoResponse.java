package apps.vidstatus.android.shotvideo.model.category.playvideo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlayVideoResponse {

    @SerializedName("success")
    private String success;

    @SerializedName("nextPageToken")
    private String nextPageToken;

    @SerializedName("items")
    private List<PlayItemsItem> items;

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

    public List<PlayItemsItem> getItems() {
        return items;
    }

    public void setItems(List<PlayItemsItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return
                "PlayVideoResponse{" +
                        "success = '" + success + '\'' +
                        ",nextPageToken = '" + nextPageToken + '\'' +
                        ",items = '" + items + '\'' +
                        "}";
    }
}