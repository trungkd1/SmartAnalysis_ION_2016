package jp.co.fujixerox.sa.ion.fragments;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.adapters.SwipeImageAdapter;
import jp.co.fujixerox.sa.ion.entities.Catalog;
import jp.co.fujixerox.sa.ion.imageloader.ImageLoader;
import jp.co.fujixerox.sa.ion.interfaces.ICatalogViewFragment;
import jp.co.fujixerox.sa.ion.interfaces.ICompareScreenActivity;
import jp.co.fujixerox.sa.ion.sync.AsyncTaskCallback;
import jp.co.fujixerox.sa.ion.sync.DownloadSampleSoundTask;
import jp.co.fujixerox.sa.ion.utils.CommonUtils;
import jp.co.fujixerox.sa.ion.utils.FileUtils;
import jp.co.fujixerox.sa.ion.utils.Utility;
import jp.co.fujixerox.sa.ion.views.MediaPlayerController;
import jp.co.fujixerox.sa.ion.views.ViewPagerIndicator;

import static jp.co.fujixerox.sa.ion.views.MediaPlayerController.AudioAction.PLAY;
import static jp.co.fujixerox.sa.ion.views.MediaPlayerController.AudioAction.STOP;

/**
 * Fragment show Catalog information (Sample Image, Sound, Method, Cause Parts)
 */
public class CatalogViewFragment extends AbstractFragment implements ICatalogViewFragment {
    public static final String TAG = CatalogViewFragment.class.getSimpleName();
    /**
     * catalog list
     */
    private List<Catalog> mCatalogList;
    /**
     * {@link ViewPagerIndicator}
     */
    private ViewPagerIndicator pagerCatalog;
    /**
     * layout catalog indicator
     */
    private LinearLayout lntCatalogIndicator;
    /**
     * button play catalog audio
     */
    private ToggleButton btnPlayCatalogAudio;
    /**
     * TextView show cause of catalog
     */
    private TextView tvCause;
    /**
     * TextView show information of catalog
     */
    private TextView tvMethod;
    /**
     * current selected page position, default is 0
     */
    private int mCurrentSelectedPage = 0;
    /**
     * hash store catalog audio
     */
    private HashMap<Integer, String> mMapCatalog;

    /**
     * {@link DownloadSampleSoundTask}
     */
    private DownloadSampleSoundTask downloadSampleSoundTask;
    /**
     * directory for save catalog file
     */
    private File saveCatalogDirectory;

    /**
     * {@link MediaPlayerController}
     */
    private MediaPlayerController mMediaPlayerController;
    /**
     * {@link ImageLoader}
     */
    private ImageLoader mImageLoader;

    private SwipeImageAdapter swipeImageAdapter;
    private View progressBarSmall;
    /**
     * catalog cause
     */
    private ArrayList<String> catalogCauseParts;
    /**
     * catalog methods
     */
    private ArrayList<String> catalogMethods;
    /*
     * catalog methods deteil mit
     */
    private ArrayList<String> catalogMethodsList;
    /**
     * Current is showing catalog
     */
    private int currentCatalogPosition;

    /**
     * Call back instance to CompareScreenActivity
     */
    private ICompareScreenActivity compareScreenActivityListener;


    public CatalogViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CatalogViewFragment.
     */
    public static CatalogViewFragment newInstance(List<Catalog> catalogList) {
        CatalogViewFragment fragment = new CatalogViewFragment();
        fragment.setAudioFormDataList(catalogList);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageLoader = new ImageLoader(getContext());
        initCatalogFolder();
        mMapCatalog = new HashMap<>();

    }

