package apps.vidstatus.android.shotvideo.model.landscape;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ItemsItem implements Serializable {

    @SerializedName("firstname")
    private String firstname;

    @SerializedName("video_url")
    private String videoUrl;

    @SerializedName("u_id")
    private String uId;

    @SerializedName("date_time_i")
    private String dateTimeI;

    @SerializedName("language")
    private String language;

    @SerializedName("id")
    private String id;

    @SerializedName("pic")
    private String pic;

    @SerializedName("title")
    private String title;

    @SerializedName("video_thumb")
    private String videoThumb;

    @SerializedName("lastname")
    private String lastname;

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getUId() {
        return uId;
    }

    public void setUId(String uId) {
        this.uId = uId;
    }

    public String getDateTimeI() {
        return dateTimeI;
    }

    public void setDateTimeI(String dateTimeI) {
        this.dateTimeI = dateTimeI;
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

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
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

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @Override
    public String toString() {
        return
                "Items{" +
                        "firstname = '" + firstname + '\'' +
                        ",video_url = '" + videoUrl + '\'' +
                        ",u_id = '" + uId + '\'' +
                        ",date_time_i = '" + dateTimeI + '\'' +
                        ",language = '" + language + '\'' +
                        ",id = '" + id + '\'' +
                        ",pic = '" + pic + '\'' +
                        ",title = '" + title + '\'' +
                        ",video_thumb = '" + videoThumb + '\'' +
                        ",lastname = '" + lastname + '\'' +
                        "}";
    }
}