package jp.co.fujixerox.sa.ion.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.utils.Utility;

/**
 * Created by TrungKD on 9/14/2016.
 */
public class ScanBarCodeActivity2 extends AppCompatActivity {
    private DecoratedBarcodeView barcodeScannerView;


    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getBarcodeFormat() == BarcodeFormat.CODE_39 && result.getText() != null) {
                barcodeScannerView.pause();
                showDialogResult(convertData(result.getText()));
            }
        }
        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(R.string.title_result_scan);
        }
        int orientation = getIntent().getIntExtra(Utility.EXTRA_INTENT.ORIENTATION,1);
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        barcodeScannerView = (DecoratedBarcodeView)findViewById(R.id.barcode_scanner);
        barcodeScannerView.decodeContinuous(callback);

    }

    public boolean checkPattern(String data, String pattern){
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(data);
        return m.matches();
    }


    private void showDialogResult(final String[] result) {
        if(result[0] != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.title_result_scan);
            String msg = getString(R.string.serial_no) + result[0] + "\n" + getString(R.string.product_name) + result[1];
            builder.setMessage(msg);

            builder.setNegativeButton(R.string.re_scan, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    barcodeScannerView.resume();
                    dialogInterface.cancel();
                }
            });

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent();
                    intent.putExtra(Utility.EXTRA_INTENT.SERIAL_NO, result[0]);
                    intent.putExtra(Utility.EXTRA_INTENT.PRODUCT_NAME, result[1]);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }else {
            barcodeScannerView.resume();
        }
    }

    private String[] convertData(String result) {
        String[] data = new String[2];
        if (checkPattern(result, Utility.PATTERN_BARCODE.PATTERN_BARCODE1)) {
            result = result.replace("--","");
            int length = result.length();
            data[0] = result.substring(0, length - 3);
            data[1] = result.substring(length - 3, length);
        }
        else if (checkPattern(result, Utility.PATTERN_BARCODE.PATTERN_BARCODE2)) {
            result = result.replace("--","");
            int length = result.length();
            data[1] = result.substring(0, length - 6);
            data[0] = result.substring(length - 6, length);
        }
        else if (checkPattern(result, Utility.PATTERN_BARCODE.PATTERN_BARCODE3)) {
            result = result.replace(" ","");
            int length = result.length();
            data[0] = result.substring(0, length - 3);
            data[1] = result.substring(length - 3, length);
        }
        else if (checkPattern(result, Utility.PATTERN_BARCODE.PATTERN_BARCODE4)) {
            result = result.replace(" ","");
            int length = result.length();
            data[1] = result.substring(0, length - 6);
            data[0] = result.substring(length - 6, length);
        }
        return data;
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
       barcodeScannerView.pause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}
