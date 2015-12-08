package com.example.dontsit.app.AlarmClockActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.dontsit.app.Database.AlarmClockDAO;
import com.example.dontsit.app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AlarmListAdapter extends BaseAdapter {

    private List<AlarmClock> clocks;
    private LayoutInflater inflater;
    private AlarmClockDAO alarmClockDAO;

    public AlarmListAdapter(Context context, List<AlarmClock> clocks) {
        inflater = LayoutInflater.from(context);
        alarmClockDAO = new AlarmClockDAO(context);
        this.clocks = clocks;
    }

    private static class ViewHolder {
        ImageView RepeatImageView;
        ImageView OneTimeImageView;
        TextView AlarmClockTextView;
        Button DeleteButton;
    }

    @Override
    public int getCount() {
        return clocks.size();
    }

    @Override
    public Object getItem(int position) {
        return clocks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return clocks.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final AlarmClock clock = clocks.get(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.listitem_clockrule, parent, false);
            holder.RepeatImageView = (ImageView) convertView.findViewById(R.id.RepeatImageView);
            holder.OneTimeImageView = (ImageView) convertView.findViewById(R.id.OneTimeImageView);
            holder.AlarmClockTextView = (TextView) convertView.findViewById(R.id.AlarmClockTextView);
            holder.DeleteButton = (Button) convertView.findViewById(R.id.DeleteButton);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (clock.getType() == AlarmClock.OneTimeAlarm) {
            holder.RepeatImageView.setVisibility(View.GONE);
            holder.OneTimeImageView.setVisibility(View.VISIBLE);
        }
        int time = clock.getTime();
        String result = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(time),
                TimeUnit.MILLISECONDS.toMinutes(time) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));

        holder.AlarmClockTextView.setText(result);
        holder.DeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmClockDAO.delete(clock.getId());
                remove(clock);
            }
        });
        return convertView;
    }

    public void remove(AlarmClock clock) {
        clocks.remove(clock);
        notifyDataSetChanged();
    }
}
