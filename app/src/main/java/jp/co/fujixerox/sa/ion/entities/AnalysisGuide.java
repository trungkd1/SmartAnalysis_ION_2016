package jp.co.fujixerox.sa.ion.entities;

import java.util.List;

/**
 * Created by TrungKD
 */
public class AnalysisGuide {
    private List<AnalysisHintItem> top;
    private List<AnalysisHintItem> bottom;

    public List<AnalysisHintItem> getTop() {
        return top;
    }

    public void setTop(List<AnalysisHintItem> top) {
        this.top = top;
    }

    public List<AnalysisHintItem> getBottom() {
        return bottom;
    }

    public void setBottom(List<AnalysisHintItem> bottom) {
        this.bottom = bottom;
    }
}
