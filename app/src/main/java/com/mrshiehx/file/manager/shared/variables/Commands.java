package com.mrshiehx.file.manager.shared.variables;

import com.mrshiehx.file.manager.beans.Command;

public class Commands {
    public static Command getSuperUserCommand(){
        return new Command("su");
    }
}
