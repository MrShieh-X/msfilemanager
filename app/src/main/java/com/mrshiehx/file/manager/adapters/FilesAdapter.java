package com.mrshiehx.file.manager.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mrshiehx.file.manager.R;
import com.mrshiehx.file.manager.beans.fileItem.AbstractFileItem;
import com.mrshiehx.file.manager.enums.FileType;
import com.mrshiehx.file.manager.utils.SharedPreferencesGetter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FilesAdapter extends ArrayAdapter<AbstractFileItem> {
    private final Context context;
    private final List<AbstractFileItem> files;
    public View convertView;
    public ViewGroup parent;
    public int scrollStatus;
    public static final Map<Integer, Drawable> THUMBNAILS = new HashMap<>();
    private final List<Thread> threadsToBeInterrupted = new LinkedList<>();

    public FilesAdapter(Context context, List<AbstractFileItem> files) {
        super(context, R.layout.file_item, files);
        this.context = context;
        this.files = files;
        Thread thread1, thread2, thread3;
        (thread1 = new Thread(() -> {
            for (AbstractFileItem fileItem : files) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                if (fileItem.getType() == FileType.PICTURE)
                    fileItem.getIcon();
            }
        })).start();
        (thread2 = new Thread(() -> {
            for (AbstractFileItem fileItem : files) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                if (fileItem.getType() == FileType.VIDEO)
                    fileItem.getIcon();
            }
        })).start();
        (thread3 = new Thread(() -> {
            for (AbstractFileItem fileItem : files) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                if (fileItem.getType() == FileType.APK)
                    fileItem.getIcon();
            }
        })).start();
        threadsToBeInterrupted.add(thread1);
        threadsToBeInterrupted.add(thread2);
        threadsToBeInterrupted.add(thread3);
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public AbstractFileItem getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        this.convertView = convertView;
        this.parent = parent;
        AbstractFileItem fileItem = (AbstractFileItem) getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.file_item, null);
            viewHolder = new ViewHolder();
            viewHolder.icon = view.findViewById(R.id.file_icon);
            viewHolder.small_icon = view.findViewById(R.id.file_small_icon);
            viewHolder.name = view.findViewById(R.id.file_name);
            viewHolder.type = view.findViewById(R.id.file_type);
            viewHolder.size = view.findViewById(R.id.file_size);
            viewHolder.date = view.findViewById(R.id.file_date);
            viewHolder.isLink = view.findViewById(R.id.isLink);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }


        //View view = LayoutInflater.from(getContext()).inflate(R.layout.file_item, null);

        /*ImageView icon = view.findViewById(R.id.file_icon);
        ImageView small_icon = view.findViewById(R.id.file_small_icon);
        TextView name = view.findViewById(R.id.file_name);
        TextView type = view.findViewById(R.id.file_type);
        TextView size = view.findViewById(R.id.file_size);
        TextView date = view.findViewById(R.id.file_date);*/
        if (fileItem.isDirectory()) {
            viewHolder.size.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.size.setVisibility(View.VISIBLE);
            viewHolder.size.setText(fileItem.getFormattedFileSize());
        }
        viewHolder.name.setText(fileItem.getFileName());
        viewHolder.type.setText(fileItem.getType().getDisplayName());
        if (fileItem.getSmallIcon() != null) {
            viewHolder.small_icon.setVisibility(View.VISIBLE);
            viewHolder.small_icon.setImageDrawable(fileItem.getSmallIcon());
        } else {
            viewHolder.small_icon.setVisibility(View.GONE);
        }
        if (!fileItem.isBacker()) {
            viewHolder.date.setVisibility(View.VISIBLE);
            viewHolder.date.setText(fileItem.getFormattedModifiedDate(SharedPreferencesGetter.getFileDateFormat()));
        } else {
            viewHolder.date.setVisibility(View.INVISIBLE);
        }


        if (fileItem.getType() == FileType.PICTURE
                || fileItem.getType() == FileType.VIDEO
                || fileItem.getType() == FileType.APK) {
            Drawable a;
            if ((a = THUMBNAILS.get(fileItem.getAbsolutePath().hashCode())) != null) {
                viewHolder.icon.setImageDrawable(a);
                if (scrollStatus == 0 || scrollStatus == 1) {
                    new Thread(() -> {
                        Drawable d = fileItem.getIcon();
                        ((Activity) context).runOnUiThread(() -> viewHolder.icon.setImageDrawable(d));
                    }).start();
                }
            } else if (fileItem.icon != null) {
                viewHolder.icon.setImageDrawable(fileItem.icon);
            } else if (scrollStatus == 0 || scrollStatus == 1) {
                new Thread(() -> {
                    Drawable d = fileItem.getIcon();
                    ((Activity) context).runOnUiThread(() -> viewHolder.icon.setImageDrawable(d));
                }).start();

            } else {
                viewHolder.icon.setImageDrawable(fileItem.getType().getIcon());
            }
        } else {
            viewHolder.icon.setImageDrawable(fileItem.getIcon());
        }

        new Thread(() -> {
            boolean a = fileItem.isSymbolicLink();
            ((Activity) context).runOnUiThread(() -> viewHolder.isLink.setVisibility(a ? View.VISIBLE : View.GONE));
        }).start();


        /*switch (fileItem.getType()){
            case PICTURE:
            case VIDEO:
            case APK:
                viewHolder.icon.setImageDrawable(fileItem.getType().getIcon());
                viewHolder.icon.setImageDrawable(fileItem.getIcon());
                break;
            default:
                viewHolder.icon.setImageDrawable(fileItem.getIcon());
                break;
        }*/
        return view;
    }

    static class ViewHolder {
        ImageView icon;
        ImageView small_icon;
        TextView name;
        TextView type;
        TextView size;
        TextView date;
        TextView isLink;
    }

    public void interruptThreads() {
        for (Thread thread : threadsToBeInterrupted) {
            try {
                thread.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
