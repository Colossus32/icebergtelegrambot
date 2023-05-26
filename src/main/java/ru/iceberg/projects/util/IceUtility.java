package ru.iceberg.projects.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

@Slf4j
public class IceUtility {

    public static String nameChecker(String text){

        text = text.substring(text.indexOf(' ')).trim();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!Character.isAlphabetic(c) && !Character.isDigit(c) && c != ' ' && c != '-') return "";
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
        String middleOfPath = "";
        String endOfPath = "";
        char firstCharacter = name.toLowerCase().charAt(0);
        if (Character.isDigit(firstCharacter)) {
            middleOfPath = "!0-9 (Цифры)";
            log.info("создан проект в директории цифр");
        }
        else if (firstCharacter >= 'a' && firstCharacter <= 'z') {
            middleOfPath = "!A-Z (ENG)";
            log.info("создан проект в английской директории");
        }
        else {
            boolean isAtFirstPartOfAlphabet = (name.charAt(0) - 'а') < 11;
            middleOfPath = isAtFirstPartOfAlphabet ? "!А-К" : "!Л-Я";
            endOfPath = "/" + name.toUpperCase().charAt(0);
            log.info("создан проект в русской директории");

        }
        endOfPath += "/" + name;

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
            dir = new File(pathIntoDB + "/замеры");
            dir.mkdir();
            dir = new File(pathIntoDB + "/jpg");
            dir.mkdir();
            dir = new File(pathIntoDB + "/print");
            dir.mkdir();
        }
        else {
            log.error("Error with the directory making");
            return "";
        }
        return pathIntoDB;
    }

    public static String cutTheCommand(String text){
        return text.substring(text.indexOf(' ') + 1);
    }

    public static String transformToLongPath(String text){
        return text.replace("Z:/", "//ICEBERG/Public/").replace("/", "\\");
    }

    public static String deleteSpaces(String text){
        text = text.trim();
        char[] arr = text.toCharArray();
        char prev = arr[0];
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < arr.length; i++) {
            char cur = arr[i];
            if (cur == ' ' && prev == ' ') continue;
            else {
                builder.append(cur);
                prev = cur;
            }
        }
        return builder.toString();
    }

    public static String addParticipants(long id) {
        if (id != 829205726 && id != 415536606) return 415536606 + " " + 829205726 + " " + id + " ";
        else return 415536606 + " " + 829205726 + " ";
    }

    public static String transformToZ(String path) {
        path = path.replace('\\', '/');
        return path.replace("//iceberg/public", "Z:");
    }
}
