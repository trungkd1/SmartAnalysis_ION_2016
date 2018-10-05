package jp.co.fujixerox.sa.ion.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.co.fujixerox.sa.ion.interfaces.IBitmapDisplay;

public class ImageLoader {

    private static final String TAG = ImageLoader.class.getSimpleName();

    MemoryCache memoryCache = new MemoryCache();
    FileCache fileCache;
    private Map<IBitmapDisplay, String> imageViews = Collections
            .synchronizedMap(new WeakHashMap<IBitmapDisplay, String>());
    ExecutorService executorService;
    Handler handler = new Handler();// handler to display images in UI thread

    public ImageLoader(Context context) {
        try {
            memoryCache.clear();
        } catch (Exception ex) {
            Log.v(TAG, "Failed when clear image cache");
        }
        fileCache = new FileCache(context);
        executorService = Executors.newFixedThreadPool(5);
    }

    /**
     * Display image with axisImageView
     * @param url
     * @param imageView
     */
    public void displayImage(String url, IBitmapDisplay imageView) {
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);
        if (bitmap != null) {
            imageView.setBitmapDisplay(bitmap);
        } else {
            queuePhoto(url, imageView);
        }
    }

    private void queuePhoto(String url, IBitmapDisplay imageView) {
        PhotoToLoad p = new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }


    private Bitmap getBitmap(String url) {
        File f = fileCache.getFile(url);

        //from SD cache
        Bitmap b = Utils.decodeFile(f);
        if (b != null)
            return b;

        //from web
        try {
            Bitmap bitmap = null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            bitmap = Utils.decodeFile(f);
            return bitmap;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

    }

    // Task for the queue
    private class PhotoToLoad {
        public String url;
        public IBitmapDisplay imageView;

        public PhotoToLoad(String url, IBitmapDisplay imageView) {
            this.url = url;
            this.imageView = imageView;
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;

        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            try {
                if (imageViewReused(photoToLoad))
                    return;
                Bitmap bmp = getBitmap(photoToLoad.url);
                memoryCache.put(photoToLoad.url, bmp);
                if (imageViewReused(photoToLoad))
                    return;
                BitmapDisplay bd = new BitmapDisplay(bmp, photoToLoad);
                handler.post(bd);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    boolean imageViewReused(PhotoToLoad photoToLoad) {
        String tag = imageViews.get(photoToLoad.imageView);
        return tag == null || !tag.equals(photoToLoad.url);
    }

    // Used to display bitmap in the UI thread
    class BitmapDisplay implements Runnable {
        private Bitmap bitmap;
        private PhotoToLoad photoToLoad;

        public BitmapDisplay(Bitmap b, PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }

        public void run() {
            if (imageViewReused(photoToLoad))
                return;
            if (bitmap != null) {
                photoToLoad.imageView.setBitmapDisplay(bitmap);
            }
        }
    }

    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }

}
