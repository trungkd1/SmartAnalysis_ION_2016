package jp.co.fujixerox.sa.ion.interfaces;

import java.util.ArrayList;
import java.util.List;

import jp.co.fujixerox.sa.ion.entities.Catalog;

/**
 * Created by fxstdpc-admin on 2016/03/04.
 */
public interface ICatalogViewFragment {
    void setCurrentCatalogPosition(int position);

    List<Catalog> getCatalogList();

    ArrayList<String> getAllCatalogCauseParts();

    ArrayList<String> getAllCatalogMethods();

    List<String> getAllCatalogMethodImageUrls();

    Catalog getCurrentCatalog();

    void enableButtonPlayCatalog(boolean enable);

}
