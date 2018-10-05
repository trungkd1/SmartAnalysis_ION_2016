package jp.co.fujixerox.sa.ion.entities;

public class IFCatalogListQueries {
    private String productname;
    private String areacode;
    private Integer frequency;
    private Integer period;
    private String condition;
    private String color;
    private String outputType;
    private String outputSize;
    private String originalType;
    private String originalSize;
    private String output;

    public IFCatalogListQueries() {

    }

    public IFCatalogListQueries(String productname, String condition, String color, String outputType,
                                String outputSize) {
        this.productname = productname;
        this.condition = condition;
        this.color = color;
        this.outputType = outputType;
        this.outputSize = outputSize;
    }

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public String getAreacode() {
        return areacode;
    }

    public void setAreacode(String areacode) {
        this.areacode = areacode;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
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

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public String getOutputSize() {
        return outputSize;
    }

    public void setOutputSize(String outputSize) {
        this.outputSize = outputSize;
    }

    public String getOriginalType() {
        return originalType;
    }

    public void setOriginalType(String originalType) {
        this.originalType = originalType;
    }

    public String getOriginalSize() {
        return originalSize;
    }

    public void setOriginalSize(String originalSize) {
        this.originalSize = originalSize;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String toString() {
        return String.format("IFCatalogListQueries[%s; %s; %s; %s; %s; %s]", productname, condition, color, outputType,
                outputSize, areacode);
    }
}
