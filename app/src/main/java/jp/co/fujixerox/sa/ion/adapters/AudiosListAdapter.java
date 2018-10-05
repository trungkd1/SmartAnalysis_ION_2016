package jp.co.fujixerox.sa.ion.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.db.AudioData;
import jp.co.fujixerox.sa.ion.db.ICloudParams;
import jp.co.fujixerox.sa.ion.services.UploadReportService;
import jp.co.fujixerox.sa.ion.utils.CommonUtils;
import jp.co.fujixerox.sa.ion.utils.Utility;

/**
 * List adapter for AudiosList
 * Created by TrungKD 
 */
public class AudiosListAdapter extends RecyclerView.Adapter{

    private static final String TAG = "AudiosLisAdapter";
    private final SortAudioDtaComparatorDESC mComparatorDesc;
    private final SortAudioDtaComparatorASC mComparatorAsc;
    private List<AudioData> mDataset;
    private AdapterCallback mAdapterCallback;
    private int mItemLayoutId;
    private String orderByFormId;
    private boolean isLoadingFromCloud;

    public AudiosListAdapter(List<AudioData> audioDataList, AdapterCallback adapterCallback, int itemLayoutId, boolean isLoadingFromCloud) {
        this(audioDataList, adapterCallback, itemLayoutId);
        this.isLoadingFromCloud = isLoadingFromCloud;
    }

