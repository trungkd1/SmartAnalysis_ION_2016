package jp.co.fujixerox.sa.ion.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.List;

import jp.co.fujixerox.sa.ion.db.AudioData;
import jp.co.fujixerox.sa.ion.db.AudioFormData;
import jp.co.fujixerox.sa.ion.db.ICloudParams;
import jp.co.fujixerox.sa.ion.entities.UploadResponseJson;

public class CloudConnector {
    private static final String TAG = CloudConnector.class.getSimpleName();


    /**
     * @param listAudioFormData: list audio_formdata
     * @return json input stream
     */
    public static InputStream getCatalogList(List<AudioFormData> listAudioFormData) {
        String url = URL.getCatalogListUrl(listAudioFormData);
        Log.v(TAG, "@@getCatalogList URL: " + url);

        InputStream inputStream = null;
        try {
            HttpGet httpGet = createHttpGet(url);
            // Execute HTTP Get Request
            HttpResponse response = executeRequest(httpGet);
            Log.v(TAG, "response for getCatalogList is null ="
                    + (response == null));
            if (response != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    inputStream = response.getEntity().getContent();
                }
                Log.v(TAG, "Status code getCatalogList:" + statusCode);
            }
        } catch (Exception ex) {
            Log.e(TAG, "getCatalogList error", ex);
        }

