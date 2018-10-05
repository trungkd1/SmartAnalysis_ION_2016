package jp.co.fujixerox.sa.ion.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.co.fujixerox.sa.ion.utils.FileUtils;
import jp.co.fujixerox.sa.ion.utils.Utility;

/**
 * ReportData Template, data, form data
 *
 * @author TrungKD
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    /* TABLE NAME */
    private final static String DB_NAME = "sa_ion_compass.db";
    /* Database version */
    private final static int DB_VER = 5;
    private static final String TAG = DatabaseHelper.class.getSimpleName();
    private static DatabaseHelper INSTANCE = null;
    private Context mContext;

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VER);
        this.mContext = context;
    }

    public static DatabaseHelper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DatabaseHelper(context);
        }
        return INSTANCE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_STATEMENT.AUDIO_DATA_CREATE);
        db.execSQL(SQL_CREATE_STATEMENT.AUDIO_FORM_DATA_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + IAudioData.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + IAudioFormData.TABLE_NAME);
            onCreate(db);
        }

    }

    /**
     * update analysis image filename
     *
     * @param audioDataId:   audios id
     * @param imageFileName: image filename
     * @return rows of record updated
     * @see SQLiteDatabase#update(String, ContentValues, String, String[])
     */
    public int updateImageFileName(long audioDataId, String imageFileName) {
        int rows = 0;
        SQLiteDatabase writableDatabase = getWritableDatabase();
        try {
            writableDatabase.beginTransaction();
            // Save to COLUMN_ANALYSIS_IMAGE
            ContentValues values = new ContentValues();
            values.put(IAudioData.COLUMN_ANALYSIS_IMAGE, imageFileName);
            Log.v(TAG, "insert table: name image");
            rows = writableDatabase.update(IAudioData.TABLE_NAME, values,
                    IAudioData.COLUMN_ID + "=?",
                    new String[]{String.valueOf(audioDataId)});
            Log.i(TAG, "rows: " + rows);
            writableDatabase.setTransactionSuccessful();
        } catch (Exception ex) {
            Log.e(TAG, "Error when update image filename: ", ex);
        } finally {
            writableDatabase.endTransaction();
        }
        return rows;
    }

    /**
     * Update AudioData in database
     *
     * @param audioData: audios data
     * @return number of record data is updated
     */
    public int updateAudioData(AudioData audioData) {
        int rows = -1;
        SQLiteDatabase writableDatabase = getWritableDatabase();
        try {
            writableDatabase.beginTransaction();
            ContentValues values = new ContentValues();

            values.put(IAudioData.COLUMN_COMMENT_SEND,
                    audioData.getComment());
            values.put(IAudioData.COLUMN_RECORD_DATE, audioData.getRecordDate());
            values.put(IAudioData.COLUMN_AUDIO_FILE_NAME,
                    audioData.getSound());
            values.put(IAudioData.COLUMN_REPORT_ID_CLOUD,
                    audioData.getReportid());
            rows = writableDatabase.update(IAudioData.TABLE_NAME, values,
                    IAudioData.COLUMN_ID + "=?",
                    new String[]{String.valueOf(audioData.getId())});
            writableDatabase.setTransactionSuccessful();
        } catch (Exception ex) {
            Log.e(TAG, "Error when update audio average: ", ex);
        } finally {
            writableDatabase.endTransaction();
        }
        return rows;
    }

    /**
     * save audio data: insert or update
     *
     * @param audioData: audio data
     * @return id of audio data has saved
     */
    private long saveAudioData(AudioData audioData) {
        long audioDataId = 0;
        boolean isUpdate = (audioData.getId() > 0);
        //begin set value to save database
        SQLiteDatabase writableDatabase = getWritableDatabase();
        try {
            writableDatabase.beginTransaction();
            // Save to table : audios
            ContentValues values = new ContentValues();
            values.put(IAudioData.COLUMN_COMMENT_SEND,
                    audioData.getComment());
            values.put(IAudioData.COLUMN_RECORD_DATE, audioData.getRecordDate());
            values.put(IAudioData.COLUMN_AUDIO_FILE_NAME,
                    audioData.getSound());
            values.put(IAudioData.COLUMN_ANALYSIS_IMAGE,
                    audioData.getPicture());  //160408 mit
            values.put(IAudioData.COLUMN_REPORT_ID_CLOUD,
                    audioData.getReportid());
            if (audioData.getCatalogId() > 0) { //only save catalog id larger 0
                values.put(IAudioData.COLUMN_CATALOG_ID,
                        audioData.getCatalogId());
            }
            values.put(IAudioData.COLUMN_CAUSE,
                    audioData.getCause());
            values.put(IAudioData.COLUMN_METHOD,
                    audioData.getMethod());
            values.put(IAudioData.COLUMN_METHOD_DETAIL,
                    audioData.getMethodDetail());
            values.put(IAudioData.COLUMN_RESULT,
                    audioData.getResult());
            values.put(IAudioData.COLUMN_LONGITUDE, audioData.getLongitude());
            values.put(IAudioData.COLUMN_LATITUDE, audioData.getLatitude());
            values.put(IAudioData.COLUMN_CAUSE_JSONFORM,
                    audioData.getCasuseJsonForm());
            values.put(IAudioData.COLUMN_METHOD_JSONFORM,
                    audioData.getMethodJsonForm());
            if (isUpdate) {
                audioDataId = audioData.getId();
                int rows = writableDatabase.update(IAudioData.TABLE_NAME,
                        values, IAudioData.COLUMN_ID + "=?",
                        new String[]{String.valueOf(audioDataId)});
                if (rows > 0) {
                    Log.v(TAG, "update table: audios success");
                } else {
                    Log.e(TAG, "update table: audios failed");
                }
            } else {
                Log.v(TAG, "insert table: audios");
                audioDataId = writableDatabase.insert(IAudioData.TABLE_NAME,
                        null, values);
                Log.v(TAG, "insert table: audios data id = " + audioDataId);
                audioData.setId(audioDataId);
            }
            if (audioDataId > 0) {
                //delete all audio form data
                deleteAllAudioFormData(audioDataId);
                // Save to table : audio_formdata
                List<AudioFormData> data = audioData.getListAudioFormData();
                for (AudioFormData audioFormData : data) {
                    if (TextUtils.isEmpty(audioFormData.getValue())) {
                        Log.e(TAG, "formDataId"+audioFormData.getFormid()+" value is null");
                        continue;
                    }
                    ContentValues valueFormData = new ContentValues();
                    valueFormData.put(IAudioFormData.COLUMN_AUDIO_DATA_ID,
                            audioDataId);
                    String formid = audioFormData.getFormid();
                    valueFormData.put(IAudioFormData.COLUMN_FORMID, formid);
                    String value = audioFormData.getValue();
                    valueFormData.put(IAudioFormData.COLUMN_VALUE, value);
                    String text = audioFormData.getText();
                    valueFormData.put(IAudioFormData.COLUMN_TEXT, text);
                    long formDataId = writableDatabase.insert(
                            IAudioFormData.TABLE_NAME, null, valueFormData);

                    Log.v(TAG, "insert table: audio_formdata: " + formDataId
                            + ":" + value);
                }
            }
            writableDatabase.setTransactionSuccessful();

        } finally {
            writableDatabase.endTransaction();
        }
        return audioDataId;
    }

    /**
     * save audio data and setup max size of audio data
     *
     * @param audioData: AudioData
     * @return audios id
     */
    public long saveAudioData(AudioData audioData, boolean needDeleteOldest) {
        long audioDataId = saveAudioData(audioData);
        //check if save successful and need delete oldest when delete oldest audios
        if (isNeedDeleteOldest(needDeleteOldest, audioDataId)) {
            deleteOldestAudioData(audioDataId);
        }
        return audioDataId;
    }

    private boolean isNeedDeleteOldest(boolean needDeleteOldest, long audioDataId) {
        return audioDataId > 0 && needDeleteOldest;
    }

    /**
     * Delete all report form data by reportDataId
     *
     * @param audioDataId: audios id
     */
    public void deleteAllAudioFormData(long audioDataId) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        int rowcount = writableDatabase.delete(IAudioFormData.TABLE_NAME,
                IAudioFormData.COLUMN_AUDIO_DATA_ID + "=?",
                new String[]{String.valueOf(audioDataId)});

        Log.d("@@deleted", String.valueOf(rowcount));
    }

    /**
     * Get one or all audio (include report) data
     *
     * @param order      true for sort order is need
     * @param audioId    id for audio data or none for all data
     * @param showAll    true get data has analysis image
     * @param orderByAsc sort type
     * @return List off audio data
     */
    public List<AudioData> getAudiosData(String order, long audioId,
                                         boolean showAll, boolean orderByAsc) {
        List<AudioData> listAudioData;
        SQLiteDatabase writableDatabase = getWritableDatabase();

		/* GET AUDIO DATA */
        String selection = null;
        String[] selectionArgs = null;
        if (audioId > 0) {
            selection = IAudioData.COLUMN_ID + "=?";
            selectionArgs = new String[]{String.valueOf(audioId)};
        }
        if (!showAll) {
            selection = IAudioData.COLUMN_ANALYSIS_IMAGE + " IS NOT NULL ";
        }

        String orderBy;
        if (TextUtils.isEmpty(order)) {
            orderBy = IAudioData.COLUMN_RECORD_DATE;
        } else {
            orderBy = order;
        }

        if (!TextUtils.isEmpty(orderBy)) {
            if (!orderByAsc) {
                orderBy = orderBy + " ASC";
            } else {
                orderBy = orderBy + " DESC";
            }
        }

        Cursor cursor = null;
        try {
            cursor = writableDatabase.query(IAudioData.TABLE_NAME, null, selection,
                    selectionArgs, null, null, orderBy);
        } catch (Exception ex) {
            Log.e(TAG, "Error when query database: ", ex);
        }
        listAudioData = getAudioFromCursor(cursor);

        return listAudioData;
    }

    /**
     * get list of audio data (audio data only)
     *
     * @param order:   order
     * @param audioId: audios id
     * @return List<AudioData> list of audio data
     */
    public List<AudioData> getAudios(String order, long audioId) {
        List<AudioData> listAudioData;
        SQLiteDatabase writableDatabase = getWritableDatabase();

		/* GET AUDIO DATA */
        String selection = null;
        String[] selectionArgs = null;
        if (audioId > 0) {
            selection = IAudioData.COLUMN_ID + "=?";
            selectionArgs = new String[]{String.valueOf(audioId)};
        }
        String orderBy;
        if (TextUtils.isEmpty(order)) {
            orderBy = IAudioData.COLUMN_RECORD_DATE + " DESC";
        } else {
            orderBy = order + " DESC";
        }
        Cursor cursor = null;
        try {
            cursor = writableDatabase.query(IAudioData.TABLE_NAME, null,
                    selection, selectionArgs, null, null, orderBy);
        } catch (Exception ex) {
            Log.e(TAG, "Error when query database: ", ex);
        }
        listAudioData = getAudioFromCursor(cursor);

        return listAudioData;
    }

    /**
     * return audios from cursor
     *
     * @param cursor: Cursor
     * @return list audios
     */
    private List<AudioData> getAudioFromCursor(Cursor cursor) {
        List<AudioData> listAudioData = null;
        if (cursor != null) {
            listAudioData = new ArrayList<>();
            boolean res = cursor.moveToFirst();
            if (res) {
                String[] columnNames = cursor.getColumnNames();
                do {
                    AudioData audioData = new AudioData();
                    for (int i = 0; i < columnNames.length; i++) {
                        String columnName = columnNames[i];
                        if (IAudioData.COLUMN_ID.equals(columnName)) {
                            audioData.setId(cursor.getLong(i));
                        } else if (IAudioData.COLUMN_RECORD_DATE
                                .equals(columnName)) {
                            audioData.setRecordDate(cursor.getLong(i));
                        } else if (IAudioData.COLUMN_AUDIO_FILE_NAME
                                .equals(columnName)) {
                            audioData.setSound(cursor.getString(i));
                        } else if (IAudioData.COLUMN_LATITUDE.equals(columnName)) {
                            audioData.setLatitude(cursor.getString(i));
                        } else if (IAudioData.COLUMN_LONGITUDE
                                .equals(columnName)) {
                            audioData.setLongitude(cursor.getString(i));
                        } else if (IAudioData.COLUMN_ANALYSIS_IMAGE
                                .equals(columnName)) {
                            audioData.setPicture(cursor.getString(i));
                        } else if (IAudioData.COLUMN_REPORT_ID_CLOUD
                                .equals(columnName)) {
                            audioData.setReportid(cursor.getString(i));
                        } else if (IAudioData.COLUMN_COMMENT_SEND
                                .equals(columnName)) {
                            audioData.setComment((cursor.getString(i)));
                        } else if (IAudioData.COLUMN_SELECTED_RECT
                                .equals(columnName)) {
                            audioData.setSelectPoints(cursor.getString(i));
                        } else if (IAudioData.COLUMN_AVERAGE_FREQUENCY
                                .equals(columnName)) {
                            audioData.setAverageFrequency(cursor.getFloat(i));
                        } else if (IAudioData.COLUMN_AVERAGE_PERIOD
                                .equals(columnName)) {
                            audioData.setAveragePeriod(cursor.getFloat(i));
                        } else if (IAudioData.COLUMN_CATALOG_ID.equals(columnName)) {
                            long value = cursor.getLong(i);
                            if (value > 0) { // only set catalog id > 0
                                audioData.setCatalogId(cursor.getLong(i));
                            }
                        } else if (IAudioData.COLUMN_CAUSE.equals(columnName)) {
                            audioData.setCause(cursor.getString(i));
                        } else if (IAudioData.COLUMN_METHOD.equals(columnName)) {
                            audioData.setMethod(cursor.getString(i));
                        } else if (IAudioData.COLUMN_METHOD_DETAIL.equals(columnName)) {
                            audioData.setMethodDetail(cursor.getString(i));
                        } else if (IAudioData.COLUMN_RESULT.equals(columnName)) {
                            audioData.setResult(cursor.getString(i));
                        } else if (IAudioData.COLUMN_CAUSE_JSONFORM.equals(columnName)) {
                            audioData.setCasuseJsonForm(cursor.getString(i));
                        } else if (IAudioData.COLUMN_METHOD_JSONFORM.equals(columnName)) {
                            audioData.setMethodJsonForm(cursor.getString(i));
                        }
                    }
                    if (audioData.getId() > 0) {
                        List<AudioFormData> datas = getAudioFormDatas(audioData
                                .getId());
                        audioData.setListAudioFormData(datas);
                    }
                    listAudioData.add(audioData);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return listAudioData;
    }

    /**
     * return audio_formdata from database
     *
     * @param audioDataId: audio_formdata id
     * @return list audio_formdata
     */
    public List<AudioFormData> getAudioFormDatas(long audioDataId) {
        List<AudioFormData> listAudioFormData = new ArrayList<>();
        SQLiteDatabase writableDatabase = getWritableDatabase();

		/* GET AUDIO DATA */
        String selection = null;
        String[] selectionArgs = null;
        if (audioDataId > 0) {
            selection = IAudioFormData.COLUMN_AUDIO_DATA_ID + "=?";
            selectionArgs = new String[]{String.valueOf(audioDataId)};
        }
        Cursor cursor = null;
        try {
            cursor = writableDatabase.query(IAudioFormData.TABLE_NAME, null,
                    selection, selectionArgs, null, null, null);
        } catch (Exception ex) {
            Log.e(TAG, "Error when query database: ", ex);
        }
        if (cursor != null && cursor.moveToFirst()) {
            String[] columnNames = cursor.getColumnNames();
            do {
                AudioFormData audioFormData = new AudioFormData();
                for (int i = 0; i < columnNames.length; i++) {
                    String columnName = columnNames[i];
                    if (IAudioFormData.COLUMN_ID.equals(columnName)) {
                        audioFormData.setId(cursor.getLong(i));
                    } else if (IAudioFormData.COLUMN_AUDIO_DATA_ID
                            .equals(columnName)) {
                        audioFormData.setAudioData_id(cursor.getLong(i));
                    } else if (IAudioFormData.COLUMN_VALUE
                            .equals(columnName)) {
                        audioFormData.setValue(cursor.getString(i));
                    } else if (IAudioFormData.COLUMN_TEXT
                            .equals(columnName)) {
                        audioFormData.setText(cursor.getString(i));
                    } else if (IAudioFormData.COLUMN_FORMID
                            .equals(columnName)) {
                        audioFormData.setFormid(cursor.getString(i));
                    }
                }
                listAudioFormData.add(audioFormData);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return listAudioFormData;
    }

    /**
     * update audio average period and average frequency
     *
     * @param audioDataId:      audios id
     * @param averagePeriod:    averagePeriod of audios
     * @param averageFrequency: averageFrequency of audios
     * @return id of audios
     */
    public long updateAudioAverages(long audioDataId, float averageFrequency,
                                    float averagePeriod, String selectedRect) {  // PeriodとFrequencyを入れ替え 20160502 mit
        SQLiteDatabase writableDatabase = getWritableDatabase();
        int rows = -1;
        try {
            writableDatabase.beginTransaction();
            // Save to table : audio_data
            ContentValues values = new ContentValues();
            values.put(IAudioData.COLUMN_AVERAGE_PERIOD, averagePeriod);
            values.put(IAudioData.COLUMN_AVERAGE_FREQUENCY, averageFrequency);
            values.put(IAudioData.COLUMN_SELECTED_RECT, selectedRect);
            rows = writableDatabase.update(IAudioData.TABLE_NAME, values,
                    IAudioData.COLUMN_ID + "=?",
                    new String[]{String.valueOf(audioDataId)});
            Log.v(TAG, "update table: audio_data result=" + (rows > 0));
            writableDatabase.setTransactionSuccessful();

        } catch (Exception ex) {
            Log.e(TAG, "Error when update audio average: ", ex);
        } finally {
            writableDatabase.endTransaction();
        }
        return rows;
    }

    /**
     * Delete all report data by audioId
     *
     * @param audioId: audios id
     */
    public void deleteAudioData(long audioId) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        int row = writableDatabase.delete(
                IAudioData.TABLE_NAME, IAudioData.COLUMN_ID + "=?",
                new String[]{String.valueOf(audioId)});
        Log.d(TAG, "@@deleted from IAudioData:" + row);
    }

    /**
     * get list audios data with id in determine array
     *
     * @param audiosIdArray: audios id list
     * @return list audio data
     */
    public List<AudioData> getAudiosPending(String[] audiosIdArray) {
        List<AudioData> listAudioData;
        SQLiteDatabase writableDatabase = getWritableDatabase();
        if (audiosIdArray == null || audiosIdArray.length == 0) {
            return null;
        }
        int length = audiosIdArray.length;
        String orderBy = IAudioData.COLUMN_RECORD_DATE + " DESC";
        StringBuilder whereClause = new StringBuilder(
                IAudioData.COLUMN_ID + " IN (");
        for (int i = 0; i < length; i++) {
            if (i == 0) {
                whereClause.append("?");
            } else {
                whereClause.append(",?");
            }
        }
        whereClause.append(")");
        Cursor cursor = null;
        try {
            cursor = writableDatabase.query(IAudioData.TABLE_NAME, null,
                    whereClause.toString(), audiosIdArray, null, null, orderBy);
        } catch (Exception ex) {
            Log.e(TAG, "Error when query database: ", ex);
        }
        listAudioData = getAudioFromCursor(cursor);
        return listAudioData;
    }

    /**
     * Check report count if larger max report max number then delete oldest report
     *
     * @param exceptedAudioId: excepted audio id: can not delete
     * @return count of deleted reports;
     */
    private int deleteOldestAudioData(long exceptedAudioId) {
        int diff = 0;
        String[] columns = new String[]{IAudioData.COLUMN_ID,
                IAudioData.COLUMN_RECORD_DATE, IAudioData.COLUMN_AUDIO_FILE_NAME, IAudioData.COLUMN_ANALYSIS_IMAGE};
        String orderBy = IAudioData.COLUMN_REPORT_ID_CLOUD + " DESC,";
        orderBy += IAudioData.COLUMN_RECORD_DATE + " ASC ";
        String[] whereArgs = {};
        SQLiteDatabase writableDatabase = getWritableDatabase();
        Cursor cursor = writableDatabase.query(IAudioData.TABLE_NAME, columns,
                IAudioData.COLUMN_ID + "!=?",
                new String[]{String.valueOf(exceptedAudioId)}, null, null, orderBy);
        if (cursor != null && cursor.moveToFirst()) {
            int count = cursor.getCount();
            //add 1 because don't query exceptedAudioId
            diff = count - Utility.REPORTS_MAX_NUMBER + 1;
            Log.v(TAG, "diff: " + diff + " and count is: " + count);
            if (diff > 0) {
                StringBuilder whereClause = new StringBuilder(IAudioData.COLUMN_ID + " IN (");
                cursor.moveToFirst();
                whereArgs = new String[diff];
                int index = 0;
                do {
                    String id = cursor.getString(0);
                    Log.v(TAG, "date:" + cursor.getString(1));
                    if (index < diff) {
                        if (index == 0) {
                            whereClause.append("?");
                        } else {
                            whereClause.append(",?");
                        }
                        whereArgs[index] = id;
                        index++;
                    } else {
                        break;
                    }
                } while (cursor.moveToNext());
                cursor.close();
                // }
                whereClause.append(")");
                Log.v(TAG, "row affected" + writableDatabase.delete(IAudioData.TABLE_NAME,
                        whereClause.toString(), whereArgs));

                Log.v(TAG, exceptedAudioId + " @@DELETED ids" + Arrays.toString(whereArgs));
            }
        }
        Log.v(TAG, "@@DELETED " + diff);
        //delete AudioFormData corresponding
        if (whereArgs == null || whereArgs.length == 0) {
            return diff;
        }
        for (String audioId : whereArgs) {
            int row = writableDatabase.delete(
                    IAudioFormData.TABLE_NAME, IAudioFormData.COLUMN_AUDIO_DATA_ID + "=?",
                    new String[]{String.valueOf(audioId)});
            Log.d(TAG, "@@deleted from AudioFormData:" + row);
        }
        return diff;
    }

    /**
     * get list audio file name that need keeping
     *
     * @return list audio file name that need keeping
     */
    public String getListKeptAudioFileName() {
        String[] columns = new String[]{IAudioData.COLUMN_ID,
                IAudioData.COLUMN_RECORD_DATE, IAudioData.COLUMN_AUDIO_FILE_NAME, IAudioData.COLUMN_REPORT_ID_CLOUD};
        String orderBy = IAudioData.COLUMN_REPORT_ID_CLOUD + " ASC,";
        orderBy += IAudioData.COLUMN_RECORD_DATE + " DESC ";
        SQLiteDatabase writableDatabase = getWritableDatabase();
        Cursor cursor = writableDatabase.query(IAudioData.TABLE_NAME, columns, null, null, null, null, orderBy);
        if (cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst();
            int index = 0;
            StringBuilder listKeptStringBuilder = new StringBuilder();
            do {
                String audioId = cursor.getString(0);
                String audioFileName = cursor.getString(2);
                String cloudReportId = cursor.getString(3);
                Log.v(TAG, "@@CloudReportId:" + cloudReportId + "|" + audioFileName + "|" + index);
                //1. Check if position still in limited file keep when add to list keeping, other goto 2
                //2. Check if at position report hasn't uploaded to cloud when add to list keeping
                if (isInLimitedKeep(index) || !isUploadedToCloud(cloudReportId)) {
                    String audioName = "";
                    //再録音で音声ファイルが削除された場合の対応追加　160411 mit
                    if (audioFileName != null) {
                        audioName = audioFileName.substring(audioFileName.lastIndexOf(File.separator) + File.separator.length());
                        Log.v(TAG, "@@CloudReportId KEPT:" + cloudReportId + "|" + audioName + "|" + index);
                        listKeptStringBuilder.append(audioName).append(Utility.CHARACTERS_SEPARATE);
                    }
                }
                index++;
            } while (cursor.moveToNext());
            cursor.close();
            return listKeptStringBuilder.toString();
        }
        return Utility.EMPTY_STRING;
    }


    /**
     * Clear column audio_file_name when audio file is deleted
     *
     * @param fileName String
     * @return
     */
    public void clearAudioFileName(String fileName) {
        //TODO UPDATE audios SET audio_file_name = NULL WHERE audio_file_name like %fileName%
        SQLiteDatabase writableDatabase = getWritableDatabase();
        String whereClause = IAudioData.COLUMN_AUDIO_FILE_NAME + " LIKE '%" + fileName + "'";
        Log.d(TAG, "where clause:" + whereClause);
//        String[] whereArgs = {fileName};
//        https://code.google.com/p/android/issues/detail?id=56062
//        can' use whereArgs because android framework has an issued
        ContentValues contentValues = new ContentValues();
//        contentValues.putNull(IAudioData.COLUMN_AUDIO_FILE_NAME);
        contentValues.put(IAudioData.COLUMN_AUDIO_FILE_NAME, Utility.EMPTY_STRING);
        int rows = -1;
        try {
            writableDatabase.beginTransaction();
            rows = writableDatabase.update(IAudioData.TABLE_NAME, contentValues, whereClause, null);
            writableDatabase.setTransactionSuccessful();
        } catch (Exception ex) {
            Log.e(TAG, "Error when update audio average: ", ex);
        } finally {
            writableDatabase.endTransaction();
        }
        if (rows > 0) {
            Log.d(TAG, "clearAudioFileName success ");
        } else {
            Log.w(TAG, "clearAudioFileName failure ");
        }

    }

    /**
     * get list of audio data (audio data only)
     *
     * @param order: order
     * @param audioId: audios id
     * @return List<AudioData> list of audio data
     */

    /**
     * get all report miss input (result, cause or method)
     *
     * @return list audios
     */
    public List<AudioData> getReportMissInput() {
        List<AudioData> listAudioData;
        SQLiteDatabase writableDatabase = getWritableDatabase();

		/* GET AUDIO DATA */
        //String selection = new StringBuilder(IAudioData.COLUMN_CAUSE).append(" is null or ").
        //        append(IAudioData.COLUMN_METHOD).append(" is null or ").append(IAudioData.COLUMN_RESULT).append(" is null").toString();
        //チェック項目を「結果」のみに変更 160302 mitsuha
        String selection = new StringBuilder(IAudioData.COLUMN_RESULT).append(" is null").toString();

        String orderBy = IAudioData.COLUMN_RECORD_DATE + " DESC";
        Cursor cursor = null;
        try {
            cursor = writableDatabase.query(IAudioData.TABLE_NAME, null,
                    selection, null, null, null, orderBy);
        } catch (Exception ex) {
            Log.e(TAG, "Error when query database: ", ex);
        }
        listAudioData = getAudioFromCursor(cursor);

        return listAudioData;
    }

    private boolean isInLimitedKeep(int position) {
        return position < Utility.AUDIO_MAX_NUMBER_2;
    }

    private boolean isUploadedToCloud(String cloudReportId) {
        return !TextUtils.isEmpty(cloudReportId);
    }

    /**
     * Use for save database file to sdcard
     *
     * @return true if backup successful
     */
    public final boolean backupDatabase() {
        File from = mContext.getDatabasePath(DB_NAME);
        File to = this.createBackupDatabaseFile();
        try {
            FileUtils.copyFile(from, to);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error backuping up database: ", e);
        }
        return false;
    }

    /**
     * Create a backup database file
     *
     * @return file
     */
    public File createBackupDatabaseFile() {
        File dir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/backup");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, DB_NAME);
    }

    public interface SQL_CREATE_STATEMENT {
        String AUDIO_DATA_CREATE = "CREATE TABLE IF NOT EXISTS "
                + IAudioData.TABLE_NAME
                + "("
                + IAudioData.COLUMN_ID
                + " INTEGER primary key autoincrement, "
                + IAudioData.COLUMN_AVERAGE_FREQUENCY
                + " REAL null, "
                + IAudioData.COLUMN_AVERAGE_PERIOD
                + " REAL null, "
                + IAudioData.COLUMN_AUDIO_FILE_NAME
                + " TEXT null, "
                + IAudioData.COLUMN_RECORD_DATE
                + " NUMERIC not null, "
                + IAudioData.COLUMN_LATITUDE
                + " TEXT null, "
                + IAudioData.COLUMN_LONGITUDE
                + " TEXT null, "
                + IAudioData.COLUMN_REPORT_ID_CLOUD
                + " TEXT null, "
                + IAudioData.COLUMN_COMMENT_SEND
                + " TEXT null,"
                + IAudioData.COLUMN_ANALYSIS_IMAGE
                + " TEXT null,"
                + IAudioData.COLUMN_SELECTED_RECT
                + " TEXT null,"
                + IAudioData.COLUMN_CATALOG_ID
                + " NUMERIC null,"
                + IAudioData.COLUMN_CAUSE
                + " TEXT null, "
                + IAudioData.COLUMN_METHOD
                + " TEXT null, "
                + IAudioData.COLUMN_METHOD_DETAIL
                + " TEXT null, "
                + IAudioData.COLUMN_RESULT
                + " TEXT null, "
                + IAudioData.COLUMN_CAUSE_JSONFORM
                + " TEXT null, "
                + IAudioData.COLUMN_METHOD_JSONFORM
                + " TEXT null "
                + " );";

        String AUDIO_FORM_DATA_CREATE = "CREATE TABLE IF NOT EXISTS "
                + IAudioFormData.TABLE_NAME
                + "("
                + IAudioFormData.COLUMN_ID
                + " INTEGER primary key autoincrement, "
                + IAudioFormData.COLUMN_AUDIO_DATA_ID
                + " NUMERIC not null, "
                + IAudioFormData.COLUMN_FORMID
                + " TEXT not null, "
                + IAudioFormData.COLUMN_TEXT
                + " TEXT not null, "
                + IAudioFormData.COLUMN_VALUE + " TEXT not null );";
    }
}
