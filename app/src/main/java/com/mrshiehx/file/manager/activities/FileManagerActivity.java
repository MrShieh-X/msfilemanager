package com.mrshiehx.file.manager.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mrshiehx.file.manager.R;
import com.mrshiehx.file.manager.activities.base.BaseActivity;
import com.mrshiehx.file.manager.adapters.FilesAdapter;
import com.mrshiehx.file.manager.application.MSFMApplication;
import com.mrshiehx.file.manager.beans.Permission;
import com.mrshiehx.file.manager.beans.fileItem.AbstractFileItem;
import com.mrshiehx.file.manager.beans.fileItem.FileItem;
import com.mrshiehx.file.manager.beans.fileItem.RootFileItem;
import com.mrshiehx.file.manager.enums.SortMethod;
import com.mrshiehx.file.manager.file.operations.AfterGoToDialog;
import com.mrshiehx.file.manager.file.operations.FileOperations;
import com.mrshiehx.file.manager.file.operations.FileOperationsDialogs;
import com.mrshiehx.file.manager.shared.variables.FilePaths;
import com.mrshiehx.file.manager.shared.variables.SharedVariables;
import com.mrshiehx.file.manager.utils.BytesUtils;
import com.mrshiehx.file.manager.utils.PathUtils;
import com.mrshiehx.file.manager.utils.SharedPreferencesGetter;
import com.mrshiehx.file.manager.utils.ShellUtils;
import com.mrshiehx.file.manager.utils.StatParser;
import com.mrshiehx.file.manager.utils.SystemUtils;
import com.mrshiehx.file.manager.utils.Utils;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FileManagerActivity extends BaseActivity {
    protected Context context = FileManagerActivity.this;
    protected Activity activity = FileManagerActivity.this;
    private ListView filesListView;
    private FloatingActionButton createFolder;
    private FloatingActionButton createFile;
    private FloatingActionMenu fab_buttons;
    private Toolbar toolbar;
    private TextView title;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageButton warning;

    private File currentFile;
    private SortMethod sortMethod;
    private int sortMethodNumber;
    private Map<File, String> position;
    private AbstractFileItem copingFile;
    private AbstractFileItem movingFile;
    private int accessMode;//0为一般情况，1为使用Shell.su，0为不能进行任何操作
    private final Map<File, Boolean> interrupts = new HashMap<>();
    //private Thread copyingThread;

    boolean ps;
    long firstTime;

    private final int REQUEST_CODE_GET_PERMISSION = 100;
    private final int REQUEST_CODE_GO_TO_SETTINGS = 1000;
    //private final int FILE_ACTION_OPEN_NUMBER=0;
    private final int FILE_ACTION_CHOOSE_OPEN_METHOD_NUMBER = 0;
    private final int FILE_ACTION_RENAME_NUMBER = 1;
    private final int FILE_ACTION_COPY_NUMBER = 2;
    private final int FILE_ACTION_MOVE_NUMBER = 3;
    private final int FILE_ACTION_DELETE_NUMBER = 4;
    private final int FILE_ACTION_CHECKSUM_NUMBER = 5;
    private final int FILE_ACTION_ATTRIBUTES_NUMBER = 6;

    AlertDialog.Builder dialog_no_permissions;
    AlertDialog dialog_no_permissions_dialog;

    protected void init() {
        setContentView(R.layout.activity_file_manager);

        //必须在initCurrentFile()之前
        SharedPreferences sharedPreferences = MSFMApplication.getSharedPreferences();
        if (sharedPreferences.getString("home", "").isEmpty()) {
            sharedPreferences.edit().putString("home", FilePaths.getSdcard().getAbsolutePath()).apply();
        }

        initVariables();
        initViews();
        sortMethod = SortMethod.valuesOf(SharedPreferencesGetter.getSortMethod(), SortMethod.BY_NAME);
        initCurrentFile();
        initListeners();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            getPermission(new Permission(SharedVariables.getPermissions()[0]));
        }
        setTitle(currentFile.getAbsolutePath());
    }

    void initVariables() {
        position = new HashMap<>();
    }

    void initCurrentFile() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        String home = sharedPreferences.getString("home", FilePaths.getSdcard().getAbsolutePath());
        if (sharedPreferences.getString("startupDir", "home").equals("last")) {
            currentFile = new File(sharedPreferences.getString("lastPath", home));
        } else {
            currentFile = new File(home);
        }
        setTitle(currentFile.getAbsolutePath());
    }

    void initViews() {
        filesListView = findViewById(R.id.files);
        createFolder = findViewById(R.id.create_folder);
        createFile = findViewById(R.id.create_file);
        toolbar = findViewById(R.id.toolbar);
        title = findViewById(R.id.title);
        fab_buttons = findViewById(R.id.fab_buttons);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        warning = findViewById(R.id.warning);

        super.setTitle("");

        toolbar.setBackgroundColor(getSharedPreferences().getBoolean("darkTheme", false) ? getResources().getColor(R.color.colorPrimary) : getResources().getColor(R.color.colorPrimaryLight));
        setSupportActionBar(toolbar);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            position.remove(currentFile);
            initFiles(currentFile);
        });

        filesListView.setDividerHeight(0);

        filesListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub
                ListAdapter listAdapter = view.getAdapter();
                if (listAdapter instanceof FilesAdapter) {
                    FilesAdapter adapter = (FilesAdapter) listAdapter;
                    switch (scrollState) {
                        case AbsListView.OnScrollListener.SCROLL_STATE_IDLE://停止  0
                            adapter.scrollStatus = 0;
                            adapter.notifyDataSetChanged();
                            //System.out.println("停止");
                            break;
                        case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL://触摸滑动  1
                            adapter.scrollStatus = 1;

                            //System.out.println("触摸滑动");
                            break;
                        case AbsListView.OnScrollListener.SCROLL_STATE_FLING://快速滑动    2
                            adapter.scrollStatus = 2;
                            //System.out.println("快速滑动");
                            break;
                        default:
                            break;
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }

    void initFiles(File dir) {
        interrupts.put(currentFile, true);
        currentFile = dir;
        interrupts.remove(dir);
        new Thread(() -> {
            List<AbstractFileItem> folders = new LinkedList<>();
            List<AbstractFileItem> files = new LinkedList<>();
            boolean showHiddenFiles = getSharedPreferences().getBoolean("showHiddenFiles", false);
            SortMethod sort = SortMethod.valuesOf(SharedPreferencesGetter.getSortMethod(), SortMethod.BY_NAME);
            boolean getRoot = SharedPreferencesGetter.getGetRoot();

            //开始分情况
            if (dir.canRead() && dir.canWrite()) {
                accessMode = 0;
            } else if (dir.canRead() && !dir.canWrite()) {
                if (getRoot && Shell.rootAccess()) {
                    accessMode = 1;
                } else {
                    accessMode = 0;
                }
            } else /*if ((!dir.canRead() && !dir.canWrite())||(!dir.canRead() && dir.canWrite())) */ {
                if (getRoot && Shell.rootAccess()) {
                    accessMode = 1;
                } else {
                    accessMode = 2;
                    runOnUiThread(() -> {
                        warning.setVisibility(View.VISIBLE);
                        warning.setOnClickListener((v) -> {
                            String sdcard = FilePaths.getSdcard().getAbsolutePath();
                            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                            dialog.setTitle(R.string.dialog_title_notice);
                            dialog.setMessage(String.format(getString(R.string.dialog_higher_version_os_unaccessable_root_directory_message), sdcard));
                            dialog.setPositiveButton(R.string.dialog_higher_version_os_unaccessable_root_directory_goto_sdcard, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    goToFile(sdcard);
                                }
                            });
                            dialog.show();
                        });
                    });
                }
            }

            if (accessMode == 0) {
                runOnUiThread(() -> warning.setVisibility(View.GONE));

                File[] filesArray = dir.listFiles();
                if (filesArray != null && filesArray.length > 0) {
                    for (File file : filesArray) {
                        if (file.getName().startsWith(".") && !showHiddenFiles) continue;
                        (file.isDirectory() ? folders : files).add(new FileItem(file));
                    }
                }
            } else if (accessMode == 1) {

                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(true);
                    warning.setVisibility(View.GONE);
                });
                a:
                try {
                    String dirPath = PathUtils.toDirectoryPath(dir.getAbsolutePath());
                    List<String> results;
                    boolean isStat;
                    try {
                        Shell.Result result = ShellUtils.executeSuCommand("stat -c '" + StatParser.STAT_FORMAT + "' '" + dirPath + "'*" + (showHiddenFiles ? (" '" + dirPath + "'.*") : ""));
                        if (result.isSuccess()) {
                            results = result.getOut();
                            isStat = true;
                        } else {
                            results = ShellUtils.executeSuCommand("ls -l " + (showHiddenFiles ? "-a " : "") + "'" + dirPath + "'").getOut();
                            isStat = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        break a;
                    }
                    try {
                        ShellUtils.executeSuCommand("mount -o rw,remount '" + dir.getAbsolutePath() + "'");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    for (String result : results) {
                        try {
                            RootFileItem rootFileItem = isStat ? RootFileItem.parseStat(result) : RootFileItem.parseLs(result, dir.getAbsolutePath());
                            if (rootFileItem == null || ".".equals(rootFileItem.getFileName()) || "..".equals(rootFileItem.getFileName()))
                                continue;
                            (rootFileItem.isDirectory() ? folders : files).add(rootFileItem);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(context, R.string.message_failed_to_list_files, Toast.LENGTH_SHORT).show();
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.message_failed_to_list_files)
                                .setMessage(e.toString())
                                .show();
                    });
                }
            }


            if (sort == SortMethod.BY_SIZE || sort == SortMethod.BY_SIZE_REVERSED) {
                Collections.sort(folders, SortMethod.BY_NAME.comparator);
            } else {
                Collections.sort(folders, sort.comparator);
            }
            Collections.sort(files, sort.comparator);

            if (!dir.getAbsolutePath().equals("/")) {
                FileItem backer = new FileItem(new File(".."), true);
                folders.add(0, backer);
            }

            folders.addAll(files);
        /*if(folders.size()>1&&warning.getVisibility()==View.VISIBLE){
            warning.setVisibility(View.GONE);
        }*/
            {
                ListAdapter old = filesListView.getAdapter();
                if (old instanceof FilesAdapter) {
                    ((FilesAdapter) old).interruptThreads();
                }
            }

            if (Boolean.TRUE.equals(interrupts.get(dir))) {
                interrupts.remove(dir);
                return;
            }
            interrupts.remove(dir);

            getSharedPreferences().edit().putString("lastPath", dir.getAbsolutePath()).apply();

            FilesAdapter filesAdapter = new FilesAdapter(context, folders);
            runOnUiThread(() -> {
                setTitle(dir.getAbsolutePath());
                filesListView.setAdapter(filesAdapter);
                initPosition();
                swipeRefreshLayout.setRefreshing(false);
            });
        }).start();
    }

    void initListeners() {
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
                AbstractFileItem item = (AbstractFileItem) filesListView.getItemAtPosition(position);
                if (item.isDirectory()) {
                    if (item.isBacker()) {
                        back();
                        //goToFile(currentFile.getParentFile().getAbsolutePath());
                        //initFiles(currentFile);
                    } else {
                        goToFile(item.getAbsolutePath());
                        //initFiles(file);
                    }
                } else {
                    FileOperations.openFile(context, item);
                }
                fab_buttons.close(true);
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
                menu.add(0, FILE_ACTION_CHECKSUM_NUMBER, 0, getText(R.string.file_action_checksum));
                menu.add(0, FILE_ACTION_ATTRIBUTES_NUMBER, 0, getText(R.string.file_action_attribute));
                fab_buttons.close(true);
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

    void showCreateFolderDialog() {
        putPosition();
        FileOperationsDialogs.showCreateFolderDialog(context, currentFile, () -> initFiles(currentFile), accessMode);
    }

    void showCreateFileDialog() {
        putPosition();
        FileOperationsDialogs.showCreateFileDialog(context, currentFile, () -> initFiles(currentFile), accessMode);
    }

    void putPosition() {
        int index = filesListView.getFirstVisiblePosition();
        View v = filesListView.getChildAt(0);
        int top = (v == null) ? 0 : v.getTop();
        position.put(currentFile, index + "/" + top);
    }

    public void goToFile(String path) {
        File newFile = new File(path);
        /*if (!newFile.exists()) {
            warning.setVisibility(View.VISIBLE);
            warning.setOnClickListener((v)->{
                new AlertDialog.Builder(context)
                        .setTitle(R.string.dialog_title_notice)
                        .setMessage(String.format(getString(R.string.message_go_to_file_not_exists),path))
                        .show();
            });
        }else{
            warning.setVisibility(View.GONE);
        }*/
        if (newFile.isFile()) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.dialog_title_notice)
                    .setMessage(String.format(getString(R.string.message_go_to_file_is_file), path))
                    .show();
            return;
        }

        putPosition();
        initFiles(newFile);
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    private void getPermissionNew() {
        dialog_no_permissions = new AlertDialog.Builder(this);
        dialog_no_permissions.setTitle(getText(R.string.dialog_no_permissions_title))
                .setMessage(getText(R.string.dialog_no_permissions_message_new))
                .setPositiveButton(getText(R.string.dialog_no_permissions_button_gotosettings), (dialog, which) -> startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).setData(Uri.parse("package:com.mrshiehx.file.manager"))))
                .setNegativeButton(getText(android.R.string.cancel), (dialog, which) -> MSFMApplication.getInstance().exit())
                .setCancelable(false);
        if (dialog_no_permissions_dialog == null || !dialog_no_permissions_dialog.isShowing())
            dialog_no_permissions_dialog = dialog_no_permissions.show();
    }

    void getPermission(Permission permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ps = shouldShowRequestPermissionRationale(permission.getName());
        }
        int WRITE_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(context, permission.getName());
        if (WRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            startRequestPermission();
        }
    }

    void startRequestPermission() {
        if (!ps) {
            ActivityCompat.requestPermissions(this, SharedVariables.getPermissions(), REQUEST_CODE_GET_PERMISSION);
        } else {
            showDialogTipUserGoToAppSettting();
        }
    }

    void back() {
        if (!Utils.isEmpty(currentFile.getParent()) && !currentFile.getAbsolutePath().equals("/")) {
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

            File oldCurrentFile = currentFile;
            goToFile(currentFile.getParent());
            position.remove(oldCurrentFile);
            //initPosition();
            //filesListView.setSelectionFromTop(index, top);
        } else {
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
        final long selectedId = info.id;
        AbstractFileItem item = (AbstractFileItem) filesListView.getItemAtPosition((int) selectedId);
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
                if (!item.isBacker()) {
                    if (!item.isDirectory()) {
                        FileOperationsDialogs.showOpenMethodDialog(context, item);
                    } else {
                        Toast.makeText(context, R.string.message_unsupported_operation, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, R.string.message_unsupported_operation, Toast.LENGTH_SHORT).show();
                }
                return true;
            case FILE_ACTION_RENAME_NUMBER:
                if (!item.isBacker()) {
                    putPosition();
                    FileOperationsDialogs.showRenameDialog(context, item, () -> initFiles(currentFile));
                } else {
                    Toast.makeText(context, R.string.message_unsupported_operation, Toast.LENGTH_SHORT).show();
                }
                return true;
            case FILE_ACTION_COPY_NUMBER:
                if (!item.isBacker()) {
                    copingFile = item;
                    movingFile = null;
                } else {
                    Toast.makeText(context, R.string.message_unsupported_operation, Toast.LENGTH_SHORT).show();
                }
                return true;
            case FILE_ACTION_MOVE_NUMBER:
                if (!item.isBacker()) {
                    movingFile = item;
                    copingFile = null;
                } else {
                    Toast.makeText(context, R.string.message_unsupported_operation, Toast.LENGTH_SHORT).show();
                }
                return true;
            case FILE_ACTION_DELETE_NUMBER:
                if (!item.isBacker()) {
                    putPosition();
                    FileOperationsDialogs.showDeleteFileDialog(context, item, () -> initFiles(currentFile));
                } else {
                    Toast.makeText(context, R.string.message_unsupported_operation, Toast.LENGTH_SHORT).show();
                }
                return true;
            case FILE_ACTION_CHECKSUM_NUMBER:
                if (item.isDirectory()) {
                    Toast.makeText(context, R.string.message_checksum_on_folder, Toast.LENGTH_SHORT).show();
                    return true;
                }
                try {
                    byte[] bytes = item.getFileBytes();
                    if (bytes == null) {
                        Toast.makeText(context, R.string.message_failed_to_read_file, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle(R.string.file_action_checksum);
                    final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_file_checksum, null);
                    dialog.setView(dialogView);
                    EditText crc32 = dialogView.findViewById(R.id.crc32);
                    EditText md5 = dialogView.findViewById(R.id.md5);
                    EditText sha1 = dialogView.findViewById(R.id.sha1);
                    EditText sha224 = dialogView.findViewById(R.id.sha224);
                    EditText sha256 = dialogView.findViewById(R.id.sha256);
                    EditText sha384 = dialogView.findViewById(R.id.sha384);
                    EditText sha512 = dialogView.findViewById(R.id.sha512);
                    EditText paste_et = dialogView.findViewById(R.id.paste_et);
                    Button copy_crc32 = dialogView.findViewById(R.id.copy_crc32);
                    Button copy_md5 = dialogView.findViewById(R.id.copy_md5);
                    Button copy_sha1 = dialogView.findViewById(R.id.copy_sha1);
                    Button copy_sha224 = dialogView.findViewById(R.id.copy_sha224);
                    Button copy_sha256 = dialogView.findViewById(R.id.copy_sha256);
                    Button copy_sha384 = dialogView.findViewById(R.id.copy_sha384);
                    Button copy_sha512 = dialogView.findViewById(R.id.copy_sha512);
                    Button paste = dialogView.findViewById(R.id.paste);
                    TextView match = dialogView.findViewById(R.id.match);

                    Map<String, String> map = new HashMap<>();
                    map.put("MD5", BytesUtils.getMD5(bytes).toLowerCase());
                    map.put("CRC32", BytesUtils.getCRC32(bytes).toLowerCase());
                    map.put("SHA1", BytesUtils.getSHA1(bytes).toLowerCase());
                    map.put("SHA224", BytesUtils.getSHA224(bytes).toLowerCase());
                    map.put("SHA256", BytesUtils.getSHA256(bytes).toLowerCase());
                    map.put("SHA384", BytesUtils.getSHA384(bytes).toLowerCase());
                    map.put("SHA512", BytesUtils.getSHA512(bytes).toLowerCase());
                    crc32.setText(map.get("CRC32"));
                    md5.setText(map.get("MD5"));
                    sha1.setText(map.get("SHA1"));
                    sha224.setText(map.get("SHA224"));
                    sha256.setText(map.get("SHA256"));
                    sha384.setText(map.get("SHA384"));
                    sha512.setText(map.get("SHA512"));
                    copy_crc32.setOnClickListener((v) -> SystemUtils.copy(map.get("CRC32")));
                    copy_md5.setOnClickListener((v) -> SystemUtils.copy(map.get("MD5")));
                    copy_sha1.setOnClickListener((v) -> SystemUtils.copy(map.get("SHA1")));
                    copy_sha224.setOnClickListener((v) -> SystemUtils.copy(map.get("SHA224")));
                    copy_sha256.setOnClickListener((v) -> SystemUtils.copy(map.get("SHA256")));
                    copy_sha384.setOnClickListener((v) -> SystemUtils.copy(map.get("SHA384")));
                    copy_sha512.setOnClickListener((v) -> SystemUtils.copy(map.get("SHA512")));
                    paste.setOnClickListener((v) -> paste_et.setText(SystemUtils.getClipboardContent()));

                    paste_et.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (!Utils.isEmpty(s)) {
                                match.setVisibility(View.VISIBLE);
                                String s1 = s.toString();
                                for (Map.Entry<String, String> entry : map.entrySet()) {
                                    if (s1.equalsIgnoreCase(entry.getValue())) {
                                        match.setTextColor(Color.parseColor("#00FF00"));
                                        match.setText(getString(R.string.file_checksum_match_with, entry.getKey()));
                                        break;
                                    } else {
                                        match.setTextColor(Color.parseColor("#FF0000"));
                                        match.setText(R.string.file_checksum_no_match);
                                    }
                                }
                            } else {
                                match.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });
                    dialog.show();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, getString(R.string.message_operation_failed_with_exception, e), Toast.LENGTH_SHORT).show();
                    //Toast.makeText(context, Arrays.toString(e.getStackTrace()), Toast.LENGTH_SHORT).show();
                }
                return true;
            case FILE_ACTION_ATTRIBUTES_NUMBER:
                if (!item.isBacker()) {
                    FileOperationsDialogs.showAttributesDialog(context, item);
                } else {
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
        if (requestCode == REQUEST_CODE_GET_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    boolean b = shouldShowRequestPermissionRationale(permissions[0]);
                    if (b) {
                        showDialogTipUserGoToAppSettting();
                    } /*else {
                        MSFMApplication.getInstance().exit();
                    }*/
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
        dialog_no_permissions_dialog = dialog_no_permissions.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GO_TO_SETTINGS) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                int i = ContextCompat.checkSelfPermission(context, SharedVariables.getPermissions()[0]);
                if (i != PackageManager.PERMISSION_GRANTED) {
                    showDialogTipUserGoToAppSettting();
                } else {
                    if (dialog_no_permissions_dialog != null && dialog_no_permissions_dialog.isShowing()) {
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
        getSharedPreferences().edit().putString("lastPath", currentFile.getAbsolutePath()).apply();
        putPosition();

    }

    void initPosition() {
        if (position.containsKey(currentFile)) {
            int index = 0;
            int top = 0;
            try {
                String s = position.get(currentFile);
                if (!Utils.isEmpty(s)) {
                    String[] ss = s.split("/");
                    index = Integer.parseInt(ss[0]);
                    top = Integer.parseInt(ss[1]);
                }
            } catch (Throwable ignore) {
            }
            filesListView.setSelectionFromTop(index, top);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                getPermissionNew();
            } else {
                if (dialog_no_permissions_dialog != null && dialog_no_permissions_dialog.isShowing())
                    dialog_no_permissions_dialog.dismiss();
            }
        }
        initFiles(currentFile);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            /*if (copyingThread != null) {
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 2000) {
                    Toast.makeText(this, getResources().getText(R.string.message_press_again_stop_operating), Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;
                } else {
                    copyingThread.interrupt();
                }
            } else {*/
            back();
            //}
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        MenuItem sort = menu.findItem(R.id.action_sort_method);
        MenuItem paste = menu.findItem(R.id.action_paste);
        //initSortMethod();
        if (sort != null)
            sort.setTitle(String.format(getString(R.string.action_sort_method), sortMethod.getDisplayName()));
        if (paste != null)
            paste.setEnabled((copingFile != null && !copingFile.getParent().equals(currentFile.getAbsolutePath())) || (movingFile != null && !movingFile.getParent().equals(currentFile.getAbsolutePath())));
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_exit) {
            new AlertDialog.Builder(this)
                    .setTitle(getText(R.string.dialog_title_notice))
                    .setMessage(getText(R.string.dialog_exit_message))
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> MSFMApplication.getInstance().exit()).show();
            /*case R.id.action_about:
                showAboutDialog();
            return true;
                break;*/
        } else if (id == R.id.action_back) {
            back();
            return true;
        } else if (id == R.id.action_create_a_folder) {
            showCreateFolderDialog();
            return true;
        } else if (id == R.id.action_create_a_file) {
            showCreateFileDialog();
            return true;
        } else if (id == R.id.action_sort_method) {
            SortMethod[] items = {SortMethod.BY_NAME, SortMethod.BY_DATE, SortMethod.BY_NAME_REVERSED, SortMethod.BY_DATE_REVERSED, SortMethod.BY_SIZE, SortMethod.BY_SIZE_REVERSED};
            CharSequence[] itemNames = {SortMethod.BY_NAME.getDisplayName(), SortMethod.BY_DATE.getDisplayName(), SortMethod.BY_NAME_REVERSED.getDisplayName(), SortMethod.BY_DATE_REVERSED.getDisplayName(), SortMethod.BY_SIZE.getDisplayName(), SortMethod.BY_SIZE_REVERSED.getDisplayName()};
            sortMethodNumber = sortMethod.ordinal();
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
                    }
                    getSharedPreferences().edit().putString("sort", sortMethod.getName()).apply();
                    initFiles(currentFile);
                }
            });
            dialog.show();
            return true;
        } else if (id == R.id.action_paste) {
            if (copingFile != null) {
                if (!copingFile.getParent().equals(currentFile.getAbsolutePath())) {
                    if (copingFile.exists()) {
                        FileOperationsDialogs.showCopyDialog(activity, copingFile, currentFile, () -> {
                            copingFile = null;
                            //copyingThread = null;
                            initFiles(currentFile);
                        }, () -> {
                            copingFile = null;
                            //copyingThread = null;
                            initFiles(currentFile);
                        });
                    } else {
                        Toast.makeText(context, R.string.message_target_file_not_exists, Toast.LENGTH_SHORT).show();
                        copingFile = null;
                    }
                } else {
                    Toast.makeText(context, getText(R.string.message_paste_to_the_same_directory), Toast.LENGTH_SHORT).show();
                }
            } else if (movingFile != null) {
                if (movingFile.getParent().equals(currentFile.getAbsolutePath())) {
                    Toast.makeText(context, getText(R.string.message_paste_moved_to_the_same_directory), Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (!movingFile.exists()) {
                    Toast.makeText(context, R.string.message_target_file_not_exists, Toast.LENGTH_SHORT).show();
                    movingFile = null;
                    return true;
                }
                boolean success;
                if (movingFile instanceof FileItem && Utils.isRootFileWithDeviceStatus(currentFile)) {
                    try {
                        success = ShellUtils.executeSuCommand("mv '" + movingFile.getAbsolutePath() + "' '" + new File(currentFile, movingFile.getFileName()).getAbsolutePath() + "'").isSuccess();
                    } catch (Exception e) {
                        e.printStackTrace();
                        success = false;
                    }
                } else
                    success = movingFile.renameTo(new File(currentFile, movingFile.getFileName()));
                if (success) {
                    movingFile = null;
                    Toast.makeText(context, R.string.message_success_move_files, Toast.LENGTH_SHORT).show();
                    initFiles(currentFile);
                } else {
                    Toast.makeText(context, R.string.message_failed_to_move_files, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, getText(R.string.message_no_pasteable_file), Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title.setText(title);
    }

    public CharSequence getTitleText() {
        return this.title.getText();
    }
}