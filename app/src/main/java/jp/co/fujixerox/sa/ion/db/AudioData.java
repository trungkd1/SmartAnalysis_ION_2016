package jp.co.fujixerox.sa.ion.db;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import jp.co.fujixerox.sa.ion.utils.FileUtils;
import jp.co.fujixerox.sa.ion.utils.Utility;

/**
 * store information about audio
 */
public class AudioData implements IAudioData, Parcelable {
    private static final String TAG = "AudioData";
    private long id = 0;
    private long recordDate;
//    private String audioFileName;
//    private String analysisImage;
    private String selectPoints;
    private float averagePeriod = 0f;
    private float averageFrequency = 0f;
    private List<AudioFormData> listAudioFormData = new ArrayList<>();
    private String latitude;
    private String longitude;
    private String dummy_sound;
    private String comment;
    private String reportid;
    private long catalogId = 0;
    private String cause = null;
    private String method = null;
    private String methodDetail = null;
    private String result = null;
    private String casuseJsonForm;
    private String methodJsonForm;
    private String picture; //image url from cloud.

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        addOrUpdateAudioFromData(ICloudParams.picture, picture, null, FileUtils.getMimeType(picture));
        this.picture = picture;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        addOrUpdateAudioFromData(ICloudParams.sound, sound, null, FileUtils.getMimeType(sound));
        this.sound = sound;
    }

    private String sound; // sound url from cloud.

    public AudioData() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

