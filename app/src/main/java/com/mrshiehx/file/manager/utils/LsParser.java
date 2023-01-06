package com.mrshiehx.file.manager.utils;

import android.annotation.SuppressLint;

import com.mrshiehx.file.manager.beans.fileItem.RootFileItem;
import com.topjohnwu.superuser.Shell;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LsParser {
    public static final Pattern LS_PATTERN = Pattern.compile("\\s*([a-zA-Z\\-]+)\\s+(\\d*)\\s*([\\S]+)\\s+([\\S]+)\\s+(\\d*|\\d+,\\s+\\d+)\\s*(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2})\\s+([\\s\\S]+)");
    public final String line;
    public final String parent;
    private Matcher matcher;
    private String permission;
    private long modifiedDate = -1;
    private long fileSize = -1;
    private LastInfo lastInfo;
    private String onlyFileName;

    public LsParser(String line, String parent) {
        this.line = line;
        this.parent = parent != null ? parent.replaceAll("/+", "/") : null;
    }

    private void _init() throws ParseException {
        if (matcher == null) {
            this.matcher = LS_PATTERN.matcher(line);
            if (!matcher.find())
                throw new ParseException("regular expression mismatch", 0);
        }
    }

    public String getPermission() throws ParseException {
        _init();
        if (permission == null) {
            permission = matcher.group(1);
        }
        return permission;
    }

    @SuppressLint("SimpleDateFormat")
    public long getModifiedDate() throws ParseException {
        _init();
        if (modifiedDate == -1) {
            modifiedDate = Objects.requireNonNull(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(Objects.requireNonNull(matcher.group(6)))).getTime();
        }
        return modifiedDate;
    }

    public long getFileSize() throws ParseException {
        _init();
        if (fileSize == -1) {
            if (getPermission().startsWith("l")) {
                try {
                    String linkTo = getLastInfo().linkTo;
                    fileSize = new LsParser(ShellUtils.executeSuCommand("ls -dl '" + linkTo + "'").getOut().get(0), linkTo.substring(0, linkTo.lastIndexOf('/'))).getFileSize();
                } catch (Exception e) {
                    e.printStackTrace();
                    fileSize = 0;
                }
            } else {
                String fifth = matcher.group(5);
                if (!Utils.isEmpty(fifth) && !fifth.contains(",")) {
                    fileSize = Long.parseLong(fifth);
                } else fileSize = 0;
            }
        }
        return fileSize;
    }

    public LastInfo getLastInfo() throws ParseException {
        _init();
        if (lastInfo == null) {
            if (Utils.isEmpty(parent))
                throw new ParseException("parent is empty", 0);

            if (Utils.isEmpty(getPermission()))
                throw new ParseException("permission is empty", 0);

            String last = matcher.group(7);
            if (Utils.isEmpty(last))
                throw new ParseException("file name is empty", 0);

            boolean isDirectory;
            String absolutePath;
            String linkTo;
            boolean isSymbolicLink;
            String fileName;

            String[] nameAndLink = last.split(" -> ");
            if (permission.startsWith("l") && nameAndLink.length > 1) {
                isSymbolicLink = true;
                fileName = nameAndLink[0].replace("\\", "");
                absolutePath = (PathUtils.toDirectoryPath(parent) + fileName).replaceAll("/+", "/");
                linkTo = nameAndLink[1].replace("\\", "").replaceAll("/+", "/");

                if (!linkTo.startsWith("/")) {
                    linkTo = PathUtils.toAbsolutePath(parent, linkTo);
                }


                if (RootFileItem.SPECIAL_PATHS.contains(absolutePath)) {
                    List<String> results;
                    if (Shell.rootAccess()) {
                        results = Shell.su("ls -l " + PathUtils.toDirectoryPath(absolutePath)).exec().getOut();
                    } else {
                        results = Shell.sh("ls -l " + PathUtils.toDirectoryPath(absolutePath)).exec().getOut();
                    }
                    isDirectory = false;
                    for (String r : results) {
                        if (r.startsWith("total ")) {
                            isDirectory = true;
                            break;
                        }
                    }
                } else
                    isDirectory = NativeUtils.NATIVE_UTILS.isDirectory(absolutePath) || NativeUtils.NATIVE_UTILS.isDirectory(linkTo);

            } else {
                isSymbolicLink = false;
                linkTo = null;
                fileName = last.replace("\\", "");
                absolutePath = (PathUtils.toDirectoryPath(parent) + fileName).replaceAll("/+", "/");
                isDirectory = permission.startsWith("d");
            }
            lastInfo = new LastInfo(isDirectory, absolutePath, linkTo, isSymbolicLink, fileName);
        }
        return lastInfo;
    }

    public String getOnlyFileName() throws ParseException {
        _init();
        if (onlyFileName == null) {
            String last = matcher.group(7);
            if (Utils.isEmpty(last))
                throw new ParseException("file name is empty", 0);

            String[] nameAndLink = last.split(" -> ");
            if (nameAndLink.length == 1) {
                onlyFileName = last.replace("\\", "");
            } else {
                onlyFileName = nameAndLink[0].replace("\\", "");
            }
        }
        return onlyFileName;
    }


    public static class LastInfo {
        public final boolean isDirectory;
        public final String absolutePath;
        public final String linkTo;
        public final boolean isSymbolicLink;
        public final String fileName;

        private LastInfo(boolean isDirectory, String absolutePath, String linkTo, boolean isSymbolicLink, String fileName) {
            this.isDirectory = isDirectory;
            this.absolutePath = absolutePath;
            this.linkTo = linkTo;
            this.isSymbolicLink = isSymbolicLink;
            this.fileName = fileName;
        }
    }
}