        return inputStream;
    }

    /**
     * download file template from cloud
     * @param url: url template
     * @return template stream
     */
    public static InputStream downloadFileTemplate(String url) {
        Log.v(TAG, "@@downloadImageStream: " + url);

        InputStream inputStream = null;
        try {
            HttpGet httpGet = createHttpGet(url);
            // Execute HTTP Get Request
            HttpResponse response = executeRequest(httpGet);
            Log.v(TAG, "response for get template is null ="
                    + (response == null));
            if (response != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    inputStream = response.getEntity().getContent();
                }
                Log.v(TAG, "Status code download template:" + statusCode);
            }
        } catch (Exception ex) {
            Log.e(TAG, "download template error", ex);
        }

        return inputStream;
    }

    public static HttpResponse executeRequest(HttpRequestBase requestBase) {
        HttpResponse response = null;
        HttpClient client = getNewHttpClient();
        try {
            response = client.execute(requestBase);
        } catch (IllegalStateException | IOException e) {
            Log.e(TAG, "@@@Error when execute request", e);
        }

        return response;
    }

    /**
     * Trusting all certificates using HttpClient over HTTPS
     *
     * @return
     */
    private static HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore
                    .getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 5000);
            HttpConnectionParams.setSoTimeout(params, 10000);
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory
                    .getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(
                    params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    public static HttpGet createHttpGet(String url) {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(Utility.HTTP_HEADER.USER_AGENT,
                CommonUtils.generateUserAgent());

        return httpGet;
    }

    private static HttpPost createHttpPost(String url) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader(Utility.HTTP_HEADER.USER_AGENT,
                CommonUtils.generateUserAgent());
        return httpPost;
    }

    /**
     * Checking for all possible internet providers
     */
    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (NetworkInfo anInfo : info)
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }

    /**
     * Get Upload report URL
     * @return
     */
    public static String getUploadReportUrl() {
        String result = null;
        String requestUrl = URL.getUploadReportUrl();
        Log.i(TAG, "@@url: " + requestUrl);
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = createHttpGet(requestUrl);

        try {
            HttpResponse response = httpclient.execute(httpget);
            Log.v(TAG, "response is null =" + (response == null));
            if (response != null) {
                Log.v(TAG, "response is NOT null ");
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    Log.v(TAG, "statusCode is SC_OK");
                    result = EntityUtils.toString(response.getEntity());
                    if (result == null || result.length() <= 0) {
                        Log.e(TAG, "result url is null or empty");
                    }
                }
                Log.v(TAG, "Status code:" + statusCode);
            }

        } catch (ClientProtocolException e) {
            Log.e(TAG, "Get URL error", e);
        } catch (IOException e) {
            Log.e(TAG, "Get URL error", e);
        }
        return result;
    }

    /**
     * Upload report to cloud
     *
     * @param uploadUrl
     * @param audioData
     * @param progressListener
     * @return
     */
    public static String uploadReportData(String uploadUrl, AudioData audioData,
                                   MultiPartEntity.ProgressListener progressListener,String hash_select) {

        if (audioData == null) {
            Log.e(TAG, "uploadReportData audio data is null");
            return null;
        }
        String result = null;
        if (uploadUrl == null) {
            Log.e(TAG, "can't upload by upload url is null");
            return Utility.EMPTY_STRING;
        }

        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = createHttpPost(uploadUrl);
        try {
            MultiPartEntity multipartEntity = new MultiPartEntity(
                    progressListener);

            if (!TextUtils.isEmpty(audioData.getReportid())) {
                addMultipartEntity(multipartEntity, ICloudParams.reportid, audioData.getReportid());
                Log.i(TAG,
                        "1.audioData.getReportid()"
                                + audioData.getReportid());
            } else {
                Log.e(TAG, "1.audioData.getReportid() IS NULL");
            }

            if (audioData.getRecordDate() != 0) {
                Log.i(TAG,
                        "2.audioData.getRecordDate()"
                                + audioData.getRecordDate());
                multipartEntity.addPart(ICloudParams.record_date,
                        new StringBody(
                                Long.toString(audioData.getRecordDate()),
                                Charset.forName("UTF-8")));
            } else {
                Log.e(TAG, "2.audioData.getRecordDate() IS NULL");
            }

            if (!TextUtils.isEmpty(audioData.getCause())) {
                multipartEntity.addPart(ICloudParams.cause, new StringBody(
                        audioData.getCause(), Charset.forName("UTF-8")));
                Log.i(TAG, "3.audioReport.getCause()" + audioData.getCause());
            } else {
                Log.e(TAG, "3.audioData.getCause() IS NULL");
            }

            if (!TextUtils.isEmpty(audioData.getMethod())) {
                multipartEntity.addPart(ICloudParams.method, new StringBody(
                        audioData.getMethod(), Charset.forName("UTF-8")));
                Log.i(TAG, "4.audioReport.getMethod()" + audioData.getMethod());
            } else {
                Log.e(TAG, "4.audioData.getMethod() IS NULL");
            }

            if (!TextUtils.isEmpty(audioData.getPicture())) {
                File imageFile = new File(audioData.getPicture());
                if (imageFile.exists()) {
                    multipartEntity.addPart(
                            ICloudParams.picture,
                            new FileBody(imageFile,
                                    FileUtils.getMimeType(audioData
                                            .getPicture())));
                    Log.i(TAG,
                            "5.audioData.getAnalysisImage()"
                                    + audioData.getPicture());
                } else {
                    Log.e(TAG, "5.audioData.getAnalysisImage() is not exist");
                }
            } else {
                Log.e(TAG,
                        "5.audioData.getAnalysisImage() IS NULL");
            }

            if (!TextUtils.isEmpty(audioData.getSound())) {
                File audioFile = new File(audioData.getSound());
                if (audioFile.exists()) {
                    multipartEntity.addPart(ICloudParams.sound, new FileBody(
                            audioFile,
                            FileUtils.getMimeType(audioData
                                    .getSound())));
                    Log.i(TAG,
                            "6.audioData.getAudioFileName()"
                                    + audioData.getSound());
                } else {
                    Log.e(TAG, "6.audioData.getAudioFileName() is not exist");
                }
            } else {
                Log.e(TAG, "6.audioData.getAudioFileName() IS NULL");
            }

            if (audioData.getCatalogId() != 0) {
                multipartEntity.addPart(
                        ICloudParams.catalogid,
                        new StringBody(Long.toString(audioData
                                .getCatalogId()), Charset.forName("UTF-8")));
                Log.i(TAG,
                        "7.audioReport.getCatalogId()"
                                + audioData.getCatalogId());
            } else {
                Log.e(TAG, "7.audioData.getCatalogId() IS NULL");
            }

            if (!TextUtils.isEmpty(audioData.getResult())) {
                multipartEntity.addPart(ICloudParams.result, new StringBody(
                        audioData.getResult(), Charset.forName("UTF-8")));
                Log.i(TAG, "8.audioReport.getResult()" + audioData.getResult());
            } else {
                multipartEntity.addPart(ICloudParams.result, new StringBody(
                        Utility.RESULT_KEY.NG, Charset.forName("UTF-8")));
                Log.i(TAG, "8.audioReport.getResult() NG");
            }

            if (audioData.getAverageFrequency() != null
                    && audioData.getAverageFrequency() != 0) {
                multipartEntity.addPart(ICloudParams.frequency, new StringBody(
                        Float.toString(audioData.getAverageFrequency()),
                        Charset.forName("UTF-8")));
                Log.i(TAG,
                        "10.audioData.getAverageFrequency()"
                                + audioData.getAverageFrequency());
            } else {
                Log.e(TAG, "10.audioData.getAverageFrequency() IS NULL");
            }

            if (audioData.getAveragePeriod() != null
                    && audioData.getAveragePeriod() != 0) {
                multipartEntity
                        .addPart(
                                ICloudParams.period,
                                new StringBody(Float.toString(audioData
                                        .getAveragePeriod()), Charset
                                        .forName("UTF-8")));
                Log.i(TAG,
                        "11.audioData.getAveragePeriod()"
                                + audioData.getAveragePeriod());
            } else {
                Log.e(TAG, "11.audioData.getAveragePeriod() IS NULL");
            }
            if (!TextUtils.isEmpty(audioData.getSelectPoints())) {
                multipartEntity
                        .addPart(
                                ICloudParams.select_point,
                                new StringBody(audioData.getSelectPoints(), Charset
                                        .forName("UTF-8")));
                Log.i(TAG,
                        "11.audioData.getSelectPoints()"
                                + audioData.getSelectPoints());
            } else {
                Log.e(TAG, "11.audioData.getSelectPoints() IS NULL");
            }
//            if (!TextUtils.isEmpty(audioData.getDummy_sound())) {
//                multipartEntity.addPart(
//                        ICloudParams.dummy_sound,
//                        new StringBody(audioData.getDummy_sound(), Charset
//                                .forName("UTF-8")));
//                Log.i(TAG,
//                        "12.audioData.getDummy_sound()"
//                                + audioData.getDummy_sound());
//            } else {
//                Log.e(TAG, "12.audioData.getDummy_sound() IS NULL");
//            }

            if (!TextUtils.isEmpty(audioData.getLatitude())) {
                multipartEntity.addPart(ICloudParams.latitude, new StringBody(
                        audioData.getLatitude(), Charset.forName("UTF-8")));
                Log.i(TAG,
                        "13.audioData.getLatitude()" + audioData.getLatitude());
            } else {
                Log.e(TAG, "13.audioData.getLatitude() IS NULL");
            }
            if (!TextUtils.isEmpty(audioData.getLongitude())) {
                multipartEntity.addPart(ICloudParams.longitude, new StringBody(
                        audioData.getLongitude(), Charset.forName("UTF-8")));
                Log.i(TAG,
                        "14.audioData.getLongitude()"
                                + audioData.getLongitude());
            } else {
                Log.e(TAG, "14.audioData.getLongitude() IS NULL");
            }
            //add accountid 160406 mit
            // Only send param accountId when reportCloudId is not exist
            if (audioData.getReportid() == null && !TextUtils.isEmpty(hash_select)){
                multipartEntity.addPart(ICloudParams.accountid, new StringBody(
                        hash_select,Charset.forName("UTF-8")));
                Log.i(TAG,
                        "15.hash_select:" + hash_select);
            }else{
                Log.e(TAG,"15.hash_select IS NULL");
            }

            List<AudioFormData>  audioFormDataList = audioData.getListAudioFormData();
            for (AudioFormData formData:audioFormDataList) {
                addMultipartEntity(multipartEntity, formData.getFormid(), formData.getValue());
            }

            long totalSize = multipartEntity.getContentLength();
            progressListener.setTotalSize(totalSize);
            httppost.setEntity(multipartEntity);

            HttpResponse response = httpclient.execute(httppost);
            Log.v(TAG, "response is null =" + (response == null));
            if (response != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    String responseJson = EntityUtils.toString(response
                            .getEntity());
                    Log.i(TAG, "responseJson: " + responseJson);
                    Gson gson = new Gson();
                    UploadResponseJson uploadResponse = gson.fromJson(
                            responseJson, UploadResponseJson.class);
                    if (uploadResponse != null) {
                        result = uploadResponse.getReportid();
                    }
                }
                Log.v(TAG, "Status code:" + statusCode);
            }
        } catch (ClientProtocolException e) {
            Log.e(TAG, "Upload ClientProtocolException", e);
        } catch (IOException e) {
            Log.e(TAG, "Upload IOException", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Upload IllegalArgumentException", e);
        }
        Log.v(TAG, "upload reportId=" + result);

        return result;
    }

    private static void addMultipartEntity(MultiPartEntity multipartEntity, String formId, String value) throws UnsupportedEncodingException {
        if (value == null) {
            value = Utility.EMPTY_STRING;
        }
        multipartEntity.addPart(formId, new StringBody(
                value, Charset.forName("UTF-8")));
        Log.i(TAG, String.format("audioFormData form id:%s; value:%s", formId, value));
    }

    /**
     * get past report from cloud
     * @param accountid : アカウントID
     * @param productname プロダクト名
     * @param count 取得レポートの最大件数
     * @param paging true:前回リクエストのpagingレスポンスが１かつ、次のページングデータを取得する場合<br>
     *                false:未指定、または上記以外：前回のリクエストのページングデータを利用しない
     * @return json stream
     */
    public static InputStream getOldReports(String accountid, String productname, String serialId, int count, boolean paging) {
        //TODO get past reports from cloud
        InputStream inputStream = null;
       String url = URL.getOldReportsUrl(accountid, productname, serialId, count, paging);
        Log.v(TAG, "@@getOldReports: " + url + "|" + accountid);
        try {
            HttpGet httpGet = createHttpGet(url);

            // Execute HTTP Get Request
            HttpResponse response = executeRequest(httpGet);
            Log.v(TAG, "response for get Past Reports is null ="
                    + (response == null));
            if (response != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    inputStream = response.getEntity().getContent();
                }
                Log.v(TAG, "Status code get Past Reports:" + statusCode);
            }
        } catch (Exception ex) {
            Log.e(TAG, "download getOldReports error", ex);
        }

        return inputStream;
    }

    /**
     * Connect to cloud and get Auth session in cookie
     * @param googleAccountToken
     * @return  Auth session
     */
    public static String getCookieAuthSession(String googleAccountToken) {
        String result = null;
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        try {
            String url = URL.geAccountIdUrl(googleAccountToken);
            HttpGet httpget = new HttpGet(url);
            Log.v(TAG, "@@requestUrl: " + url );
            //create http post
                response = httpclient.execute(httpget);
                Log.v(TAG, "response is null =" + (response == null));
                if (response != null) {
                    Log.v(TAG, "response is NOT null ");
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == HttpStatus.SC_OK) {
                        Log.v(TAG, "statusCode is SC_OK");
                        result = EntityUtils.toString(response.getEntity());
                        if (result == null || result.length() <= 0) {
                            Log.e(TAG, "result url is null or empty");
                        }

                        List<Cookie> cookies =  httpclient
                                .getCookieStore().getCookies();
                        if (cookies == null || cookies.isEmpty()) {
                            Log.e(TAG, "@@@Cookies not found");
                        } else {
                            //get auth value store in cookies
                            for (Cookie cookie : cookies) {
                                Log.v(TAG, "@@@COOKIE: " + cookie.toString());
                                if (Utility.HTTP_HEADER.AUTH.equals(cookie.getName())) {
                                    Log.i(TAG, "@@@DETECTED AUTH");
                                    result = cookie.getValue();
                                    break;
                                }
                            }
                        }
                    }
                    Log.v(TAG, "Status code:" + statusCode);
                }

            } catch (ClientProtocolException e) {
                Log.e(TAG, "Get URL error", e);
            } catch (IOException e) {
                Log.e(TAG, "Get URL error", e);
            }
            return result;
    }

}
