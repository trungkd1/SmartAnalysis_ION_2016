package jp.co.fujixerox.sa.ion.db;

public interface IAudioData {

    String TABLE_NAME = "audios";// name of table to store audio recording condition
    String COLUMN_ID = "id"; // id field
    String COLUMN_RECORD_DATE = "record_date"; // time recording date
    String COLUMN_AUDIO_FILE_NAME = "audio_file_name"; // audio file name
    String COLUMN_AVERAGE_PERIOD = "average_period"; // average period in ms
    String COLUMN_AVERAGE_FREQUENCY = "average_frequency"; // average frequency in Hz
    String COLUMN_LATITUDE = "latitude"; // latitude
    String COLUMN_LONGITUDE = "longitude"; //longitude
    String COLUMN_COMMENT_SEND = "comment_send"; //comment
    String COLUMN_ANALYSIS_IMAGE = "analysis_image"; // analysis image
    String COLUMN_REPORT_ID_CLOUD = "report_id_cloud"; // report id from cloud
    String COLUMN_SELECTED_RECT = "selected_rect"; // selected area
    String COLUMN_CATALOG_ID = "catalog_id"; // catalog id
    String COLUMN_CAUSE = "cause"; // catalog cause
    String COLUMN_METHOD = "method"; // catalog method
    String COLUMN_METHOD_DETAIL = "methodDetail"; // catalog method detail
    String COLUMN_RESULT = "result"; // catalog fix
    String COLUMN_CAUSE_JSONFORM = "cause_jsonform"; // catalog cause input (internal use only)
    String COLUMN_METHOD_JSONFORM = "method_jsonform"; // catalog method input (internal use only)

}
