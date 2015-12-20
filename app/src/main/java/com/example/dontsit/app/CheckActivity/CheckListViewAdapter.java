package com.example.dontsit.app.CheckActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.dontsit.app.R;

import java.util.ArrayList;
import java.util.List;

public class CheckListViewAdapter extends BaseAdapter {

    private List<CheckItem> checkItems = new ArrayList<CheckItem>();
    private LayoutInflater inflater;

    public CheckListViewAdapter(Context context, List<CheckItem> checkItems) {
        inflater = LayoutInflater.from(context);
        this.checkItems = checkItems;
    }

    private static class ViewHolder {
        ImageView CheckImageView;
        TextView CheckDescription;
    }

    @Override
    public int getCount() {
        return checkItems.size();
    }

    @Override
    public Object getItem(int position) {
        return checkItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final CheckItem checkItem = checkItems.get(position);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.listitem_checkitem, parent, false);
            holder.CheckImageView = (ImageView) convertView.findViewById(R.id.CheckImageView);
            holder.CheckDescription = (TextView) convertView.findViewById(R.id.CheckDescription);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.CheckImageView.setImageResource(
                checkItem.isChecked()? R.drawable.check_box : R.drawable.check_green);
        holder.CheckDescription.setText(checkItem.getDescription());

        return convertView;
    }
}
