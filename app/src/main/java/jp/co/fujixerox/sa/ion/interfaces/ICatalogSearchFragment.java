package jp.co.fujixerox.sa.ion.interfaces;

import java.util.List;

import jp.co.fujixerox.sa.ion.db.AudioFormData;

/**
 * Created by fxstdpc-admin on 2016/03/07.
 */
public interface ICatalogSearchFragment {
    void addAudioFormData(AudioFormData audioFormData);
    List<AudioFormData> getAudioFormDataList();
    void searchCatalogs();
}
