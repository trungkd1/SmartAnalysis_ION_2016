package jp.co.fujixerox.sa.ion.db;

import android.os.Parcel;
import android.os.Parcelable;

import jp.co.fujixerox.sa.ion.utils.Utility;

public class AudioFormData implements IAudioFormData, Parcelable {
    public static final Parcelable.Creator<AudioFormData> CREATOR = new Parcelable.Creator<AudioFormData>() {
        public AudioFormData createFromParcel(Parcel in) {
            return new AudioFormData(in);
        }

        public AudioFormData[] newArray(int size) {
            return new AudioFormData[size];
        }
    };
    private long id = 0;
    private long audioData_id = 0;
    private String formid = null;
    private String value = null;
    private String text = null;
    private String mimeType;

    public AudioFormData() {

    }

    public AudioFormData(String formid, String value, String text) {
        this.formid = formid;
        this.value = value;
        this.text = text;
    }

    public AudioFormData(String formid, String value, String text, String mimeType) {
        this.formid = formid;
        this.value = value;
        this.text = text;
        this.mimeType = mimeType;
    }


    protected AudioFormData(Parcel in) {
        id = in.readLong();
        audioData_id = in.readLong();
        formid = in.readString();
        value = in.readString();
        text = in.readString();
        mimeType = in.readString();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAudioData_id() {
        return audioData_id;
    }

    public void setAudioData_id(long audioData_id) {
        this.audioData_id = audioData_id;
    }

    public String getFormid() {
        return formid;
    }

    public void setFormid(String formid) {
        this.formid = formid;
    }

    public String getValue() {
        return value;
    }
    public String getValueForCatalog(String inId, String inVal){ //カタログ対応　共通部は取得で使わない  160419 mit
        String outVal=inVal;
        if(inId.equals("productname")){
            outVal = convProdNameValue(inVal);
        }else if(inId.equals("output")){
            outVal = "";
        }else if(inId.equals("color")){
            if(inVal.equals("Color")) {
                outVal = "Color";
            }else if(inVal.equals("BW")) {
                outVal = "BW";
            }
        }else if(inId.equals("output_size")){
            outVal = "";
        }else if(inId.equals("areacode")){
            outVal = "";
        }else if(inId.equals("condition")){
            outVal = "";
        }

        return outVal;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getText() {
        return (text == null) ? Utility.EMPTY_STRING : text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(audioData_id);
        dest.writeString(formid);
        dest.writeString(value);
        dest.writeString(text);
        dest.writeString(mimeType);
    }

    public String toString() {
        return String.format("AUDIO FORM DATA[%d;%s;%s]", audioData_id, formid, value);
    }

    //同一速度のプロダクト名をコンバート(カタログ検索用) 160420 mit
    private String convProdNameValue(String pvalue){
        switch(pvalue){
            case "APDC4C2270":
            case "APDC4C2275":
                return "APDC4C2270";
            case "APDC4C3370":
            case "APDC4C3375":
                return "APDC4C3370";
            case "APDC4C4470":
            case "APDC4C4475":
                return "APDC4C4470";
            case "APDC4C5570":
            case "APDC4C5575":
                return "APDC4C5570";
            case "APDC5C2275":
            case "APDC5C2276":
                return "APDC5C2275";
            case "APDC5C3375":
            case "APDC5C3376":
                return "APDC5C3375";
            case "APDC5C4475":
            case "APDC5C4476":
                return "APDC5C4475";
            case "APDC5C6675":
            case "APDC5C6676":
            case "APDC5C7775":
            case "APDC5C7776":
                return "APDC5C6675";
            default:
                return pvalue;
        }

    }

    @Override
    public boolean equals(Object o) {
       if (this == o) {
           return true;
       } else if (o == null) {
           return false;
       } else if (o instanceof AudioFormData) {
           AudioFormData that = ((AudioFormData)o);
           if (that.getFormid().equals(this.formid) &&
                   that.getValue().equals(this.value)) {
               return true;
           } else {
               return false;
           }
       } else {
           return false;
       }
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
