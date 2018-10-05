package jp.co.fujixerox.sa.ion.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ToggleButton;

import java.io.File;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.activities.RecordingScreenActivity;
import jp.co.fujixerox.sa.ion.db.AudioData;
import jp.co.fujixerox.sa.ion.dialogs.ConfirmDialogFragment;
import jp.co.fujixerox.sa.ion.interfaces.IAnalysisViewFragment;
import jp.co.fujixerox.sa.ion.interfaces.ICompareScreenActivity;
import jp.co.fujixerox.sa.ion.utils.CommonUtils;
import jp.co.fujixerox.sa.ion.utils.Utility;
import jp.co.fujixerox.sa.ion.views.AxisImageView;
import jp.co.fujixerox.sa.ion.views.MediaPlayerController;

import static jp.co.fujixerox.sa.ion.views.MediaPlayerController.AudioAction.PLAY;
import static jp.co.fujixerox.sa.ion.views.MediaPlayerController.AudioAction.STOP;

/**
 * Created by TrungKD
 * The fragment to show Analysis Image, and play record audio
 */
public class AnalysisViewFragment extends AbstractFragment implements IAnalysisViewFragment {
    public static final String TAG = AnalysisViewFragment.class.getSimpleName();
    /**
     * Button for play analysis audio
     */
    private ToggleButton btnPlayOriginalAudio;
    /**
     *
     */
    private ImageButton btnRecordAgain;
    /**
     * Image analysis
     */
    private AxisImageView axisImageView;

    /**
     *
     */
    boolean fromTopScreen;
    /**
     * {@link AudioData}
     */
    private AudioData mAudioData;
    /**
     * {@link MediaPlayerController}
     */
    private MediaPlayerController mediaPlayerController;

    /**
     * Call back instance to CompareScreenActivity
     */
    private ICompareScreenActivity compareScreenActivityListener;

    /**
     * Constructor
     */
    public AnalysisViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param audioData: AudioData
     * @return A new instance of fragment CatalogViewFragment.
     */
    public static AnalysisViewFragment newInstance(AudioData audioData) {
        AnalysisViewFragment analysisViewFragment = new AnalysisViewFragment();
        analysisViewFragment.setAudioData(audioData);
        return analysisViewFragment;
    }

    /**
     * set AudioData for fragment
     *
     * @param audioData: AudioData
     */
    public void setAudioData(AudioData audioData) {
        mAudioData = audioData;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_analysis_view, container, false);
        if (mAudioData == null) {
            return null;
        }
        initView(view);
        displayAnalysisResultImage();
        return view;
    }

    /**
     * setOnCompletedListener all view by resource id
     *
     * @param containerView: container view
     */
    private void initView(View containerView) {
        axisImageView = (AxisImageView) containerView.findViewById(R.id.img_analysis_result);
        btnPlayOriginalAudio = (ToggleButton) containerView.findViewById(R.id.btn_play_original_audio);
        btnRecordAgain = (ImageButton) containerView.findViewById(R.id.btn_record_again_compare);
        if (fromTopScreen == true) invisibleRecordAgainBtn();
        //handle event for button
        btnPlayOriginalAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnPlayOriginalAudio.isChecked()) {
                    /* DefaultApplication.getInstance().trackEvent(getString(R.string.category_ui_event),
                            getString(R.string.action_button_press), getString(R.string.label_play)); //音声再生開始 160407 mit*/
                    //register media player
                    if (mediaPlayerController == null) {
                        mediaPlayerController = new MediaPlayerController(view.getContext(), mAudioData.getSound());
                    }
                    axisImageView.startSeek(Utility.AUDIO_DURATION, null);
                    mediaPlayerController.onClick(PLAY);
                    mediaPlayerController.setOnCompletedListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            Log.v(TAG, "audio play finish");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btnPlayOriginalAudio.setChecked(false); //Click for stop
                                }
                            });
                        }
                    });
                } else {
                    mediaPlayerController.onClick(STOP);
                    axisImageView.stopSeek();
                }
            }
        });
        btnPlayOriginalAudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (compareScreenActivityListener != null) {
                        compareScreenActivityListener.onRecordAudioPlaying();
                    }
                } else {
                    if (compareScreenActivityListener != null) {
                        compareScreenActivityListener.onAudioPlayingFinish();
                    }
                }
            }
        });
        //handle event for button
        /*
      ImageButton click for record audio again
     */
        containerView.findViewById(R.id.btn_record_again_compare)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //handle click record again
                        onClickRecordAgain();
                    }
                });
    }

    /**
     * back to front activity
     */
    private void onClickRecordAgain() {
        final ConfirmDialogFragment dialogFragment = new ConfirmDialogFragment();
        dialogFragment.setTitle(getString(R.string.record_again_confirm));
        dialogFragment.setMessage(getString(R.string.record_again_message));
        dialogFragment.setOnButtonClickListener(new ConfirmDialogFragment.OnButtonClickListener() {
            @Override
            public void onOK() {
                /*DefaultApplication.getInstance().trackEvent(getString(R.string.category_dialog),
                        getString(R.string.action_button_press), getString(R.string.record_again)); 160407 mit */
                //goto recording screen to record again
                Intent i = new Intent(getContext(), RecordingScreenActivity.class);
                i.putExtra(Utility.EXTRA_INTENT.AUDIO_REPORT, mAudioData);
                startActivity(i);
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }

            @Override
            public void onCancel() {
                dialogFragment.dismiss();
            }
        });
        dialogFragment.show(getFragmentManager(), TAG);
        //Tracking Event
        /* DefaultApplication.getInstance().trackEvent(getString(R.string.category_ui_event),
                getString(R.string.action_show_dialog), getString(R.string.record_again_message)); 160407 mit */
    }

    /**
     * display analysis result image
     */
    private void displayAnalysisResultImage() {
        String analysisImageFilePath = mAudioData.getPicture();
        loadImage(axisImageView, analysisImageFilePath);
    }

    /**
     * loading image from image file path
     *
     * @param imgView
     * @param imageFilePath: path of image file in local
     */
    private void loadImage(AxisImageView imgView, String imageFilePath) {
        // show analysis image
        if (!TextUtils.isEmpty(imageFilePath)) {
            Bitmap bmpAnalysis = CommonUtils.decodeFile(new File(imageFilePath));
            imgView.setBitmap(bmpAnalysis);
        }
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
     * {@inheritDoc}
     */
    public void onDestroy() {
        super.onDestroy();
        //cancel get catalog task
        if (mediaPlayerController != null) {
            mediaPlayerController.release();
        }
    }

    public void setFromTopScreenCompare(Boolean fromTopScreen) {
        this.fromTopScreen = fromTopScreen;
    }

    private void invisibleRecordAgainBtn() {
        btnRecordAgain.setVisibility(View.INVISIBLE);
    }

    /**
     * enable button play audio
     *
     * @param enable: boolean
     */
    @Override
    public void setEnablePlayButton(boolean enable) {
        btnPlayOriginalAudio.setEnabled(enable);
    }

    @Override
    public void onStop() {
        if (mediaPlayerController != null) {
            mediaPlayerController.onClick(STOP);
            btnPlayOriginalAudio.setChecked(false);
            axisImageView.stopSeek();
        }
        super.onStop();
    }

    public boolean isPlayingAudio(){
        return btnPlayOriginalAudio.isChecked();
    }
}
