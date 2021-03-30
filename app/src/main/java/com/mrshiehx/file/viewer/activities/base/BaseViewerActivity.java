package com.mrshiehx.file.viewer.activities.base;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.mrshiehx.file.manager.R;
import com.mrshiehx.file.manager.activities.base.BaseActivity;
import com.mrshiehx.file.manager.utils.Utils;

import java.io.File;
import java.io.IOException;

public abstract class BaseViewerActivity extends BaseActivity {
    protected Context context=BaseViewerActivity.this;
    protected File file;
    @Override
    protected void init() {
        ProgressDialog loading = new ProgressDialog(context);
        loading.setMessage(getText(R.string.dialog_hex_editor_loading_file));
        loading.setCancelable(false);
        String filePath = getIntent().getStringExtra("filePath");
        if(getIntent().getBooleanExtra("backButton",false)){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        Uri uri = intent.getData();
        boolean var=!Utils.isEmpty(filePath);
        if (var) {
            loading.show();
            file = new File(filePath);
            getSupportActionBar().setSubtitle(file.getName());
            if (file.exists()) {

                new Thread(() -> {
                    Looper.prepare();
                    runOnUiThread(() -> {
                        try {
                            this.loadData(file);
                            loading.dismiss();
                        } catch (IOException e) {
                            e.printStackTrace();
                            loading.dismiss();
                            new AlertDialog.Builder(context)
                                    .setTitle(R.string.message_hex_editor_failed_to_read_file)
                                    .setMessage(e.toString())
                                    .show();
                        }catch (OutOfMemoryError e){
                            e.printStackTrace();
                            loading.dismiss();
                            new AlertDialog.Builder(context)
                                    .setTitle(R.string.message_hex_editor_failed_to_read_file)
                                    .setMessage(String.format(getString(R.string.message_exception_out_of_memory),e.toString()))
                                    .show();
                        }
                    });
                    Looper.loop();
                }).start();

            } else {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.dialog_title_notice)
                        .setMessage(String.format(getString(R.string.dialog_hex_editor_file_not_exists), file.getAbsolutePath()))
                        .show();

            }
        }
    }

    protected abstract void loadData(File file)throws IOException,OutOfMemoryError;
    protected abstract void backNoFinish();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            backNoFinish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backNoFinish();
        }else{
            super.onKeyDown(keyCode, event);
        }
        return true;
    }
}
