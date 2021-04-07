package com.mrshiehx.file.manager.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mrshiehx.file.manager.R;
import com.mrshiehx.file.manager.activities.base.BaseActivity;
import com.mrshiehx.file.manager.adapters.FilesAdapter;
import com.mrshiehx.file.manager.application.MSFMApplication;
import com.mrshiehx.file.manager.beans.FileItem;
import com.mrshiehx.file.manager.beans.Permission;
import com.mrshiehx.file.manager.enums.SortMethod;
import com.mrshiehx.file.manager.file.operations.AfterGoToDialog;
import com.mrshiehx.file.manager.file.operations.FileOperations;
import com.mrshiehx.file.manager.file.operations.FileOperationsDialogs;
import com.mrshiehx.file.manager.interfaces.Void;
import com.mrshiehx.file.manager.shared.variables.Commands;
import com.mrshiehx.file.manager.shared.variables.FilePaths;
import com.mrshiehx.file.manager.shared.variables.SharedVariables;
import com.mrshiehx.file.manager.utils.FileUtils;
import com.mrshiehx.file.manager.utils.SharedPreferencesGetter;
import com.mrshiehx.file.manager.utils.SystemUtils;
import com.mrshiehx.file.manager.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileManagerActivity extends BaseActivity {
    protected Context context= FileManagerActivity.this;
    private ListView filesListView;
    private FloatingActionButton createFolder;
    private FloatingActionButton createFile;
    private FloatingActionMenu fab_buttons;
    private Toolbar toolbar;
    private TextView title;

    private File currentFile;
    private SortMethod sortMethod;
    private int sortMethodNumber;
    private Map<File,String> position;
    private File copingFile;
    private File movingFile;

    boolean ps;
    long firstTime;
    
    private final int REQUEST_CODE_GET_PERMISSION=100;
    private final int REQUEST_CODE_GO_TO_SETTINGS=1000;
    //private final int FILE_ACTION_OPEN_NUMBER=0;
    private final int FILE_ACTION_CHOOSE_OPEN_METHOD_NUMBER=0;
    private final int FILE_ACTION_RENAME_NUMBER=1;
    private final int FILE_ACTION_COPY_NUMBER=2;
    private final int FILE_ACTION_MOVE_NUMBER=3;
    private final int FILE_ACTION_DELETE_NUMBER=4;
    private final int FILE_ACTION_ATTRIBUTES_NUMBER=5;

    AlertDialog.Builder dialog_no_permissions;
    AlertDialog dialog_no_permissions_dialog;

    protected Runtime runtime;
    protected void init(){
        setContentView(R.layout.activity_file_manager);
        initVariables();
        initViews();
        initSortMethod();
        initCurrentFile();
        initListeners();
        getPermission(new Permission(SharedVariables.getPermissions()[0]));
        setTitle(currentFile.getAbsolutePath());
        getRoot();
    }

    void initVariables(){
        runtime=Runtime.getRuntime();
        position=new HashMap<>();
/*
        try {
            runtime.exec("su");
            //runtime.exec("cd /");
            String result="";
            Process a= runtime.exec("ls /data");
            InputStream is = a.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader mReader = new BufferedReader(isr);
            String string;
            while ((string = mReader.readLine()) != null) {
                result = result + string + "\n";
            }
            new AlertDialog.Builder(context).setMessage(result).show();
        }catch (Exception e){
            e.printStackTrace();
        }*/
    }

    void getRoot(){
        if(SharedPreferencesGetter.getGetRoot()){
            try {
                runtime.exec(Commands.getSuperUserCommand().getArguments()[0]);
                initFiles(currentFile);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, getText(R.string.message_failed_to_get_root), Toast.LENGTH_SHORT).show();
            }
        }
    }

    void initSortMethod(){
        sortMethod=SortMethod.BY_NAME;
        if(SharedPreferencesGetter.getSortMethod().equals(SortMethod.BY_DATE.getName())){
            sortMethod=SortMethod.BY_DATE;
        }
    }

    void initCurrentFile(){
        SharedPreferences sharedPreferences=getSharedPreferences();
        String home=sharedPreferences.getString("home", FilePaths.getSdcard().getAbsolutePath());
        if(sharedPreferences.getString("startupDir","home").equals("last")) {
            currentFile = new File(sharedPreferences.getString("lastPath", home));
        }else{
            currentFile=new File(home);
        }
        setTitle(currentFile.getAbsolutePath());
    }

    void initViews(){
        filesListView=findViewById(R.id.files);
        createFolder=findViewById(R.id.create_folder);
        createFile=findViewById(R.id.create_file);
        toolbar=findViewById(R.id.toolbar);
        title=findViewById(R.id.title);
        fab_buttons=findViewById(R.id.fab_buttons);

        super.setTitle("");
        setSupportActionBar(toolbar);

        filesListView.setDividerHeight(0);
    }

    void initFiles(File fileF) {
        List<FileItem> folders = new ArrayList<>();
        List<FileItem> files = new ArrayList<>();
        if (!fileF.getAbsolutePath().equals(FilePaths.getRootPath())) {
            FileItem backer = new FileItem(new File(".."));
            backer.setIsBacker(true);
            folders.add(backer);
        }
        boolean showHiddenFiles = getSharedPreferences().getBoolean("showHiddenFiles", false);
        String sort = SharedPreferencesGetter.getSortMethod();
        String[] listArray = fileF.list();

        if (listArray != null) {
            if (sort.equals(SortMethod.BY_DATE.getName())) {
                Map<File, Long> foldersMap = new HashMap<>();
                Map<File, Long> filesMap = new HashMap<>();
                try {
                    for (String s : listArray) {
                        File file = new File(fileF, s);

                        if (file.getName().startsWith(".")) {
                            if (showHiddenFiles) {
                                if (file.isFile()) {
                                    filesMap.put(file, file.lastModified());
                                } else {
                                    foldersMap.put(file, file.lastModified());
                                }
                            }
                        } else {
                            if (file.isFile()) {
                                filesMap.put(file, file.lastModified());
                            } else {
                                foldersMap.put(file, file.lastModified());
                            }
                        }
                    }

                    /**这是反的*/

                    List<Map.Entry<File, Long>> entryList1 = new ArrayList<>(foldersMap.entrySet());
                    Collections.sort(entryList1, new Comparator<Map.Entry<File, Long>>() {
                        @Override
                        public int compare(Map.Entry<File, Long> e1, Map.Entry<File, Long> e2) {
                            int re = e2.getValue().compareTo(e1.getValue());
                            if (re != 0) {
                                return re;
                            } else {
                                return e2.getKey().compareTo(e1.getKey());
                            }
                        }
                    });


                    List<Map.Entry<File, Long>> entryList2 = new ArrayList<>(filesMap.entrySet());
                    Collections.sort(entryList2, new Comparator<Map.Entry<File, Long>>() {
                        @Override
                        public int compare(Map.Entry<File, Long> e1, Map.Entry<File, Long> e2) {
                            int re = e2.getValue().compareTo(e1.getValue());
                            if (re != 0) {
                                return re;
                            } else {
                                return e2.getKey().compareTo(e1.getKey());
                            }
                        }
                    });

                    List<File> foldersFinal = new ArrayList<>();
                    List<File> filesFinal = new ArrayList<>();

                    /**顺序调转*/
                    for (int i = 0; i < entryList1.size(); i++) {
                        foldersFinal.add(entryList1.get(entryList1.size() - 1 - i).getKey());
                    }
                    for (int i = 0; i < entryList2.size(); i++) {
                        filesFinal.add(entryList2.get(entryList2.size() - 1 - i).getKey());
                    }

                    /*List<Map.Entry<File,Long>> entryList2 = new ArrayList<>(filesMap.entrySet());
                    Collections.sort(entryList2, new Comparator<Map.Entry<File,Long>>() {
                        @Override
                        public int compare(Map.Entry<File,Long> me1, Map.Entry<File,Long> me2) {
                            return me1.getValue().compareTo(me2.getValue());
                        }
                    });*/

                    foldersFinal.addAll(filesFinal);
                    for (int i = 0; i < foldersFinal.size(); i++) {
                        folders.add(new FileItem(foldersFinal.get(i)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, R.string.message_failed_to_list_files, Toast.LENGTH_SHORT).show();
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.message_failed_to_list_files)
                            .setMessage(e.toString())
                            .show();
                }
            } else {
                try {
                    List<String> list = Arrays.asList(listArray);
                    Collections.sort(list, String::compareToIgnoreCase);
                    for (String path : list) {
                        File file = new File(fileF, path);
                        if (file.getName().startsWith(".")) {
                            if (showHiddenFiles) {
                                if (file.isDirectory()) {
                                    folders.add(new FileItem(file));
                                } else {
                                    files.add(new FileItem(file));
                                }
                            }
                        } else {
                            if (file.isDirectory()) {
                                folders.add(new FileItem(file));
                            } else {
                                files.add(new FileItem(file));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, R.string.message_failed_to_list_files, Toast.LENGTH_SHORT).show();
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.message_failed_to_list_files)
                            .setMessage(e.toString())
                            .show();
                }
            }
        } else {
            if (fileF.getAbsolutePath().equals(FilePaths.getRootPath())) {
                String sdcard = FilePaths.getSdcard().getAbsolutePath();
                String system = FilePaths.getSystemPart().getAbsolutePath();
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle(R.string.dialog_title_notice);
                dialog.setMessage(String.format(getString(R.string.dialog_higher_version_os_unaccessable_root_directory_message), system, sdcard));
                dialog.setPositiveButton(R.string.dialog_higher_version_os_unaccessable_root_directory_goto_sdcard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        goToFile(sdcard);
                    }
                });
                dialog.setNeutralButton(R.string.dialog_higher_version_os_unaccessable_root_directory_goto_system, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        goToFile(system);
                    }
                });
                dialog.show();
            }
        }
        folders.addAll(files);

        FilesAdapter filesAdapter = new FilesAdapter(context, folders);
        //View c = filesAdapter.convertView;
        //ViewGroup g = filesAdapter.parent;
        filesListView.setAdapter(filesAdapter);
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < files.size(); i++) {
                    if (files.get(i).getType() == FileType.APK || files.get(i).getType() == FileType.PICTURE || files.get(i).getType() == FileType.VIDEO) {
                        int finalI = i;
                        runOnUiThread(() -> {
                            ((ImageView) filesAdapter.getView(finalI, c, g).findViewById(R.id.file_icon)).setImageDrawable(files.get(finalI).getIcon());
                            filesListView.setAdapter(filesAdapter);
                        });
                    }
                }
            }
        }).start();*/
        setTitle(fileF.getAbsolutePath());
        initPosition();
    }

    void initListeners(){
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGoToFileDialog();
            }
        });
        title.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SystemUtils.copy(getTitleText());
                Toast.makeText(context, getText(R.string.message_success_copy_current_dir), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        filesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileItem item=(FileItem)filesListView.getItemAtPosition(position);
                File file=item.getFile();
                if(file.isDirectory()) {
                    if (item.isBacker()) {
                        back();
                        //goToFile(currentFile.getParentFile().getAbsolutePath());
                        //initFiles(currentFile);
                    } else {
                        goToFile(file.getAbsolutePath());
                        //initFiles(file);
                    }
                }else{
                    FileOperations.openFile(context,item,file);
                }
            }
        });
        createFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_buttons.close(true);
                showCreateFolderDialog();
            }
        });
        createFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_buttons.close(true);
                showCreateFileDialog();
            }
        });


        filesListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                //menu.add(0, FILE_ACTION_OPEN_NUMBER, 0, getText(R.string.file_action_open));
                menu.add(0, FILE_ACTION_CHOOSE_OPEN_METHOD_NUMBER, 0, getText(R.string.file_action_choose_open_method));
                menu.add(0, FILE_ACTION_RENAME_NUMBER, 0, getText(R.string.file_action_rename));
                menu.add(0, FILE_ACTION_COPY_NUMBER, 0, getText(R.string.file_action_copy));
                menu.add(0, FILE_ACTION_MOVE_NUMBER, 0, getText(R.string.file_action_move));
                menu.add(0, FILE_ACTION_DELETE_NUMBER, 0, getText(R.string.file_action_delete));
                menu.add(0, FILE_ACTION_ATTRIBUTES_NUMBER, 0, getText(R.string.file_action_attribute));
            }
        });
    }

    void showGoToFileDialog() {
        FileOperationsDialogs.showGoToFileDialog(context, currentFile, new AfterGoToDialog() {
            @Override
            public void clickedYes(File currentFile) {
                goToFile(currentFile.getAbsolutePath());
                //MainActivity.this.currentFile = currentFile;
                //initFiles(currentFile);
            }
        });
    }

    void showCreateFolderDialog(){
        FileOperationsDialogs.showCreateFolderDialog(context, currentFile, () -> initFiles(currentFile));
    }

    void showCreateFileDialog(){
        FileOperationsDialogs.showCreateFileDialog(context, currentFile, () -> initFiles(currentFile));
    }

    void putPosition(){
        int index = filesListView.getFirstVisiblePosition();
        View v = filesListView.getChildAt(0);
        int top = (v == null) ? 0 : v.getTop();
        position.put(currentFile,index+"/"+top);
    }

    void goToFile(String path){
        File newFile=new File(path);
        if(!newFile.exists()){
            new AlertDialog.Builder(context)
                    .setTitle(R.string.dialog_title_notice)
                    .setMessage(String.format(getString(R.string.message_go_to_file_not_exists),path))
                    .show();
            return;
        }
        if(newFile.isFile()){
            new AlertDialog.Builder(context)
                    .setTitle(R.string.dialog_title_notice)
                    .setMessage(String.format(getString(R.string.message_go_to_file_is_file),path))
                    .show();
            return;
        }

        putPosition();
        currentFile=newFile;
        initFiles(currentFile);
        setTitle(path);
        getSharedPreferences().edit().putString("lastPath",path).apply();
    }

    void getPermission(Permission permission){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ps = shouldShowRequestPermissionRationale(permission.getName());
        }
        int WRITE_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(context, permission.getName());
        if (WRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            startRequestPermission();
        }
    }

    void startRequestPermission(){
        if(!ps) {
            ActivityCompat.requestPermissions(this, SharedVariables.getPermissions(), REQUEST_CODE_GET_PERMISSION);
        }else{
            showDialogTipUserGoToAppSettting();
        }
    }

    void back(){
        if(!Utils.isEmpty(currentFile.getParent())&&!currentFile.getAbsolutePath().equals(FilePaths.getRootPath())) {
            /*int index=0;
            int top=0;
            try {
                String s = position.get(currentFile.getParentFile());
                if (!Utils.isEmpty(s)) {
                    String[] ss = s.split("/");
                    index = Integer.parseInt(ss[0]);
                    top = Integer.parseInt(ss[1]);
                }
            }catch (Throwable ignore){}*/

            File oldCurrentFile=currentFile;
            goToFile(currentFile.getParent());
            position.remove(oldCurrentFile);
            initPosition();
            //filesListView.setSelectionFromTop(index, top);
        }else{
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                Toast.makeText(this, getResources().getText(R.string.message_press_again_exit_application), Toast.LENGTH_SHORT).show();
                firstTime = secondTime;
            } else {
                MSFMApplication.getInstance().exit();
            }
        }
    }

    private void goToAppSetting() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_CODE_GO_TO_SETTINGS);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem menuItem) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
        final long selectedId=info.id;
        FileItem item = (FileItem) filesListView.getItemAtPosition((int) selectedId);
        File file=item.getFile();
        switch (menuItem.getItemId()) {
            /*case FILE_ACTION_OPEN_NUMBER:
                if(!item.isBacker()){
                    if(file.isFile()){
                        FileOperations.openFile(context,item,file);
                    }else{
                        goToFile(file.getAbsolutePath());
                    }
                }else{
                    back();
                }
                return true;*/
            case FILE_ACTION_CHOOSE_OPEN_METHOD_NUMBER:
                if(!item.isBacker()) {
                    if(file.isFile()){
                        FileOperationsDialogs.showOpenMethodDialog(context,file);
                    }else{
                        Toast.makeText(context, R.string.message_unsupported_operation, Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(context, R.string.message_unsupported_operation, Toast.LENGTH_SHORT).show();
                }
                return true;
            case FILE_ACTION_RENAME_NUMBER:
                if(!item.isBacker()){
                    FileOperationsDialogs.showRenameDialog(context, file, () -> initFiles(currentFile));
                }else{
                    Toast.makeText(context, R.string.message_unsupported_operation, Toast.LENGTH_SHORT).show();
                }
                return true;
            case FILE_ACTION_COPY_NUMBER:
                if(!item.isBacker()){
                    copingFile=file;
                    movingFile=null;
                }else{
                    Toast.makeText(context, R.string.message_unsupported_operation, Toast.LENGTH_SHORT).show();
                }
                return true;
            case FILE_ACTION_MOVE_NUMBER:
                if(!item.isBacker()){
                    movingFile=file;
                    copingFile=null;
                }else{
                    Toast.makeText(context, R.string.message_unsupported_operation, Toast.LENGTH_SHORT).show();
                }
                return true;
            case FILE_ACTION_DELETE_NUMBER:
                if(!item.isBacker()){
                    FileOperationsDialogs.showDeleteFileDialog(context,file, new Void() {
                        @Override
                        public void execute() {
                            initFiles(currentFile);
                        }
                    });
                }else{
                    Toast.makeText(context, R.string.message_unsupported_operation, Toast.LENGTH_SHORT).show();
                }
                return true;
            case FILE_ACTION_ATTRIBUTES_NUMBER:
                if(!item.isBacker()){
                    FileOperationsDialogs.showAttributesDialog(context,item,file);
                }else{
                    Toast.makeText(context, R.string.message_unsupported_operation, Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onContextItemSelected(menuItem);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE_GET_PERMISSION){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    boolean b = shouldShowRequestPermissionRationale(permissions[0]);
                    if (b) {
                        showDialogTipUserGoToAppSettting();
                    } else {
                        MSFMApplication.getInstance().exit();
                    }
                }
            }
        }
    }
    private void showDialogTipUserGoToAppSettting() {
        dialog_no_permissions = new AlertDialog.Builder(this);
        dialog_no_permissions.setTitle(getText(R.string.dialog_no_permissions_title))
                .setMessage(getText(R.string.dialog_no_permissions_message))
                .setPositiveButton(getText(R.string.dialog_no_permissions_button_gotosettings), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToAppSetting();
                    }
                }).setNegativeButton(getText(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MSFMApplication.getInstance().exit();
            }
        }).setCancelable(false);
        dialog_no_permissions_dialog=dialog_no_permissions.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GO_TO_SETTINGS) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int i = ContextCompat.checkSelfPermission(context, SharedVariables.getPermissions()[0]);
                if (i != PackageManager.PERMISSION_GRANTED) {
                    showDialogTipUserGoToAppSettting();
                } else {
                    if (dialog_no_permissions_dialog != null&&dialog_no_permissions_dialog.isShowing()) {
                        dialog_no_permissions_dialog.dismiss();
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSharedPreferences().edit().putString("lastPath",currentFile.getAbsolutePath()).apply();
        putPosition();

    }

    void initPosition(){
        if(position.containsKey(currentFile)){
            int index=0;
            int top=0;
            try {
                String s = position.get(currentFile);
                if (!Utils.isEmpty(s)) {
                    String[] ss = s.split("/");
                    index = Integer.parseInt(ss[0]);
                    top = Integer.parseInt(ss[1]);
                }
            }catch (Throwable ignore){}
            filesListView.setSelectionFromTop(index, top);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initFiles(currentFile);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
        }else{
            super.onKeyDown(keyCode, event);
        }
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        MenuItem sort=menu.findItem(R.id.action_sort_method);
        MenuItem paste=menu.findItem(R.id.action_paste);
        //initSortMethod();
        if(sort!=null)
            sort.setTitle(String.format(getString(R.string.action_sort_method),sortMethod.getDisplayName()));
        if(paste!=null)
            paste.setEnabled((copingFile!=null&&!copingFile.getParentFile().equals(currentFile))||(movingFile!=null&&!movingFile.getParentFile().equals(currentFile)));
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_exit:
                new AlertDialog.Builder(this)
                        .setTitle(getText(R.string.dialog_title_notice))
                        .setMessage(getText(R.string.dialog_exit_message))
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> MSFMApplication.getInstance().exit()).show();
                break;
            /*case R.id.action_about:
                showAboutDialog();
                break;*/
            case R.id.action_back:
                back();
                break;
            case R.id.action_create_a_folder:
                showCreateFolderDialog();
                break;
            case R.id.action_create_a_file:
                showCreateFileDialog();
                break;
            case R.id.action_sort_method:
                SortMethod[] items = {SortMethod.BY_NAME, SortMethod.BY_DATE};
                CharSequence[] itemNames = {SortMethod.BY_NAME.getDisplayName(), SortMethod.BY_DATE.getDisplayName()};
                if (sortMethod == items[0]) {
                    sortMethodNumber = 0;
                } else {
                    sortMethodNumber = 1;
                }
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle(getText(R.string.dialog_sort_method_title));
                dialog.setSingleChoiceItems(itemNames, sortMethodNumber, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sortMethodNumber = which;
                    }
                });
                dialog.setPositiveButton(getText(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (sortMethodNumber != -1) {
                            sortMethod = items[sortMethodNumber];
                        }/*
                        if (which == 1) {
                            sortMethod = SortMethod.BY_DATE;
                        } else {
                            sortMethod = SortMethod.BY_NAME;
                        }*/
                        getSharedPreferences().edit().putString("sort", sortMethod.getName()).apply();
                        initFiles(currentFile);
                    }
                });
                dialog.show();
                break;
            case R.id.action_paste:
                if(copingFile!=null){
                    if (!copingFile.getParentFile().equals(currentFile)) {
                        if (copingFile.exists()) {
                            FileOperationsDialogs.showCopyDialog(context, copingFile, currentFile, () -> {
                                copingFile = null;
                                initFiles(currentFile);
                            });
                        } else {
                            Toast.makeText(context, R.string.message_target_file_not_exists, Toast.LENGTH_SHORT).show();
                            copingFile=null;
                        }
                    } else {
                        Toast.makeText(context, getText(R.string.message_paste_to_the_same_directory), Toast.LENGTH_SHORT).show();
                    }
                }else if(movingFile!=null){
                    if (!movingFile.getParentFile().equals(currentFile)) {
                        if (movingFile.exists()) {
                            FileOperationsDialogs.showMoveDialog(context, movingFile, currentFile, () -> {
                                if(movingFile.isDirectory()) FileUtils.deleteDirectory(movingFile);
                                else movingFile.delete();
                                movingFile = null;
                                initFiles(currentFile);
                            });
                        } else {
                            Toast.makeText(context, R.string.message_target_file_not_exists, Toast.LENGTH_SHORT).show();
                            movingFile = null;
                        }
                    } else {
                        Toast.makeText(context, getText(R.string.message_paste_moved_to_the_same_directory), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(context, getText(R.string.message_no_pasteable_file), Toast.LENGTH_SHORT).show();
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title){
        this.title.setText(title);
    }

    public CharSequence getTitleText() {
        return this.title.getText();
    }
}
/**useless code*/
/**void showAboutDialog(){
 new AlertDialog.Builder(this)
 .setTitle(String.format(getString(R.string.dialog_about_title), ApplicationUtils.getVersionName(),ApplicationUtils.getVersionCode()))
 .setMessage(R.string.dialog_about_message)
 .setPositiveButton(R.string.dialog_about_visit_github_of_this_application, new DialogInterface.OnClickListener() {
@Override
public void onClick(DialogInterface dialog, int which) {
Utils.goToWebsite(context,"https://github.com/MrShieh-X/msfilemanager");
}
})
 .setNegativeButton(R.string.dialog_about_visit_github_of_author, new DialogInterface.OnClickListener() {
@Override
public void onClick(DialogInterface dialog, int which) {
Utils.goToWebsite(context,"https://github.com/MrShieh-X");
}
})
 .setNeutralButton(R.string.dialog_about_visit_msxw, new DialogInterface.OnClickListener() {
@Override
public void onClick(DialogInterface dialog, int which) {
Utils.goToWebsite(context,"https://mrshieh-x.github.io");
}
}).show();
 }
 */