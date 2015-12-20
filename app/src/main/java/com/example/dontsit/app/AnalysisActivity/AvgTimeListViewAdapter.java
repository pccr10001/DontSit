package com.example.dontsit.app.AnalysisActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.dontsit.app.R;

import java.util.List;

public class AvgTimeListViewAdapter extends BaseAdapter {

    private List<ShowData> datas;
    private LayoutInflater inflater;

    public AvgTimeListViewAdapter(Context context, List<ShowData> datas) {
        inflater = LayoutInflater.from(context);
        this.datas = datas;
    }

    private static class ViewHolder {
        TextView TitleTextView;
        TextView ValueTextView;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        ShowData data = datas.get(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.listitem_analysisitem, parent, false);
            holder.TitleTextView = (TextView) convertView.findViewById(R.id.AnalysisItemTitleTextView);
            holder.TitleTextView.setTextColor(data.getColor());
            holder.ValueTextView = (TextView) convertView.findViewById(R.id.AnalysisItemValueTextView);
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
