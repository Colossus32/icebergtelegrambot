package ru.iceberg.projects.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Date;

@Slf4j
public class IceUtility {

    public static String nameChecker(String text){

        text = text.substring(text.indexOf(' ')).trim();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!Character.isAlphabetic(c) && !Character.isDigit(c) && c != ' ' && c != '-' && c != '/') return "";
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
        String[] nameParts = name.split("/");
        String project = nameParts[0], subProject = nameParts[1];

        char firstCharacter = name.toLowerCase().charAt(0);

        if (Character.isDigit(firstCharacter)) {
            middleOfPath = "!0-9 (Цифры)";
            log.info("директория цифр");
        }
        else if (firstCharacter >= 'a' && firstCharacter <= 'z') {
            middleOfPath = "!A-Z (ENG)";
            log.info("английская директория");
        }
        else {
            boolean isAtFirstPartOfAlphabet = (name.charAt(0) - 'а') < 11;
            middleOfPath = isAtFirstPartOfAlphabet ? "!А-К" : "!Л-Я";
            endOfPath = "/" + name.toUpperCase().charAt(0);
            log.info("русская директория");

        }
        int year = (new Date().getYear() + 1900);

        endOfPath += "/" + project + "/" + year + "/" + subProject;

        endOfPath = endOfPath.replace('-', ' ');

        String pathIntoDB = "Z:/" + middleOfPath + endOfPath;

        log.info("path is " + pathIntoDB);

        File dir = new File(pathIntoDB);
        if(!dir.exists()) {
            if (dir.mkdirs()) log.info("The directory was created");
            else log.warn("The mistake with creating path.");
            dir = new File(pathIntoDB + "/согласованно-" + name);
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
            log.error("Error with the directory making - root");
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

    public static String createNewPathFromOld(String oldPath, String tail) {
        //path/project/
        if (oldPath.contains("/20")) oldPath = oldPath.substring(0, oldPath.indexOf("20"));
        return String.format("%s%d/%s",oldPath,1900 + new Date().getYear(), tail);
    }
    public static void createDirectoryTree(String pathIntoDB) {
        File dir = new File(pathIntoDB);
        if(!dir.exists()) {
            if (dir.mkdirs()) log.info("The directory was created");
            else log.warn("The mistake with creating path.");
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
        } else log.error("Ошибка создания дерева папок проекта");
    }
}
