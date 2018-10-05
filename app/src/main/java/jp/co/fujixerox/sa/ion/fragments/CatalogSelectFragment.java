package jp.co.fujixerox.sa.ion.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import jp.co.fujixerox.sa.ion.R;
import jp.co.fujixerox.sa.ion.db.AudioFormData;
import jp.co.fujixerox.sa.ion.db.ICloudParams;
import jp.co.fujixerox.sa.ion.entities.Catalog;
import jp.co.fujixerox.sa.ion.entities.CatalogList;
import jp.co.fujixerox.sa.ion.entities.Item;
import jp.co.fujixerox.sa.ion.entities.Value;
import jp.co.fujixerox.sa.ion.interfaces.ICatalogScreenActivity;
import jp.co.fujixerox.sa.ion.interfaces.ICatalogSearchFragment;
import jp.co.fujixerox.sa.ion.interfaces.ICatalogSelectListFragment;
import jp.co.fujixerox.sa.ion.sync.AsyncTaskCallback;
import jp.co.fujixerox.sa.ion.sync.DownloadCatalogTask;
import jp.co.fujixerox.sa.ion.utils.JsonParser;
import jp.co.fujixerox.sa.ion.utils.Utility;
import jp.co.fujixerox.sa.ion.views.AbstractItemView;
import jp.co.fujixerox.sa.ion.views.ItemViewGenerator;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ICatalogSelectListFragment} interface
 * to handle interaction events.
 * Use the {@link CatalogSelectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CatalogSelectFragment extends AbstractFragment implements ICatalogSearchFragment {

    private ItemViewGenerator itemViewGenerator;
    private List<AudioFormData> audioFormDataList = new ArrayList<>();
    private DownloadCatalogTask downloadCatalogTask = null;
    private ICatalogScreenActivity catalogScreenActivity;
    private List<Item> items;
    private LinearLayout inputSectionLayout;
    private AbstractItemView.OnValueChangedListener listener;

    public CatalogSelectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment CatalogSelectFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CatalogSelectFragment newInstance() {
        CatalogSelectFragment fragment = new CatalogSelectFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_catalog_search, container, false);
        items = JsonParser.getListItems(
                Utility.ASSETS_JSON_PATH,
                Utility.JSON_FILE_NAME.CATALOG_SEARCH, getResources().getAssets());
        itemViewGenerator = new ItemViewGenerator(getContext());
        inputSectionLayout = (LinearLayout) view
                .findViewById(R.id.fragment_container);
        listener = new AbstractItemView.OnValueChangedListener() {
            @Override
            public void onValueChanged(String formId, Value value) {
                boolean isAllValidated = itemViewGenerator.isValidated();
                AudioFormData data = new AudioFormData(formId, value.getValue(), value.getText());
                addAudioFormData(data);

            }
        };
        List<Item> copyItems = new ArrayList<>(items);
        for (Item item : copyItems){
            if (item.getFormid().equals(ICloudParams.cause) && item.getPattern().equals(Utility.INPUT_PATTERN.CHECK.name())){
                items.remove(item);
            }
            if(item.getFormid().equals(ICloudParams.productname) && !(item.getInputtype().equals(Utility.INPUT_PATTERN.SELECT.name()))){
                items.remove(item);
            }
        }

        itemViewGenerator.setSectionInputLayout(Arrays.asList(ICloudParams.catalogFormIds), items, inputSectionLayout, null, null, listener);
        Log.i("create view", "create view");
        return view;
    }

    private void resetSelections() {
        inputSectionLayout.removeAllViews();
        itemViewGenerator.setSectionInputLayout(Arrays.asList(ICloudParams.catalogFormIds),items, inputSectionLayout, null, null, listener);
        clearFormData();
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
    }

    @Override
    public void addAudioFormData(AudioFormData audioFormData) {
        Iterator<AudioFormData> iterator = audioFormDataList.iterator();
        while (iterator.hasNext()) {
            AudioFormData formData = iterator.next();
            if (formData.getFormid().equals(audioFormData.getFormid())) {
                iterator.remove();
            }
        }
        audioFormDataList.add(audioFormData);
    }

    private void clearFormData() {
        audioFormDataList.clear();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (downloadCatalogTask != null && !downloadCatalogTask.isCancelled()){
            downloadCatalogTask.cancel(true);
        }
    }

    @Override
    public List<AudioFormData> getAudioFormDataList() {
        return audioFormDataList;
    }

    @Override
    public void searchCatalogs() {
        if (audioFormDataList.isEmpty()) {
            Toast.makeText(getContext(), R.string.search_conditions_are_not_input, Toast.LENGTH_LONG).show();
        } else {
            //AsyncTask search catalog
            //Callback to activity to call showCatalogView
            //強制カラー＆普通紙 160420 mit
            AudioFormData formColor = new AudioFormData();
            AudioFormData formType = new AudioFormData();
            formColor.setFormid("color");
            formColor.setValue("Color");
            formColor.setText("4Color");
            formColor.setId(audioFormDataList.get(0).getId());
            audioFormDataList.add(formColor);
            formType.setFormid("output_type");
            formType.setValue("Normal");
            formType.setText("普通紙");
            formColor.setId(audioFormDataList.get(0).getId());
            audioFormDataList.add(formType);
            downloadCatalogTask = new DownloadCatalogTask(getContext(), audioFormDataList, new AsyncTaskCallback() {
                private ProgressDialog progressDialog;

                @Override
                public void onPrepare(PROGRESS_TYPE type) {
                    progressDialog = new ProgressDialog(getContext());
                    progressDialog.setMessage(getString(R.string.loading_catalog));
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }

                @Override
                public void onSuccess(Object result) {
                    progressDialog.dismiss();
                    CatalogList mCatalogListInfo = (CatalogList) result;
                    List<Catalog> catalogList = mCatalogListInfo.getCatalogs();
                    catalogScreenActivity.showCatalogView(catalogList);
//                    resetSelections();
                }

                @Override
                public void onFailed(int errorMessageId) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), errorMessageId,
                            Toast.LENGTH_SHORT).show();
//                    resetSelections();
                }

                @Override
                public void onFinish(PROGRESS_TYPE loadingType) {
                    //do nothing
                }
            });
            downloadCatalogTask.execute();
        }
    }
}
