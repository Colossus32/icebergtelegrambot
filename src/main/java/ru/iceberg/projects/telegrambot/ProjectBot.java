package ru.iceberg.projects.telegrambot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.iceberg.projects.util.IceUtility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
@Slf4j
@PropertySource("classpath:/application.properties")
public class ProjectBot {

    TelegramBot bot;

    private String val;

    private String port;

    private long generalId;

    public ProjectBot() {

        try {
            BufferedReader reader = new BufferedReader(new FileReader("bottoken.txt"));
            String token = reader.readLine();
            String sPort = reader.readLine();
            long gId = Long.parseLong(reader.readLine());
            String gVal = reader.readLine();
            reader.close();

            this.bot = new TelegramBot(token);
            this.port = sPort;
            this.generalId = gId;
            this.val = gVal;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void listen(){

        bot.setUpdatesListener(element -> {
            element.forEach(pock -> {

                if (pock != null && pock.message() != null && pock.message().text() != null){
                    if (!val.contains(String.valueOf(pock.message().chat().id()))) {
                        bot.execute(new SendMessage(pock.message().chat().id(), "you're not validated user."));
                        //return;
                    }
                    else {
                        String text = pock.message().text().toLowerCase();
                        Long chatId = pock.message().chat().id();
                        log.info("got not null message: " + text + " from " + chatId);

                        if (text.equals("/start")){
                            saveUserToDBonStart(chatId);
                        }
                        if (text.startsWith("/new ")){
                            createNewProjectShort(chatId, text);
                        }
                        if (text.startsWith("/name ")) {
                            changeUserName(chatId,text);
                        }
                        if (text.startsWith("/help")) drawMenu(chatId);

                        if (text.startsWith("/active")){
                            getAllActiveProjects(chatId);
                        }
                        if (text.startsWith("/delete ")) {
                            deleteProjectById(chatId, text);
                        }
                        if (text.startsWith("/finish")) {
                            finishProjectById(chatId, text);
                        }
                        if (text.startsWith("/all")) {
                            showAllProjects(chatId);
                        }
                        if (text.startsWith("/projectname ")) {
                            changeProjectName(chatId, text);
                        }
                        if (text.startsWith("/addtag")){
                            addTagToTheProject(chatId, text);
                        }
                        if (text.startsWith("/addworker")){
                            addWorkerInstruction(chatId);
                        }
                        if (text.startsWith("/worker")){
                            addWorkerToProject(chatId, text);
                        }
                        if (text.startsWith("/findtag")){
                            findProjectsByTag(chatId, text);
                        }
                        if (text.startsWith("/my")) {
                            findMyActiveProjects(chatId);
                        }
                    }

                }

            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void findMyActiveProjects(Long chatId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/projects/active/" + chatId))
                .GET()
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            bot.execute(new SendMessage(chatId, response.body()));

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "${report.dailyprojects}")
    //@Scheduled(cron = "${report.test}")
    public void sendDailyMail(){

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://localhost:%s/api/v1/projects/report", port)))
                .GET()
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request,HttpResponse.BodyHandlers.ofString());
            String bigData = response.body();
            if (!bigData.equals("")) {
                String[] allUsers = bigData.split(",");
                generalReport(allUsers);
                for (String s : allUsers) {
                    if (!s.startsWith("" + generalId)) sendToEachWorkerReport(s);
                }
            } else {
                log.warn("нет активных проектов");
                request = HttpRequest.newBuilder()
                        .uri(URI.create(String.format("http://localhost:%s/api/v1/users/allids", port)))
                        .GET()
                        .build();
                response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                String[] sIds = response.body().split(" ");
                for (String stringId : sIds){
                    bot.execute(new SendMessage(Long.parseLong(stringId), "Нет активных проектов."));
                }
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToEachWorkerReport(String s) {
        String[] info = s.split("\\.");
        long id = Long.parseLong(info[0]);
        String name = info[1];
        String projects = info[2]
                //.substring(1)
                .replace("_", "\n");
        String message = String.format("%s, у тебя сейчас такие проекты:\n%s", name, projects);
        bot.execute(new SendMessage(id, message));
    }

    private void generalReport(String[] allUsers) {

        StringBuilder builder = new StringBuilder();

        for (String s : allUsers) {
            s = s.substring(s.indexOf('.') + 1).replace(".", " : ").replace("_", ", ");
            builder.append(s).append('\n');
        }
        bot.execute(new SendMessage(generalId, builder.toString()));
    }

    private void findProjectsByTag(Long chatId, String text) {
        text = text.substring(text.indexOf(' ') + 1);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://localhost:%s/api/v1/projects/tags?tag=%s", port, text)))
                .GET()
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            String answer = response.body();

            if (answer.equals("")) bot.execute(new SendMessage(chatId, String.format("Проектов с тэгом %s не найдено.", text)));
            else bot.execute(new SendMessage(chatId, answer));

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void addWorkerToProject(Long chatId, String text) {
        String[] strings = text.split(" ");
        if (IceUtility.onlyDigitChecker(strings[1]) && IceUtility.onlyDigitChecker(strings[2])) {
            int projectId = Integer.parseInt(strings[1]);
            int workerId = Integer.parseInt(strings[2]);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("http://localhost:%s/api/v1/projects/workers?projectid=%d&workerid=%d", port, projectId,workerId)))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            try{
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                bot.execute(new SendMessage(chatId, response.body()));
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        } else botSayInputError(chatId);
    }

    private void addWorkerInstruction(Long chatId) {
        //запрос на активные проекты
        getAllActiveProjects(chatId);

        //запрос на всех работников
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://localhost:%s/api/v1/users/all", port)))
                .GET()
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            bot.execute(new SendMessage(chatId, response.body()));
            bot.execute(new SendMessage(chatId,"Введите /worker id проекта id работника \nпример: /worker 35 1"));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getStartMenu() {
        return "Есть такие команды:\n" +
                "СОЗДАЕМ ПРОЕКТ:\n" +
                "/new название - сам создает новый проект в базе\n" +
                "/addtag цифра тэг - добавляет к проекту под этим id этот тэг\n" +
                "/addworker - инструкция по добавлению участника к проекту\n" +
                "РЕДАКТИРОВАНИЕ:\n" +
                "/name имя - изменяет ваше имя пользователя\n" +
                "/finish цифра - заканчивает проект, он больше не активен\n" +
                "/projectname цифра название - меняет имя указанного проекта\n" +
                //"/delete цифра - удаляет проект с этим id\n" +

                "Поиск:\n" +
                "/my - находит ваши активные проекты\n" +
                "/active - выводит список активных проектов\n" +
                "/all - выводит все проекты\n" +
                "/findtag название - находит все проекты с таким тэгом\n" +
                "/help - выводит список доступных команд";
    }

    private void addTagToTheProject(Long chatId, String text) {
        String[] strings = text.split(" ");
        if (IceUtility.onlyDigitChecker(strings[1])){
            int id = Integer.parseInt(strings[1]);
            String name = text.substring(strings[0].length() + strings[1].length() + 1);
            name = IceUtility.nameChecker(name).replace(' ','-');
            if (name.equals(" ")) {
                botSayInputError(chatId);
                return;
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("http://localhost:%s/api/v1/projects/tags?id=%d&tag=%s", port, id, name)))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            try{
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                bot.execute(new SendMessage(chatId, response.body()));
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void changeProjectName(Long chatId, String text) {
        String[] strings = text.split(" ");
        if (IceUtility.onlyDigitChecker(strings[1])){
            int id = Integer.parseInt(strings[1]);
            String name = text.substring(strings[0].length() + strings[1].length() + 1);
            log.info("изменение имени проекта на " + name);
            name = IceUtility.nameChecker(name);
            if (name.equals("")) {
                botSayInputError(chatId);
                return;
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("http://localhost:%s/api/v1/projects/%d?name=%s", port, id, name)))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            try{
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                bot.execute(new SendMessage(chatId, response.body()));
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }

        } else botSayInputError(chatId);
    }

    private void showAllProjects(Long chatId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://localhost:%s/api/v1/projects/all", port)))
                .GET()
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            bot.execute(new SendMessage(chatId, response.body()));
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void finishProjectById(Long chatId, String text) {
        text = IceUtility.cutTheCommand(text);
        if (IceUtility.onlyDigitChecker(text)){
            int id = Integer.parseInt(text);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("http://localhost:%s/api/v1/projects/%d", port, id)))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();
            try{
                HttpResponse<String> response = HttpClient.newHttpClient().send(request,HttpResponse.BodyHandlers.ofString());
                bot.execute(new SendMessage(chatId, response.body()));
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }

        } else botSayInputError(chatId);
    }

    private void deleteProjectById(Long chatId, String text) {
        text = IceUtility.cutTheCommand(text);
        if (IceUtility.onlyDigitChecker(text)) {
            int id = Integer.parseInt(text);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("http://localhost:%s/api/v1/projects/%d", port,id)))
                    .DELETE()
                    .build();
            try {
                HttpResponse<String> response = HttpClient.newHttpClient().send(request,HttpResponse.BodyHandlers.ofString());
                bot.execute(new SendMessage(chatId, response.body()));
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        } else botSayInputError(chatId);
    }

    private void botSayInputError(Long chatId) {
        bot.execute(new SendMessage(chatId,"Ошибка ввода." ));
    }

    private void getAllActiveProjects(Long chatId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://localhost:%s/api/v1/projects/active", port)))
                .GET()
                .build();
        try{
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            bot.execute(new SendMessage(chatId, response.body()));
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void drawMenu(Long chatId) {
        bot.execute(new SendMessage(chatId, getStartMenu()));
    }

    private void changeUserName(Long chatId, String text) {
        text = IceUtility.cutTheCommand(text).trim();
        text = text.replace(' ', '-');
        if (!text.equals("")){
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("http://localhost:%s/api/v1/users?id=%d&name=%s", port, chatId,text)))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();
            try{
                HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
        bot.execute(new SendMessage(chatId, "Ваше имя теперь " + text));
    }

    private void createNewProjectShort(Long chatId, String text) {
        log.info("get into bot part........");
        text = IceUtility.nameChecker(text);
        if (!text.equals("")){
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("http://localhost:%s/api/v1/projects?author=%d&name=%s", port, chatId,text)))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            try {
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                bot.execute(new SendMessage(chatId, response.body()));
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }

    }



    //нужно будет сконфигурировать юзера и передать его в теле... по идее id = chatID, спросить разве что имя
    private void saveUserToDBonStart(Long chatId) {

        bot.execute(new SendMessage(chatId, "Добро пожаловать в Телеграмбот \"Айсберг\".\n" + getStartMenu()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://localhost:%s/api/v1/users/guests?id=%d", port, chatId)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
            log.error("error on a save user to database");
            e.printStackTrace();
        }
    }
}
