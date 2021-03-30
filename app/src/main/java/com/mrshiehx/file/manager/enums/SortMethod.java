package com.mrshiehx.file.manager.enums;

import com.mrshiehx.file.manager.R;

import static com.mrshiehx.file.manager.utils.ResourceUtils.*;

public enum SortMethod {
    BY_NAME("name",getTextByLocale(R.string.file_sort_method_name)),
    BY_DATE("date",getTextByLocale(R.string.file_sort_method_date));

    private final String name;
    private final CharSequence displayName;

    SortMethod(String name, CharSequence displayName){
        this.name=name;
        this.displayName=displayName;
    }

    public String getName() {
        return name;
    }

    public CharSequence getDisplayName() {
        return displayName;
    }
}
