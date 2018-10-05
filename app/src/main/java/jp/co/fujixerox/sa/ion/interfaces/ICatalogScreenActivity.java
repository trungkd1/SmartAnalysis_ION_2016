package jp.co.fujixerox.sa.ion.interfaces;

import java.util.List;

import jp.co.fujixerox.sa.ion.entities.Catalog;

/**
 * Created by fxstdpc-admin on 2016/03/07.
 */
public interface ICatalogScreenActivity {
    void showCatalogView(List<Catalog> catalogList);
}
