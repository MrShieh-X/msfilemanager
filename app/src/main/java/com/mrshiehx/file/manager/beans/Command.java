package com.mrshiehx.file.manager.beans;

import com.mrshiehx.file.manager.utils.SystemUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Command {
    private String[] arguments;
    public Command(String... arguments){
        this.arguments=arguments;
    }

    public String[]getArguments(){
        return arguments;
    }

    public String getCommandText(){
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append(arguments[0]);
        for(int i=0;i<arguments.length-1;i++){
            stringBuilder.append(" ");
            stringBuilder.append(arguments[i]);
        }
        return stringBuilder.toString();
    }

    public Command addArgument(String argument){
        List<String>list = Arrays.asList(arguments);
        list.add(argument);
        this.arguments = (String[]) list.toArray();
        return this;
    }

    public Process execute()throws IOException{
        return SystemUtils.executeCommand(this.getCommandText());
    }
}
