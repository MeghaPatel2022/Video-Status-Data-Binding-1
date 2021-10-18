package apps.vidstatus.android.shotvideo.model.category.playvideo;

import com.google.gson.annotations.SerializedName;

public class PlayItemsItem {

    @SerializedName("video_url")
    private String videoUrl;

    @SerializedName("language")
    private String language;

    @SerializedName("id")
    private String id;

    @SerializedName("tag")
    private String tag;

    @SerializedName("title")
    private String title;

    @SerializedName("video_thumb")
    private String videoThumb;

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoThumb() {
        return videoThumb;
    }

    public void setVideoThumb(String videoThumb) {
        this.videoThumb = videoThumb;
    }

    @Override
    public String toString() {
        return
                "ItemsItem{" +
                        "video_url = '" + videoUrl + '\'' +
                        ",language = '" + language + '\'' +
                        ",id = '" + id + '\'' +
                        ",tag = '" + tag + '\'' +
                        ",title = '" + title + '\'' +
                        ",video_thumb = '" + videoThumb + '\'' +
                        "}";
    }
}