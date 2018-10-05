package jp.co.fujixerox.sa.ion.entities;

import java.util.List;

/**
 * 自分が送信した過去レポートリスト
 */
public class ReportData {
    private int count;
    private int paging;
    private List<Report> report;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPaging() {
        return paging;
    }

    public void setPaging(int paging) {
        this.paging = paging;
    }

    public List<Report> getReport() {
        return report;
    }

    public void setReport(List<Report> report) {
        this.report = report;
    }
}
