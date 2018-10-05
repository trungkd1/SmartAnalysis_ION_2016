package jp.co.fujixerox.sa.ion.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.co.fujixerox.sa.ion.DefaultApplication;
import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.adapters.SettingListAdapter;
import jp.co.fujixerox.sa.ion.sync.AsyncTaskCallback;
import jp.co.fujixerox.sa.ion.sync.RequestAuthTask;
import jp.co.fujixerox.sa.ion.dialogs.AddAccountConfirmDialog;
import jp.co.fujixerox.sa.ion.utils.CloudConnector;
import jp.co.fujixerox.sa.ion.utils.CommonUtils;
import jp.co.fujixerox.sa.ion.utils.Utility;

/**
 * Created by TrungKD on 1/21/2016.
 * GoogleAccount認証選択画面
 */
public class SettingScreenActivity extends AbstractFragmentActivity {

    public static final String TAG = SettingScreenActivity.class.getSimpleName();

    private static final int REQUEST_CODE = 1;
    private static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    // List action name display on Setting screen;
    private List<String> listAction;
    // account name google authentication;
    private String account_auth;
    private String account_select;
    private String account_hash;
    // token authentication;
    private String googleAccountToken;
    /**
     * onClick item listView on SettingScreen
     */
    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0:
                    showAccountsDialog();
                    break;
                default:
                    break;
            }
        }
    };
    private SettingListAdapter adapter;
    private AsyncTaskCallback processAfterGetCookie =
            new AsyncTaskCallback() {

                @Override
                public void onPrepare(PROGRESS_TYPE type) {
                    //do nothing
                }

                @Override
                public void onSuccess(Object object) {
                    String cookie = (String) object;
                    if (cookie == null) {
                        showToastMessage(R.string.no_connect_server);
                    } else if (cookie.length() == 0) {
                        showToastMessage(R.string.get_cookie_fail);
                    } else {
                        onSaveCookieAndAccount(account_auth, cookie);
                        onUpdateScreen();
                    }
                }

                @Override
                public void onFailed(int errorMessageId) {

                }

                @Override
                public void onFinish(PROGRESS_TYPE loadingType) {

                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_screen);
        onDisplayScreen();
        sendTrackingSetting(this);
    }

    private void onDisplayScreen() {
        ListView listView = (ListView) findViewById(R.id.lvScreenSetting);
        listAction = new ArrayList<>();
        //get Account google authen in SharePreferences;
        account_auth = CommonUtils.getStringPreferences(this, Utility.SHARE_PREFERENCES.KEY_ACCOUNT_AUTHEN);
        account_select = CommonUtils.getStringPreferences(this, Utility.SHARE_PREFERENCES.KEY_ACCOUNT_SELECTED);
        //check if has account auth when display account auth in list, other show text no account google authenticate
        if (!TextUtils.isEmpty(account_auth)) {
            listAction.add(0, account_auth);
        } else if(!TextUtils.isEmpty(account_select)){  //選択されたアカウントを表示　160405 mit
            listAction.add(0, account_select);
           //onUpdateScreen();
        } else {
            //show text not account google login device
            listAction.add(0, getResources().getString(R.string.no_account_google_authenticate));
        }
        // use your custom layout
        adapter = new SettingListAdapter(this, R.layout.item_setting_layout, listAction);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);
    }

    private String[] getAccountNames() {
        AccountManager mAccountManager = AccountManager.get(SettingScreenActivity.this);
        Account[] accounts = mAccountManager.getAccountsByType(GOOGLE_ACCOUNT_TYPE);
        String[] names = new String[accounts.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = accounts[i].name;
        }
        return names;
    }

    /**
     * show Accounts Google in device
     */
    private void showAccountsDialog() {
        final String[] accountNames = getAccountNames();
        if (accountNames.length == 0) {
            AddAccountConfirmDialog dialog = new AddAccountConfirmDialog();
            dialog.show(getSupportFragmentManager(), TAG);
            return;
        }
        if (Build.VERSION.SDK_INT >= 14) {
            Log.v(TAG, "@@Get Account SDK ABOVE 14");
            Intent intent = AccountManager.newChooseAccountIntent(null, null,
                            new String[]{GOOGLE_ACCOUNT_TYPE}, false, null, null, null, null);
            startActivityForResult(intent, REQUEST_CODE);
        } else {
            Log.v(TAG, "@@Get Account SDK OLD");
            showAccountsDialogSDKOld();
        }

    }

    /**
     * show list account google with device sdk version <14
     */
    private void showAccountsDialogSDKOld() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.select_account_google));
        final String[] accountNames = getAccountNames();
        builder.setItems(accountNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!CloudConnector.isConnectingToInternet(SettingScreenActivity.this)) {
                    showToastMessage(R.string.no_connect_internet);
                } else {
                    getTokenFromAccountManager(accountNames[which]);
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!CloudConnector.isConnectingToInternet(this) && false) { //認証を保留するため強制スキップ 160406 mit
            showToastMessage(R.string.no_connect_internet);
        } else if ((( requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) || true ) && data != null) { //認証を保留するため強制 160406 mit
           account_select = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            Log.v(TAG, "@@Account Name: " + account_select);
            /*
            getTokenFromAccountManager(account_name);
            //Tracking Event
            DefaultApplication.getInstance().trackEvent(getString(R.string.category_ui_event),
                    getString(R.string.action_select_authentication), account_name);
            認証処理をスキップ 160405 mit */
            //認証をなくして選択されたアカウントだけ送信 160405 mit
           account_hash = CommonUtils.digest(account_select);
            Log.v(TAG, "@@Account Name: " + account_select);
            onSaveSelectedAccount(account_select, account_hash);
            onUpdateScreen();
            //
            DefaultApplication.getInstance().trackEvent(
                    getString(R.string.action_select_authentication),
                    account_hash,
                    account_hash);
        }
    }

    /**
     * get auth token by Account Google
     *
     * @param accountName
     */
    private void getTokenFromAccountManager(String accountName) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getResources().getString(R.string.processing));
        dialog.show();

        AccountManagerCallback accountManagerCallback = new AccountManagerCallback() {
            @Override
            public void run(AccountManagerFuture future) {
                dialog.dismiss();
                Bundle bundle;
                try {
                    bundle = (Bundle) future.getResult();
                    account_auth = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);
                    googleAccountToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    Log.v(TAG, "@@ACCOUNT: " + account_auth + "|" + googleAccountToken);
                    getCookieFromIPFCloud(googleAccountToken);
                } catch (OperationCanceledException | AuthenticatorException e) {
                    Log.e(TAG, "@@AUTHENTICATION_FAIL", e);
                    showToastMessage(R.string.authentication_fail);
                } catch (IOException ex) {
                    Log.e(TAG, "@@AUTHENTICATION_FAIL", ex);
                    showToastMessage(R.string.network_error);
                }
            }
        };

        AccountManager manager = AccountManager.get(this);
        manager.getAuthToken(new Account(accountName, GOOGLE_ACCOUNT_TYPE), "mail", null, this, accountManagerCallback, null);
    }

    private void showToastMessage(String message) {
        Toast.makeText(SettingScreenActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void showToastMessage(int stringId) {
        Toast.makeText(SettingScreenActivity.this, stringId, Toast.LENGTH_SHORT).show();
    }

    private void getCookieFromIPFCloud(String googleAccountToken) {
        new RequestAuthTask(this, processAfterGetCookie).execute(googleAccountToken);
    }

    /**
     * save cookie and Account Google in SharedPreferences
     *
     * @param account
     * @param cookie
     */
    private void onSaveCookieAndAccount(String account, String cookie) {
        CommonUtils.saveStringPreferences(this, Utility.SHARE_PREFERENCES.KEY_ACCOUNT_AUTHEN, account);
        CommonUtils.saveStringPreferences(this, Utility.SHARE_PREFERENCES.KEY_COOKIE_AUTHEN, cookie);
    }
    //選択したアカウント/hashの保存 160404 mit
    private void onSaveSelectedAccount(String account, String acc_hash){
        CommonUtils.saveStringPreferences(this, Utility.SHARE_PREFERENCES.KEY_ACCOUNT_SELECTED,account);
        CommonUtils.saveStringPreferences(this, Utility.SHARE_PREFERENCES.KEY_HASH_SELECTED,acc_hash);
    }

    private void onUpdateScreen() {
        listAction.remove(0);
        listAction.add(0, account_select);
        adapter.notifyDataSetChanged();
    }
}
