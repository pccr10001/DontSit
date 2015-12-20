package com.example.dontsit.app.CheckActivity;

import android.support.annotation.NonNull;

public class CheckItem implements Comparable {

    private String Description;
    private boolean IsChecked = true;
    private int order;

    public CheckItem() {
        this("", false, 0);
    }

    public CheckItem(String Description) {
        this(Description, false, 0);
    }

    public CheckItem(String Description, boolean IsChecked) {
        this(Description, IsChecked, 0);
    }

    public CheckItem(String Description, int order) {
        this(Description, false, order);
    }

    public CheckItem(String Description, boolean IsChecked, int order) {
        this.Description = Description;
        this.IsChecked = IsChecked;
        this.order = order;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public boolean isChecked() {
        return IsChecked;
    }

    public void setChecked(boolean checked) {
        this.IsChecked = checked;
    }

    @Override
    public String toString() {
        return Description + ", " + IsChecked + ", " + order;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        return Integer.compare(order, ((CheckItem) another).order);
    }
}
