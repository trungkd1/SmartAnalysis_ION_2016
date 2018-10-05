package jp.co.fujixerox.sa.ion.entities;

import android.graphics.PointF;

/**
 * 自分が送信した過去レポート
 */
public class Report {
    String reportid;
    String productgroup;
    String productname;
    String serialid;
    long record_date;
    String type; //異音の種類
    String cause;
    String method;
    String method_detail;
    String picture;
    String sound;
    String catalogid;
    String result;
    String areacode;
    float frequency;
    float period;
//    String dummy_sound;
    String category; // 異音表現カテゴリ
    String condition;
    String color;
    String output_type ;
    String output_size ;
    String original_type;
    String original_size;
    String output;
    String latitude;
    String longitude;
    String comment;
    long report_date;
    long updated;
    String start; //起動の時
    PointF[] select_point;

    public String getReportid() {
        return reportid;
    }

    public void setReportid(String reportid) {
        this.reportid = reportid;
    }

    public String getProductgroup() {
        return productgroup;
    }

    public void setProductgroup(String productgroup) {
        this.productgroup = productgroup;
    }

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public String getSerialid() {
        return serialid;
    }

    public void setSerialid(String serialid) {
        this.serialid = serialid;
    }

    public long getRecord_date() {
        return record_date;
    }

    public void setRecord_date(long record_date) {
        this.record_date = record_date;
    }

    public String getType() {
        return type;
    }

    public void setType(String tupe) {
        this.type = tupe;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public void setMethod(String method) {
        this.method = method;
    }
    public String getMethod(){ return method;}

    public String getMethod_detail() {
        return method_detail;
    }

    public void setMethod_detail(String method_detail) {
        this.method_detail = method_detail;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getCatalogid() {
        return catalogid;
    }

    public void setCatalogid(String catalogid) {
        this.catalogid = catalogid;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getAreacode() {
        return areacode;
    }

    public void setAreacode(String areacode) {
        this.areacode = areacode;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public float getPeriod() {
        return period;
    }

    public void setPeriod(float period) {
        this.period = period;
    }

//    public String getDummy_sound() {
//        return dummy_sound;
//    }
//
//    public void setDummy_sound(String dummy_sound) {
//        this.dummy_sound = dummy_sound;
//    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getOutput_type() {
        return output_type;
    }

    public void setOutput_type(String output_type) {
        this.output_type = output_type;
    }

    public String getOutput_size() {
        return output_size;
    }

    public void setOutput_size(String output_size) {
        this.output_size = output_size;
    }

    public String getOriginal_type() {
        return original_type;
    }

    public void setOriginal_type(String original_type) {
        this.original_type = original_type;
    }

    public String getOriginal_size() {
        return original_size;
    }

    public void setOriginal_size(String original_size) {
        this.original_size = original_size;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getReport_date() {
        return report_date;
    }

    public void setReport_date(long report_date) {
        this.report_date = report_date;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public PointF[] getSelect_point() {
        return select_point;
    }

    public void setSelect_point(PointF[] select_point) {
        this.select_point = select_point;
    }
}
