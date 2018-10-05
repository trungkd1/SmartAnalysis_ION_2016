package jp.co.fujixerox.sa.ion.utils;

import android.content.res.AssetManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.co.fujixerox.sa.ion.db.ICloudParams;
import jp.co.fujixerox.sa.ion.entities.CatalogList;
import jp.co.fujixerox.sa.ion.entities.Item;
import jp.co.fujixerox.sa.ion.entities.Value;

public class JsonParser {

    private static final String TAG = JsonParser.class.getSimpleName();
    public static final Gson gson = new Gson();
    public static Type LIST_TYPE = new TypeToken<List<String>>() {}.getType();
    /**
     * get CatalogList object from inputStream response from cloud
     *
     * @param content
     * @return
     */
    public static CatalogList getCatalogList(InputStream content) {
        try {
            CatalogList templateList = gson.fromJson(
                    FileUtils.readFully(content, Utility.ENCODING), CatalogList.class);
            return templateList;
        } catch (Exception ex) {
            Log.e(TAG, "Error", ex);
        } finally {
            try {
                content.close();
            } catch (IOException e) {
                Log.e(TAG, "Error", e);
            }
        }
        return null;
    }

    /**
     * get items input from file json store in asset
     *
     */
    public static Item getItem(String assetPath, String fileName, AssetManager assetManager) {
        InputStream content = null;
        Item item = null;
        try {
            content = FileUtils.readStreamFromAsset(assetPath, fileName, assetManager);
            item = gson.fromJson(FileUtils.readFully(content, Utility.ENCODING), Item.class);
        } catch (Exception ex) {
            Log.e(TAG, "Error", ex);
        } finally {
            FileUtils.closeStream(content);
        }
        return item;
    }

    /**
     * get items input from file json store in asset
     *
     */
    public static List<Item> getListItems(String assetPath, String fileName, AssetManager assetManager) {
        InputStream content = null;
        List<Item> items = null;
        try {
            content = FileUtils.readStreamFromAsset(assetPath, fileName, assetManager);
            Type type = new TypeToken<ArrayList<Item>>() {
            }.getType();
            items = gson.fromJson(FileUtils.readFully(content, Utility.ENCODING), type);
        } catch (Exception ex) {
            Log.e(TAG, "Error", ex);
        } finally {
            FileUtils.closeStream(content);
        }
        return items;
    }

    /**
     * Make json string from ArrayList<String> object
     * @param list ArrayList<String>
     * @return string of json
     */
    public static String makeJsonStringArray(List<String> list) {
        Type type = new TypeToken<List<String>>() {
        }.getType();
        String result = gson.toJson(list, type);
        return result;
    }


    /**
     * Make json string from ArrayList<String> object
     * @param list ArrayList<String>
     * @return string of json
     */
    public static String makeJsonStringArray(String... list) {
        return makeJsonStringArray(Arrays.asList(list));
    }
    /**
     * Make ArrayList<String> object from json string
     * @param json string of json
     * @return ArrayList<String> object
     */
    public static ArrayList<String> makeArrayListOfString(String json) {
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        ArrayList<String> result = gson.fromJson(json, type);
        return result;
    }


    /**
     * Find productgroup from productname
     */
    public static String findProductgroup(String productName, AssetManager assetManager) {
        String productGroupName = null;
//        Item itemProductGroup = getItem(Utility.ASSETS_JSON_PATH, Utility.JSON_FILE_NAME.PRODUCTS, assetManager);

        List<Item> Items = getListItems(Utility.ASSETS_JSON_PATH, Utility.JSON_FILE_NAME.PRODUCTS, assetManager);
        Item itemProductGroup = new Item();
        for(Item item :Items){
            if(ICloudParams.productgroup.equals(item.getFormid())){
                itemProductGroup = item;
            }
        }
        if (itemProductGroup == null) {
            return "" ;
        }
        List<Value> valueList = itemProductGroup.getListvalue();
        outer:
        for (Value productGroupValue :
                valueList) {
            List<Item> itemList = productGroupValue.getItems();
            for (Item item :
                    itemList) {
                List<Value> productNameList = item.getListvalue();
                for (Value productValue :
                        productNameList) {
                    if (productValue.getValue().equals(productName)) {
                        productGroupName = productGroupValue.getValue();
                        break outer;
                    }
                }
            }
        }
        return productGroupName;
    }
}
