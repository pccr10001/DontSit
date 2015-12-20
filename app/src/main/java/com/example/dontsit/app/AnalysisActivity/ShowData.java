package com.example.dontsit.app.AnalysisActivity;

public class ShowData {

    private String Title;
    private String Value;
    private int Color;

    public ShowData(String title, String value) {
        this(title, value, android.graphics.Color.BLACK);
    }

    public ShowData(String title, String value, int color) {
        this.Title = title;
        this.Value = value;
        this.Color = color;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

    public int getColor() {
        return Color;
    }

    public void setColor(int color) {
        Color = color;
    }

    @Override
    public String toString() {
        return Title + ", " + Value + ", " + Color;
    }

}
