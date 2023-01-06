package com.mrshiehx.file.manager.beans.fileItem;

import android.graphics.drawable.Drawable;

import com.mrshiehx.file.manager.utils.LsParser;
import com.mrshiehx.file.manager.utils.ShellUtils;
import com.mrshiehx.file.manager.utils.StatParser;
import com.mrshiehx.file.manager.utils.StringUtils;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RootFileItem extends AbstractFileItem implements Serializable {
    public static final List<String> SPECIAL_PATHS = Collections.unmodifiableList(Arrays.asList("/bugreports"));
    private final String absolutePath;
    private final String fileName;
    private final long fileSize;
    private long modifiedDate;
    private final boolean isDirectory;
    private final boolean isSymbolicLink;
    private final String linkTo;
    private final String permission;

    public RootFileItem(String absolutePath, String fileName, long fileSize, long modifiedDate, boolean isDirectory, boolean isSymbolicLink, String linkTo, String permission, boolean isBacker, Drawable smallIcon) {
        super(isBacker, smallIcon, isDirectory, fileName);
        this.absolutePath = absolutePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.modifiedDate = modifiedDate;
        this.isDirectory = isDirectory;
        this.isSymbolicLink = isSymbolicLink;
        this.linkTo = linkTo;
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

    @Override
    public String getAbsolutePath() {
        return absolutePath;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public long getFileSize() {
        if (isDirectory)
            return -1;
        return fileSize;
    }

    @Override
    public long getModifiedDate() {
        return modifiedDate;
    }

    @Override
    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public boolean isSymbolicLink() {
        return isSymbolicLink;
    }

    @Override
    public String getLinkTo() {
        if (!isSymbolicLink)
            return null;
        //throw new UnsupportedOperationException("This operation cannot be performed on a non symbolic linked file");
        return linkTo;
    }

    @Override
    public boolean delete() {
        try {
            return ShellUtils.executeSuCommand("rm -rf '" + getAbsolutePath() + "'").isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean renameTo(File dest) {
        try {
            return ShellUtils.executeSuCommand("mv '" + getAbsolutePath() + "' '" + dest.getAbsolutePath() + "'").isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public byte[] getFileBytes() throws IOException {
        /**code Waiting for improvement*/
        String fileContent = getFileContent();
        if (fileContent != null) {
            return fileContent.getBytes();
        }
        return null;
    }

    @Override
    public String getFileContent() throws IOException {
        Shell.Result result = ShellUtils.executeSuCommand("cat '" + getAbsolutePath() + "'");
        if (result.isSuccess()) {
            StringBuilder sb = new StringBuilder();
            int size = result.getOut().size();
            for (int i = 0; i < size; i++) {
                String s = result.getOut().get(i);

                sb.append(s);
                if (i + 1 != size) {
                    sb.append('\n');
                }
                /*if (i + 1 != size) {
                    sb.append(s.substring(0, s.length() - 1));
                    sb.append('\n');
                } else {
                    if (!s.endsWith("$")) {
                        sb.append(s);
                    } else {
                        sb.append(s.substring(0, s.length() - 1));
                        sb.append('\n');
                    }
                }*/
            }
            return sb.toString();
        } else throw new IOException();
    }

    @Override
    public void appendBytes(byte[] bytes) throws IOException {
        appendBytes(StringUtils.newStringFromBytes(bytes));
    }

    @Override
    public void modifyAllBytes(byte[] bytes) throws IOException {
        modifyAllBytes(StringUtils.newStringFromBytes(bytes));
    }

    @Override
    public void appendBytes(String content) throws IOException {
        if (!ShellUtils.executeSuCommand("echo -n '" + content + "' >> '" + getAbsolutePath() + "'").isSuccess())
            throw new IOException();
    }

    @Override
    public void modifyAllBytes(String content) throws IOException {
        if (!ShellUtils.executeSuCommand("echo '" + content + "' > '" + getAbsolutePath() + "'").isSuccess())
            throw new IOException();
    }

    @Override
    public int modifyDate(String date) throws Exception {
        int code = ShellUtils.executeSuCommand("touch -m -d '" + date + "' '" + getAbsolutePath() + "'").getCode();
        if (code == 0) {
            try {
                this.modifiedDate = new LsParser(ShellUtils.executeSuCommand("ls -dl '" + getAbsolutePath() + "'").getOut().get(0), null).getModifiedDate();
            } catch (Exception e) {
                this.modifiedDate = -1;
                e.printStackTrace();
            }
        }
        return code;
    }

    @Override
    public boolean exists() {
        return sExists(getAbsolutePath());
    }

    public static RootFileItem parseStat(String line) throws ParseException {
        //注释它们是因为如果有文件名就叫“Permission denied”或“No such file or directory”那就处理不了。就算不要这两行代码，正则表达式也不会通过
        //if (line.contains("Permission denied")) throw new PermissionDeniedException();
        //if (line.contains("No such file or directory")) return null;

        StatParser statParser = new StatParser(line);
        StatParser.LastInfo lastInfo = statParser.getLastInfo();

        return new RootFileItem(
                lastInfo.absolutePath,
                statParser.getFileName(),
                statParser.getFileSize(),
                statParser.getModifiedDate(),
                lastInfo.isDirectory,
                lastInfo.isSymbolicLink,
                lastInfo.linkTo,
                statParser.getPermission(),
                false,
                null);
    }


    public static RootFileItem parseLs(String line, String parent) throws ParseException {
        //注释它们是因为如果有文件名就叫“Permission denied”或“No such file or directory”那就处理不了。就算不要这两行代码，正则表达式也不会通过
        //if (line.contains("Permission denied")) throw new PermissionDeniedException();
        //if (line.contains("No such file or directory")) return null;
        if (line.startsWith("total ")) return null;

        LsParser lsParser = new LsParser(line, parent);
        LsParser.LastInfo lastInfo = lsParser.getLastInfo();

        return new RootFileItem(
                lastInfo.absolutePath,
                lastInfo.fileName,
                lsParser.getFileSize(),
                lsParser.getModifiedDate(),
                lastInfo.isDirectory,
                lastInfo.isSymbolicLink,
                lastInfo.linkTo,
                lsParser.getPermission(),
                false,
                null);
    }

    public static boolean sExists(String absolutePath) {
        for (String s : ShellUtils.executeSuCommand("ls -dl '" + absolutePath + "'").getOut()) {
            if (LsParser.LS_PATTERN.matcher(s).find())
                return true;
        }
        return false;
    }

    @Override
    public boolean createNewFile() throws IOException {
        try {
            return ShellUtils.executeSuCommand("touch '" + getAbsolutePath() + "'").isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
