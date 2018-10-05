package jp.co.fujixerox.sa.ion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;
import jp.co.fujixerox.sa.ion.R;

/**
 * Created by TrungKD.
 */
public class SettingListAdapter extends ArrayAdapter<String> {

    Context context;
    int	itemResID;
    List<String> list;

    public SettingListAdapter(Context context, int itemLayoutId, List<String> objects) {
        super(context, itemLayoutId, objects);
        this.context = context;
        this.itemResID = itemLayoutId;
        this.list = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(itemResID, parent, false);
            viewHolder.nameAccount = (TextView) convertView.findViewById(R.id.item_value);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.nameAccount.setText(list.get(position));
        return convertView;
    }

    public static class ViewHolder{
        TextView nameAccount;
    }
}
