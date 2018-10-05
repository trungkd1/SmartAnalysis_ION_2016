package jp.co.fujixerox.sa.ion.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.adapters.AudiosListAdapter;
import jp.co.fujixerox.sa.ion.db.AudioData;
import jp.co.fujixerox.sa.ion.db.DatabaseHelper;
import jp.co.fujixerox.sa.ion.db.ICloudParams;
import jp.co.fujixerox.sa.ion.views.SelectAudioHeadingView;

/**
 * Dialog show audios list
 * Created by TrungKD
 */
public class AudiosListDialogFragment extends DialogFragment {
    private static final String TAG = "RecordListDialogLogFragment";
    private List<AudioData> audios;
    private AudiosListAdapter mAdapter;
    private DatabaseHelper databaseHelper;
    private DialogInterface.OnKeyListener mOnKeyListener;
    private OnAudioFileSelectedListener mOnAudioFileSelectedListener;
    private View contentView;
    /**
     * List item click listener
     */
    private AudiosListAdapter.AdapterCallback onClickListener = new AudiosListAdapter.AdapterCallback() {
        @Override
        public void onMethodCallback(AudioData audioData) {
            mOnAudioFileSelectedListener.onAudioFileSelected(audioData);
            dismiss();
        }

        @Override
        public void onDeleteCallback(int position, AudioData audioData) {
            // do nothing
        }
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.dialog_select_audios, null);
        builder.setView(contentView);
        if (mOnKeyListener != null) {
            builder.setOnKeyListener(mOnKeyListener);
        }
        Dialog dialog = builder.create();

        // set view for order audios
        View.OnClickListener onClickListener = new View.OnClickListener() {
            int id = 0;
            boolean isDesc = true;
            @Override
            public void onClick(View view) {
                id = view.getId();
                Object tag = view.getTag(id);
                if (tag != null) {
                    isDesc = (boolean)tag;
                    isDesc = !isDesc;
                }
                view.setTag(id, isDesc);
                if (id == R.id.heading_audio_file_name) {
                    mAdapter.sortByFormId(ICloudParams.picture, isDesc);
                } else if (id == R.id.heading_color_type) {
                    mAdapter.sortByFormId(ICloudParams.color, isDesc);
                } else if (id == R.id.heading_productname) {
                    mAdapter.sortByFormId(ICloudParams.productname, isDesc);
                } else if (id == R.id.heading_paper_type) {
                    mAdapter.sortByFormId(ICloudParams.output_type, isDesc);
                } else if (id == R.id.heading_recording_date) {
                    mAdapter.sortByFormId(ICloudParams.record_date, isDesc);
                } else if (id == R.id.heading_serial_number) {
                    mAdapter.sortByFormId(ICloudParams.serialid, isDesc);
                }
                onClickHeadingSelectAudio((SelectAudioHeadingView) view);
            }
        };
        contentView
                .findViewById(R.id.heading_recording_date).setOnClickListener(onClickListener);

        contentView
                .findViewById(R.id.heading_serial_number).setOnClickListener(onClickListener);

        contentView
                .findViewById(R.id.heading_productname).setOnClickListener(onClickListener);

        contentView
                .findViewById(R.id.heading_color_type).setOnClickListener(onClickListener);

        contentView
                .findViewById(R.id.heading_paper_type).setOnClickListener(onClickListener);

        contentView
                .findViewById(R.id.heading_audio_file_name).setOnClickListener(onClickListener);

        databaseHelper = DatabaseHelper.getInstance(getActivity());
        audios = databaseHelper.getAudios(AudioData.COLUMN_RECORD_DATE, -1);
        RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.table_audios);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        // specify an mAdapter (see also next example)
        mAdapter = new AudiosListAdapter(audios, this.onClickListener, R.layout.item_audio_layout);
        recyclerView.setAdapter(mAdapter);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    /**
     * handle event when click on heading column in dialog select audio
     *
     * @param headingModelName Title name
     */
    private void onClickHeadingSelectAudio(SelectAudioHeadingView headingModelName) {
        deActiveHeadingSelectAudio();
        headingModelName.setVisibility(View.VISIBLE);
        headingModelName.changeIconOrder();
    }

    private void deActiveHeadingSelectAudio() {
        contentView
                .findViewById(R.id.heading_recording_date).setVisibility(View.INVISIBLE);

        contentView
                .findViewById(R.id.heading_serial_number).setVisibility(View.INVISIBLE);

        contentView
                .findViewById(R.id.heading_productname).setVisibility(View.INVISIBLE);

        contentView
                .findViewById(R.id.heading_color_type).setVisibility(View.INVISIBLE);

        contentView
                .findViewById(R.id.heading_paper_type).setVisibility(View.INVISIBLE);

        contentView
                .findViewById(R.id.heading_audio_file_name).setVisibility(View.INVISIBLE);
    }

    public void setOnKeyListener(DialogInterface.OnKeyListener onKeyListener) {
        this.mOnKeyListener = onKeyListener;
    }

    public void setOnAudioFileSelectedListener(OnAudioFileSelectedListener listener) {
        this.mOnAudioFileSelectedListener = listener;
    }

    public interface OnAudioFileSelectedListener {
        void onAudioFileSelected(AudioData audioData);
    }
}
