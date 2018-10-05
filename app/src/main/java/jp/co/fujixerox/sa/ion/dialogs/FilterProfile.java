package jp.co.fujixerox.sa.ion.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by fxstdpc-admin on 2015/12/10.
 */
public class FilterProfile  {
    private int id;
    private String[] checkStringList;
    private boolean[] checkedList;
    private List<String> checkedStringList = new ArrayList<>();
    private FilterProfile parentFilter;

    public FilterProfile(int id, String[] checkList){
        this.id = id;
        this.checkStringList = checkList;
        checkedList = new boolean[checkList.length];
        Arrays.fill(checkedList, true);
        checkedStringList.addAll(Arrays.asList(checkList));
    }

    public String[] getCheckStringArray() {
        return checkStringList;
    }

    public void setCheckStringList(String[] checkStringList) {
        this.checkStringList = checkStringList;
    }

    public boolean[] getCheckedList() {
        return checkedList;
    }

    public void setCheckedList(boolean[] checkedList) {
        this.checkedList = checkedList;
    }

    public FilterProfile getParentFilter() {
        return parentFilter;
    }

    public void setParentFilter(FilterProfile parentFilter) {
        this.parentFilter = parentFilter;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setChecked(int which, boolean value) {
        checkedList[which] = value;
        String filterText = checkStringList[which];
        if (value) {
            if (!checkedStringList.contains(filterText))
                checkedStringList.add(filterText);
        } else {
            if (checkedStringList.contains(filterText))
                checkedStringList.remove(filterText);
        }
    }

    public void setAllChecked(boolean isChecked) {
        for (int i = 0; i < checkedList.length; i++) {
           setChecked(i, isChecked);
        }
    }

    public List<String> getCheckedStringList() {
        return checkedStringList;
    }

    public void setCheckedStringList(List<String> checkedStringList) {
        this.checkedStringList = checkedStringList;
    }

    public boolean isAllChecked() {
        boolean result = true;
        for (boolean checked:
             checkedList) {
            if (checked == false) {
                result = false;
                break;
            }
        }
        return result;
    }

}