package jp.co.fujixerox.sa.ion.sync;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.utils.CloudConnector;

/**
 * The task executes authentication and get cookie (session googleAccountToken) from IPF Cloud.
 */
public class RequestAuthTask extends AsyncTask<String, String, String> {

    private static final String TAG = "RequestAuthTask";
    private ProgressDialog pDialog;
    private Context context;
    private AsyncTaskCallback asyncTaskCallback;

    public RequestAuthTask(Context context, AsyncTaskCallback asyncTaskCallback){
        this.context = context;
        this.asyncTaskCallback = asyncTaskCallback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showProgressDialog();
    }

    private void showProgressDialog() {
        pDialog = new ProgressDialog(context);
        pDialog.setMessage(context.getResources().getString(R.string.authenticating));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    @Override
    protected String doInBackground(String... googleAccountTokens) {
       return CloudConnector.getCookieAuthSession(googleAccountTokens[0]);
    }

    @Override
    protected void onPostExecute(String cookieAuth) {
        pDialog.dismiss();
        asyncTaskCallback.onSuccess(cookieAuth);
    }

};