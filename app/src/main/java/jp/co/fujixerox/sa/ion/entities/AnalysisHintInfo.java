package jp.co.fujixerox.sa.ion.entities;

import java.util.List;

/**
 * Analysis guide data
 */
public class AnalysisHintInfo {
    /**
     * list of data
     */
    private List<AnalysisHintItem> mDataListTop;

    private List<AnalysisHintItem> mDataListBottom;

    /**
     * Contructor
     *
     * @param dataListTop
     * @param dataListBottom
     */
    public AnalysisHintInfo(List<AnalysisHintItem> dataListTop, List<AnalysisHintItem> dataListBottom) {
        mDataListTop = dataListTop;
        mDataListBottom = dataListBottom;
    }

    /**
     * get data list top
     *
     * @return list data
     */
    public List<AnalysisHintItem> getDataListTop() {
        return mDataListTop;
    }

    /**
     * get data list bottom
     *
     * @return
     */
    public List<AnalysisHintItem> getDataListBottom() {
        return mDataListBottom;
    }
}
