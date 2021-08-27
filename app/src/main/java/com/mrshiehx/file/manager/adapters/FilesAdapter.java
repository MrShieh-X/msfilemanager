package com.mrshiehx.file.manager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mrshiehx.file.manager.R;
import com.mrshiehx.file.manager.beans.FileItem;
import com.mrshiehx.file.manager.enums.FileType;
import com.mrshiehx.file.manager.utils.SharedPreferencesGetter;

import java.io.File;
import java.util.List;

public class FilesAdapter extends ArrayAdapter<FileItem> {
    private final Context context;
    private final List<FileItem> files;
    public View convertView;
    public ViewGroup parent;
    public int scrollStatus;

    public FilesAdapter(Context context, List<FileItem> files) {
        super(context,R.layout.file_item,files);
        this.context=context;
        this.files = files;
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public FileItem getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        this.convertView=convertView;
        this.parent=parent;
        FileItem fileItem = (FileItem) getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView==null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.file_item, null);
            viewHolder=new ViewHolder();
            viewHolder.icon = view.findViewById(R.id.file_icon);
            viewHolder.small_icon = view.findViewById(R.id.file_small_icon);
            viewHolder.name = view.findViewById(R.id.file_name);
            viewHolder.type = view.findViewById(R.id.file_type);
            viewHolder.size = view.findViewById(R.id.file_size);
            viewHolder.date = view.findViewById(R.id.file_date);
            view.setTag(viewHolder);
        }else{
            view=convertView;
            viewHolder=(ViewHolder)view.getTag();
        }



        //View view = LayoutInflater.from(getContext()).inflate(R.layout.file_item, null);

        File file=fileItem.getFile();

        /*ImageView icon = view.findViewById(R.id.file_icon);
        ImageView small_icon = view.findViewById(R.id.file_small_icon);
        TextView name = view.findViewById(R.id.file_name);
        TextView type = view.findViewById(R.id.file_type);
        TextView size = view.findViewById(R.id.file_size);
        TextView date = view.findViewById(R.id.file_date);*/
        if(file.isDirectory()){
            viewHolder.size.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.size.setVisibility(View.VISIBLE);
            viewHolder.size.setText(fileItem.getFormattedFileSize());
        }
        viewHolder.name.setText(fileItem.getFileName());
        viewHolder.type.setText(fileItem.getType().getDisplayName());
        if(fileItem.getSmallIcon()!=null){
            viewHolder.small_icon.setVisibility(View.VISIBLE);
            viewHolder.small_icon.setImageDrawable(fileItem.getSmallIcon());
        }else{
            viewHolder.small_icon.setVisibility(View.GONE);
        }
        if(!fileItem.isBacker()) {
            viewHolder.date.setVisibility(View.VISIBLE);
            viewHolder.date.setText(fileItem.getFormattedModifiedDate(SharedPreferencesGetter.getFileDateFormat()));
        }else{
            viewHolder.date.setVisibility(View.INVISIBLE);
        }
        if(fileItem.getType()== FileType.PICTURE
                ||fileItem.getType()== FileType.VIDEO
                ||fileItem.getType()== FileType.APK) {
            if (fileItem.picture != null) {
                viewHolder.icon.setImageDrawable(fileItem.picture);
            } else if (scrollStatus == 0 || scrollStatus == 1) {
                viewHolder.icon.setImageDrawable(fileItem.getIcon());
            } else {
                viewHolder.icon.setImageDrawable(fileItem.getType().getIcon());
            }
        }else{
            viewHolder.icon.setImageDrawable(fileItem.getIcon());
        }
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

    class ViewHolder{
        ImageView icon;
        ImageView small_icon;
        TextView name;
        TextView type;
        TextView size;
        TextView date;
    }
}
