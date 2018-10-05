package jp.co.fujixerox.sa.ion.entities;

/**
 * Created by TrungKD
 */
public class AnalysisHintItem {
    private String knowhow;
    private String referenceImage;
    private String referenceImageFilePath;

    public AnalysisHintItem(String _knowhow, String _referenceImage) {
        this.knowhow = _knowhow;
        this.referenceImage = _referenceImage;
    }

    public String getKnowhow() {
        return knowhow;
    }

    public void setKnowhow(String knowhow) {
        this.knowhow = knowhow;
    }

    public String getReferenceImage() {
        return referenceImage;
    }

    public void setReferenceImage(String referenceImage) {
        this.referenceImage = referenceImage;
    }

    public String getReferenceImageFilePath() {
        return referenceImageFilePath;
    }

    public void setReferenceImageFilePath(String referenceImageFilePath) {
        this.referenceImageFilePath = referenceImageFilePath;
    }

    public String toString() {
        return new StringBuilder().append("AnalysisHintItem[Knowhow: ").append(knowhow)
                .append(";ReferenceImageFilePath: ").append(referenceImageFilePath)
                .append(";ReferenceImage:").append(referenceImage).append("]").toString();
    }
}
