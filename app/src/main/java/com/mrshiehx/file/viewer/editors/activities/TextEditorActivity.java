package com.mrshiehx.file.viewer.editors.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.mrshiehx.file.manager.R;
import com.mrshiehx.file.manager.utils.ApplicationUtils;
import com.mrshiehx.file.manager.utils.FileUtils;
import com.mrshiehx.file.manager.utils.Utils;
import com.mrshiehx.file.viewer.activities.base.BaseViewerActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TextEditorActivity extends BaseViewerActivity {
    protected Context context=TextEditorActivity.this;
    private EditText editText;
    private boolean modified=false;
    private boolean firstModify=true;
    @Override
    protected void init() {
        initViews();
        initListeners();
        super.init();
    }

    private void initListeners(){
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!firstModify){
                    modified=true;
                    makeTitleBeModified();
                }
                firstModify=false;
            }
        });
    }

    protected void loadData(File file)throws IOException,OutOfMemoryError{
        editText.setText(new String(FileUtils.toByteArray(file)));
    }

    private void initViews(){
        setContentView(R.layout.activity_text_editor);
        editText=findViewById(R.id.text_editor_edittext);
    }

    private void makeTitleBeModified(){
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            CharSequence charSequence=actionBar.getSubtitle();
            if(!Utils.isEmpty(charSequence)) {
                if (!charSequence.toString().endsWith("*")){
                    actionBar.setSubtitle(charSequence+" *");
                }
            }
        }
    }

    private void removeTitleStar(){
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            CharSequence charSequence=actionBar.getSubtitle();
            if(!Utils.isEmpty(charSequence)) {
                if (charSequence.toString().endsWith(" *")){
                    actionBar.setSubtitle(charSequence.subSequence(0,charSequence.length()-2));
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_text_editor, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_text_editor_save:
                saveFile();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void backNoFinish(){
        if(!modified) {
            finish();
        }else{
            ApplicationUtils.showDialog(context, getText(R.string.dialog_title_notice), String.format(getString(R.string.dialog_text_editor_file_modified_back_message), file.getName()), getText(R.string.action_save_name),getText(android.R.string.no),getText(R.string.action_back_name), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveFile();
                    finish();
                }
            },null, (dialog, which) -> finish(), true);
        }
    }

    private void saveFile(){
        if (file != null) {
            try {
                if (!file.exists()) file.createNewFile();
                FileWriter writer = new FileWriter(file, false);
                writer.write(editText.getText().toString());
                writer.flush();
                writer.close();
                Toast.makeText(context, getString(R.string.message_success_save), Toast.LENGTH_SHORT).show();
                modified=false;
                removeTitleStar();
            } catch (IOException e) {
                e.printStackTrace();
                ApplicationUtils.showDialog(context, getText(R.string.message_failed_to_save_file), e.toString(), null, null, null, null, null, null, true);
            }
        } else {
            Toast.makeText(context, getText(R.string.message_not_opening_file), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        menu.findItem(R.id.action_text_editor_save).setEnabled(file!=null&&modified);
        return super.onMenuOpened(featureId, menu);
    }
}
