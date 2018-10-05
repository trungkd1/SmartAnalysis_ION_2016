package jp.co.fujixerox.sa.ion.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

/**
 * class FileUtils
 */
public class FileUtils {
    /**
     * Delete file or folder.
     *
     * @param path
     * .
     * @return TRUE if delete successfully, otherwise return FALSE.
     */

    private static final String TAG = FileUtils.class.getSimpleName();

    static public boolean deleteFileOrFolder(File path) {
        try {
            if (path.exists()) {
                if (path.isFile()) {
                    path.delete();

                } else {
                    File[] files = path.listFiles();
                    if (files == null) {
                        return true;
                    }
                    for (File file : files) {
                        if (file.isDirectory()) {
                            deleteFileOrFolder(file);
                        } else {
                            file.delete();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error", e);
        }
        return (path.delete());
    }

    /**
     * read stream from file path
     *
     * @param path
     * @return
     */
    public static InputStream readStreamFromFilePath(String path) {
        Log.v(TAG, "FILE PATH: " + path);
        InputStream input = null;
        File source = new File(path);
        try {
            try {
                input = new FileInputStream(source);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Error", e);
            }
            return input;
        } finally {
            closeStream(input);
        }
    }

    /**
     * download File from cloud uri
     *
     * @param path
     * @param url
     * @return
     */
    public static String downloadFileFromUrl(File path, String url) {
        InputStream inputStream = null;
        String fileName = null;
        File dest = null;
        HttpResponse response;
        try {
            HttpGet httpGet = CloudConnector.createHttpGet(url);
            // Execute HTTP Get Request
            response = CloudConnector.executeRequest(httpGet);
            Log.v(TAG, "response for downloadFileFromUrl is null ="
                    + (response == null));
            if (response != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    inputStream = response.getEntity().getContent();
                    Header header = response
                            .getFirstHeader("Content-Disposition");
                    fileName = extractingFileNameFromContentDisposition(header
                            .getValue());
                    Log.i(TAG, "fileName: " + fileName);
                }
                Log.v(TAG, "Status code downloadFileFromUrl:" + statusCode);

            }
        } catch (Exception ex) {
            Log.e(TAG, "downloadImageStream error", ex);
        }

        try {
            if (fileName == null || fileName.equals("")) {
                return null;
            }
            dest = new File(path, fileName);
            if (inputStream != null) {
                copyFileUsingFileStreams(inputStream, dest);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error", e);
            }
        }

        try {
            if (dest != null && dest.exists()) {
                return dest.toString();
            }
            // } else {
            // return null;
            // }
        } catch (Exception fx) {
            fx.printStackTrace();
        }
        return null;

    }

    /**
     * get filename from ContentDisposition of response header
     *
     * @param raw
     * @return
     */
    private static String extractingFileNameFromContentDisposition(String raw) {
        String defaultFileName = System.currentTimeMillis() + "";
        if (raw != null && raw.contains("filename")) {
            raw = raw.replaceAll("\"", " ").replaceAll("=", " ")
                    .replaceAll("  ", " ");
            String[] portions = raw.split(" ");
            if (portions.length > 1) {
                defaultFileName = portions[portions.length - 1];
            }
        }
        return defaultFileName;
    }

    /**
     * Create new folder.
     *
     * @return boolean.
     * @throws Exception
     */
    public static boolean createFolder(String path) throws IOException {
        File folder = new File(path);
        return folder.exists() || folder.mkdirs();
    }

    /**
     * Create new file.
     *
     * @return boolean.
     * @throws Exception
     */
    public static boolean createFile(String path) throws IOException {
        File file = new File(path);
        return !file.exists() || file.delete();
    }

    /**
     * create folder to store catalog sample download from cloud
     *
     * @param dataFilesDir
     * @return
     */
    public static String createFolderCatalog(File dataFilesDir) {
        String folderStorePath = dataFilesDir.getAbsolutePath()
                + File.separator + Utility.APP_CATALOG_FOLDER_NAME;
        try {
            if (createFolder(folderStorePath)) {
                return folderStorePath;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error", e);
        }
        return null;
    }

    /**
     * get report template file name
     * @param context: Context
     * @param fileName: template file name
     * @return report template file path
     */
    public static String getReportTemplateFilename(Context context, String fileName) {
        try {
            FileUtils.createFolder(context.getExternalFilesDir(null) + File.separator
                    + Utility.REPORT_TEMPLATE_FOLDER);
        } catch (IOException e1) {
            Log.e(TAG, "Error when create template folder ", e1);
        }
        return (context.getExternalFilesDir(null)
                + File.separator
                + Utility.AUDIO_RECORDER_FOLDER
                + File.separator
                + fileName);
    }

    /**
     * Check device uses SD card or not.
     *
     * @return Boolean.
     * @throws Exception
     */
    public static boolean isSDCardAvailable() throws Exception {
        return android.os.Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * copy 2 files
     *
     * @param source
     * @param dest
     * @throws IOException
     */
    public static void copyFileUsingFileStreams(File source, File dest)
            throws IOException {
        InputStream input = null;
        try {
            input = new FileInputStream(source);
            copyFileUsingFileStreams(input, dest);
        } finally {
            closeStream(input);
        }
    }

    /**
     * copy from input stream
     *
     * @param input
     * @param dest
     * @throws IOException
     */
    public static void copyFileUsingFileStreams(InputStream input, File dest)
            throws IOException {
        OutputStream output = null;
        try {
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
            output.flush();
        } finally {
            closeStream(input);
            closeStream(output);
        }
    }

    /**
     * Copy file from InputStream to OutputStream
     * @param in
     * @param out
     * @throws IOException
     */
    public static void copyFileStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    /**
     * Read file from asset to InputStream
     * @param assetPath
     * @param fileName
     * @param assetManager
     * @return
     */
    public static InputStream readStreamFromAsset(String assetPath,
                                                  String fileName, AssetManager assetManager) {
        InputStream in = null;
        try {
            in = assetManager.open(String.format("%s/%s", assetPath, fileName));
        } catch (IOException e) {
            Log.e(TAG, "Failed to read stream asset file: " + fileName, e);
        }
        return in;
    }

    private static byte[] readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toByteArray();
    }

    public static String readFully(InputStream content, String encoding) {
        try {
            String a = new String(readFully(content),encoding);
            //Log.d(TAG,a);  //Log除去 160510 mit
            //return new String(readFully(content), encoding);
            return a;
        } catch (IOException e) {
            Log.e(TAG, "Error", e);
            return null;
        }

    }

    public static void closeStream(Closeable stream) {
        if (stream == null) {
            return;
        }
        try {
            stream.close();
        } catch (Exception ex) {
            Log.e(TAG, "Error", ex);
        }
    }

    /**
     * create a file image from bitmap
     *
     * @param bmp Bitmap
     * @return file path of image or null if can't create image
     */
    public static String saveAnalysisBitmapToFile(File imageFilePath, Bitmap bmp) {
        FileOutputStream fileOutputStream = null;
        String filePath = null;
        try {
            // create a File object for the parent directory
            fileOutputStream = new FileOutputStream(imageFilePath);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream); // bm
            // is
            // the
            // bitmap
            // object
            byte[] bsResized = byteArrayOutputStream.toByteArray();
            fileOutputStream.write(bsResized);
            filePath = imageFilePath.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "IO Error", e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "IO Error", e);
                }
            }
        }
        return filePath;
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    /**
     * Copy a file
     *
     * @param src
     * @param dst
     * @throws IOException
     */
    public static void copyFile(File src, File dst) throws IOException {
        if (!src.exists()) return;
        FileInputStream in = new FileInputStream(src);
        FileOutputStream out = new FileOutputStream(dst);
        FileChannel fromChannel = null, toChannel = null;
        try {
            fromChannel = in.getChannel();
            toChannel = out.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            if (fromChannel != null)
                fromChannel.close();
            if (toChannel != null)
                toChannel.close();
            in.close();
            out.close();
        }
    }

    /**
     * copy file in asset path to folder in local
     * @param assetManager: Asset manager
     * @param assetPath: Asset file path
     * @param fileDestination: File destination in local
     */
    public static void copyAssets(AssetManager assetManager, String assetPath, File fileDestination) {
        String[] files = null;
        try {
            files = assetManager.list(assetPath);
        } catch (IOException ex) {
            Log.e(TAG, "@@Error when list file in asset path: " + assetPath, ex);
        }
        if (files == null || files.length == 0) {
            return;
        }
        for (String fileName : files) {
            InputStream in;
            OutputStream out;
            try {
                in = assetManager.open(String.format("%s/%s", assetPath,
                        fileName));
                File outFile = new File(fileDestination, fileName);

                if (!outFile.exists()) {
                    boolean result = outFile.createNewFile();
                    Log.v(TAG, "create file is " + result);
                }
                out = new FileOutputStream(outFile);
                FileUtils.copyFileStream(in, out);
                in.close();
                out.flush();
                out.close();
            } catch (IOException ex) {
                Log.e(TAG, "@@Error to copy asset file: " + fileName, ex);
            }
        }

    }
}