    public AudiosListAdapter(List<AudioData> audioDataList, AdapterCallback adapterCallback, int itemLayoutId) {
        this.mDataset = audioDataList;
        this.mAdapterCallback = adapterCallback;
        this.mItemLayoutId = itemLayoutId;
        this.mComparatorDesc = new SortAudioDtaComparatorDESC();
        this.mComparatorAsc = new SortAudioDtaComparatorASC();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        RecyclerView.ViewHolder vh = null;
        View itemView = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        if (itemView == null) {
            Log.e(TAG, "Item layout id is not support");
        } else {
            if (viewType == R.layout.item_audio_layout) {
                vh = new ViewHolder(itemView);
            } else if (viewType == R.layout.row_report_list || viewType == R.layout.row_catalog_list) {
                vh =  new ViewHolder2(itemView, isLoadingFromCloud);
            } else if (viewType == R.layout.progressbar_item) {
                vh = new ProgressViewHolder(itemView);
            }
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolder) {
            AudioData audioData = mDataset.get(position);
            ViewHolder audioViewHolder = (ViewHolder) holder;
            audioViewHolder.setDataForView(audioData);
            audioViewHolder.getContentView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // something call back to fragment
                    mAdapterCallback.onMethodCallback(mDataset.get(position));
                }
            });
            View deleteButton = audioViewHolder.getContentView().findViewById(R.id.btnDelete);
            if (deleteButton != null) {
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // something call back to fragment
                        mAdapterCallback.onDeleteCallback(position, mDataset.get(position));
                    }
                });
            }
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }


    @Override
    public int getItemViewType(int position) {
        return mDataset.get(position) != null ? mItemLayoutId : R.layout.progressbar_item;
//        return mItemLayoutId;
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void removeItemAt(int position) {
        mDataset.remove(position);
        notifyItemChanged(position);
        notifyDataSetChanged();
    }
    /**
     * Set data for list
     * @param audioDataList
     */
    public void setDataset(List<AudioData> audioDataList) {
        this.mDataset.clear();
        this.mDataset.addAll(audioDataList);
        notifyDataSetChanged();
    }

    /**
     * add items to your adapter
     * @param audios: list audio
     */
    public void addItems(List<AudioData> audios) {
        mDataset.addAll(audios);
        notifyDataSetChanged();
    }

    /**
     * add footer
     */
    public void addFooter() {
        mDataset.add(null);
        notifyItemInserted(mDataset.size() - 1);
    }

    /**
     * remove footer if it's progress bar
     */
    public void removeFooter() {
        if (mDataset.get(mDataset.size() - 1) == null) {
            mDataset.remove(mDataset.size() - 1);
            notifyItemRemoved(mDataset.size());
        }
    }

    /**
     * Change order formid
     */
    public void sortByFormId(String formId, boolean isDesc) {
        this.orderByFormId = formId;
        if (isDesc) {
            Collections.sort(mDataset, mComparatorDesc);
        } else {
            Collections.sort(mDataset, mComparatorAsc);
        }
        notifyDataSetChanged();
    }


    /**
     * Provide a reference to the views for each data item
     * Complex data items may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private View contentView;
        // each data item is just a string in this case
        protected TextView txtAudioDate;
        protected TextView txtAudioFileName;
        protected TextView txtAudioColorType;
        protected TextView txtAudioPaperType;
        protected TextView txtAudioModeName;
        protected TextView txtAudioSerialNumber;

        public ViewHolder(View v) {
            super(v);
            this.contentView = v;
            v.setClickable(true);
            txtAudioDate = (TextView) v.findViewById(R.id.tvAudioRecordingDate);
            txtAudioFileName = (TextView) v.findViewById(R.id.tvAudioFileName);
            txtAudioColorType = (TextView) v.findViewById(R.id.tvAudioColorType);
            txtAudioPaperType = (TextView) v.findViewById(R.id.tvAudioPaperType);
            txtAudioModeName = (TextView) v.findViewById(R.id.tvAudioModeName);
            txtAudioSerialNumber = (TextView) v.findViewById(R.id.tvAudioSerialNumber);
        }

        public View getContentView() {
            return contentView;
        }

        /**
         * Set data to view items
         * @param audioData
         */
        public void setDataForView(AudioData audioData) {
            txtAudioDate.setText(CommonUtils.convertDateToString(
                    audioData.getRecordDate(), Utility.DATE_PATTERN));
            String filePath = audioData.getSound();
            if (filePath != null) {
                String fileName = filePath.substring(filePath
                        .lastIndexOf(File.separator)
                        + File.separator.length());
                txtAudioFileName.setText(fileName);

            } else {
                txtAudioFileName.setText(android.R.string.unknownName);
                Log.e(TAG, "file path is null");
            }
            //解析画面の表示をvalueからtextに変更 160420 mit
            txtAudioColorType.setText(audioData.getTextByFormId(ICloudParams.color));
            txtAudioModeName.setText(audioData.getTextByFormId(ICloudParams.productname));
            txtAudioSerialNumber.setText(audioData.getTextByFormId(ICloudParams.serialid));
            txtAudioPaperType.setText(audioData.getTextByFormId(ICloudParams.output_type));
        }

    }

    /**
     * View holder for ReportData and Catalog list
     */
    public class ViewHolder2 extends ViewHolder {
        // each data item is just a string in this case
        protected View ivAudioExist;
        protected View ivImageExist;
        protected TextView tvFix;

        public ViewHolder2(View v, boolean isLoadingFromCloud) {
            super(v);
            ivAudioExist = v.findViewById(R.id.ivAudio);
            ivImageExist = v.findViewById(R.id.ivImage);
            txtAudioDate = (TextView) v.findViewById(R.id.tvDate);
            txtAudioModeName = (TextView) v.findViewById(R.id.tvProductname);
            txtAudioSerialNumber = (TextView) v.findViewById(R.id.tvSerial);
            tvFix = (TextView) v.findViewById(R.id.tvFix);
            if(isLoadingFromCloud){
              v.findViewById(R.id.btnDelete).setVisibility(View.GONE);
            }
        }


        /**
         * Set data to view items
         * @param audioData AudioData
         */
        public void setDataForView(AudioData audioData) {
            if (UploadReportService.checkSameAudioDataIsSending(audioData)) {
                int gray = ContextCompat.getColor(getContentView().getContext(), R.color.grey_300);
                getContentView().setBackgroundColor(gray);
            } else {
                getContentView().setBackgroundResource(R.drawable.list_item_bg);
            }

            txtAudioDate.setText(CommonUtils.convertDateToString(
                    audioData.getRecordDate(), Utility.DATE_PATTERN));
            if (!TextUtils.isEmpty(audioData.getSound())) {
                ivAudioExist.setVisibility(View.VISIBLE);
            } else {
                ivAudioExist.setVisibility(View.INVISIBLE);
            }
            if (!TextUtils.isEmpty(audioData.getPicture())) {
                ivImageExist.setVisibility(View.VISIBLE);
            } else {
                ivImageExist.setVisibility(View.INVISIBLE);
            }
            if (audioData.getResult()!= null) {
                if (audioData.getResult().equals(Utility.RESULT_KEY.OK)) {
                    tvFix.setText(R.string.result_fixed);
                } else  if (audioData.getResult().equals(Utility.RESULT_KEY.NON_RECURRING)) {
                    tvFix.setText(R.string.result_non_recurring);
                } else  if (audioData.getResult().equals(Utility.RESULT_KEY.UNKNOWN)) {
                    tvFix.setText(R.string.result_unknown);
                } else {
                    tvFix.setText(R.string.result_not_fixed);
                }
                tvFix.setTextColor(Color.BLACK);
                tvFix.setTypeface(Typeface.DEFAULT);
            } else {
                tvFix.setText(R.string.txt_not_input); //160420 mit
                tvFix.setTextColor(Color.RED);
                tvFix.setTypeface(Typeface.DEFAULT_BOLD);
                //tvFix.setText(Utility.EMPTY_STRING);
            }

            txtAudioModeName.setText(audioData.getTextByFormId(ICloudParams.productname));
            txtAudioSerialNumber.setText(audioData.getValueByFormId(ICloudParams.serialid));
        }

    }

    public interface AdapterCallback {
        void onMethodCallback(AudioData audioData);
        void onDeleteCallback(int position, AudioData audioData);
    }

    private class SortAudioDtaComparatorDESC implements Comparator<AudioData> {
        @Override
        public int compare(AudioData lhs, AudioData rhs) {
            String leftValue = lhs.getValueByFormId(orderByFormId);
            String rightValue = rhs.getValueByFormId(orderByFormId);
            return leftValue.compareTo(rightValue);
        }

    }

    private class SortAudioDtaComparatorASC implements Comparator<AudioData> {
        @Override
        public int compare(AudioData lhs, AudioData rhs) {
            String leftValue = lhs.getValueByFormId(orderByFormId);
            String rightValue = rhs.getValueByFormId(orderByFormId);
            return rightValue.compareTo(leftValue);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
        }
    }
}
