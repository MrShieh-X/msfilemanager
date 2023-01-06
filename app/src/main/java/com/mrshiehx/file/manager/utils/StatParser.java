package com.mrshiehx.file.manager.utils;

import com.mrshiehx.file.manager.beans.fileItem.RootFileItem;
import com.topjohnwu.superuser.Shell;

import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatParser {
    public static final String STAT_FORMAT = "%F %Y %s %A %N";
    public static final Pattern STAT_PATTERN = Pattern.compile("\\s*(directory|regular file|regular empty file|character device|symbolic link)\\s+(\\d+)\\s+(\\d+)\\s+([a-zA-Z\\-]+)\\s+([\\s\\S]+)");
    public final String line;
    private Matcher matcher;
    private int fileType = -1;
    private String permission;
    private long modifiedDate = -1;
    private long fileSize = -1;
    private LastInfo lastInfo;
    private String fileName;

    public StatParser(String line) {
        this.line = line;
    }

    private void _init() throws ParseException {
        if (matcher == null) {
            this.matcher = STAT_PATTERN.matcher(line);
            if (!matcher.find())
                throw new ParseException("regular expression mismatch", 0);
        }
    }

    public int getFileType() throws ParseException {
        _init();
        if (fileType == -1) {
            String first = matcher.group(1);
            if ("directory".equalsIgnoreCase(first)) {
                fileType = 0;
            } else if ("regular file".equalsIgnoreCase(first) || "regular empty file".equalsIgnoreCase(first)) {
                fileType = 1;
            } else if ("character device".equalsIgnoreCase(first)) {
                fileType = 2;
            } else if ("symbolic link".equalsIgnoreCase(first)) {
                fileType = 3;
            } else
                throw new ParseException("unknown file type", 0);
        }
        return fileType;
    }

    public String getPermission() throws ParseException {
        _init();
        if (permission == null) {
            permission = matcher.group(4);
        }
        return permission;
    }

    public long getFileSize() throws ParseException {
        _init();
        if (fileSize == -1) {
            if (getPermission().startsWith("l")) {
                try {
                    fileSize = new StatParser(ShellUtils.executeSuCommand("stat -c '" + StatParser.STAT_FORMAT + "' '" + getLastInfo().linkTo + "'").getOut().get(0)).getFileSize();
                } catch (Exception e) {
                    e.printStackTrace();
                    fileSize = 0;
                }
            } else {
                fileSize = Long.parseLong(Objects.requireNonNull(matcher.group(3)));
            }
        }
        return fileSize;
    }

    public long getModifiedDate() throws ParseException {
        _init();
        if (modifiedDate == -1) {
            modifiedDate = Long.parseLong(Objects.requireNonNull(matcher.group(2))) * 1000;
        }
        return modifiedDate;
    }

    public LastInfo getLastInfo() throws ParseException {
        _init();
        if (lastInfo == null) {
            String last = matcher.group(5);
            if (Utils.isEmpty(last))
                throw new ParseException("file name is empty", 0);
            last = last.trim();

            if (Utils.isEmpty(getPermission()))
                throw new ParseException("permission is empty", 0);

            String absolutePath;
            boolean isSymbolicLink;
            boolean isDirectory;
            String linkTo;
            if (permission.startsWith("l") && (last.contains(" -> `")) && getFileType() == 3) {
                String[] apAndLink = last.split(" -> `");


                absolutePath = apAndLink[0].replaceAll("/+", "/");
                String link = apAndLink[1];
                isSymbolicLink = true;
                linkTo = link.substring(0, link.lastIndexOf('\'')).replaceAll("/+", "/");
                if (!linkTo.startsWith("/")) {
                    linkTo = PathUtils.toAbsolutePath(absolutePath.substring(0, absolutePath.lastIndexOf('/') + 1), linkTo);
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
                absolutePath = last.replaceAll("/+", "/");
                isDirectory = permission.startsWith("d") && getFileType() == 0;
            }
            lastInfo = new LastInfo(isDirectory, absolutePath, linkTo, isSymbolicLink);
        }
        return lastInfo;

    }

    public String getFileName() throws ParseException {
        _init();
        if (fileName == null) {
            String absolutePath = getLastInfo().absolutePath;
            if ("/".equals(absolutePath)) {
                fileName = "/";
            } else {
                fileName = absolutePath.substring(absolutePath.lastIndexOf("/") + 1);
            }
        }
        return fileName;
    }


    public static class LastInfo {
        public final boolean isDirectory;
        public final String absolutePath;
        public final String linkTo;
        public final boolean isSymbolicLink;

        private LastInfo(boolean isDirectory, String absolutePath, String linkTo, boolean isSymbolicLink) {
            this.isDirectory = isDirectory;
            this.absolutePath = absolutePath;
            this.linkTo = linkTo;
            this.isSymbolicLink = isSymbolicLink;
        }
    }
}
