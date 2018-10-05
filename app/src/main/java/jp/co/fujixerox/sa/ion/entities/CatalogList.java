package jp.co.fujixerox.sa.ion.entities;

import java.util.List;

/**
 * Json CatalogList object
 * @see Catalog
 */
public class CatalogList {
    private int count;
    private List<Catalog> catalogs;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Catalog> getCatalogs() {
        return catalogs;
    }

    public void setCatalogs(List<Catalog> catalogs) {
        this.catalogs = catalogs;
    }

    public String toString() {
        return String.format("CatalogList[count: %d]", count);
    }

}
