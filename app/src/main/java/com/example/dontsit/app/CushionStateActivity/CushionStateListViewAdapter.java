package com.example.dontsit.app.CushionStateActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.dontsit.app.AnalysisActivity.ShowData;
import com.example.dontsit.app.R;

import java.util.List;

public class CushionStateListViewAdapter extends BaseAdapter {

    private List<ShowData> showDatas;
    private LayoutInflater inflater;

    public CushionStateListViewAdapter(Context context, List<ShowData> showDatas) {
        inflater = LayoutInflater.from(context);
        this.showDatas = showDatas;
    }

    private static class ViewHolder {
        TextView TitleTextView;
        TextView ValueTextView;
    }

    @Override
    public int getCount() {
        return showDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return showDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        ShowData data = showDatas.get(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.listitem_cushionstate, parent, false);
            holder.TitleTextView = (TextView) convertView.findViewById(R.id.CushionStateTitleTextView);
            holder.TitleTextView.setTextColor(data.getColor());
            holder.ValueTextView = (TextView) convertView.findViewById(R.id.CushionStateValueTextView);
            holder.ValueTextView.setTextColor(data.getColor());
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.TitleTextView.setText(data.getTitle());
        holder.ValueTextView.setText(data.getValue());

        return convertView;
    }
}
