package jp.co.fujixerox.sa.ion.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.List;

import jp.co.fujixerox.sa.ion.db.AudioFormData;
import jp.co.fujixerox.sa.ion.db.ICloudParams;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * This class use OkHttp library to request (get/post) to cloud.
 * Created by fxstdpc-admin on 2016/07/14.
 */
public class CloudConnector2 {
    private static final String TAG = CloudConnector2.class.getSimpleName();
    private static final String BOUNDARY =  String.valueOf(System.currentTimeMillis());;
    private static OkHttpClient client;

    public static OkHttpClient getClient() {
        if (client == null) {
            client = new OkHttpClient();
        }
        return client;
    }


    /**
     * Http get, sent request to Cloud server
     *
     * @param cloudApi String
     * @param dataList List<FormData>
     * @param callback Callback
     * @return Call
     */
    public static Call get(String cloudApi, List<AudioFormData> dataList, Callback callback) {
        Log.d(TAG, "OkHttp.get:" + cloudApi);
        String url = ICloudInterface.createRequestUrl(cloudApi);
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        if (dataList != null) {
            for (AudioFormData formData : dataList) {
                Log.d(TAG, "formData:" + formData.toString());
                urlBuilder.addQueryParameter(formData.getFormid(), formData.getValue());
            }
        }
        Request request = getRequestBuilder().url(urlBuilder.build().toString()).build();

        Call call = getClient().newCall(request);
        if (callback != null) {
            call.enqueue(callback);
        }
        return call;
    }

    /**
     * Http Post, send multipart data to cloud server
     *
     * @param cloudApi String
     * @param dataList List<FormData>
     * @param callback Callback
     * @return Call
     */
    public static Call post(String cloudApi, List<AudioFormData> dataList, Callback callback) {
        Log.d(TAG, "OkHttp.post:" + cloudApi);
        Request.Builder requestBuilder = getRequestBuilder();
        MultipartBody.Builder builder = new MultipartBody.Builder(BOUNDARY);
        builder.setType(MultipartBody.FORM);
        for (AudioFormData formData : dataList) {
            if (!TextUtils.isEmpty(formData.getMimeType())) {
                Log.d(TAG, "add formData:" + formData.toString());
                addBinaryDataToBuilder(formData, builder);
            } else {
                String value = formData.getValue();
                if (value != null) {
                    builder.addFormDataPart(formData.getFormid(), value);
                } else {
                    Log.w(TAG, "formData is null skip: " + formData.getFormid());
                }
            }
        }
        try {
            RequestBody requestBody = builder.build();
            requestBuilder.url(ICloudInterface.createRequestUrl(cloudApi)).post(requestBody);
            Call call = getClient().newCall(requestBuilder.build());
            call.enqueue(callback);
            return call;
        } catch (IllegalStateException e) {
            Log.e(TAG, "POST fail", e);
        }
        return null;
    }

    /**
     * Http Post, send multipart data to cloud server
     *
     * @param dataList List<FormData>
     * @param callback Callback
     * @return Call
     */
    public static Call postReport(String uploadUrl, List<AudioFormData> dataList, String accountId,
                                  Callback callback, ProgressRequestBody.Listener progressListener) {
        Log.d(TAG, "OkHttp.post:" + uploadUrl);
        Request.Builder requestBuilder = getRequestBuilder();
        MultipartBody.Builder builder = new MultipartBody.Builder(BOUNDARY);
        builder.setType(MultipartBody.FORM);
        if (accountId != null) {
            builder.addFormDataPart(ICloudParams.accountid, accountId);
        }
        for (AudioFormData formData : dataList) {
            if (ICloudParams.sound.equals(formData.getFormid()) ||
                    ICloudParams.picture.equals(formData.getFormid())) {
                if (formData.getValue() != null) {
                    formData.setMimeType(FileUtils.getMimeType(formData.getValue()));
                    Log.d(TAG, "add formData:" + formData.toString());
                    addBinaryDataToBuilder(formData, builder);
                }
            } else {
                String value = formData.getValue();
                if (value != null) {
                    Log.d(TAG, "add formData:" + formData.toString());
                    if (ICloudParams.catalogid.equals(formData.getFormid())
                            && Long.getLong(formData.getValue(), 0) <= 0) {
                        Log.w(TAG, "formData is 0 skip: " + formData.getFormid());
                        continue;
                    }
                    builder.addFormDataPart(formData.getFormid(), value);
                } else {
                    Log.w(TAG, "formData is null skip: " + formData.getFormid());
                }
            }
        }
        try {
            ProgressRequestBody requestBody = new ProgressRequestBody(builder.build(), progressListener) ;
            requestBuilder.url(uploadUrl).post(requestBody);
            Call call = getClient().newCall(requestBuilder.build());
            call.enqueue(callback);
            return call;
        } catch (IllegalStateException e) {
            Log.e(TAG, "POST fail", e);
        }
        return null;
    }
    /**
     * Set file to post multipart
     *
     * @param data    FormData
     * @param builder MultipartBody.Builder
     */
    private static void addBinaryDataToBuilder(AudioFormData data, MultipartBody.Builder builder) {
        String filePath = data.getValue();
        File file = new File(filePath);
        Log.v(TAG, "file path:" + filePath);
        if (file.exists()) {
            builder.addFormDataPart(data.getFormid(), file.getName(),
                    RequestBody.create(MediaType.parse(data.getMimeType()), file));
        } else {
            Log.e(TAG, "File not exist");
        }

    }


    private static Request.Builder getRequestBuilder() {
        return new Request.Builder()
                .header(ICloudInterface.HTTP_HEADER.USER_AGENT, CommonUtils.generateUserAgent());

    }

}
