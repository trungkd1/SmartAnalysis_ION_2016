package jp.co.fujixerox.sa.ion.entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import jp.co.fujixerox.sa.ion.utils.JsonParser;

/**
 * Json Catalog object
 */
public class Catalog implements Parcelable {
    /**
     * カタログIDの文字列
     */
    private long catalog_id;
    /**
     * 擬音種類の文字列<br>
     * 例：短周期音、連続音
     */
    private String type;
    /**
     * 原因パーツの文字列
     */
    private String cause;
    /**
     * 処置方法の文字列
     */
    private String method;
    /**
     * 処置方法詳細の文字列
     */
    private String method_detail;
    /**
     * 確認方法詳細の文字列
     */
    private String confirm_detail;
    /**
     * 処置方法詳細の画像URL1のリスト
     */
    private String method_image;
    /**
     * 確認方法詳細の画像URL1のリスト
     */
    private String confirm_image;
    /**
     * 擬音表現の文字列
     */
    private String category;
    /**
     * 発生条件の文字列
     */
    private String condition;
    /**
     * 動作モード<br>
     * 例：4C/BW
     */
    private String color;
    /**
     * FFT画像のダウンロードURL
     */
    private String sample_picture;
    /**
     * 音源のダウンロードURL
     */
    private String sample_sound;
//    /**
//     * Catalog method image url list
//     */
//    private List<String> method_images = new ArrayList<>();

//    public List<String> getMethod_images() {
//        return method_images;
//    }
//
//    public void setMethod_images(List<String> method_images) {
//        this.method_images = method_images;
//    }
    public List<String> getMethodImagesList() {
        return JsonParser.gson.fromJson(this.method_image, JsonParser.LIST_TYPE);
    }

    public List<String> getConfirmImagesList() {
        return JsonParser.gson.fromJson(this.confirm_image, JsonParser.LIST_TYPE);
    }
    public long getCatalog_id() {
        return catalog_id;
    }

    public void setCatalog_id(long catalog_id) {
        this.catalog_id = catalog_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeText() {
        String typeValue="";

        return typeValue;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSample_picture() {
        return sample_picture;
    }

    public void setSample_picture(String sample_picture) {
        this.sample_picture = sample_picture;
    }

    public String getSample_sound() {
        return sample_sound;
    }

    public void setSample_sound(String sample_sound) {
        this.sample_sound = sample_sound;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMethod_detail() {
        return method_detail;
    }

    public void setMethod_detail(String method_detail) {
        this.method_detail = method_detail;
    }

    public String getMethod_image() {
        return method_image;
    }

    public void setMethod_image(String method_image) {
        this.method_image = method_image;
    }

    public String getConfirm_detail() {
        return confirm_detail;
    }

    public void setConfirm_detail(String confirm_detail) {
        this.confirm_detail = confirm_detail;
    }

    public String getConfirm_image() {
        return confirm_image;
    }

    public void setConfirm_image(String confirm_image) {
        this.confirm_image = confirm_image;
    }

    public String toString() {
        return String.format("CATALOG_SCREEN[catalog_id:%d; sample_picture:%s; sample_sound: %s, cause: %s, info: %s, des: %s]", catalog_id, sample_picture,
                sample_picture, cause, method, method_detail);
    }

    public Catalog() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.catalog_id);
        dest.writeString(this.type);
        dest.writeString(this.category);
        dest.writeString(this.sample_picture);
        dest.writeString(this.sample_sound);
        dest.writeString(this.cause);
        dest.writeString(this.method);
        dest.writeString(this.method_detail);
        dest.writeString(this.condition);
        dest.writeString(this.color);
        dest.writeString(this.method_image);
        dest.writeString(this.confirm_detail);
        dest.writeString(this.confirm_image);
//        dest.writeStringList(this.method_images);
    }

    protected Catalog(Parcel in) {
        this.catalog_id = in.readLong();
        this.type = in.readString();
        this.category = in.readString();
        this.sample_picture = in.readString();
        this.sample_sound = in.readString();
        this.cause = in.readString();
        this.method = in.readString();
        this.method_detail = in.readString();
        this.condition = in.readString();
        this.color = in.readString();
        this.method_image = in.readString();
        this.confirm_detail = in.readString();
        this.confirm_image = in.readString();
//        this.method_images = in.createStringArrayList();
    }

    public static final Creator<Catalog> CREATOR = new Creator<Catalog>() {
        @Override
        public Catalog createFromParcel(Parcel source) {
            return new Catalog(source);
        }

        @Override
        public Catalog[] newArray(int size) {
            return new Catalog[size];
        }
    };
}
