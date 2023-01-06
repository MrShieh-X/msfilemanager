package com.mrshiehx.file.manager.enums;

import static com.mrshiehx.file.manager.utils.ResourceUtils.getTextByLocale;

import com.mrshiehx.file.manager.R;
import com.mrshiehx.file.manager.beans.fileItem.AbstractFileItem;

import java.util.Comparator;

public enum SortMethod {
    BY_NAME("name", getTextByLocale(R.string.file_sort_method_name), (o1, o2) -> o1.getFileName().compareToIgnoreCase(o2.getFileName())),
    BY_DATE("date", getTextByLocale(R.string.file_sort_method_date), (o1, o2) -> Long.compare(o1.getModifiedDate(), o2.getModifiedDate())),
    BY_NAME_REVERSED("nameR", getTextByLocale(R.string.file_sort_method_name_reversed), (o1, o2) -> o2.getFileName().compareToIgnoreCase(o1.getFileName())),
    BY_DATE_REVERSED("dateR", getTextByLocale(R.string.file_sort_method_date_reversed), (o1, o2) -> Long.compare(o2.getModifiedDate(), o1.getModifiedDate())),
    BY_SIZE("size", getTextByLocale(R.string.file_sort_method_size), (o1, o2) -> Long.compare(o1.getFileSize(), o2.getFileSize())),
    BY_SIZE_REVERSED("sizeR", getTextByLocale(R.string.file_sort_method_size_reversed), (o1, o2) -> Long.compare(o2.getFileSize(), o1.getFileSize()));

    private final String name;
    private final CharSequence displayName;
    public final Comparator<AbstractFileItem> comparator;

    SortMethod(String name, CharSequence displayName, Comparator<AbstractFileItem> comparator) {
        this.name = name;
        this.displayName = displayName;
        this.comparator = comparator;
    }

    public String getName() {
        return name;
    }

    public CharSequence getDisplayName() {
        return displayName;
    }

    public static SortMethod valuesOf(String name, SortMethod defaultReturning) {
        for (SortMethod sortMethod : values()) {
            if (sortMethod.name.equalsIgnoreCase(name)) {
                return sortMethod;
            }
        }
        return defaultReturning;
    }
}
