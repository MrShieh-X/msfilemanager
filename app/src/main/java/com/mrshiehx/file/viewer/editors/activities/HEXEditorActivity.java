package com.mrshiehx.file.viewer.editors.activities;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.mrshiehx.file.manager.R;
import com.mrshiehx.file.manager.utils.Utils;
import com.mrshiehx.file.viewer.activities.base.BaseViewerActivity;
import com.mrshiehx.file.viewer.adapters.HexItemsAdapter;
import com.mrshiehx.file.viewer.beans.HexItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HEXEditorActivity extends BaseViewerActivity {
    protected Context context = HEXEditorActivity.this;
    private ListView hex;
    private Button b0, b1, b2, b3, b4, b5, b6, b7, b8, b9, ba, bb, bc, bd, be, bf;
    private TextView current;

    @Override
    protected void init() {
        initViews();
        //initData();
        super.init();
    }

    private void initViews() {
        setContentView(R.layout.activity_hex_editor);
        hex = findViewById(R.id.hex_list);
        b0 = findViewById(R.id.hex_0);
        b1 = findViewById(R.id.hex_1);
        b2 = findViewById(R.id.hex_2);
        b3 = findViewById(R.id.hex_3);
        b4 = findViewById(R.id.hex_4);
        b5 = findViewById(R.id.hex_5);
        b6 = findViewById(R.id.hex_6);
        b7 = findViewById(R.id.hex_7);
        b8 = findViewById(R.id.hex_8);
        b9 = findViewById(R.id.hex_9);
        ba = findViewById(R.id.hex_a);
        bb = findViewById(R.id.hex_b);
        bc = findViewById(R.id.hex_c);
        bd = findViewById(R.id.hex_d);
        be = findViewById(R.id.hex_e);
        bf = findViewById(R.id.hex_f);
        hex.setDividerHeight(0);
        initListeners();
    }

    private void initListeners() {
        b0.setOnClickListener((view) -> add((byte) 0x0));
        b1.setOnClickListener((view) -> add((byte) 0x1));
        b2.setOnClickListener((view) -> add((byte) 0x2));
        b3.setOnClickListener((view) -> add((byte) 0x3));
        b4.setOnClickListener((view) -> add((byte) 0x4));
        b5.setOnClickListener((view) -> add((byte) 0x5));
        b6.setOnClickListener((view) -> add((byte) 0x6));
        b7.setOnClickListener((view) -> add((byte) 0x7));
        b8.setOnClickListener((view) -> add((byte) 0x8));
        b9.setOnClickListener((view) -> add((byte) 0x9));
        ba.setOnClickListener((view) -> add((byte) 0xA));
        bb.setOnClickListener((view) -> add((byte) 0xB));
        bc.setOnClickListener((view) -> add((byte) 0xC));
        bd.setOnClickListener((view) -> add((byte) 0xD));
        be.setOnClickListener((view) -> add((byte) 0xE));
        bf.setOnClickListener((view) -> add((byte) 0xF));
    }

    private void add(byte a) {
        byte q = 0xa;
    }

    /*private void initData() {
        String filePath = getIntent().getStringExtra("filePath");
        if (!Utils.isEmpty(filePath)) {
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
    }*/

    @Override
    protected void backNoFinish() {
        /**code*/
        finish();
    }

    protected void loadData(File file) throws IOException,OutOfMemoryError {
        FileInputStream fileInputStream = new FileInputStream(file);
        String str = Utils.readHex(fileInputStream);
        System.out.println(str);
        int m = str.length() / 2;
        if (m * 2 < str.length()) {
            m++;
        }
        int[] var1 = new int[m];
        String[] strs = new String[m];
        int j = 0;
        for (int i = 0; i < str.length(); i++) {
            if (i % 2 == 0) {
                strs[j] = "" + str.charAt(i);
            } else {
                strs[j] = strs[j] + "" + str.charAt(i);
                j++;
            }
        }
        for (int i = 0; i < strs.length; i++) {
            String stri = strs[i];
            if (stri.length() != 2) {
                stri += "0";
            }
            var1[i] = Integer.parseInt(stri, 16);
        }
        /*StringBuilder builder=new StringBuilder();

        for(int i:var1){
            builder.append(Integer.toHexString(i));
            builder.append("/");
        }
        System.out.println(builder.toString());
*/
        List<HexItem> items = new ArrayList<>();

        for (int i = 0; i < var1.length; i=i+8) {
            int var21 = var1[i];
            int var22 = HexItem.NULL;
            int var23 = HexItem.NULL;
            int var24 = HexItem.NULL;
            int var25 = HexItem.NULL;
            int var26 = HexItem.NULL;
            int var27 = HexItem.NULL;
            int var28 = HexItem.NULL  ;
            if (i + 1 <= var1.length-1) {
                var22 = var1[i+ 1];
            }
            if (i + 2 <= var1.length-1) {
                var23 = var1[i+ 2];
            }
            if (i + 3 <= var1.length-1) {
                var24 = var1[i+3];
            }
            if (i + 4 <= var1.length-1) {
                var25 = var1[i+4];
            }
            if (i + 5 <= var1.length-1) {
                var26 = var1[i+5];
            }
            if (i + 6 <= var1.length-1) {
                var27 = var1[i+6];
            }
            if (i + 7 <= var1.length-1) {
                var28 = var1[i+7];
            }
            items.add(new HexItem(var21, var22, var23, var24, var25, var26, var27, var28));
        }
        HexItemsAdapter hexItemsAdapter = new HexItemsAdapter(context, items);
        hex.setAdapter(hexItemsAdapter);
    }

    private void saveFile(){
        /**code*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_hex_editor, menu);
        return true;
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
}