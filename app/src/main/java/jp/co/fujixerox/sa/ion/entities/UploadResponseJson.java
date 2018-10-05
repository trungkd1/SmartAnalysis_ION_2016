package jp.co.fujixerox.sa.ion.entities;

/**
 * Json response object from cloud when report is uploaded
 */
public class UploadResponseJson {
    private String reportid;

    public String getReportid() {
        return reportid;
    }

    public void setReportid(String reportid) {
        this.reportid = reportid;
    }

    @Override
    public String toString() {
        return "UploadResponseJson [reportid=" + reportid + "]";
    }
}
