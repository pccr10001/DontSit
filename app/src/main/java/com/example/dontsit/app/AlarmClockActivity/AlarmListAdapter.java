package com.example.dontsit.app.AlarmClockActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.dontsit.app.Database.AlarmClockDAO;
import com.example.dontsit.app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AlarmListAdapter extends BaseAdapter {

    private List<AlarmClock> clocks;
    private LayoutInflater inflater;
    private Context context;
    private AlarmClockDAO alarmClockDAO;

    public AlarmListAdapter(Context context, List<AlarmClock> clocks) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.clocks = clocks;
    }

    private static class ViewHolder {
        private int id;
        TextView RepeatTextView;
        TextView ResetTextView;
        TextView AlarmClockTextView;
        CheckBox EnableCheckbox;
        ImageView DeleteButton;
    }

    public Object getItemById(int id) {
        for (AlarmClock clock : clocks)
            if (clock.getId() == id)
                return clock;
        return null;
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
        AlarmClock clock = clocks.get(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.listitem_clockrule, parent, false);
            holder.RepeatTextView = (TextView) convertView.findViewById(R.id.RepeatTextView);
            holder.ResetTextView = (TextView) convertView.findViewById(R.id.ResetTextView);
            holder.AlarmClockTextView = (TextView) convertView.findViewById(R.id.AlarmClockTextView);
            holder.EnableCheckbox = (CheckBox) convertView.findViewById(R.id.ClockEnableCheckbox);
            holder.DeleteButton = (ImageView) convertView.findViewById(R.id.DeleteButton);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.RepeatTextView.setVisibility(clock.isRepeated() ? View.VISIBLE : View.GONE);
        holder.ResetTextView.setVisibility(clock.isResettable() ? View.VISIBLE : View.GONE);
        holder.EnableCheckbox.setChecked(clock.isEnabled());
        holder.id = clock.getId();
        holder.EnableCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                alarmClockDAO = new AlarmClockDAO(context);
                AlarmClock temp = alarmClockDAO.get(holder.id);
                temp.setEnabled(isChecked);
                alarmClockDAO.update(temp);
            }
        });
        holder.DeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmClockDAO = new AlarmClockDAO(context);
                AlarmClock temp = alarmClockDAO.get(holder.id);
                clocks.remove(temp);
                alarmClockDAO.delete(temp.getId());
                notifyDataSetChanged();
            }
        });
        int time = clock.getTime();
        String result = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(time),
                TimeUnit.MILLISECONDS.toMinutes(time) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));

        holder.AlarmClockTextView.setText(result);
        return convertView;
    }

    public void remove(AlarmClock clock) {
        clocks.remove(clock);
        notifyDataSetChanged();
    }
}
