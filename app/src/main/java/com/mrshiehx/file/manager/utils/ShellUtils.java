package com.mrshiehx.file.manager.utils;

import com.mrshiehx.file.manager.R;
import com.mrshiehx.file.manager.application.MSFMApplication;
import com.mrshiehx.file.manager.exceptions.NoRootPermissionException;
import com.topjohnwu.superuser.Shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class ShellUtils {
    public static Shell.Result executeSuCommand(String cmd) throws NoRootPermissionException {
        if (!Shell.rootAccess())
            throw new NoRootPermissionException(MSFMApplication.getContext().getString(R.string.message_device_not_root));
        return Shell.su(cmd).exec();
    }

    /*public static List<String> executeSuCommandAsList(String cmd) throws Exception {
        Shell.Result result = executeSuCommand(cmd);
        if (!result.isSuccess()) {
            throw new Exception(MSFMApplication.getContext().getString(R.string.message_failed_to_execute_command_with_error_code,result.getCode()));
        }
        return result.getOut();
    }*/

    public static Shell.Result executeShCommand(String[] cmd) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(cmd);
        process.getOutputStream().close();
        List<String> out = new LinkedList<>();
        try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = input.readLine()) != null) {
                out.add(line);
            }
        }
        List<String> err = new LinkedList<>();
        try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = input.readLine()) != null) {
                err.add(line);
            }
        }
        int code = process.waitFor();
        process.destroy();
        return new Shell.Result() {
            public List<String> getOut() {
                return out;
            }

            public List<String> getErr() {
                return err;
            }

            public int getCode() {
                return code;
            }
        };
    }

    /*public static List<String> executeShCommandAsList(String[] cmd) throws IOException, InterruptedException {
        Shell.Result result = executeShCommand(cmd);
        int code=result.getCode();
        if (code!=0) {
            throw new Exception(MSFMApplication.getContext().getString(R.string.message_failed_to_execute_command_with_error_code,code));
        }
        return result.getOut();
    }*/

    /*public static ShellResult executeRootCommand(String[] command, File dir) throws Exception {

        Process process = *//*Runtime.getRuntime().exec(command, null, dir)*//*MSFMApplication.getInstance().getRootProcess();
        process.getOutputStream().write("ls -p -l /sdcard".getBytes());
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        List<String> out=new LinkedList<>();
        while((line = reader.readLine())!=null){
            out.add(line);
        }
        process.getInputStream().close();
        BufferedReader readerE = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        List<String> err=new LinkedList<>();
        while((line = readerE.readLine())!=null){
            err.add(line);
        }
        process.getErrorStream().close();
        return new ShellResult(out,err,process.waitFor());
    }

    public static class ShellResult {
        private final List<String> out;
        private final List<String> err;
        private final int code;

        public ShellResult(List<String> out, List<String> err, int code) {
            this.out = out;
            this.err = err;
            this.code = code;
        }

        public List<String> getOut() {
            return Collections.unmodifiableList(this.out);
        }

        public List<String> getErr() {
            return Collections.unmodifiableList(this.err);
        }

        public int getCode() {
            return this.code;
        }

        public boolean isSuccess() {
            return getCode() == 0;
        }
    }*/
}