    private void setAudioFormDataList(List<Catalog> catalogList) {
        this.mCatalogList = catalogList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View containerView = inflater.inflate(R.layout.fragment_catalog_view, container, false);
        //setOnCompletedListener view: find view by res id
        initView(containerView);
        if (mCatalogList != null && !mCatalogList.isEmpty()) {
            setCausesAndMethods(mCatalogList);
            //populate data to view pager indicator
            populateDataToViewPagerIndicator();
        } else {
            Log.e(TAG, "mCatalogList is empty");
        }
        return containerView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ICompareScreenActivity) {
            compareScreenActivityListener = (ICompareScreenActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * setOnCompletedListener all view by resource id
     *
     * @param containerView: container view
     */
    private void initView(View containerView) {
        //progress loading catalog
        pagerCatalog = (ViewPagerIndicator) containerView.findViewById(R.id.pager_catalog);
        lntCatalogIndicator = (LinearLayout) containerView.findViewById(R.id.layout_catalog_indicator);
        btnPlayCatalogAudio = (ToggleButton) containerView.findViewById(R.id.btn_play_catalog_audio);
        progressBarSmall = containerView.findViewById(R.id.ProgressBarSmall);
        btnPlayCatalogAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnPlayCatalogAudio.isChecked()) {
                    Log.v(TAG, "start seek");
                    swipeImageAdapter.getCurrentItem(pagerCatalog.getCurrentItem())
                            .startSeek(Utility.AUDIO_DURATION, null);
                    mMediaPlayerController.onClick(PLAY);
                    mMediaPlayerController.setOnCompletedListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            Log.v(TAG, "play complete");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btnPlayCatalogAudio.setChecked(false);
                                }
                            });
                        }
                    });
                } else {
                    mMediaPlayerController.onClick(STOP);
                    swipeImageAdapter.getCurrentItem(pagerCatalog.getCurrentItem())
                            .stopSeek();
                }
            }
        });

        btnPlayCatalogAudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (compareScreenActivityListener != null) {
                        compareScreenActivityListener.onCatalogAudioPlaying();
                    }
                } else {
                    if (compareScreenActivityListener != null) {
                        compareScreenActivityListener.onAudioPlayingFinish();
                    }
                }
            }
        });
        tvCause = (TextView) containerView.findViewById(R.id.tv_cause);
        tvMethod = (TextView) containerView.findViewById(R.id.tv_Method);
    }

    /**
     * return catalog Cause Parts and catalog method from catalog data
     */
    public void setCausesAndMethods(List<Catalog> catalogList) {
        if (catalogList == null || catalogList.isEmpty()) {
            return;
        }
        catalogCauseParts = new ArrayList<>();
        catalogMethods = new ArrayList<>();

        for (Catalog catalog : catalogList) {
            if (!catalogCauseParts.contains(catalog.getCause()))
                catalogCauseParts.add(catalog.getCause());
            if (!catalogMethods.contains(catalog.getMethod()))
                catalogMethods.add(catalog.getMethod());
        }
    }

    /**
     * populate data to view pager indicator area
     */
    private void populateDataToViewPagerIndicator() {
        swipeImageAdapter = new SwipeImageAdapter(getContext(), mCatalogList, mImageLoader);
        pagerCatalog.setAdapter(swipeImageAdapter);
        //set catalog content at first position
        updateCatalogContent();
        //set pager indicator
        pagerCatalog.setPagerIndicator(lntCatalogIndicator, mCatalogList.size(), new ViewPagerIndicator.OnPageIndicatorChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Do nothing
            }

            @Override
            public void onPageSelected(int position) {
                btnPlayCatalogAudio.setChecked(false);
                swipeImageAdapter.getCurrentItem(pagerCatalog.getCurrentItem())
                        .stopSeek();
                if (mMediaPlayerController != null) {
                    mMediaPlayerController.onClick(STOP);
                }
                //update current selected catalog
                mCurrentSelectedPage = position;
                setCurrentCatalogPosition(position);
                //update catalog content at position
                updateCatalogContent();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Do nothing
            }
        });
    }

    /**
     * update catalog content at position
     */
    private void updateCatalogContent() {
        setEnableButtonPlayRelatedToAnalysisPlayButton();
        //populate data to catalog info area
        showCauseAndMethod();
        //retrieve audio
        if (hasSound()) {
            downloadAudioCatalog();
        } else {
            CommonUtils.showToast(getContext(), R.string.no_sound, Gravity.TOP);
            enableButtonPlayCatalog(false);
            progressBarSmall.setVisibility(View.INVISIBLE);
        }
    }

    public boolean hasSound() {
        return !TextUtils.isEmpty(mCatalogList.get(mCurrentSelectedPage).getSample_sound());
    }

    /**
     * retrieve audio catalog at current position
     */
    private void downloadAudioCatalog() {
        //cancel current retrieve audio task
        cancelDownloadSoundTask();
        //if catalog audio at position has exist when return now
        if (isCatalogAudioExist(mCurrentSelectedPage)) {
            String catalogAudioPath = mMapCatalog.get(mCurrentSelectedPage);
            if (mMediaPlayerController == null) {
                mMediaPlayerController = new MediaPlayerController(getContext(), catalogAudioPath);
            } else {
                mMediaPlayerController.changeAudio(catalogAudioPath);
            }
            setEnableButtonPlayRelatedToAnalysisPlayButton();
        } else {
            //setOnCompletedListener retrieve audio task
            downloadSampleSoundTask = new DownloadSampleSoundTask(saveCatalogDirectory, new AsyncTaskCallback() {
                @Override
                public void onPrepare(PROGRESS_TYPE type) {
                    enableButtonPlayCatalog(false);
                    showProgress(true);
                }

                @Override
                public void onSuccess(Object object) {
                    showProgress(false);
                    String catalogAudioPath = (String) object;
                    Toast.makeText(getContext(), "Retrieve audio catalog successful", Toast.LENGTH_SHORT).show();
                    mMapCatalog.put(mCurrentSelectedPage, catalogAudioPath);
                    //enable button play catalog
                    if (mMediaPlayerController == null) {
                        mMediaPlayerController = new MediaPlayerController(getContext(), catalogAudioPath);
                    } else {
                        mMediaPlayerController.changeAudio(catalogAudioPath);
                    }
                    setEnableButtonPlayRelatedToAnalysisPlayButton();
                }

                @Override
                public void onFailed(int errorMessageId) {
                    //do nothing
                }

                @Override
                public void onFinish(PROGRESS_TYPE loadingType) {
                    //do nothing
                }

            });
            if (mCatalogList == null || mCatalogList.isEmpty()) {
                return;
            }
            String sampleSoundUrl = mCatalogList.get(mCurrentSelectedPage).getSample_sound();
            if (sampleSoundUrl != null) {
                downloadSampleSoundTask.execute(sampleSoundUrl);
            } else {
                enableButtonPlayCatalog(false);
                showProgress(false);
                mMediaPlayerController.changeAudio(null);
//                mMapCatalog.put(mCurrentSelectedPage, null);
            }
        }
    }

    private void setEnableButtonPlayRelatedToAnalysisPlayButton() {
        if (compareScreenActivityListener != null) {
            if (!compareScreenActivityListener.isRecordAudioPlaying()) {
                enableButtonPlayCatalog(true);
            } else {
                enableButtonPlayCatalog(false);
            }
        } else {
            enableButtonPlayCatalog(true);
        }
    }

    private void cancelDownloadSoundTask() {
        if (downloadSampleSoundTask != null && !downloadSampleSoundTask.isCancelled()) {
            Log.d(TAG, "cancel download sample sound task");
            downloadSampleSoundTask.cancel(true);
        }
    }

    /**
     * check audio catalog at position is downloaded or not
     *
     * @param position: position of catalog in list
     * @return true if catalog audio is downloaded
     */
    private boolean isCatalogAudioExist(int position) {
        return mMapCatalog != null && mMapCatalog.containsKey(position);
    }

    /**
     * cancel running retrieve audio task
     */
    @Override
    public void cancelAllBackgroundTask() {
        Log.i(TAG, "cancelAllBackgroundTask");
        cancelDownloadSoundTask();
        clearCatalogAudioCache();
        if (mImageLoader != null) {
            mImageLoader.clearCache();
        }
        if (mMediaPlayerController != null) {
            mMediaPlayerController.release();
        }
    }

    /**
     * make folder to store all data of catalog
     */
    private void initCatalogFolder() {
        try {
            FileUtils.deleteFileOrFolder(new File(
                    Utility.APP_CATALOG_FOLDER_NAME_SDCARD));
            FileUtils.createFolder(getContext().getExternalFilesDir(null)
                    + Utility.APP_CATALOG_FOLDER_NAME_SDCARD);
            saveCatalogDirectory = new File(getContext().getExternalFilesDir(null)
                    + Utility.APP_CATALOG_FOLDER_NAME_SDCARD);
        } catch (Exception ex) {
            Log.e(TAG, "Error when create folder catalog", ex);
        }
    }

    /**
     * Show part cause and method
     */
    private void showCauseAndMethod() {
        //populate data to catalog info view
        if (mCatalogList == null || mCatalogList.isEmpty()) {
            return;
        }
        Catalog catalog = mCatalogList.get(mCurrentSelectedPage);
        tvCause.setText(catalog.getCause());
        tvMethod.setText(catalog.getMethod());
//        tvMethodDetail.setText(catalog.getMethod_detail());
    }

    /**
     * enable button play catalog
     *
     * @param enable: boolean true when enable false when disable
     */
    public void enableButtonPlayCatalog(boolean enable) {
        if (!enable) {
            btnPlayCatalogAudio.setEnabled(false);
        } else if (isCatalogAudioExist(mCurrentSelectedPage)) {
            btnPlayCatalogAudio.setEnabled(true);

        }
    }

    private void showProgress(boolean show) {
        if (show) {
            progressBarSmall.setVisibility(View.VISIBLE);
        } else {
            progressBarSmall.setVisibility(View.GONE);
        }
    }

    /**
     * {@link Activity#onResume()}
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "handleOnResume");
        if (isCatalogAudioExist(mCurrentSelectedPage)) {
            btnPlayCatalogAudio.setEnabled(true);
        }
    }

    /**
     * clear folder store catalog audio
     */
    private void clearCatalogAudioCache() {
        FileUtils.deleteFileOrFolder(saveCatalogDirectory);
    }

    @Override
    public void setCurrentCatalogPosition(int position) {
        this.currentCatalogPosition = position;
    }

    @Override
    public List<Catalog> getCatalogList() {
        return mCatalogList;
    }

    @Override
    public ArrayList<String> getAllCatalogCauseParts() {
        return catalogCauseParts;
    }

    @Override
    public ArrayList<String> getAllCatalogMethods() {
        return catalogMethods;
    }

    @Override
    public List<String> getAllCatalogMethodImageUrls() {
//        Catalog currentCatalog = mCatalogList.get(currentCatalogPosition);
//        List<String> methodUrlList = null;
//        if (currentCatalog != null) {
//            methodUrlList = currentCatalog.getMethodImagesList();
//            //TODO Provisional show catalog image
//            if (methodUrlList.isEmpty()) {
//                for (Catalog catalog : mCatalogList)
//                    methodUrlList.add(catalog.getSample_picture());
//            }
//        } else {
//            Log.e(TAG, "current catalog is null");
//        }
//        return methodUrlList;
        return new ArrayList<>();
    }

    @Override
    public Catalog getCurrentCatalog() {
        return mCatalogList.get(mCurrentSelectedPage);
    }

//    @Override
//    public void setEnablePlayButton(boolean enable) {
//        Log.i(TAG, "Enable play button on catalog view: " + String.valueOf(enable));
//        btnPlayCatalogAudio.setEnabled(enable);
//    }

    @Override
    public void onStop() {
        if (mMediaPlayerController != null) {
            btnPlayCatalogAudio.setChecked(false);
            mMediaPlayerController.onClick(STOP);
            swipeImageAdapter.getCurrentItem(pagerCatalog.getCurrentItem())
                    .stopSeek();
        }
        super.onStop();
    }
}