//    public String getAudioFileName() {
//        return audioFileName;
//    }
//
//    public void setAudioFileName(String audioFileName) {
//        addOrUpdateAudioFromData(ICloudParams.sound, audioFileName, FileUtils.getMimeType(audioFileName));
//        this.audioFileName = audioFileName;
//    }

    public String getSelectPoints() {
        return selectPoints;
    }

    public void setSelectPoints(String selectPoints) {
        addOrUpdateAudioFromData(ICloudParams.select_point, selectPoints, null, null);
        this.selectPoints = selectPoints;
    }

    public Float getAveragePeriod() {
        return averagePeriod;
    }

    public void setAveragePeriod(Float averagePeriod) {
        addOrUpdateAudioFromData(ICloudParams.period, String.valueOf(averagePeriod), null, null);
        this.averagePeriod = averagePeriod;
    }

    public Float getAverageFrequency() {
        return averageFrequency;
    }

    public void setAverageFrequency(Float averageFrequency) {
        addOrUpdateAudioFromData(ICloudParams.frequency, String.valueOf(averageFrequency), null, null);
        this.averageFrequency = averageFrequency;
    }

    public long getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(long recordDate) {
        addOrUpdateAudioFromData(ICloudParams.record_date, Long.toString(recordDate), null, null);
        this.recordDate = recordDate;
    }

    public List<AudioFormData> getListAudioFormData() {
        return listAudioFormData;
    }
    public List<AudioFormData> getListAudioFormDataCatalog(){
        List<AudioFormData> bufdata = new ArrayList<>(listAudioFormData);
        return convGetCatalogForTrial(bufdata);
    }

    public void setListAudioFormData(List<AudioFormData> listAudioFormData) {
        if (hasConditoinItem(listAudioFormData)) {
            removeAllConditionItems();
        }
        for (AudioFormData newdata:
             listAudioFormData) {
            if(newdata.getFormid().equals(ICloudParams.result)){
                result= newdata.getValue();
            }
            addOrUpdateAudioFromData(newdata.getFormid(), newdata.getValue(), newdata.getText(), newdata.getMimeType());
        }
    }

    /**
     * Check condition item exist in AudioFormDataList
     * @param audioFormDataList
     * @return
     */
    private boolean hasConditoinItem(List<AudioFormData> audioFormDataList) {
        for (AudioFormData audioFormData :
                audioFormDataList) {
            if (ICloudParams.condition.equals(audioFormData.getFormid())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove all child items of condition
     */
    private void removeAllConditionItems() {
        List<String> listOfConditionFormId = Arrays.asList(ICloudParams.conditionInputParams);
        Iterator<AudioFormData> iterator = listAudioFormData.iterator();
        while (iterator.hasNext()) {
            AudioFormData formData = iterator.next();
            if (listOfConditionFormId.contains(formData.getFormid())) {
                iterator.remove();
            }
        }
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        addOrUpdateAudioFromData(ICloudParams.latitude, latitude, null, null);
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        addOrUpdateAudioFromData(ICloudParams.longitude, longitude, null, null);
        this.longitude = longitude;
    }

    public String getDummy_sound() {
        return dummy_sound;
    }

    public void setDummy_sound(String dummy_sound) {
        addOrUpdateAudioFromData(ICloudParams.dummy_sound, dummy_sound, null, null);
        this.dummy_sound = dummy_sound;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        addOrUpdateAudioFromData(ICloudParams.comment, comment, null, null);
        this.comment = comment;
    }

    public String getReportid() {
        return reportid;
    }

    public void setReportid(String reportid) {
        addOrUpdateAudioFromData(ICloudParams.reportid, reportid, null, null);
        this.reportid = reportid;
    }

    public long getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(long catalogId) {
        addOrUpdateAudioFromData(ICloudParams.catalogid, String.valueOf(catalogId), null, null);
        this.catalogId = catalogId;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        addOrUpdateAudioFromData(ICloudParams.cause, cause, null, null);
        this.cause = cause;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        addOrUpdateAudioFromData(ICloudParams.method, method, null, null);
        this.method = method;
    }

    public String getMethodDetail() {
        return methodDetail;
    }

    public void setMethodDetail(String methodDetail) {
        addOrUpdateAudioFromData(ICloudParams.method_detail, methodDetail, null, null);
        this.methodDetail = methodDetail;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        addOrUpdateAudioFromData(ICloudParams.result, result, null, null);
        this.result = result;
    }

    public String getMethodJsonForm() {
        return methodJsonForm;
    }

    public void setMethodJsonForm(String methodJsonForm) {
        this.methodJsonForm = methodJsonForm;
    }

    public String getCasuseJsonForm() {
        return casuseJsonForm;
    }

    public void setCasuseJsonForm(String casuseJsonForm) {
        this.casuseJsonForm = casuseJsonForm;
    }

    public String getTextByFormId(String formId) {
        String text = "";
        AudioFormData audioFormData = null;
        if (listAudioFormData == null) {
            return text;
        } else {
            for (AudioFormData formData : listAudioFormData) {
                if (TextUtils.equals(formData.getFormid(), formId)) {
                    audioFormData = formData;
                    break;
                }
            }
        }
        if (audioFormData != null) {
            if (TextUtils.isEmpty(audioFormData.getText())) {
                text = audioFormData.getValue();
            } else {
                text = audioFormData.getText();
            }
        }
        return text;
    }

    /**
     * Get value from form id
     * @param formId cloud form id
     * @See ICloudParams
     * @return
     */
    public String getValueByFormId(String formId) {
        if (ICloudParams.picture.equals(formId)) {
            if(picture != null) {
                return picture.substring(picture.lastIndexOf(File.separator, picture.length()));
            }
        } else if (ICloudParams.sound.equals(formId)) {
            if(sound != null) {
                return sound.substring(sound.lastIndexOf(File.separator, sound.length()));
            }
        } else if (ICloudParams.record_date.equals(formId)) {
            if(recordDate != 0) {
                return String.valueOf(recordDate);
            }
        } else if (ICloudParams.catalogid.equals(formId)) {
            if(catalogId != 0) {
                return String.valueOf(catalogId);
            }
        } else if (ICloudParams.comment.equals(formId)) {
            if (comment != null) {
                return comment;
            }
        } else if (ICloudParams.dummy_sound.equals(formId)) {
            if(dummy_sound != null) {
                return dummy_sound;
            }
        } else if (ICloudParams.reportid.equals(formId)) {
            if(reportid != null) {
                return reportid;
            }
        } else if (ICloudParams.result.equals(formId)) {
            if(result != null) {
                return result;
            }
        } else if (listAudioFormData != null) {
            for (AudioFormData formData : listAudioFormData) {
                if (TextUtils.equals(formData.getFormid(), formId)) {
                    return formData.getValue();
                }
            }
        } else if (ICloudParams.comment.equals(formId)) {
            for (AudioFormData formData : listAudioFormData) {
                if (TextUtils.equals(formData.getFormid(), formId)) {
                    return formData.getValue();
                }
            }
        }
        return Utility.EMPTY_STRING;
    }

    /**
     * Get audiodata by formId
     * @param formId
     * @return null if not found
     */
    public AudioFormData getAudioFormDataByFormId(String formId) {
        for (AudioFormData formData : listAudioFormData) {
            if (TextUtils.equals(formData.getFormid(), formId)) {
                return formData;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "AudioData [id=" + id
                + ", recordDate=" + recordDate + ", audioFileName="
                + sound
                + ", selectPoints=" + selectPoints + ", averagePeriod=" + averagePeriod
                + ", averageFrequency=" + averageFrequency + ", listAudioFormData=" + listAudioFormData
                + ", latitude=" + latitude + ", longitude=" + longitude
                + ", analysisImage=" + picture
                + "reportid=" + reportid
                + ", getId()=" + getId()
                + ", getAudioFileName()="
                + getSound()
                + ", getSelectPoints()=" + getSelectPoints() + ", getAveragePeriod()="
                + getAveragePeriod() + ", getAverageFrequency()="
                + getAverageFrequency() + ", getRecordDate()="
                + getRecordDate()
                + ", getListAudioFormData()=" + getListAudioFormData()
                + ", getLatitude()="
                + getLatitude() + ", getLongitude()=" + getLongitude()
                + ", getAnalysisImage()="
                + getPicture() + ", getReportid()="
                + getReportid() + ", describeContents()="
                + describeContents() + ", getClass()=" + getClass()
                + ", hashCode()=" + hashCode() + ", toString()="
                + super.toString() + "]\n";
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.recordDate);
        dest.writeString(this.selectPoints);
        dest.writeFloat(this.averagePeriod);
        dest.writeFloat(this.averageFrequency);
        dest.writeTypedList(listAudioFormData);
        dest.writeString(this.latitude);
        dest.writeString(this.longitude);
        dest.writeString(this.dummy_sound);
        dest.writeString(this.comment);
        dest.writeString(this.reportid);
        dest.writeLong(this.catalogId);
        dest.writeString(this.cause);
        dest.writeString(this.method);
        dest.writeString(this.methodDetail);
        dest.writeString(this.result);
        dest.writeString(this.casuseJsonForm);
        dest.writeString(this.methodJsonForm);
        dest.writeString(this.picture);
        dest.writeString(this.sound);
    }

    private AudioData(Parcel in) {
        this.id = in.readLong();
        this.recordDate = in.readLong();
        this.selectPoints = in.readString();
        this.averagePeriod = in.readFloat();
        this.averageFrequency = in.readFloat();
        in.readTypedList(listAudioFormData, AudioFormData.CREATOR);
        this.latitude = in.readString();
        this.longitude = in.readString();
        this.dummy_sound = in.readString();
        this.comment = in.readString();
        this.reportid = in.readString();
        this.catalogId = in.readLong();
        this.cause = in.readString();
        this.method = in.readString();
        this.methodDetail = in.readString();
        this.result = in.readString();
        this.casuseJsonForm = in.readString();
        this.methodJsonForm = in.readString();
        this.picture = in.readString();
        this.sound = in.readString();
    }

    public static final Creator<AudioData> CREATOR = new Creator<AudioData>() {
        public AudioData createFromParcel(Parcel source) {
            return new AudioData(source);
        }

        public AudioData[] newArray(int size) {
            return new AudioData[size];
        }
    };

    //トライアル用カタログ取得 160419 mit
    public List<AudioFormData> convGetCatalogForTrial(List<AudioFormData> listAuidoCatalog){
        //機種名
        for(AudioFormData  listdata : listAuidoCatalog){
            if(listdata.getFormid().equals("productname")){
                listdata.setValue(convProdNameValue(listdata.getValue()));
                listdata.setText(convProdNameText(listdata.getText()));
            }else if(listdata.getFormid().equals("output")){
                listdata.setValue("");
                listdata.setText("");
            }else if(listdata.getFormid().equals("color")){
                if(listdata.getValue().equals("Color")) {
                    listdata.setValue("Color");
                    listdata.setText("4C");
                }else{
                    listdata.setValue("BW");
                    listdata.setText("BW");
                }
            }else if(listdata.getFormid().equals("output_size")){
                listdata.setValue("");
                listdata.setText("");
            }else if(listdata.getFormid().equals("areacode")){
                listdata.setValue("");
                listdata.setText("");
            }else if(listdata.getFormid().equals("output")){
                listdata.setValue("");
                listdata.setText("");
            }else if(listdata.getFormid().equals("condition")){
                listdata.setValue("");
                listdata.setText("");
            }
        }

        return listAuidoCatalog;
    }

    private String convProdNameValue(String pvalue){
        switch(pvalue){
            case "APDC4C2270":
            case "APDC4C2275":
                return "APDC4C2270";
            case "APDC4C3370":
            case "APDC4C3375":
                return "APDC4C337";
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
                return "APDC5C6675";
            case "APDC5C7775":
            case "APDC5C7776":
                return "APDC5C7775";
            default:
                return pvalue;
        }

    }

    private String convProdNameText(String ptext){
        return ptext;
    }

    /**
     * Get difference between two list of AudioFormData
     * @param audioFormDataList
     * @return
     */
    public Collection<AudioFormData> getDifference(List<AudioFormData> audioFormDataList) {
        Collection<AudioFormData> different = new HashSet<>();
        for (AudioFormData audioFormData1 :
                    audioFormDataList) {
                if (this.listAudioFormData.contains(audioFormData1)) {
                    Log.v(TAG, "same");
                } else if (!TextUtils.isEmpty(audioFormData1.getValue())){
                    different.add(audioFormData1);
                    Log.v(TAG, "not same");
                } else {
                    //do nothing because data is empty or null
                }
        }
        return different;
    }

    /**
     * Add or Update a AudioFormData to list
     * @param formId
     * @param formValue
     * @param mimeType
     * @return true is update, false is add
     */
    public boolean addOrUpdateAudioFromData(String formId, String formValue, String formText, String mimeType) {
        boolean searchResult = false;
        for (AudioFormData data:
             listAudioFormData) {
            if (data.getFormid().equals(formId)) {
                data.setValue(formValue);
                data.setText(formText);
                data.setMimeType(mimeType);
                searchResult = true;
                break;
            }
        }
        if (!searchResult) {
            listAudioFormData.add(new AudioFormData(formId, formValue, formText, mimeType));
        }
        return searchResult;
    }

}
