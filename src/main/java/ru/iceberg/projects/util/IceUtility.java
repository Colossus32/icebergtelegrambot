package ru.iceberg.projects.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class IceUtility {

    public static String nameChecker(String text){

        text = text.substring(text.indexOf(' ')).trim();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!Character.isAlphabetic(c) && !Character.isDigit(c) && c != ' ') return "";
        }

        return text.replace(' ', '-');
    }

    public static boolean onlyDigitChecker(String text){
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) return false;
        }
        return true;
    }

    public static String createPath(String name) {
        boolean isAtFirstPartOfAlphabet = (name.charAt(0) - 'а') < 11;
        String middleOfPath = isAtFirstPartOfAlphabet ? "!А-К" : "!Л-Я";

        String endOfPath = "/" + name.toUpperCase().charAt(0) + "/" + name;
        endOfPath = endOfPath.replace('-', ' ');

        String pathIntoDB = "Z:/" + middleOfPath + endOfPath;

        log.info("path is " + pathIntoDB);
        File dir = new File(pathIntoDB);
        if(!dir.exists()) {
            dir.mkdir();
            log.info("The directory was created");
            dir = new File(pathIntoDB + "/согласованно");
            dir.mkdir();
            dir = new File(pathIntoDB + "/3ds");
            dir.mkdir();
            dir = new File(pathIntoDB + "/cdr");
            dir.mkdir();
            dir = new File(pathIntoDB + "/материалы");
            dir.mkdir();
            dir = new File(pathIntoDB + "/pdf");
            dir.mkdir();
        }
        else log.error("Error with the directory making");
        return pathIntoDB;
    }

    public static String cutTheCommand(String text){
        return text.substring(text.indexOf(' ') + 1);
    }

    public static String transformToLongPath(String text){
        return text.replace("Z:/", "//ICEBERG/Public/").replace("/", "\\");
    }
}
