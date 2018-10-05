package jp.co.fujixerox.sa.ion.utils;

public class SettingParams {
    private int fft; //fft size
    private int timeAnalysis; //time for analysis audio
    private int stepWidth; //step width 16/32/64

    public SettingParams(int fft, int timeAnalysis, int stepWidth) {
        this.fft = fft;
        this.timeAnalysis = timeAnalysis;
        this.stepWidth = stepWidth;
    }

    public int getFft() {
        return fft;
    }

    public void setFft(int fft) {
        this.fft = fft;
    }

    public int getTimeAnalysis() {
        return timeAnalysis;
    }

    public void setTimeAnalysis(int timeAnalysis) {
        this.timeAnalysis = timeAnalysis;
    }

    public int getStepWidth() {
        return stepWidth;
    }

    public void setStepWidth(int stepWidth) {
        this.stepWidth = stepWidth;
    }

    public String toString() {
        return String.format("SETTING_SCREEN API[%d; %d; %d]", fft, timeAnalysis, stepWidth);
    }

}
