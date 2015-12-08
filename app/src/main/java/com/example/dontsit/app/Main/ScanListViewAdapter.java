package com.example.dontsit.app.Main;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.dontsit.app.MainActivity;
import com.example.dontsit.app.R;

import java.util.List;

public class ScanListViewAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private MainActivity main;
    List<BluetoothDevice> devices;

    public ScanListViewAdapter(MainActivity main, List<BluetoothDevice> devices) {
        inflater = LayoutInflater.from(main);
        this.main = main;
        this.devices = devices;
    }

    private static class ViewHolder {
        ImageView CushionView;
        TextView MacView;
        TextView NameView;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final BluetoothDevice device = devices.get(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.listitem_scandevice, parent, false);

            holder.CushionView = (ImageView) convertView.findViewById(R.id.CushionPicture);
            holder.MacView = (TextView) convertView.findViewById(R.id.MacTextView);
            holder.NameView = (TextView) convertView.findViewById(R.id.NameTextView);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.MacView.setText(device.getAddress());
        holder.NameView.setText(device.getName());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.chooseMac(device.getAddress());
            }
        });
        return convertView;
    }

}
