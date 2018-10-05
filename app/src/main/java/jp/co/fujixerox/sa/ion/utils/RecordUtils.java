package jp.co.fujixerox.sa.ion.utils;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

/**
 * Recording utility class
 *
 * Created by TrungKD
 */
public class RecordUtils {
    private static final String TAG = RecordUtils.class.getSimpleName();
    private static final long BYTE_RATE = Utility.RECORDER_BPP
            * Utility.RECORDER_SAMPLERATE * 1 / 8;
    private static final long SAMPLE_AUDIO_LENGTH = BYTE_RATE * 9;
    private static final int CHANNELS = 1;
    private static AudioRecord recorder = null;
    private static int bufferSize = 0;
    private static LinkedList<byte[]> bufferAudio = null;
    private static long totalAudioLen = 0; // PCM data only
    private static long totalDataLen = 0; // PCM data + Header
    private static byte data[];

    /**
     * Define AudioRecord
     *
     * @return AudioRecord
     */
    public static AudioRecord findAudioRecord() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        try {
            Log.i(TAG, "Attempting rate " + Utility.RECORDER_SAMPLERATE + "Hz");
            bufferSize = AudioRecord.getMinBufferSize(
                    Utility.RECORDER_SAMPLERATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            Log.v(TAG, "BUFFER SIZE: " + bufferSize);

            if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                AudioRecord recorder = new AudioRecord(AudioSource.DEFAULT,
                        Utility.RECORDER_SAMPLERATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, bufferSize);

                if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                    return recorder;
            }
        } catch (Exception e) {
            Log.e(TAG, Utility.RECORDER_SAMPLERATE + "Exception:", e);
        }
        return null;
    }

    /**
     * Start recording
     */
    public static boolean startRecording() {
        boolean startResult = false;
        recorder = findAudioRecord();
        if (recorder != null) {
            int state = recorder.getState();
            if (state == AudioRecord.STATE_INITIALIZED) {
                startResult = true;
                // 44100 samples (frames) are taken every second
                recorder.setPositionNotificationPeriod(Utility.RECORDER_SAMPLERATE);
//                recorder.setRecordPositionUpdateListener(callback);
                recorder.startRecording();
            } else {
                Log.e(TAG, state + " when initializing native AudioRecord object.");
            }
        } else {
            Log.e(TAG, " when initializing native AudioRecord object.");
        }
        return startResult;
    }

    /**
     * Read bytes data from recorder stream while loop to stop record
     *
     * @return byte[]
     */
    public static void readRecordStream() {
        Log.v(TAG, "START RECORDING: " + System.currentTimeMillis());

        int nineSecondBlockAudioLength = (int) (SAMPLE_AUDIO_LENGTH / bufferSize) + 1;
        int fourSecondBlockAudioLength = nineSecondBlockAudioLength / 2;
        Log.i(TAG, "@@HAFT LENGTH: " + fourSecondBlockAudioLength);
        resetAudioBuffer(bufferSize, fourSecondBlockAudioLength);
        data = new byte[bufferSize];
        int read;
        while (true) {
            read = recorder.read(data, 0, bufferSize);
            if (read > 0) {
                byte[] copy = Arrays.copyOf(data, read);
                totalAudioLen += read;
                if (bufferAudio != null) {
                    bufferAudio.add(copy);
                } else {
                    Log.e(TAG, "buffer audio is null");
                    break;
                }
                if (bufferAudio.size() >= nineSecondBlockAudioLength) {
                    byte[] pop = bufferAudio.pop();
                    if (pop != null) {
                        totalAudioLen -= pop.length;
                    }
                }
            } else {
                // Stop recording save file audio in file
                break;
            }
        }
        Log.v(TAG, "READ BLOCK COUNT: " + bufferAudio.size());
        Log.v(TAG, "STOP RECORDING: " + System.currentTimeMillis());
    }

    /**
     * fill salient data into buffer audio before record
     *
     * @param buffer_size
     * @param blockAudioLength
     */
    private static void resetAudioBuffer(int buffer_size, int blockAudioLength) {
        totalAudioLen = 0;
        bufferAudio = new LinkedList<>();
        byte[] silence = new byte[buffer_size];
        Arrays.fill(silence, (byte) 0);
        for (int i = 0; i < blockAudioLength; i++) {
            bufferAudio.add(silence);
            totalAudioLen += buffer_size;
        }
    }

    /**
     * Write buffer data to file
     */
    public static String writeFile(Context context) {
        String filename = getFilename(context);
        try {
            FileOutputStream os;
            try {
                os = new FileOutputStream(filename);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "@@ cant get file", e);
                return null;
            }
            /* WRITE HEADER */
            totalDataLen = totalAudioLen + 36;
            writeWaveFileHeader(os, totalAudioLen, totalDataLen,
                    Utility.RECORDER_SAMPLERATE, CHANNELS, BYTE_RATE);

			/* WRITE DATA PCM */
            Log.v(TAG, "@@@Buffer audio: " + bufferAudio.size());
            while (bufferAudio.size() > 0) {
                try {
                    byte[] return_pop = bufferAudio.pop();
                    if (return_pop != null) {
                        os.write(return_pop);
                    }
                    os.flush();
                } catch (IOException e) {
                    Log.e(TAG, "error cant write block", e);
                }
            }
            bufferAudio.clear();
            recorder.release();
            os.close();
        } catch (IOException e) {
            Log.e(TAG, "Error when close output stream", e);
            return null;
        }

        return filename;
    }

    /**
     * stop recording if filename has exist when store wave file
     */
    public static void stopRecording() {
        if (null != recorder) {
            int state = recorder.getState();
            if (state == AudioRecord.STATE_INITIALIZED) {
                recorder.stop();
            } else {
                Log.e(TAG, "invalid state:" + state);
            }
        } else {
            Log.e(TAG, " recorder is null");
        }
    }

    /**
     * Get File Path
     *
     * @return Path
     * @throws IOException
     */
    private static String getFilename(Context context) {

        try {
//			FileUtils.createFolder(Utility.PATH_SDCARD + File.separator
//					+ Utility.AUDIO_RECORDER_FOLDER);
            FileUtils.createFolder(context.getExternalFilesDir(null) + File.separator
                    + Utility.AUDIO_RECORDER_FOLDER);
        } catch (IOException e1) {
            Log.e(TAG, "Error when create folder ", e1);
        }

        return (context.getExternalFilesDir(null)
//				+ Utility.PATH_SDCARD
                + File.separator
                + Utility.AUDIO_RECORDER_FOLDER
                + File.separator
                + new SimpleDateFormat(Utility.DATE_PATTERN_AUDIO)
                .format(new Date()) + Utility.FILE_EXT_WAV);
    }

    /**
     * get audio recorder folder
     * @param context: Context
     * @return audio recorder folder
     */
    public static String getAudioRecorderFolder(Context context) {
        return context.getExternalFilesDir(null) + File.separator
                + Utility.AUDIO_RECORDER_FOLDER;
    }


    /**
     * Write Wave File Header
     *
     * @param out
     * @param totalAudioLen
     * @param totalDataLen
     * @param longSampleRate
     * @param channels
     * @param byteRate
     * @throws IOException
     */
    public static void writeWaveFileHeader(FileOutputStream out,
                                           long totalAudioLen, long totalDataLen, long longSampleRate,
                                           int channels, long byteRate) throws IOException {
        Log.d(TAG, "totalAudioLen=" + totalAudioLen);
        Log.d(TAG, "totalDataLen=" + totalDataLen);
        Log.d(TAG, "longSampleRate=" + longSampleRate);
        Log.d(TAG, "channels=" + channels);
        Log.d(TAG, "BYTE_RATE=" + byteRate);

        byte[] header = new byte[44];

        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);
        header[33] = 0;
        header[34] = Utility.RECORDER_BPP;
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
        out.flush();
    }

}
