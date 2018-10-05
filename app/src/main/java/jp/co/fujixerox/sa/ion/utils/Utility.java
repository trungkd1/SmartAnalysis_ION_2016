package jp.co.fujixerox.sa.ion.utils;

import java.io.File;

/**
 * Utility constance variables
 */
public class Utility {
    public static final String EMPTY_STRING = "";
    public static final String ESCAPE_FORWARD_SLASH = "/";
    public static final String APP_DATA_FOLDER_NAME = "SaIonCompass";
    public static final String APP_CATALOG_FOLDER_NAME = "catalog";
    public static final String APP_CATALOG_FOLDER_NAME_SDCARD =
            File.separator
                    + APP_DATA_FOLDER_NAME + File.separator + "catalog";
    public static final String APP_REPORT_FOLDER_NAME_SDCARD =
            File.separator
                    + APP_DATA_FOLDER_NAME + File.separator + "report";
    public static final String APP_ANALYSIS_GUIDE_FOLDER_NAME = File.separator
            + APP_DATA_FOLDER_NAME + File.separator + "analysis_guide";

    public static final String REQUIRED_MARK = " <font color='red' >*</font>";

    public static final int AUDIO_DURATION = 8000; //ms
    public static final int RECORDER_BPP = 16;
    public static final int RECORDER_SAMPLERATE = 44100;
    public static final String AUDIO_RECORDER_FOLDER = APP_DATA_FOLDER_NAME
            + File.separator + "AudioRecorder";
    public static final String REPORT_TEMPLATE_FOLDER = APP_DATA_FOLDER_NAME
            + File.separator + "ReportTemplate";

    public static final String FILE_EXT_WAV = ".wav";
    public static final String FILE_EXT_PNG = ".png";
    public static final String PHOTO_ASSET_PATH = "images";
    public static final String ASSETS_JSON_PATH = "json";
    public static final String ASSETS_COMPARE_GUIDE_HTML_PATH = "compare_guide/html";
    public interface JSON_FILE_NAME {
        String AUDIO_CONDITIONS = "conditions.json";
        String PRODUCTS = "conditions.json";
////        String CATALOG_SEARCH = "catalog_search.json";
        String CATALOG_SEARCH = "conditions.json";
    }
    public static final boolean IS_BARCODE = true; //Select screen type is Barcode

    public static final String DATE_PATTERN = "yyyy/MM/dd";
    public static final String DATE_PATTERN_AUDIO = "yyyyMMddkkmmss"; //24h 160411 mit
    public static final Object NUMBER = "number";
    public static final String USER_AGENT_VALUE = "ionTool{version}/Android{android_version}/{manufacturer}/{model}";

    public static final int TIME_INTERVAL_UPDATE_AUDIO_PROCESS = 10; // ms
    public static final String ENCODING = "UTF-8";
    public static String[] PHOTO_ASSET_FILE_NAME = {
        "place_front.jpg",//101～106
        "place_right.jpg",//201～206
        "place_back.jpg", //301～306
        "place_left.jpg"//401～406
    };

    public static int[] FFT_VALUES = {
            512
    };
    public static int[] STEP_WIDTH_VALUES = {
            16, 32, 64
    };
    public static int[] ANALYSIS_TIME_VALUES = {
            1, 5, 8
    };
    public enum INPUT_PATTERN {
        SELECT, TEXT, OTHER, BOOL, RADIO, CHECK
    }
    public interface OPTIONS {
        int WIDTH = 400; // pixel unit
        int HEIGHT = 256; // pixel unit
    }

    public interface SHARE_PREFERENCES {
        String MY_PREFERENCES = "SaIonPrefs";
        String KEY_FFT_SIZE = "fftSize";
        String KEY_STEP_WIDTH = "step_width";
        String KEY_ANALYSIS_TIME = "time_analysis";
        String KEY_REPORTS_UPLOAD_PENDING = "reports_upload_pending";
        String KEY_ACCOUNT_AUTHEN = "email_authen";
        String KEY_COOKIE_AUTHEN = "cookie_authen";
        //選択アカウント
        String KEY_ACCOUNT_SELECTED = "email_select";
        String KEY_HASH_SELECTED = "";
    }

    public interface EXTRA_INTENT {
        String FROM_START_SCREEN = "jp.co.fujixerox.sa.ion.from_start_screen";
        String HAS_CATALOG_INFO = "jp.co.fujixerox.sa.ion.has_catalog_info";
        String CATALOG_SCREEN = "jp.co.fujixerox.sa.ion.catalog_screen";
        String CATALOG_SCREEN_LISTMODE = "catalog_screen_list_mode";
        String CATALOG_CAUSE_PARTS = "cause_parts";
        String CATALOG_METHODS = "methods";
        String CATALOG_METHOD_CONFIRM = "method_confirm";
        String CATALOG_METHOD_DETAIL = "method_detail";
        String CANCEL_SENDING_REPORT = "cancel";
        String RESUME_SENDING_REPORT = "resume";
        String AUDIO_REPORT = "jp.co.fujixerox.sa.ion.db.audioreportdata";
        String IS_OLD_REPORT = "jp.co.fujixerox.sa.ion.db.isoldreport";
        String SERIAL_NO = "serial_no";
        String PRODUCT_NAME = "product_name";
        String ORIENTATION = "orientation";
        //Request code for scaning Barcode
        int REQUEST_CODE_SCAN_BARCODE = 2;
    }

    /**
     *  Pattern for barcode
     */
    public interface PATTERN_BARCODE {
        String PATTERN_BARCODE1 = "^[0-9][0-9]{5}\\-\\-[a-zA-Z0-9]{3}$";
        String PATTERN_BARCODE2 = "^[a-zA-Z0-9]{3}\\-\\-[0-9][0-9]{5}$";
        String PATTERN_BARCODE3 = "^[0-9][0-9]{5} [a-zA-Z0-9]{3}$";
        String PATTERN_BARCODE4 = "^[a-zA-Z0-9]{3} [0-9][0-9]{5}$";
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

    public static String CACHE_DIR = "cache_dir";//cache dir store image cache

    public static int REPORTS_MAX_NUMBER = 30; // report max size (max audios in database)

    public static int AUDIO_MAX_NUMBER = 5; // audio max number

    public static int AUDIO_MAX_NUMBER_2 = AUDIO_MAX_NUMBER > REPORTS_MAX_NUMBER ? REPORTS_MAX_NUMBER : AUDIO_MAX_NUMBER;

    public static final String CHARACTERS_SEPARATE = ";";// characters separate

    public static final int ALARM_TRIGGER_TIME_HOUR_OF_DAY = 10;

    public static final int ALARM_TRIGGER_TIME_MINUTE = 30;

}
