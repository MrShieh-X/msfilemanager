package com.mrshiehx.file.manager.file.operations;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mrshiehx.file.manager.R;
import com.mrshiehx.file.manager.beans.FileItem;
import com.mrshiehx.file.manager.enums.FileViewer;
import com.mrshiehx.file.manager.interfaces.Void;
import com.mrshiehx.file.manager.shared.variables.SharedVariables;
import com.mrshiehx.file.manager.utils.ApplicationUtils;
import com.mrshiehx.file.manager.utils.FileUtils;
import com.mrshiehx.file.manager.utils.SharedPreferencesGetter;
import com.mrshiehx.file.manager.utils.SystemUtils;
import com.mrshiehx.file.manager.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mrshiehx.file.manager.utils.ResourceUtils.getString;
import static com.mrshiehx.file.manager.utils.ResourceUtils.getText;

public class FileOperationsDialogs {
    private static boolean doneCopy = false;

    private FileOperationsDialogs() {
    }

    public static void showGoToFileDialog(Context context, File currentFile, AfterGoToDialog afterFileOperationDialog) {
        final EditText editText = new EditText(context);
        editText.setText(currentFile.getAbsolutePath());
        editText.setSingleLine();
        editText.setSelection(editText.length());
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(getText(R.string.action_goto_dir_name));
        dialog.setView(editText);
        dialog.setPositiveButton(getText(android.R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!Utils.isEmpty(editText.getText())) {
                    afterFileOperationDialog.clickedYes(new File(String.valueOf(editText.getText())));
                }
            }
        });
        dialog.setNegativeButton(getText(android.R.string.no), null);
        dialog.show();
    }

    public static void showCreateFolderDialog(Context context, File currentFile, Void afterGoToDialog) {
        final EditText editText = new EditText(context);
        editText.setSingleLine();
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(getText(R.string.action_create_a_folder_name));
        dialog.setView(editText);
        dialog.setPositiveButton(getText(android.R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!Utils.isEmpty(editText.getText())) {
                    try {
                        File file = new File(currentFile, editText.getText().toString());
                        if (file.exists()) {
                            Toast.makeText(context, getText(R.string.message_file_exists), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (file.mkdir() && file.exists()) {
                            Toast.makeText(context, getText(R.string.message_success_mkdir), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, getText(R.string.message_failed_to_mkdir), Toast.LENGTH_SHORT).show();
                        }
                        afterGoToDialog.execute();

                    } catch (Exception e) {
                        e.printStackTrace();
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.message_failed_to_rename)
                                .setMessage(e.toString())
                                .show();
                    }
                }
            }
        });
        dialog.setNegativeButton(getText(android.R.string.no), null);
        dialog.show();
    }

    public static void showCreateFileDialog(Context context, File currentFile, Void afterGoToDialog) {
        final EditText editText = new EditText(context);
        editText.setSingleLine();
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(getText(R.string.action_create_a_file_name));
        dialog.setView(editText);
        dialog.setPositiveButton(getText(android.R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!Utils.isEmpty(editText.getText())) {
                    File file = new File(currentFile, editText.getText().toString());
                    if (file.exists()) {
                        Toast.makeText(context, getText(R.string.message_file_exists), Toast.LENGTH_SHORT).show();
                        ;
                        return;
                    }
                    try {
                        if (file.createNewFile() && file.exists()) {
                            Toast.makeText(context, getText(R.string.message_success_cnf), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, getText(R.string.message_failed_to_cnf), Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.message_failed_to_cnf)
                                .setMessage(e.toString())
                                .show();
                    }
                    afterGoToDialog.execute();
                }
            }
        });
        dialog.setNegativeButton(getText(android.R.string.no), null);
        dialog.show();
    }

    public static void showDeleteFileDialog(Context context, File targetFile, Void afterGoToDialog) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(R.string.dialog_title_notice);
        dialog.setMessage(String.format(getString(R.string.dialog_delete_file_message), targetFile.getName()));
        dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if (targetFile.isDirectory()) {
                        FileUtils.deleteDirectory(targetFile);
                        if (!targetFile.exists() && targetFile.listFiles() == null) {
                            Toast.makeText(context, getText(R.string.message_success_to_delete_folder), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, getText(R.string.message_failed_to_delete_folder), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (targetFile.delete() && !targetFile.exists()) {
                            Toast.makeText(context, getText(R.string.message_success_to_delete_file), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, getText(R.string.message_failed_to_delete_file), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.message_failed_to_rename)
                            .setMessage(e.toString())
                            .show();
                }
                afterGoToDialog.execute();

            }
        });
        dialog.setNegativeButton(android.R.string.cancel, null);
        dialog.show();
    }

    public static void showAttributesDialog(Context context, FileItem item, File file) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(R.string.file_action_attribute);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_file_attribute, null);
        dialog.setView(dialogView);
        TextView file_attribute_name = dialogView.findViewById(R.id.file_attribute_name);
        TextView file_attribute_type = dialogView.findViewById(R.id.file_attribute_type);
        TextView file_attribute_directory = dialogView.findViewById(R.id.file_attribute_directory);
        TextView file_attribute_path = dialogView.findViewById(R.id.file_attribute_path);
        TextView file_attribute_date = dialogView.findViewById(R.id.file_attribute_date);

        file_attribute_name.setText(String.format(getString(R.string.dialog_file_attribute_name), file.getName()));
        file_attribute_type.setText(String.format(getString(R.string.dialog_file_attribute_type), item.getType().getDisplayName()));
        file_attribute_directory.setText(String.format(getString(R.string.dialog_file_attribute_directory), FileUtils.addSeparatorToPath(file.getParent())));
        file_attribute_path.setText(String.format(getString(R.string.dialog_file_attribute_path), file.getAbsolutePath()));
        file_attribute_date.setText(String.format(getString(R.string.dialog_file_attribute_date), item.getFormattedModifiedDate(SharedPreferencesGetter.getFileDateFormat())));

        file_attribute_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemUtils.copy(file.getName());
                Toast.makeText(context, getText(R.string.message_success_copy), Toast.LENGTH_SHORT).show();
            }
        });

        file_attribute_directory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemUtils.copy(FileUtils.addSeparatorToPath(file.getParent()));
                Toast.makeText(context, getText(R.string.message_success_copy), Toast.LENGTH_SHORT).show();
            }
        });

        file_attribute_path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemUtils.copy(file.getAbsolutePath());
                Toast.makeText(context, getText(R.string.message_success_copy), Toast.LENGTH_SHORT).show();
            }
        });


        dialog.show();
        TextView file_attribute_size = dialogView.findViewById(R.id.file_attribute_size);
        TextView file_attribute_folders = dialogView.findViewById(R.id.file_attribute_folders);
        TextView file_attribute_files = dialogView.findViewById(R.id.file_attribute_files);

        if (file.isFile() || !file.isDirectory()) {
            file_attribute_folders.setVisibility(View.GONE);
            file_attribute_files.setVisibility(View.GONE);
        }

        file_attribute_size.setText(getString(R.string.dialog_file_attribute_size).substring(0, getString(R.string.dialog_file_attribute_size).indexOf("%1$s")) + getText(R.string.dialog_file_attribute_calculating));
        file_attribute_folders.setText(String.format(getString(R.string.dialog_file_attribute_folders), getText(R.string.dialog_file_attribute_calculating)));
        file_attribute_files.setText(String.format(getString(R.string.dialog_file_attribute_files), getText(R.string.dialog_file_attribute_calculating)));
        if (file.isFile()) {
            file_attribute_size.setText(String.format(getString(R.string.dialog_file_attribute_size), String.valueOf(item.getFileSize()), item.getFormattedFileSize()));
        }
        //Object[] var=getFolderSize(file,fileSize,folders,files);
        new Thread(new Runnable() {
            @Override
            public void run() {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (file.isDirectory()) {
                            long[] var = FileOperations.calculate(file);
                            long fileSize = (long) var[0];
                            int folders = (int) var[1];
                            int files = (int) var[2];
                            file_attribute_size.setText(String.format(getString(R.string.dialog_file_attribute_size), fileSize, FileUtils.getFormatSize(fileSize)));
                            file_attribute_folders.setText(String.format(getString(R.string.dialog_file_attribute_folders), folders));
                            file_attribute_files.setText(String.format(getString(R.string.dialog_file_attribute_files), files));
                        } else {
                            file_attribute_size.setText(String.format(getString(R.string.dialog_file_attribute_size), item.getFileSize(), item.getFormattedFileSize()));
                        }
                    }
                });
            }
        }).start();

    }

    public static void showRenameDialog(Context context, File target, Void v) {
        EditText editText = new EditText(context);
        editText.setText(target.getName());
        editText.setSelection(editText.length());
        editText.setSingleLine();
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(R.string.dialog_rename_title);
        dialog.setView(editText);
        dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!Utils.isEmpty(editText.getText())) {
                    try {
                        File destFile = new File(target.getParent(), editText.getText().toString());
                        if (target.renameTo(destFile) && destFile.exists()) {
                            Toast.makeText(context, getText(R.string.message_success_rename), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, getText(R.string.message_failed_to_rename), Toast.LENGTH_SHORT).show();
                        }
                        v.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.message_failed_to_rename)
                                .setMessage(e.toString())
                                .show();
                    }
                }
            }
        });
        dialog.setNegativeButton(android.R.string.cancel, null);
        dialog.show();
    }

    public static void showApkInformationDialog(Context context, File apkFile, Drawable icon, String name, String version, String packageName, int versionCode, String size) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_apk_details, null);
        dialog.setView(dialogView);
        ImageView iconID = dialogView.findViewById(R.id.apk_icon);
        iconID.setImageDrawable(icon);
        TextView apk_name = dialogView.findViewById(R.id.apk_name);
        apk_name.setText(name);
        TextView apk_version = dialogView.findViewById(R.id.apk_version);
        apk_version.setText(version);
        TextView apk_version_code = dialogView.findViewById(R.id.apk_version_code);
        apk_version_code.setText(String.valueOf(versionCode));
        TextView apk_package_name = dialogView.findViewById(R.id.apk_package_name);
        apk_package_name.setText(packageName);
        TextView apk_file_size = dialogView.findViewById(R.id.apk_file_size);
        apk_file_size.setText(size);

        apk_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemUtils.copyWithToast(name);
            }
        });

        apk_package_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemUtils.copyWithToast(packageName);
            }
        });

        dialog.setPositiveButton(R.string.dialog_apk_details_button_install, (dialog1, which) -> FileOperations.installApk(context, apkFile));
        dialog.setNegativeButton(android.R.string.cancel, null);
        dialog.show();
    }

    public static void showCopyDialog(Context context, File source, File to, Void voib) {
        showCopyDialog(context, source, to, voib,getText(R.string.dialog_file_copy_title),getText(R.string.dialog_file_copy_to));
    }

    public static void showMoveDialog(Context context, File source, File to, Void voib) {
        showCopyDialog(context, source, to, voib,getText(R.string.dialog_file_move_title),getText(R.string.dialog_file_move_to));
    }

    /**
     * @param to The parent not son
     **/
    public static void showCopyDialog(Context context, File source, File to, Void voib, CharSequence title,CharSequence copyToText) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setCancelable(false);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_file_copy, null);
        dialog.setView(dialogView);
        ProgressBar progressBar = dialogView.findViewById(R.id.dialog_copy_progress);
        TextView current = dialogView.findViewById(R.id.dialog_copy_current);
        TextView sourceTv = dialogView.findViewById(R.id.dialog_copy_source);
        TextView toTv = dialogView.findViewById(R.id.dialog_copy_to);
        TextView toTextTv = dialogView.findViewById(R.id.dialog_copy_to_text);
        toTextTv.setText(copyToText);
        sourceTv.setText(source.getAbsolutePath());
        toTv.setText(FileUtils.addSeparatorToPath(to.getAbsolutePath()));
        AlertDialog nDialog = dialog.show();
        TextView remaining = dialogView.findViewById(R.id.dialog_copy_remaining);
        TextView percentage = dialogView.findViewById(R.id.dialog_copy_percentage);
        TextView processed = dialogView.findViewById(R.id.dialog_copy_processed);
        TextView total = dialogView.findViewById(R.id.dialog_copy_total);
        remaining.setText(getText(R.string.dialog_file_attribute_calculating));
        new Thread(() -> {
            Looper.prepare();
            long[] vars = new long[]{0, 0, 1};
            if (source.isDirectory()) {
                vars = FileOperations.calculate(source);
            }

            long allInt = vars[1] + vars[2];
            if (source.isDirectory()) {
                allInt++;
            }
            //Toast.makeText(context, vars[1]+"/"+vars[2], Toast.LENGTH_SHORT).show();
            String all = String.valueOf(allInt);
            long finalAllInt = allInt;
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    total.setText(all);
                    remaining.setText(all);
                    progressBar.setMax((int) finalAllInt);

                }
            });
            try {
                List<String>failed=new ArrayList<>();
                if (source.isDirectory()) {
                    copyDirectory(context, source, to.getAbsolutePath(), source.getName(), progressBar, current, remaining, percentage, processed,failed);
                } else {
                    copyFile(context, source, new File(to, source.getName()), progressBar, current, remaining, percentage, processed,failed);
                }
                if(failed.size()>0) {
                    StringBuilder faileds = new StringBuilder();
                    faileds.append(failed.get(0));
                    for (int i=1;i<failed.size();i++) {
                        faileds.append("\n");
                        faileds.append(failed.get(i));
                    }

                    ((Activity) context).runOnUiThread(() -> ApplicationUtils.showDialog(context, getText(R.string.dialog_files_them_failed_to_copy_title), faileds.toString(), null, null, null, null, null, null, true));
                }
            } catch (Exception e) {
                e.printStackTrace();
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.message_failed_to_copy_files)
                                .setMessage(e.toString())
                                .show();

                    }
                });
            }
            while (true) {
                if (Integer.parseInt(processed.getText().toString())==Integer.parseInt(total.getText().toString())) {
                    doneCopy = false;
                    ((Activity) context).runOnUiThread(() -> {
                        current.setText(R.string.dialog_file_copy_done);
                        nDialog.dismiss();
                        voib.execute();
                    });
                    Looper.loop();
                    break;
                }
            }
        }).start();
    }

    private static void copyDirectory(Context context, File from, String toWillNewDirNameIsAtFromName, String afterThatName, ProgressBar progressBar, TextView current, TextView remaining, TextView percentage, TextView processed, List<String>failed) throws IOException {
        if (from != null && !Utils.isEmpty(toWillNewDirNameIsAtFromName) && from.exists()) {
            File toWillNewDirNameIsAtFrom = new File(toWillNewDirNameIsAtFromName);
            File to = new File(toWillNewDirNameIsAtFrom, afterThatName);
            if (!to.exists()) {
                to.mkdirs();
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(progressBar.getProgress() + 1);
                        remaining.setText(String.valueOf(Integer.parseInt(remaining.getText().toString()) - 1));
                        processed.setText(String.valueOf(Integer.parseInt(processed.getText().toString()) + 1));
                        float prog = (float) progressBar.getProgress() / progressBar.getMax() * 100;
                        percentage.setText(String.valueOf((int) prog));

                    }
                });
                if (from.listFiles() != null) {
                    for (File file : from.listFiles()) {
                        if (file.isFile()) {
                            copyFile(context, file, new File(to, file.getName()), progressBar, current, remaining, percentage, processed,failed);
                        } else {
                            copyDirectory(context, file, to.getAbsolutePath(), file.getName(), progressBar, current, remaining, percentage, processed,failed);
                        }
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                doneCopy = true;
            } else {
                long[] calculated = FileOperations.calculate(from);
                long files = calculated[1] + calculated[2] + 1;

                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.dialog_title_notice)
                                .setCancelable(false)
                                .setMessage(String.format(getString(R.string.dialog_copy_file_exists_message), to.getAbsolutePath()))
                                .setPositiveButton(R.string.dialog_copy_file_exists_replace, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            if (from.listFiles() != null) {
                                                for (File file : from.listFiles()) {
                                                    if (file.isFile()) {
                                                        copyFile(context, file, new File(to, file.getName()), progressBar, current, remaining, percentage, processed,failed);
                                                    } else {
                                                        copyDirectory(context, file, to.getAbsolutePath(), file.getName(), progressBar, current, remaining, percentage, processed,failed);
                                                    }
                                                    try {
                                                        Thread.sleep(1);
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                            doneCopy = true;
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressBar.setProgress(progressBar.getProgress() + (int) files);
                                                remaining.setText(String.valueOf(Integer.parseInt(remaining.getText().toString()) - files));
                                                processed.setText(String.valueOf(Integer.parseInt(processed.getText().toString()) + files));
                                                float prog = (float) progressBar.getProgress() / progressBar.getMax() * 100;
                                                percentage.setText(String.valueOf((int) prog));

                                            }
                                        });
                                    }
                                })
                                .setNegativeButton(R.string.dialog_copy_file_exists_skip, (dialog, which) -> {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setProgress(progressBar.getProgress() + (int) files);
                                            remaining.setText(String.valueOf(Integer.parseInt(remaining.getText().toString()) - files));
                                            processed.setText(String.valueOf(Integer.parseInt(processed.getText().toString()) + files));
                                            float prog = (float) progressBar.getProgress() / progressBar.getMax() * 100;
                                            percentage.setText(String.valueOf((int) prog));

                                        }
                                    });
                                }).show();
                    }
                });

            }
        }
    }

    private static void copyFile(Context context, File source, File to, ProgressBar progressBar, TextView current, TextView remaining, TextView percentage, TextView processed, List<String>failed)
            throws IOException {
        if (null == source) return;
        if (source.isDirectory())
            copyDirectory(context, source, to.getParent(), to.getName(), progressBar, current, remaining, percentage, processed,failed);

        ((Activity) context).runOnUiThread(() -> current.setText(source.getName()));
        if (to.exists()) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    new AlertDialog.Builder(context)
                            .setTitle(R.string.dialog_title_notice)
                            .setMessage(String.format(getString(R.string.dialog_copy_file_exists_message), to.getAbsolutePath()))
                            .setCancelable(false)
                            .setPositiveButton(R.string.dialog_copy_file_exists_replace, (DialogInterface dialog, int which) -> {
                                try {
                                    to.delete();
                                    to.createNewFile();
                                    FileUtils.copy(source, to);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setProgress(progressBar.getProgress() + 1);
                                        remaining.setText(String.valueOf(Integer.parseInt(remaining.getText().toString()) - 1));
                                        processed.setText(String.valueOf(Integer.parseInt(processed.getText().toString()) + 1));
                                        float prog = (float) progressBar.getProgress() / progressBar.getMax() * 100;
                                        percentage.setText(String.valueOf((int) prog));

                                    }
                                });
                            })
                            .setNegativeButton(R.string.dialog_copy_file_exists_skip, (dialog, which) -> {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setProgress(progressBar.getProgress() + 1);
                                        remaining.setText(String.valueOf(Integer.parseInt(remaining.getText().toString()) - 1));
                                        processed.setText(String.valueOf(Integer.parseInt(processed.getText().toString()) + 1));
                                        float prog = (float) progressBar.getProgress() / progressBar.getMax() * 100;
                                        percentage.setText(String.valueOf((int) prog));
                                    }
                                });
                            }).show();
                }
            });
        } else {
            to.createNewFile();
            try {
                FileUtils.copy(source, to);
            }catch (IOException e){
                e.printStackTrace();
                failed.add(source.getAbsolutePath());
            }
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setProgress(progressBar.getProgress() + 1);
                    remaining.setText(String.valueOf(Integer.parseInt(remaining.getText().toString()) - 1));
                    processed.setText(String.valueOf(Integer.parseInt(processed.getText().toString()) + 1));
                    float prog = (float) progressBar.getProgress() / progressBar.getMax() * 100;
                    percentage.setText(String.valueOf((int) prog));

                }
            });
        }
    }

    public static void showOpenMethodDialog(Context context, File file) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        FileViewer[] viewers = SharedVariables.getFileViewers();
        dialog.setTitle(R.string.file_action_choose_open_method);
        CharSequence[] items = new CharSequence[viewers.length];
        for (int i = 0; i < viewers.length; i++) {
            items[i] = viewers[i].getDisplayName();
        }
        dialog.setItems(items, (dialog1, which) -> viewers[which].getOpener().open(context, file));
        dialog.show();
    }

}
