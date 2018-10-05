package jp.co.fujixerox.sa.ion.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.db.AudioFormData;
import jp.co.fujixerox.sa.ion.db.ICloudParams;
import jp.co.fujixerox.sa.ion.dialogs.CatalogFilterDialogFragment;
import jp.co.fujixerox.sa.ion.dialogs.FilterProfile;
import jp.co.fujixerox.sa.ion.entities.Catalog;
import jp.co.fujixerox.sa.ion.entities.CatalogList;
import jp.co.fujixerox.sa.ion.entities.Item;
import jp.co.fujixerox.sa.ion.entities.Value;
import jp.co.fujixerox.sa.ion.interfaces.ICatalogScreenActivity;
import jp.co.fujixerox.sa.ion.interfaces.ICatalogSelectListFragment;
import jp.co.fujixerox.sa.ion.sync.AsyncTaskCallback;
import jp.co.fujixerox.sa.ion.sync.DownloadCatalogTask;
import jp.co.fujixerox.sa.ion.utils.JsonParser;
import jp.co.fujixerox.sa.ion.utils.Utility;
import jp.co.fujixerox.sa.ion.views.AbstractItemView;
import jp.co.fujixerox.sa.ion.views.SpinnerItemView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ICatalogSelectListFragment} interface
 * to handle interaction events.
 * Use the {@link CatalogListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CatalogListFragment extends AbstractFragment implements ICatalogSelectListFragment {
    private static final String TAG = CatalogListFragment.class.getSimpleName();
    private ICatalogScreenActivity catalogScreenActivity;
    private static LinearLayoutManager mLayoutManager;
    private List<View> mListFilterButton = new ArrayList<>();
    private ArrayList<Catalog> mSelectedCatalogList = new ArrayList<>();
    private ArrayList<AudioFormData> mAudioFormDataList = new ArrayList<>();
    private DownloadCatalogTask downloadCatalogTask = null;

    public CatalogListFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment CatalogListFragment.
     */
    public static CatalogListFragment newInstance() {
        CatalogListFragment fragment = new CatalogListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (downloadCatalogTask != null && !downloadCatalogTask.isCancelled()) {
            downloadCatalogTask.cancel(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout frameLayout = new FrameLayout(getActivity());
        populateViewForOrientation(inflater, frameLayout);
        createProductSelectView(frameLayout);
        mLayoutManager = new LinearLayoutManager(getActivity());
        return frameLayout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ICatalogScreenActivity) {
            catalogScreenActivity = (ICatalogScreenActivity) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ICatalogScreenActivity");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        catalogScreenActivity = null;
    }

    /**
     * Create input form product
     *
     * @param containView
     */
    private void createProductSelectView(final View containView) {
        final SpinnerItemView spinnerItemView = (SpinnerItemView) containView.findViewById(R.id.spinner);
        List<Item> itemList = JsonParser.getListItems(
                Utility.ASSETS_JSON_PATH,
                Utility.JSON_FILE_NAME.AUDIO_CONDITIONS, getResources().getAssets());
        Item item = new Item();
        for (Item i : itemList) {
            if (i.getFormid().equals(ICloudParams.productgroup)) {
                item = i;
            }
        }

        if (item != null) {
            spinnerItemView.setItem(item);
            spinnerItemView.setOnValueChangedListener(new AbstractItemView.OnValueChangedListener() {
                @Override
                public void onValueChanged(String formId, Value value) {
                    AudioFormData audioFormData = new AudioFormData();
                    audioFormData.setFormid(formId);
                    audioFormData.setValue(value.getValue());
                    addAudioFormData(audioFormData);
                    if (!ICloudParams.productname.equals(formId)) {
                        //do nothing if formid is not "productname
                        return;
                    }
                    // search catalog list from selected product name
                    downloadCatalogList();
                    removeAllSelectedCatalog();
                }
            });
        } else {
            Log.e(TAG, "Can't get products from assets");
        }
    }


    /**
     * Set action show catalog filter dialog when filter button clicked
     */
    private void setCatalogFilterEvent(View containView, final CatalogSelectListAdapter adapter, final List<Catalog> data) {
        mListFilterButton.clear(); // reset filter button list
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final int id = v.getId();
                FilterProfile filterProfile;
                Iterator<View> iterator = mListFilterButton.iterator();
                if (iterator.hasNext()) {
                    //get current top id
                    int topId = iterator.next().getId();
                    if (topId == id) {
                        //get top filter
                        filterProfile = (FilterProfile) v.getTag();
                    } else {
                        // create filter from current data list
                        String[] checkList = createFilterData(v, adapter.data);
                        filterProfile = new FilterProfile(id, checkList);
                    }
                } else {
                    //create filter from origin data list
                    String[] checkList = createFilterData(v, data);
                    filterProfile = new FilterProfile(id, checkList);
                }
                //Create dialog
                CatalogFilterDialogFragment dialogFragment = new CatalogFilterDialogFragment();
                // set filter profile to show filter check list
                dialogFragment.setFilterProfile(filterProfile);
                dialogFragment.setOnFilterChangeListener(
                        new CatalogFilterDialogFragment.OnFilterChangeListener() {
                            @Override
                            public void onFilterChanged(FilterProfile filterProfile) {
                                mListFilterButton.remove(v);
                                if (filterProfile.isAllChecked()) {
                                    v.setTag(null);
                                } else {
                                    v.setTag(filterProfile);
                                    mListFilterButton.add(0, v);
                                }

                                // filter list
                                adapter.setData(runFilter(new ArrayList<>(data)));

                                adapter.notifyDataSetChanged();
                                if (!filterProfile.isAllChecked()) {
                                    ((TextView) v).setTextColor(getResources().getColor(R.color.blue_500));
                                } else {
                                    ((TextView) v).setTextColor(getResources().getColor(R.color.rgb_black));
                                }

                            }
                        });
                dialogFragment.show(getFragmentManager(), TAG);
                //Tracking Event
                /* DefaultApplication.getInstance().trackEvent(getString(R.string.category_ui_event),
                        getString(R.string.action_show_dialog), getString(R.string.label_filter_dialog)); 160407 mit */
            }
        };

        initFilterButton(containView, R.id.btn_filter_ion_type, onClickListener);
        initFilterButton(containView, R.id.btn_filter_ion_category, onClickListener);
        initFilterButton(containView, R.id.btn_filter_ion_cause, onClickListener);
        initFilterButton(containView, R.id.btn_filter_ion_color, onClickListener);
        //initFilterButton(containView, R.id.btn_filter_ion_codition, onClickListener); //原因を除去 160510 mit
    }

    /**
     * Init filter button
     *
     * @param containView
     * @param resId
     * @param onClickListener
     */
    private void initFilterButton(View containView, int resId, View.OnClickListener onClickListener) {
        TextView textView = (TextView) containView.findViewById(resId);
        textView.setOnClickListener(onClickListener);
        textView.setTextColor(getResources().getColor(R.color.rgb_black));
        textView.setTag(null);
    }

    /**
     * 現在フィルターしているコラムをすべて取得する
     *
     * @return List<FilterProfile>
     */
    private List<FilterProfile> getListOfProfileList() {
        List<FilterProfile> listOfListFilter = new ArrayList<>();
        for (View view :
                mListFilterButton) {
            FilterProfile filterProfile = (FilterProfile) view.getTag();
            if (filterProfile != null) {
                listOfListFilter.add(filterProfile);
            }
        }
        return listOfListFilter;
    }

    /**
     * 選択したフィルターによりカタログリストの絞り込みを実施する
     *
     * @param originCatalogList
     * @return
     */
    private List<Catalog> runFilter(List<Catalog> originCatalogList) {
        List<FilterProfile> filterProfileList = getListOfProfileList();
        List<Catalog> newData = filterOneColumn(filterProfileList, originCatalogList);

        return newData;
    }

    /**
     * @param filterProfiles
     * @param data
     * @return
     */
    private List<Catalog> filterOneColumn(List<FilterProfile> filterProfiles, List<Catalog> data) {
        for (FilterProfile filterProfile :
                filterProfiles) {
            data = filterProfile(filterProfile, data);
        }
        return data;
    }

    /**
     * 絞り込み要件によりカタログリスト表示を変わります。
     *
     * @param filterProfile
     * @param data
     * @return List<Catalog>
     */
    private List<Catalog> filterProfile(FilterProfile filterProfile, List<Catalog> data) {
        int id = filterProfile.getId();
        List<String> filters = filterProfile.getCheckedStringList();
        if (filters.isEmpty()) {
            data.clear();
            return data;
        }
        if (id == R.id.btn_filter_ion_type) {
            Iterator<Catalog> iterator = data.iterator();
            while (iterator.hasNext()) {
                Catalog catalog = iterator.next();
                if (!filters.contains(catalog.getType())) {
                    iterator.remove();
                }
            }
        } else if (id == R.id.btn_filter_ion_category) {
            Iterator<Catalog> iterator = data.iterator();
            while (iterator.hasNext()) {
                Catalog catalog = iterator.next();
                if (!filters.contains(catalog.getCategory())) {
                    iterator.remove();
                }
            }
        } else if (id == R.id.btn_filter_ion_color) {
            Iterator<Catalog> iterator = data.iterator();
            while (iterator.hasNext()) {
                Catalog catalog = iterator.next();
                if (!filters.contains(catalog.getColor())) {
                    iterator.remove();
                }
            }
        } else if (id == R.id.btn_filter_ion_cause) {
            Iterator<Catalog> iterator = data.iterator();
            while (iterator.hasNext()) {
                Catalog catalog = iterator.next();
                if (!filters.contains(catalog.getCause())) {
                    iterator.remove();
                }
            }
        } /*else if (id == R.id.btn_filter_ion_codition) {
            Iterator<Catalog> iterator = data.iterator();
            while (iterator.hasNext()) {
                Catalog catalog = iterator.next();
                if (!filters.contains(catalog.getCondition())) {
                    iterator.remove();
                }
            }
        }*/ //　原因を除去　160510 mit
        return data;
    }

    /**
     * Create filter string array
     *
     * @param view
     * @param data
     * @return
     */
    public String[] createFilterData(View view, List<Catalog> data) {
        String[] result;
        HashSet<String> set = new HashSet<>();
        int id = view.getId();
        if (id == R.id.btn_filter_ion_type) {
            for (Catalog catalog :
                    data) {
                set.add(catalog.getType());
            }
        }
        if (id == R.id.btn_filter_ion_category) {
            for (Catalog catalog :
                    data) {
                set.add(catalog.getCategory());
            }
        }
        if (id == R.id.btn_filter_ion_cause) {
            for (Catalog catalog :
                    data) {
                boolean re = set.add(catalog.getCause());
//                Log.v(TAG, "Add"+catalog.getCause()+" is "+re);
            }
        }
        if (id == R.id.btn_filter_ion_color) {
            for (Catalog catalog :
                    data) {
                set.add(catalog.getColor());
            }
        }/* if (id == R.id.btn_filter_ion_codition) {
            for (Catalog catalog:
                    data) {
                set.add(catalog.getCondition());
            }
        }*/ //原因を除去　160510 mit
        result = set.toArray(new String[set.size()]);
        return result;
    }

    private class CatalogSelectListAdapter extends RecyclerView.Adapter<CatalogSelectListAdapter.ViewHolder> {
        private final String TAG = CatalogSelectListAdapter.class.getSimpleName();
        //        private SparseBooleanArray mSelectedPositions = new SparseBooleanArray();
        private HashMap<Long, Boolean> itemState = new HashMap<>();
        private List<Catalog> data;

        public CatalogSelectListAdapter(List<Catalog> data) {
            this.data = data;
        }

        @Override
        public CatalogSelectListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
            return new CatalogSelectListAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final CatalogSelectListAdapter.ViewHolder holder, final int position) {
            final Catalog catalogInfo = data.get(position);
            holder.setDataForView(catalogInfo);
            View contentView = holder.getContentView();
            contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean isSelected = itemState.get(catalogInfo.getCatalog_id());
                    if (isSelected == null || !isSelected) {
                        itemState.put(catalogInfo.getCatalog_id(), Boolean.TRUE);
                    } else {
                        itemState.put(catalogInfo.getCatalog_id(), Boolean.FALSE);
                    }
                    setListItemHighLight(catalogInfo, v);
                }
            });
            setListItemHighLight(catalogInfo, contentView);

        }

        /**
         * Set item highlight when selected and clear when unselected
         *
         * @param catalogInfo
         * @param contentView
         */
        private void setListItemHighLight(Catalog catalogInfo, View contentView) {
            Boolean isSelected = itemState.get(catalogInfo.getCatalog_id());
            if (Boolean.TRUE.equals(isSelected)) {
                contentView.setBackgroundResource(R.drawable.list_item_focus);
                addSelectedCatalog(catalogInfo);
            } else {
                contentView.setBackgroundResource(R.drawable.list_item_bg);
                removeSelectedCatalog(catalogInfo);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return R.layout.item_catalog_select;
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

//        private void setItemChecked(int position, boolean isChecked) {
//            mSelectedPositions.put(position, isChecked);
//        }
//
//        private boolean isItemChecked(int position) {
//            return mSelectedPositions.get(position);
//        }

        public void setData(List<Catalog> data) {
            this.data = data;
        }


        /**
         * Provide a reference to the views for each data item
         * Complex data items may need more than one view per item, and
         * you provide access to all the views for a data item in a view holder
         */
        public class ViewHolder extends RecyclerView.ViewHolder {
            private View contentView;
            // each data item is just a string in this case
            private TextView tv_ion_type;
            private TextView tv_ion_category;
            private TextView tv_ion_cause;
            private TextView tv_ion_color;
            //private TextView tv_ion_condition;

            public ViewHolder(View v) {
                super(v);
                this.contentView = v;
                v.setClickable(true);
                tv_ion_type = (TextView) v.findViewById(R.id.tv_ion_type);
                tv_ion_category = (TextView) v.findViewById(R.id.tv_ion_category);
                tv_ion_cause = (TextView) v.findViewById(R.id.tv_ion_cause);
                tv_ion_color = (TextView) v.findViewById(R.id.tv_ion_color);
                //tv_ion_condition = (TextView) v.findViewById(R.id.tv_ion_condition);
            }

            public View getContentView() {
                return contentView;
            }

            /**
             * Set data to view items
             *
             * @param catalogInfo CatalogInfo
             */
            public void setDataForView(Catalog catalogInfo) {
                //json 160408 mit
                /*
                List<Item> items =  JsonParser.getListItems(
                        Utility.ASSETS_JSON_PATH,
                        Utility.JSON_FILE_NAME.CATALOG_SEARCH, getResources().getAssets());

                */
                //ここまで
                //tv_ion_type.setText(catalogInfo.getType());
                tv_ion_type.setText(getTypeText(catalogInfo.getType()));
                tv_ion_category.setText(catalogInfo.getCategory());
                tv_ion_cause.setText(catalogInfo.getCause());
                tv_ion_color.setText(catalogInfo.getColor());
                //tv_ion_condition.setText(catalogInfo.getCondition());
            }
        }
    }

    /*
     TypeのValueをJsonからText値に変換 160510 mit
     */
    private String getTypeText(String typeValue) {
        String typeText = "";
        AssetManager assetManager = getActivity().getAssets();
        List<Item> mItems = JsonParser.getListItems(
                Utility.ASSETS_JSON_PATH,
                Utility.JSON_FILE_NAME.CATALOG_SEARCH, assetManager);
        for (Item item : mItems) {
            if (item.getFormid().equals("type")) {
                for (Value val : item.getListvalue()) {
                    if (typeValue.equals(val.getValue())) {
                        typeText = val.getText();
                        break;
                    }
                }
            }
        }
        return typeText;
    }

    @Override
    public void addSelectedCatalog(Catalog catalog) {
        if (!mSelectedCatalogList.contains(catalog)) {
            mSelectedCatalogList.add(catalog);
        }
    }

    @Override
    public void removeSelectedCatalog(Catalog catalog) {
        mSelectedCatalogList.remove(catalog);
    }

    @Override
    public void removeAllSelectedCatalog() {
        mSelectedCatalogList.clear();
    }

    @Override
    public void addAudioFormData(AudioFormData audioFormData) {
        Iterator<AudioFormData> iterator = mAudioFormDataList.iterator();
        while (iterator.hasNext()) {
            AudioFormData formData = iterator.next();
            if (formData.getFormid().equals(audioFormData.getFormid())) {
                iterator.remove();
            }
        }
        mAudioFormDataList.add(audioFormData);
    }

    @Override
    public List<AudioFormData> getAudioFormDataList() {
        return mAudioFormDataList;
    }


    /**
     * Search catalog
     */
    @Override
    public void searchCatalogs() {
        catalogScreenActivity.showCatalogView(mSelectedCatalogList);
    }

    /**
     * Download catalog list
     */
    public void downloadCatalogList() {
        //強制カラー＆普通紙
        AudioFormData formColor = new AudioFormData();
        AudioFormData formType = new AudioFormData();
        formColor.setFormid("color");
        formColor.setValue("Color");
        formColor.setText("4Color");
        formColor.setId(mAudioFormDataList.get(0).getId());
        mAudioFormDataList.add(formColor);
        formType.setFormid("output_type");
        formType.setValue("Normal");
        formType.setText("普通紙");
        formColor.setId(mAudioFormDataList.get(0).getId());
        mAudioFormDataList.add(formType);

        downloadCatalogTask = new DownloadCatalogTask(getContext(), mAudioFormDataList,
                new AsyncTaskCallback() {
                    ProgressDialog dialog = null;

                    @Override
                    public void onPrepare(PROGRESS_TYPE type) {
                        dialog = new ProgressDialog(getContext());
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setMessage(getString(R.string.loading_catalog));
                        dialog.show();
                    }

                    @Override
                    public void onSuccess(Object object) {
                        CatalogList catalogListInfo = (CatalogList) object;
                        dialog.dismiss();
                        if (catalogListInfo != null) {
                            List<Catalog> catalogList = catalogListInfo.getCatalogs();
                            createCatalogListView(catalogList);
                        }
                    }

                    @Override
                    public void onFailed(int errorMessageId) {
                        dialog.dismiss();
                        Toast.makeText(getContext(), errorMessageId,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFinish(PROGRESS_TYPE loadingType) {

                    }
                });
        downloadCatalogTask.execute();
    }

    /**
     * Create catalog list containView
     */
    public void createCatalogListView(List<Catalog> data) {
        View containView = getView();
        if (containView != null) {
            RecyclerView recyclerView = (RecyclerView) containView.findViewById(R.id.recycler_view);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            recyclerView.setHasFixedSize(true);
            // use a linear layout manager
            recyclerView.setLayoutManager(mLayoutManager);
            CatalogSelectListAdapter adapter = new CatalogSelectListAdapter(data);
            recyclerView.setAdapter(adapter);
            setCatalogFilterEvent(containView, adapter, data);
        } else {
            Log.e(TAG, "content view is null");
        }
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {
        viewGroup.removeAllViewsInLayout();
        View subview = inflater.inflate(R.layout.fragment_catalog_select, viewGroup);
    }


}
