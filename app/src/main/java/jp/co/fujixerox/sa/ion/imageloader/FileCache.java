package jp.co.fujixerox.sa.ion.imageloader;

import android.content.Context;

import java.io.File;

import jp.co.fujixerox.sa.ion.utils.Utility;

public class FileCache {

    private File cacheDir;

    public FileCache(Context context) {
        //Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
//            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),Utility.CACHE_DIR);
            File dataFilesDir = context.getFilesDir();
            cacheDir = new File(dataFilesDir, Utility.CACHE_DIR);
        } else
            cacheDir = context.getCacheDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();
    }

    public File getFile(String url) {
        //I identify images by hashcode. Not a perfect solution, good for the demo.
        String filename = String.valueOf(url.hashCode());
        File f = new File(cacheDir, filename);
        return f;

    }


    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files == null)
            return;
        for (File f : files)
            f.delete();
    }

}