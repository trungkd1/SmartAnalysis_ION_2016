package jp.co.fujixerox.sa.ion.utils;

import android.net.Uri;

import java.util.Arrays;
import java.util.List;

import jp.co.fujixerox.sa.ion.db.AudioFormData;
import jp.co.fujixerox.sa.ion.db.ICloudParams;

/**
 * Created by fxstdpc-admin on 2016/02/12.
 */
public class URL {
    private static final String SCHEME = "https";
    private static final String AUTHORITY = "fxionmirror.appspot.com";
    private static final String PATH = "api";

    public static String getTemplateUrl() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(AUTHORITY)
                .path(PATH)
                .appendPath(API.GET_TEMPLATE);
        return builder.build().toString();
    }

    public static String geAccountIdUrl(String authtoken) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(AUTHORITY)
                .path(PATH)
                .appendPath(API.GET_ACCOUNTID)
                .appendQueryParameter(PARAM.authtoken, authtoken);
        return builder.build().toString();
    }

    public static String getCatalogListUrl(List<AudioFormData>listAudioFormData) {
        List<String> catalogParams = Arrays.asList(ICloudParams.catalogParams);
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(AUTHORITY)
                .path(PATH)
                .appendPath(API.GET_CATALOG_LIST);
        for (AudioFormData data : listAudioFormData) {
            if (!catalogParams.contains(data.getFormid())) {
                continue;
            }
            String value_catalog =  data.getValueForCatalog(data.getFormid(),data.getValue());
            if (value_catalog != null) {
                builder.appendQueryParameter(data.getFormid(), value_catalog); //data.getValue ⇒ data.getValueForCatalog 160419 mit
            }
        }
       return builder.build().toString();
    }

    public static String getOldReportsUrl(String accountid, String productname, String serialId, int count, boolean paging) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(AUTHORITY)
                .path(PATH)
                .appendPath(API.GET_OLDREPORT)
                .appendQueryParameter(PARAM.count, String.valueOf(count))
                .appendQueryParameter(PARAM.paging, (paging ? "1" : "0"));
        if (accountid != null) {
            builder.appendQueryParameter(PARAM.accountid, accountid);
        }
        if (productname != null) {
            builder.appendQueryParameter(PARAM.productname, productname);
        }
        if (serialId != null) {
            builder.appendQueryParameter(PARAM.serialid, serialId);
        }
        return builder.build().toString();
    }

    public static String getUploadReportUrl() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(AUTHORITY)
                .path(PATH)
                .appendPath(API.GET_REPORT_URL);
        return builder.build().toString();
    }
    private interface API {
        String GET_CATALOG_LIST = "getcataloglist";
        String GET_REPORT_URL = "geturl";
        //TODO will set later
        String GET_TEMPLATE = "";
        String GET_ACCOUNTID = "getaccountid";
        String GET_OLDREPORT = "getoldreport";
    }

    private interface PARAM {
        String accountid = "accountid";
        String productname = "productname";
        String count = "count";
        String paging = "paging";
        String authtoken  = "authtoken";
        String serialid  = "serialid";
    }
}
