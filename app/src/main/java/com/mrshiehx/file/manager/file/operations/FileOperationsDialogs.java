package com.mrshiehx.file.manager.file.operations;

import static com.mrshiehx.file.manager.utils.ResourceUtils.getString;
import static com.mrshiehx.file.manager.utils.ResourceUtils.getText;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mrshiehx.file.manager.R;
import com.mrshiehx.file.manager.activities.FileManagerActivity;
import com.mrshiehx.file.manager.beans.fileItem.AbstractFileItem;
import com.mrshiehx.file.manager.beans.fileItem.FileItem;
import com.mrshiehx.file.manager.beans.fileItem.RootFileItem;
import com.mrshiehx.file.manager.enums.FileViewer;
import com.mrshiehx.file.manager.interfaces.Void;
import com.mrshiehx.file.manager.shared.variables.SharedVariables;
import com.mrshiehx.file.manager.utils.ApplicationUtils;
import com.mrshiehx.file.manager.utils.FileUtils;
import com.mrshiehx.file.manager.utils.PathUtils;
import com.mrshiehx.file.manager.utils.SharedPreferencesGetter;
import com.mrshiehx.file.manager.utils.ShellUtils;
import com.mrshiehx.file.manager.utils.SystemUtils;
import com.mrshiehx.file.manager.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FileOperationsDialogs {

    private FileOperationsDialogs() {
    }

    public static void showGoToFileDialog(Context context, File currentFile, AfterGoToDialog afterFileOperationDialog) {
        final EditText editText = new EditText(context);
        editText.setText(currentFile.getAbsolutePath());
        editText.setSingleLine();
        editText.setSelection(editText.length());
        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(80, 0, 80, 0);
        editText.setLayoutParams(lp);
        linearLayout.addView(editText);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(getText(R.string.action_goto_dir_name));
        dialog.setView(linearLayout);
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

    public static void showCreateFolderDialog(Context context, File currentFile, Void onSucceed, int accessMode) {
        final EditText editText = new EditText(context);
        editText.setSingleLine();
        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(80, 0, 80, 0);
        editText.setLayoutParams(lp);
        linearLayout.addView(editText);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(getText(R.string.action_create_a_folder_name));
        dialog.setView(linearLayout);
        dialog.setPositiveButton(getText(android.R.string.yes), (dialog1, which) -> {
            if (!Utils.isEmpty(editText.getText())) {
                try {
                    File file = new File(currentFile, editText.getText().toString());
                    if (accessMode == 0) {
                        if (file.exists()) {
                            Toast.makeText(context, getText(R.string.message_file_exists), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (file.mkdir() && file.exists()) {
                            Toast.makeText(context, getText(R.string.message_success_mkdir), Toast.LENGTH_SHORT).show();
                            onSucceed.execute();
                        } else {
                            Toast.makeText(context, getText(R.string.message_failed_to_mkdir), Toast.LENGTH_SHORT).show();
                        }
                    } else if (accessMode == 2) {
                        Toast.makeText(context, R.string.message_unable_to_mkdir, Toast.LENGTH_SHORT).show();
                    } else if (accessMode == 1) {
                        if (RootFileItem.sExists(file.getAbsolutePath())) {
                            Toast.makeText(context, getText(R.string.message_file_exists), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (ShellUtils.executeSuCommand("mkdir '" + file.getAbsolutePath() + "'").isSuccess()) {
                            Toast.makeText(context, getText(R.string.message_success_mkdir), Toast.LENGTH_SHORT).show();
                            onSucceed.execute();
                        } else {
                            Toast.makeText(context, getText(R.string.message_failed_to_mkdir), Toast.LENGTH_SHORT).show();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.message_failed_to_mkdir)
                            .setMessage(e.toString())
                            .show();
                }
            }
        });
        dialog.setNegativeButton(getText(android.R.string.no), null);
        dialog.show();
    }

    public static void showCreateFileDialog(Context context, File currentFile, Void onSucceed, int accessMode) {
        final EditText editText = new EditText(context);
        editText.setSingleLine();
        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(80, 0, 80, 0);
        editText.setLayoutParams(lp);
        linearLayout.addView(editText);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(getText(R.string.action_create_a_file_name));
        dialog.setView(linearLayout);
        dialog.setPositiveButton(getText(android.R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!Utils.isEmpty(editText.getText())) {
                    try {
                        File file = new File(currentFile, editText.getText().toString());
                        if (accessMode == 0) {
                            if (file.exists()) {
                                Toast.makeText(context, getText(R.string.message_file_exists), Toast.LENGTH_SHORT).show();
                                onSucceed.execute();
                                return;
                            }
                            if (file.createNewFile() && file.exists()) {
                                Toast.makeText(context, getText(R.string.message_success_cnf), Toast.LENGTH_SHORT).show();
                                onSucceed.execute();
                            } else {
                                Toast.makeText(context, getText(R.string.message_failed_to_cnf), Toast.LENGTH_SHORT).show();
                            }
                        } else if (accessMode == 2) {
                            Toast.makeText(context, R.string.message_unable_to_cnf, Toast.LENGTH_SHORT).show();
                        } else if (accessMode == 1) {
                            if (RootFileItem.sExists(file.getAbsolutePath())) {
                                Toast.makeText(context, getText(R.string.message_file_exists), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (ShellUtils.executeSuCommand("touch '" + file.getAbsolutePath() + "'").isSuccess()) {
                                Toast.makeText(context, getText(R.string.message_success_cnf), Toast.LENGTH_SHORT).show();
                                onSucceed.execute();
                            } else {
                                Toast.makeText(context, getText(R.string.message_failed_to_cnf), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.message_failed_to_cnf)
                                .setMessage(getFailedMessage(e.toString()))
                                .show();
                    }
                }
            }
        });
        dialog.setNegativeButton(getText(android.R.string.no), null);
        dialog.show();
    }

    private static String getFailedMessage(String exception) {
        if (exception.contains("Permission denied"))
            return getString(R.string.message_permission_denied);
        if (exception.contains("Read-only file system"))
            return getString(R.string.message_read_only);
        return exception;
    }

    public static void showDeleteFileDialog(Context context, AbstractFileItem targetFile, Void onSucceed) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(R.string.dialog_title_notice);
        dialog.setMessage(String.format(getString(R.string.dialog_delete_file_message), targetFile.getFileName()));
        dialog.setPositiveButton(android.R.string.yes, (dialog1, which) -> {
            try {
                if (targetFile.delete()) {
                    Toast.makeText(context, getText(targetFile.isDirectory() ? R.string.message_success_to_delete_folder : R.string.message_success_to_delete_file), Toast.LENGTH_SHORT).show();
                    onSucceed.execute();
                } else {
                    Toast.makeText(context, getText(targetFile.isDirectory() ? R.string.message_failed_to_delete_folder : R.string.message_failed_to_delete_file), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                new AlertDialog.Builder(context)
                        .setTitle(targetFile.isDirectory() ? R.string.message_failed_to_delete_folder : R.string.message_failed_to_delete_file)
                        .setMessage(e.toString())
                        .show();
            }
        });
        dialog.setNegativeButton(android.R.string.cancel, null);
        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    public static void showAttributesDialog(Context context, AbstractFileItem item) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(R.string.file_action_attribute);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_file_attribute, null);
        dialog.setView(dialogView);
        TextView file_attribute_name = dialogView.findViewById(R.id.file_attribute_name);
        TextView file_attribute_type = dialogView.findViewById(R.id.file_attribute_type);
        TextView file_attribute_directory = dialogView.findViewById(R.id.file_attribute_directory);
        TextView file_attribute_path = dialogView.findViewById(R.id.file_attribute_path);
        TextView file_attribute_date = dialogView.findViewById(R.id.file_attribute_date);
        TextView file_attribute_linkto = dialogView.findViewById(R.id.file_attribute_linkto);
        ProgressBar loading = dialogView.findViewById(R.id.loading);


        //LinearLayout file_attribute_name_layout = dialogView.findViewById(R.id.file_attribute_name_layout);
        //LinearLayout file_attribute_type_layout = dialogView.findViewById(R.id.file_attribute_type_layout);
        LinearLayout file_attribute_directory_layout = dialogView.findViewById(R.id.file_attribute_directory_layout);
        //LinearLayout file_attribute_path_layout = dialogView.findViewById(R.id.file_attribute_path_layout);
        //LinearLayout file_attribute_date_layout = dialogView.findViewById(R.id.file_attribute_date_layout);
        LinearLayout file_attribute_linkto_layout = dialogView.findViewById(R.id.file_attribute_linkto_layout);


        setCopyingSpannableText(context, file_attribute_name, item.getFileName());
        String parent = item.getParent();
        if (parent != null)
            file_attribute_directory.setText(FileUtils.addSeparatorToPath(parent));
        else
            file_attribute_directory_layout.setVisibility(View.GONE);
        file_attribute_path.setText(item.getAbsolutePath());
        String dateText = item.getFormattedModifiedDate(SharedPreferencesGetter.getFileDateFormat());
        setModifier(context, dateText, file_attribute_date, item);


        CharSequence typeName;
        if (item.isSymbolicLink()) {
            typeName = item.isDirectory() ? getText(R.string.file_type_folder_link) : getText(R.string.file_type_file_link);
        } else {
            typeName = item.getType().getDisplayName();
        }
        file_attribute_type.setText(typeName);


        setCopyingSpannableText(context, file_attribute_directory, FileUtils.addSeparatorToPath(parent));
        setCopyingSpannableText(context, file_attribute_path, item.getAbsolutePath());


        AlertDialog alertDialog = dialog.show();

        if (item.isSymbolicLink()) {
            String linkTo = item.getLinkTo();
            if (!Utils.isEmpty(linkTo)) {
                SpannableString spannableString = new SpannableString(linkTo);
                UnderlineSpan underlineSpan = new UnderlineSpan();
                spannableString.setSpan(underlineSpan, 0, linkTo.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (item.isDirectory()) {
                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            ((FileManagerActivity) context).goToFile(linkTo);
                            alertDialog.dismiss();

                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                        }
                    };
                    spannableString.setSpan(clickableSpan, 0, linkTo.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    file_attribute_linkto.setText(spannableString);
                    file_attribute_linkto.setMovementMethod(LinkMovementMethod.getInstance());
                } else {
                    setCopyingSpannableText(context, file_attribute_linkto, linkTo);
                }
            }
        } else {
            file_attribute_linkto_layout.setVisibility(View.GONE);
        }


        TextView file_attribute_size = dialogView.findViewById(R.id.file_attribute_size);
        TextView file_attribute_folders = dialogView.findViewById(R.id.file_attribute_folders);
        TextView file_attribute_files = dialogView.findViewById(R.id.file_attribute_files);
        //LinearLayout file_attribute_size_layout = dialogView.findViewById(R.id.file_attribute_size_layout);
        LinearLayout file_attribute_folders_layout = dialogView.findViewById(R.id.file_attribute_folders_layout);
        LinearLayout file_attribute_files_layout = dialogView.findViewById(R.id.file_attribute_files_layout);

        //Object[] var=getFolderSize(file,fileSize,folders,files);
        if (item.isDirectory()) {
            file_attribute_size.setText(getText(R.string.dialog_file_attribute_calculating));
            file_attribute_folders.setText(getText(R.string.dialog_file_attribute_calculating));
            file_attribute_files.setText(getText(R.string.dialog_file_attribute_calculating));
            loading.setVisibility(View.VISIBLE);
            Thread thread = new Thread(() -> {
                long[] var = FileOperations.calculate(item);

                if (Thread.currentThread().isInterrupted()) return;

                if (var == null) return;
                long fileSize = var[0];
                int folders = (int) var[1];
                int files = (int) var[2];
                if (alertDialog.isShowing()) {
                    ((Activity) context).runOnUiThread(() -> {
                        file_attribute_size.setText(fileSize + " (" + FileUtils.getFormatSize(fileSize) + ")");
                        file_attribute_folders.setText(String.valueOf(folders));
                        file_attribute_files.setText(String.valueOf(files));
                        loading.setVisibility(View.GONE);
                    });
                }
            });
            thread.start();
            alertDialog.setOnDismissListener(dialog1 -> thread.interrupt());
        } else {
            file_attribute_folders_layout.setVisibility(View.GONE);
            file_attribute_files_layout.setVisibility(View.GONE);
            file_attribute_size.setText(item.getFileSize() + " (" + item.getFormattedFileSize() + ")");
        }
    }

    private static void setModifier(Context context, String dateText, TextView file_attribute_date, AbstractFileItem item) {
        SpannableString spannableString = new SpannableString(dateText);
        UnderlineSpan underlineSpan = new UnderlineSpan();
        spannableString.setSpan(underlineSpan, 0, dateText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                final EditText editText = new EditText(context);
                editText.setText(dateText);
                editText.setSingleLine();
                editText.setSelection(editText.length());
                LinearLayout linearLayout = new LinearLayout(context);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(80, 0, 80, 0);
                editText.setLayoutParams(lp);
                linearLayout.addView(editText);
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle(getText(R.string.dialog_modify_date_title));
                dialog.setView(linearLayout);
                dialog.setPositiveButton(getText(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            int code = item.modifyDate(String.valueOf(editText.getText()));
                            if (code == 0) {
                                setModifier(context, item.getFormattedModifiedDate(SharedPreferencesGetter.getFileDateFormat()), file_attribute_date, item);
                            } else {
                                Toast.makeText(context, String.format(context.getString(R.string.message_failed_to_execute_command_with_error_code), code), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, String.format(getString(R.string.message_operation_failed_with_exception), e), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.setNegativeButton(getText(android.R.string.no), null);
                dialog.show();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
            }
        };
        spannableString.setSpan(clickableSpan, 0, dateText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        file_attribute_date.setText(spannableString);
        file_attribute_date.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private static void setCopyingSpannableText(Context context, TextView textView, String text) {
        SpannableString spannableString = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                SystemUtils.copy(text);
                Toast.makeText(context, getText(R.string.message_success_copy), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
            }
        };
        spannableString.setSpan(clickableSpan, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static void showRenameDialog(Context context, AbstractFileItem target, Void onSucceed) {
        EditText editText = new EditText(context);
        editText.setText(target.getFileName());
        editText.setSelection(editText.length());
        editText.setSingleLine();
        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(80, 0, 80, 0);
        editText.setLayoutParams(lp);
        linearLayout.addView(editText);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(R.string.dialog_rename_title);
        dialog.setView(linearLayout);
        dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!Utils.isEmpty(editText.getText())) {
                    try {
                        File destFile = new File(target.getParent(), editText.getText().toString());
                        if (target.renameTo(destFile)) {
                            Toast.makeText(context, getText(R.string.message_success_rename), Toast.LENGTH_SHORT).show();
                            onSucceed.execute();
                        } else {
                            Toast.makeText(context, getText(R.string.message_failed_to_rename), Toast.LENGTH_SHORT).show();
                        }
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

    public static void showApkInformationDialog(Context context, AbstractFileItem apkFile, Drawable icon, String name, String version, String packageName, int versionCode, String size) {
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

    /**
     * @param to The parent not son
     **/
    public static void showCopyDialog(Activity activity, AbstractFileItem source, File to, Void onComplete, Void onInterrupted) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(getText(R.string.dialog_file_copy_title));
        dialog.setCancelable(false);
        final View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_file_copy, null);
        dialog.setView(dialogView);
        ProgressBar progressBar = dialogView.findViewById(R.id.dialog_copy_progress);
        TextView current = dialogView.findViewById(R.id.dialog_copy_current);
        TextView sourceTv = dialogView.findViewById(R.id.dialog_copy_source);
        TextView toTv = dialogView.findViewById(R.id.dialog_copy_to);
        TextView toTextTv = dialogView.findViewById(R.id.dialog_copy_to_text);
        toTextTv.setText(getText(R.string.dialog_file_copy_to));
        sourceTv.setText(source.getAbsolutePath());
        toTv.setText(FileUtils.addSeparatorToPath(to.getAbsolutePath()));

        AlertDialog nDialog = dialog.show();


        TextView remaining = dialogView.findViewById(R.id.dialog_copy_remaining);
        TextView percentage = dialogView.findViewById(R.id.dialog_copy_percentage);
        TextView processed = dialogView.findViewById(R.id.dialog_copy_processed);
        TextView total = dialogView.findViewById(R.id.dialog_copy_total);
        remaining.setText(getText(R.string.dialog_file_attribute_calculating));

        Thread thread = new Thread(() -> {
            long[] vars;
            if (source.isDirectory()) {
                vars = FileOperations.calculate(source);
                if (vars == null) {
                    activity.runOnUiThread(nDialog::dismiss);
                    onInterrupted.execute();
                    return;
                }
            } else {
                vars = new long[]{0, 0, 1};
            }

            long allInt = vars[1] + vars[2];
            if (source.isDirectory()) {
                allInt++;
            }
            String all = String.valueOf(allInt);
            long finalAllInt = allInt;
            activity.runOnUiThread(() -> {
                total.setText(all);
                remaining.setText(all);
                progressBar.setMax((int) finalAllInt);

            });

            try {
                List<File> failed = new ArrayList<>();
                boolean useRoot = !(source instanceof FileItem) || Utils.isRootFileWithDeviceStatus(to);

                /*if (source instanceof FileItem && Utils.isRootFileWithDeviceStatus(to)) {
                    useRoot = true;
                } else if (source instanceof FileItem && !Utils.isRootFileWithDeviceStatus(to)) {
                    useRoot = false;
                } else if (source instanceof RootFileItem && Utils.isRootFileWithDeviceStatus(to)) {
                    useRoot = true;
                } else if (source instanceof RootFileItem && !Utils.isRootFileWithDeviceStatus(to)) {
                    useRoot = true;
                }*/

                if (!useRoot) {
                    if (source.isDirectory()) {
                        copyDirectory(activity,
                                ((FileItem) source).getFile(),
                                to.getAbsolutePath(),
                                source.getFileName(),
                                progressBar,
                                current,
                                remaining,
                                percentage,
                                processed,
                                failed,
                                onInterrupted);
                    } else {
                        copyFile(activity,
                                ((FileItem) source).getFile(),
                                new File(to, source.getFileName()),
                                progressBar,
                                current,
                                remaining,
                                percentage,
                                processed,
                                failed);
                    }
                } else {
                    if (source.isDirectory()) copyDirectoryRoot(activity,
                            new File(source.getAbsolutePath()),
                            to.getAbsolutePath(),
                            source.getFileName(),
                            progressBar,
                            current,
                            remaining,
                            percentage,
                            processed,
                            failed,
                            onInterrupted);
                    else copyFileRoot(activity,
                            new File(source.getAbsolutePath()),
                            new File(to, source.getFileName()),
                            progressBar,
                            current,
                            remaining,
                            percentage,
                            processed,
                            failed);

                }
                if (failed.size() > 0) {
                    StringBuilder faileds = new StringBuilder();
                    faileds.append(failed.get(0));
                    for (int i = 1; i < failed.size(); i++) {
                        faileds.append("\n");
                        faileds.append(failed.get(i).getAbsolutePath());
                    }

                    activity.runOnUiThread(() -> ApplicationUtils.showDialog(activity, getText(R.string.dialog_files_them_failed_to_copy_title), faileds.toString(), null, null, null, null, null, null, true));
                }
            } catch (Exception e) {
                e.printStackTrace();
                activity.runOnUiThread(() -> new AlertDialog.Builder(activity)
                        .setTitle(R.string.message_failed_to_copy_files)
                        .setMessage(e.toString())
                        .show());
            }
            activity.runOnUiThread(() -> {
                current.setText(R.string.dialog_file_copy_done);
                nDialog.dismiss();
                //onComplete.execute();
            });
            onComplete.execute();


            /*while (!finished) {
                if (Integer.parseInt(processed.getText().toString()) == Integer.parseInt(total.getText().toString())) {
                    activity.runOnUiThread(() -> {
                        current.setText(R.string.dialog_file_copy_done);
                        nDialog.dismiss();
                        //onComplete.execute();
                    });
                    break;
                }
            }*/
        });
        thread.start();

        /*final long[] firstTime = {0};
        nDialog.setCanceledOnTouchOutside(false);//对话框区域外点击无效，但是可以点击返回键
        nDialog.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                long secondTime = System.currentTimeMillis();
                //Toast.makeText(activity, "st: "+secondTime+", ft:"+firstTime[0]+", +", Toast.LENGTH_SHORT).show();
                if (secondTime - firstTime[0] > 2000) {
                    Toast.makeText(activity, R.string.message_press_again_stop_operating, Toast.LENGTH_SHORT).show();
                    firstTime[0] = secondTime;
                } else {
                    thread.interrupt();
                }
                return true;
            }
            return false;
        });*/
    }

    private static void copyDirectory(Activity activity,
                                      File from,
                                      String toParent,
                                      String afterThatName,
                                      ProgressBar progressBar,
                                      TextView current,
                                      TextView remaining,
                                      TextView percentage,
                                      TextView processed,
                                      List<File> failed,
                                      Void onInterrupted) {
        if (from != null && from.exists() && !Utils.isEmpty(toParent)) {
            File toParentFile = new File(toParent);
            File to = new File(toParentFile, afterThatName);
            if (to.isDirectory() && to.listFiles() != null && to.listFiles().length == 0) {
                to.delete();
            }
            if (!to.exists()) {
                to.mkdirs();
                activity.runOnUiThread(() -> {
                    progressBar.setProgress(progressBar.getProgress() + 1);
                    remaining.setText(String.valueOf(Integer.parseInt(remaining.getText().toString()) - 1));
                    processed.setText(String.valueOf(Integer.parseInt(processed.getText().toString()) + 1));
                    float prog = (float) progressBar.getProgress() / progressBar.getMax() * 100;
                    percentage.setText(String.valueOf((int) prog));

                });
                File[] files;
                if ((files = from.listFiles()) != null) {
                    for (File file : files) {
                        if (Thread.currentThread().isInterrupted()) {
                            onInterrupted.execute();
                            return;
                        }
                        if (file.isFile()) {
                            copyFile(activity, file, new File(to, file.getName()), progressBar, current, remaining, percentage, processed, failed);
                        } else {
                            copyDirectory(activity, file, to.getAbsolutePath(), file.getName(), progressBar, current, remaining, percentage, processed, failed, onInterrupted);
                        }
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                long[] calculated = FileOperations.calculate(from);
                if (calculated == null) {
                    onInterrupted.execute();
                    return;
                }

                long files = calculated[1] + calculated[2] + 1;

                activity.runOnUiThread(() -> new AlertDialog.Builder(activity)
                        .setTitle(R.string.dialog_title_notice)
                        .setCancelable(false)
                        .setMessage(String.format(getString(R.string.dialog_copy_file_exists_message), to.getAbsolutePath()))
                        .setPositiveButton(R.string.dialog_copy_file_exists_replace, (dialog, which) -> {
                            File[] files1;
                            if ((files1 = from.listFiles()) != null) {
                                for (File file : files1) {


                                    if (Thread.currentThread().isInterrupted()) {
                                        onInterrupted.execute();
                                        return;
                                    }
                                    if (file.isFile()) {
                                        copyFile(activity, file, new File(to, file.getName()), progressBar, current, remaining, percentage, processed, failed);
                                    } else {
                                        copyDirectory(activity, file, to.getAbsolutePath(), file.getName(), progressBar, current, remaining, percentage, processed, failed, onInterrupted);
                                    }
                                    try {
                                        Thread.sleep(1);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            activity.runOnUiThread(() -> {
                                progressBar.setProgress(progressBar.getProgress() + (int) files);
                                remaining.setText(String.valueOf(Integer.parseInt(remaining.getText().toString()) - files));
                                processed.setText(String.valueOf(Integer.parseInt(processed.getText().toString()) + files));
                                float prog = (float) progressBar.getProgress() / progressBar.getMax() * 100;
                                percentage.setText(String.valueOf((int) prog));

                            });
                        })
                        .setNegativeButton(R.string.dialog_copy_file_exists_skip, (dialog, which) -> {
                            activity.runOnUiThread(() -> {
                                progressBar.setProgress(progressBar.getProgress() + (int) files);
                                remaining.setText(String.valueOf(Integer.parseInt(remaining.getText().toString()) - files));
                                processed.setText(String.valueOf(Integer.parseInt(processed.getText().toString()) + files));
                                float prog = (float) progressBar.getProgress() / progressBar.getMax() * 100;
                                percentage.setText(String.valueOf((int) prog));

                            });
                        }).show());
            }
        }
    }

    private static void copyFile(Activity activity,
                                 File source,
                                 File to,
                                 ProgressBar progressBar,
                                 TextView current,
                                 TextView remaining,
                                 TextView percentage,
                                 TextView processed,
                                 List<File> failed) {
        activity.runOnUiThread(() -> current.setText(source.getName()));
        if (to.exists()) {
            activity.runOnUiThread(() -> new AlertDialog.Builder(activity)
                    .setTitle(R.string.dialog_title_notice)
                    .setMessage(String.format(getString(R.string.dialog_copy_file_exists_message), to.getPath()))
                    .setCancelable(false)
                    .setPositiveButton(R.string.dialog_copy_file_exists_replace, (DialogInterface dialog, int which) -> {
                        try {
                            to.delete();
                            to.createNewFile();
                            FileUtils.copy(source, to);
                        } catch (IOException e) {
                            e.printStackTrace();
                            failed.add(source);
                        }
                        activity.runOnUiThread(() -> {
                            progressBar.setProgress(progressBar.getProgress() + 1);
                            remaining.setText(String.valueOf(Integer.parseInt(remaining.getText().toString()) - 1));
                            processed.setText(String.valueOf(Integer.parseInt(processed.getText().toString()) + 1));
                            float prog = (float) progressBar.getProgress() / progressBar.getMax() * 100;
                            percentage.setText(String.valueOf((int) prog));

                        });
                    })
                    .setNegativeButton(R.string.dialog_copy_file_exists_skip, (dialog, which) -> {
                        activity.runOnUiThread(() -> {
                            progressBar.setProgress(progressBar.getProgress() + 1);
                            remaining.setText(String.valueOf(Integer.parseInt(remaining.getText().toString()) - 1));
                            processed.setText(String.valueOf(Integer.parseInt(processed.getText().toString()) + 1));
                            float prog = (float) progressBar.getProgress() / progressBar.getMax() * 100;
                            percentage.setText(String.valueOf((int) prog));
                        });
                    }).show());
        } else {
            try {
                to.createNewFile();
                FileUtils.copy(source, to);
            } catch (IOException e) {
                e.printStackTrace();
                failed.add(source);
            }
            activity.runOnUiThread(() -> {
                progressBar.setProgress(progressBar.getProgress() + 1);
                remaining.setText(String.valueOf(Integer.parseInt(remaining.getText().toString()) - 1));
                processed.setText(String.valueOf(Integer.parseInt(processed.getText().toString()) + 1));
                float prog = (float) progressBar.getProgress() / progressBar.getMax() * 100;
                percentage.setText(String.valueOf((int) prog));

            });
        }
    }


    private static void copyDirectoryRoot(Activity activity,
                                          File from,
                                          String toParent,
                                          String afterThatName,
                                          ProgressBar progressBar,
                                          TextView current,
                                          TextView remaining,
                                          TextView percentage,
                                          TextView processed,
                                          List<File> failed,
                                          Void onInterrupted) {
        if (from != null && RootFileItem.sExists(from.getAbsolutePath()) && !Utils.isEmpty(toParent)) {
            File toParentFile = new File(toParent);
            File to = new File(toParentFile, afterThatName);
            /*if (to.isDirectory() && to.listFiles() != null && to.listFiles().length == 0) {
                to.delete();
            }*/
            if (!RootFileItem.sExists(to.getAbsolutePath())) {
                ShellUtils.executeSuCommand("mkdir '" + to.getAbsolutePath() + "'");
                activity.runOnUiThread(() -> {
                    progressBar.setProgress(progressBar.getProgress() + 1);
                    remaining.setText(String.valueOf(Integer.parseInt(remaining.getText().toString()) - 1));
                    processed.setText(String.valueOf(Integer.parseInt(processed.getText().toString()) + 1));
                    float prog = (float) progressBar.getProgress() / progressBar.getMax() * 100;
                    percentage.setText(String.valueOf((int) prog));

                });
                List<RootFileItem> rootFileItems = new LinkedList<>();
                for (String s : ShellUtils.executeSuCommand("ls -a -l '" + PathUtils.toDirectoryPath(from.getAbsolutePath()) + "'").getOut()) {
                    try {
                        RootFileItem rootFileItem = RootFileItem.parseLs(s, from.getAbsolutePath());
                        if (rootFileItem != null && !".".equals(rootFileItem.getFileName()) && !"..".equals(rootFileItem.getFileName())) {
                            rootFileItems.add(rootFileItem);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                for (RootFileItem file : rootFileItems) {

                    if (Thread.currentThread().isInterrupted()) {
                        onInterrupted.execute();
                        return;
                    }
                    if (file.isDirectory()) {
                        copyDirectoryRoot(activity, new File(file.getAbsolutePath()), to.getAbsolutePath(), file.getName(), progressBar, current, remaining, percentage, processed, failed, onInterrupted);
                    } else {
                        copyFileRoot(activity, new File(file.getAbsolutePath()), new File(to, file.getName()), progressBar, current, remaining, percentage, processed, failed);
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            } else {
                long[] calculated = FileOperations.calculate(new RootFileItem(from.getAbsolutePath(), null, 0, 0, true, false, null, null, false, null));
                if (calculated == null) {
                    onInterrupted.execute();
                    return;
                }

                long files = calculated[1] + calculated[2] + 1;

                activity.runOnUiThread(() -> new AlertDialog.Builder(activity)
                        .setTitle(R.string.dialog_title_notice)
                        .setCancelable(false)
                        .setMessage(String.format(getString(R.string.dialog_copy_file_exists_message), to.getAbsolutePath()))
                        .setPositiveButton(R.string.dialog_copy_file_exists_replace, (dialog, which) -> {
                            List<RootFileItem> rootFileItems = new LinkedList<>();
                            for (String s : ShellUtils.executeSuCommand("ls -a -l '" + PathUtils.toDirectoryPath(from.getAbsolutePath()) + "'").getOut()) {
                                try {
                                    RootFileItem rootFileItem = RootFileItem.parseLs(s, from.getAbsolutePath());
                                    if (rootFileItem != null && !".".equals(rootFileItem.getFileName()) && !"..".equals(rootFileItem.getFileName())) {
                                        rootFileItems.add(rootFileItem);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            for (RootFileItem file : rootFileItems) {
                                if (Thread.currentThread().isInterrupted()) {
                                    onInterrupted.execute();
                                    return;
                                }
                                if (file.isDirectory()) {
                                    copyDirectoryRoot(activity, new File(file.getAbsolutePath()), to.getAbsolutePath(), file.getName(), progressBar, current, remaining, percentage, processed, failed, onInterrupted);
                                } else {
                                    copyFileRoot(activity, new File(file.getAbsolutePath()), new File(to, file.getName()), progressBar, current, remaining, percentage, processed, failed);
                                }
                                try {
                                    Thread.sleep(1);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            activity.runOnUiThread(() -> {
                                progressBar.setProgress(progressBar.getProgress() + (int) files);
                                remaining.setText(String.valueOf(Integer.parseInt(remaining.getText().toString()) - files));
                                processed.setText(String.valueOf(Integer.parseInt(processed.getText().toString()) + files));
                                float prog = (float) progressBar.getProgress() / progressBar.getMax() * 100;
                                percentage.setText(String.valueOf((int) prog));

                            });
                        })
                        .setNegativeButton(R.string.dialog_copy_file_exists_skip, (dialog, which) -> {
                            activity.runOnUiThread(() -> {
                                progressBar.setProgress(progressBar.getProgress() + (int) files);
                                remaining.setText(String.valueOf(Integer.parseInt(remaining.getText().toString()) - files));
                                processed.setText(String.valueOf(Integer.parseInt(processed.getText().toString()) + files));
                                float prog = (float) progressBar.getProgress() / progressBar.getMax() * 100;
                                percentage.setText(String.valueOf((int) prog));

                            });
                        }).show());
            }
        }
    }

    private static void copyFileRoot(Activity activity,
                                     File source,
                                     File to,
                                     ProgressBar progressBar,
                                     TextView current,
                                     TextView remaining,
                                     TextView percentage,
                                     TextView processed,
                                     List<File> failed) {
        activity.runOnUiThread(() -> current.setText(source.getName()));
        if (RootFileItem.sExists(to.getAbsolutePath())) {
            activity.runOnUiThread(() -> new AlertDialog.Builder(activity)
                    .setTitle(R.string.dialog_title_notice)
                    .setMessage(String.format(getString(R.string.dialog_copy_file_exists_message), to.getPath()))
                    .setCancelable(false)
                    .setPositiveButton(R.string.dialog_copy_file_exists_replace, (DialogInterface dialog, int which) -> {
                        try {
                            if (!ShellUtils.executeSuCommand("cp -f '" + source.getAbsolutePath() + "' '" + to.getAbsolutePath() + "'").isSuccess()) {
                                failed.add(source);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            failed.add(source);
                        }
                        activity.runOnUiThread(() -> {
                            progressBar.setProgress(progressBar.getProgress() + 1);
                            remaining.setText(String.valueOf(Integer.parseInt(remaining.getText().toString()) - 1));
                            processed.setText(String.valueOf(Integer.parseInt(processed.getText().toString()) + 1));
                            float prog = (float) progressBar.getProgress() / progressBar.getMax() * 100;
                            percentage.setText(String.valueOf((int) prog));

                        });
                    })
                    .setNegativeButton(R.string.dialog_copy_file_exists_skip, (dialog, which) -> {
                        activity.runOnUiThread(() -> {
                            progressBar.setProgress(progressBar.getProgress() + 1);
                            remaining.setText(String.valueOf(Integer.parseInt(remaining.getText().toString()) - 1));
                            processed.setText(String.valueOf(Integer.parseInt(processed.getText().toString()) + 1));
                            float prog = (float) progressBar.getProgress() / progressBar.getMax() * 100;
                            percentage.setText(String.valueOf((int) prog));
                        });
                    }).show());
        } else {
            try {
                if (!ShellUtils.executeSuCommand("cp '" + source.getAbsolutePath() + "' '" + to.getAbsolutePath() + "'").isSuccess()) {
                    failed.add(source);
                }
            } catch (Exception e) {
                e.printStackTrace();
                failed.add(source);
            }
            activity.runOnUiThread(() -> {
                progressBar.setProgress(progressBar.getProgress() + 1);
                remaining.setText(String.valueOf(Integer.parseInt(remaining.getText().toString()) - 1));
                processed.setText(String.valueOf(Integer.parseInt(processed.getText().toString()) + 1));
                float prog = (float) progressBar.getProgress() / progressBar.getMax() * 100;
                percentage.setText(String.valueOf((int) prog));
            });
        }
    }

    public static void showOpenMethodDialog(Context context, AbstractFileItem file) {
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
