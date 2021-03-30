package com.mrshiehx.file.viewer.activities;

import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mrshiehx.file.manager.utils.ImageUtils;
import com.mrshiehx.file.viewer.activities.base.BaseViewerActivity;
import com.mrshiehx.file.viewer.widgets.ProfessionalImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PictureViewerActivity extends BaseViewerActivity {
    private ImageView image;
    @Override
    protected void init() {
        initViews();
        super.init();
    }

    private void initViews(){
        LinearLayout mainLayout = new LinearLayout(context);
        image = new ProfessionalImageView(context);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        image.setLayoutParams(llp);
        mainLayout.setLayoutParams(llp);
        mainLayout.addView(image);
        setContentView(mainLayout);
    }

    @Override
    protected void loadData(File file) throws IOException, OutOfMemoryError {
        image.setImageDrawable(ImageUtils.inputStream2Drawable(new FileInputStream(file)));
        //if(image.getDrawable()==null) Toast.makeText(context, R.string.message_failed_to_load_picture, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void backNoFinish() {
        finish();
    }
}
