package com.example.dontsit.app.AchievementActivity;

public class Achievement {

    private int Id;
    private String Name;
    private String Description;
    private String ImagePath;
    private boolean locked = true;
    private int Type;
    private int SpecDuration;
    private int SpecValue;

    public static final int SpecDurationType = 0;
    public static final int SpecDayType = 1;

    public Achievement() {
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public int getSpecValue() {
        return SpecValue;
    }

    public void setSpecValue(int specValue) {
        SpecValue = specValue;
    }

    public int getSpecDuration() {
        return SpecDuration;
    }

    public void setSpecDuration(int specDuration) {
        SpecDuration = specDuration;
    }
}
