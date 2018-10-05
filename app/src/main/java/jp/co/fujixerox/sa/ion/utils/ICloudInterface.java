package jp.co.fujixerox.sa.ion.utils;

import android.net.Uri;

/**
 * Class define constant of Cloud Interface.
 * Created by fxstdpc-admin on 2016/07/14.
 */
public class ICloudInterface {
    private static final String SCHEME = "https";
    private static final String DOMAIN = "fxionmirror.appspot.com";
    private static final String PATH = "api";

    /**
     * Create request url to cloud.
     */
    public static String createRequestUrl(String cloudApi) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(DOMAIN)
                .path(PATH)
                .appendPath(cloudApi);
        return builder.build().toString();
    }
    /**
     * http header parameters
     */
    public interface HTTP_HEADER {
        String AUTH = "auth";
        String USER_AGENT = "user-agent";
    }

    public interface RESULT_KEY {
        String OK = "OK";
        String NG = "NG";
        String UNKNOWN = "UNKNOWN";
        String NON_RECURRING = "NON-RECURRING";
    }

    public interface API {
        String GET_CATALOG_LIST = "getcataloglist";
        String GET_REPORT_URL = "geturl";
        //TODO will set later
        String GET_TEMPLATE = "";
        String GET_ACCOUNTID = "getaccountid";
        String GET_OLDREPORT = "getoldreport";
    }

    public interface PARAM {
        String accountid = "accountid";
        String productname = "productname";
        String count = "count";
        String paging = "paging";
        String authtoken  = "authtoken";
        String serialid  = "serialid";
    }
}
