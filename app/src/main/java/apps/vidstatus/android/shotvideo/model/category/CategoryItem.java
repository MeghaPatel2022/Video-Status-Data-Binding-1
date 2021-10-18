package apps.vidstatus.android.shotvideo.model.category;

import com.google.gson.annotations.SerializedName;

public class CategoryItem {

    @SerializedName("cat_image")
    private String catImage;

    @SerializedName("cat_tags")
    private String catTags;

    @SerializedName("cat_name")
    private String catName;

    @SerializedName("cat_type")
    private String catType;

    @SerializedName("id")
    private String id;

    public String getCatImage() {
        return catImage;
    }

    public void setCatImage(String catImage) {
        this.catImage = catImage;
    }

    public String getCatTags() {
        return catTags;
    }

    public void setCatTags(String catTags) {
        this.catTags = catTags;
    }

    public String getCatName() {
        return catName;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }

    public String getCatType() {
        return catType;
    }

    public void setCatType(String catType) {
        this.catType = catType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return
                "CategoryItem{" +
                        "cat_image = '" + catImage + '\'' +
                        ",cat_tags = '" + catTags + '\'' +
                        ",cat_name = '" + catName + '\'' +
                        ",cat_type = '" + catType + '\'' +
                        ",id = '" + id + '\'' +
                        "}";
    }
}