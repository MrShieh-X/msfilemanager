package com.mrshiehx.file.manager.beans.fileItem;

import android.graphics.drawable.Drawable;

import com.mrshiehx.file.manager.utils.FileUtils;
import com.mrshiehx.file.manager.utils.LsParser;
import com.mrshiehx.file.manager.utils.ShellUtils;
import com.mrshiehx.file.manager.utils.StringUtils;
import com.mrshiehx.file.manager.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class FileItem extends AbstractFileItem implements Serializable {

    protected final File file;
    private String linkTo;
    private boolean gotLinkTo;

    public FileItem(File file) {
        this(file, false);
    }

    public FileItem(File file, boolean isBacker) {
        this(file, isBacker, null);
    }

    public FileItem(File file, boolean isBacker, Drawable smallIcon) {
        super(isBacker, smallIcon, file.isDirectory(), file.getName());
        this.file = file;
    }

    private String getLinkTo0() {
        if (!isBacker) {
            if (linkTo == null && !gotLinkTo) {
                try {
                    linkTo = new LsParser(ShellUtils.executeShCommand(new String[]{"ls", "-dl", getAbsolutePath()}).getOut().get(0), file.getParentFile().getAbsolutePath()).getLastInfo().linkTo;
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                gotLinkTo = true;
            }
            return linkTo;
        } else return null;
    }

    @Override
    public boolean isSymbolicLink() {
        return !Utils.isEmpty(getLinkTo0());
    }

    @Override
    public String getLinkTo() {
        return getLinkTo0();
    }

    @Override
    public boolean delete() {
        /*return file.delete();*/
        try {
            return ShellUtils.executeShCommand(new String[]{"rm", "-rf", getAbsolutePath()}).getCode() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean renameTo(File dest) {
        return file.renameTo(dest);
    }

    @Override
    public byte[] getFileBytes() throws IOException {
        return FileUtils.toByteArray(file);
    }

    @Override
    public String getFileContent() throws IOException {
        return StringUtils.newStringFromBytes(getFileBytes());
    }

    @Override
    public void appendBytes(byte[] bytes) throws IOException {
        FileOutputStream out = new FileOutputStream(file, true);
        out.write(bytes);
        out.close();
    }

    @Override
    public void modifyAllBytes(byte[] bytes) throws IOException {
        FileOutputStream out = new FileOutputStream(file, false);
        out.write(bytes);
        out.close();
    }

    @Override
    public void appendBytes(String content) throws IOException {
        this.appendBytes(content.getBytes());
    }

    @Override
    public void modifyAllBytes(String content) throws IOException {
        this.modifyAllBytes(content.getBytes());
    }

    @Override
    public int modifyDate(String date) throws Exception {
        return ShellUtils.executeShCommand(new String[]{"touch", "-m", "-d", date, getAbsolutePath()}).getCode();
    }

    @Override
    public long getFileSize() {
        if (isDirectory())
            return -1;
        return file.length();
    }


    @Override
    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    @Override
    public String getFileName() {
        return file.getName();
    }

    @Override
    public long getModifiedDate() {
        return file.lastModified();
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    public File getFile() {
        return file;
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public boolean createNewFile() throws IOException {
        return file.createNewFile();
    }
}
