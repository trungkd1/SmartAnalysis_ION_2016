package jp.co.fujixerox.sa.ion.sync;

/**
 * Class: the event listener for the get catalog processing
 */
public interface AsyncTaskCallback {

    enum PROGRESS_TYPE {
        FIRST_LOADING, LOADING_MORE, NONE
    }

    void onPrepare(PROGRESS_TYPE type);

    void onSuccess(Object object);

    void onFailed(int errorMessageId);

    void onFinish(PROGRESS_TYPE loadingType);
}
