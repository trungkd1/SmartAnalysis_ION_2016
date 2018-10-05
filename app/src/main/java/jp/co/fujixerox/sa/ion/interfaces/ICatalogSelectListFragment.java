package jp.co.fujixerox.sa.ion.interfaces;

import java.util.List;

import jp.co.fujixerox.sa.ion.db.AudioFormData;
import jp.co.fujixerox.sa.ion.entities.Catalog;

/**
 * This interface must be implemented by activities that contain this
 * fragment to allow an interaction in this fragment to be communicated
 * to the activity and potentially other fragments contained in that
 * activity.
 * <p/>
 * See the Android Training lesson <a href=
 * "http://developer.android.com/training/basics/fragments/communicating.html"
 * >Communicating with Other Fragments</a> for more information.
 */
public interface ICatalogSelectListFragment {
    void addSelectedCatalog(Catalog catalog);
    void removeSelectedCatalog(Catalog catalog);
    void removeAllSelectedCatalog();
    void addAudioFormData(AudioFormData audioFormData);
    List<AudioFormData> getAudioFormDataList();
    void searchCatalogs();
}
