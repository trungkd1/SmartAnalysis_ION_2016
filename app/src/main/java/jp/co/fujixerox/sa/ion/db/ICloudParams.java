package jp.co.fujixerox.sa.ion.db;

public interface ICloudParams {
    String reportid = "reportid";
    String productgroup = "productgroup"; //local only
    String productname = "productname";
    String serialid = "serialid";
    String record_date = "record_date";
    String type = "type"; //異音の種類
    String cause = "cause";
    String method = "method";
    String method_detail = "method_detail";
    String picture = "picture";
    String sound = "sound";
    String catalogid = "catalogid";
    String result = "result";
    String areacode = "areacode";
    String frequency = "frequency";
    String period = "period";
    String dummy_sound = "dummy_sound";
    String category = "category"; // 異音表現カテゴリ
    String condition = "condition";
    String color = "color";
    String output_type = "output_type";
    String output_size = "output_size";
    String original_type = "original_type";
    String original_size = "original_size";
    String output = "output";
    String latitude = "latitude";
    String longitude = "longitude";
    String comment = "comment";
    String accountid = "accountid";
    String start = "start"; //起動の時
    String select_point = "select_point"; //json string
    String other = "other"; //json string

    String[] conditionInputParams = new String[]{condition, color, output_type, output_size, original_type, original_size, start};
    /**
     * Params for Catalog screen
     */
    String[] catalogParams = new String[]{productname, type, cause, areacode, frequency, period, category, color, output_type, output_size, original_type, original_size, output};
    /**
     * Params for Compare screen
     */
    String[] catalogParams2 = new String[]{productname, type, areacode, frequency, period, category, color, output_type, output_size, original_type, original_size, output};
    /**
     * Show formIds for Catalog screen
     */
    String[] catalogFormIds = new String[] {productgroup, productname,type, category,cause};
    /**
     * Show formIds for Report screen
     */
    String[] reportFormIds = new String[] {serialid,areacode,productgroup, productname, output, condition, comment, result};
    /**
     * Show formIds for Record screen
     */
    String[] recordFormIds = new String[] {serialid, areacode, productgroup, productname, output, condition, comment};
}

