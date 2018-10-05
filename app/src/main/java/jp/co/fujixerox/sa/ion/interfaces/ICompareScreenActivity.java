package jp.co.fujixerox.sa.ion.interfaces;

/**
 * Created by fxstdpc-admin on 2016/07/06.
 * Interface call back from fragment to CompareScreenActivity
 */
public interface ICompareScreenActivity {
    void onRecordAudioPlaying();
    void onCatalogAudioPlaying();
    void onAudioPlayingFinish();
    boolean isRecordAudioPlaying();
}
