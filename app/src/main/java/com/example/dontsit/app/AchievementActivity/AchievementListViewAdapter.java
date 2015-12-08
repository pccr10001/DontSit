package com.example.dontsit.app.AchievementActivity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.dontsit.app.R;

import java.util.ArrayList;
import java.util.List;

public class AchievementListViewAdapter extends BaseAdapter {

    private List<Achievement> achievements = new ArrayList<Achievement>();
    private LayoutInflater inflater;
    private Context context;

    public AchievementListViewAdapter(Context context,  List<Achievement> achievements) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.achievements = achievements;
    }

    private static class ViewHolder {
        ImageView AchievementImageView;
        TextView AchievementTitle;
        TextView AchievementDescription;
        ImageView AchievementImageView2;
        TextView AchievementTitle2;
        TextView AchievementDescription2;
    }

    @Override
    public int getCount() {
        return achievements.size();
    }

    @Override
    public Object getItem(int position) {
        return achievements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return achievements.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final Achievement achievement = achievements.get(position);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.listitem_achievement, parent, false);
            holder.AchievementImageView = (ImageView) convertView.findViewById(R.id.AchievementImageView);
            holder.AchievementTitle = (TextView) convertView.findViewById(R.id.AchievementTitle);
            holder.AchievementDescription = (TextView) convertView.findViewById(R.id.AchievementDescription);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        int imageResource = context.getResources()
                .getIdentifier(achievement.getImagePath(), null, context.getPackageName());
        Drawable image = context.getResources().getDrawable(imageResource, context.getTheme());
        holder.AchievementImageView.setImageDrawable(image);
        holder.AchievementTitle.setText(achievement.getName());
        holder.AchievementDescription.setText(achievement.getDescription());

        return convertView;
    }
}
