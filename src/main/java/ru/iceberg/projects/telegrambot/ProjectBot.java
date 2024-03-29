package ru.iceberg.projects.telegrambot;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.iceberg.projects.entity.Project;
import ru.iceberg.projects.entity.User;
import ru.iceberg.projects.util.IceUtility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

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
                        if (text.startsWith("/existed")) {
                            saveExistedProject(chatId, text);
                        }
                        if (text.startsWith("/wakeup")) {
                            wakeUpProject(chatId, text);
                        }
                        if (text.startsWith("/addnew")) {
                            addSubProject(chatId, text);
                        }
                    }

                }

            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    //to do new realisation
    private void addSubProject(Long chatId, String text) {
        text = IceUtility.cutTheCommand(text);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://localhost:%s/api/v1/projects/sub", port)))
                .POST(HttpRequest.BodyPublishers.ofString(text))
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            bot.execute(new SendMessage(chatId, response.body()));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void wakeUpProject(Long chatId, String text) {

        String[] strings = text.split(" ");
        Long projectId = Long.parseLong(strings[1]);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://localhost:%s/api/v1/projects/wake/%d", port, projectId)))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            bot.execute(new SendMessage(chatId, response.body()));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void saveExistedProject(Long chatId, String text) {
        text = IceUtility.cutTheCommand(text);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/projects/long"))
                .POST(HttpRequest.BodyPublishers.ofString( chatId + " " + text))
                .build();

        try{
            HttpResponse<String> response = HttpClient.newHttpClient().send(request,HttpResponse.BodyHandlers.ofString());
            bot.execute(new SendMessage(chatId, response.body()));
        } catch (InterruptedException | IOException e){
            e.printStackTrace();
        }
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
    public void sendDailyMail(){
        //запрашиваем все активные проекты и всех пользователей, чтобы организовать рассылку
        HttpRequest projectRequest = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://localhost:%s/api/v1/projects/report", port)))
                .GET()
                .build();
        HttpRequest usersRequest = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://localhost:%s/api/v1/users/json", port)))
                .GET()
                .build();
        try {
            HttpResponse<String> projectResponse = HttpClient.newHttpClient().send(projectRequest, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> userResponse = HttpClient.newHttpClient().send(usersRequest, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JavaType projectType = mapper.getTypeFactory().constructCollectionType(Set.class,Project.class);
            Set<Project> projectSet = mapper.readValue(projectResponse.body(), projectType);
            JavaType userType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, User.class);
            List<User> userList = mapper.readValue(userResponse.body(), userType);

            //мапим айдишник пользователя с проектами, в которых он является участником
            Map<Long, Set<Project>> map = new HashMap<>();
            for (Project p : projectSet) {
                String[] participants = p.getParticipants().split(" ");
                for (String s : participants) {
                    long currentId = Long.parseLong(s);
                    if (!map.containsKey(currentId)) map.put(currentId, new HashSet<>());
                    Set<Project> existedProjectSet = map.get(currentId);
                    existedProjectSet.add(p);
                    map.put(currentId, existedProjectSet);
                }
            }

            //убираем директора из рассылки персональных проектов
            userList.remove(generalId);

            //делаем рассылку проектов каждому персонально
            StringBuilder bigBuilder = new StringBuilder();
            for (User u : userList) {
                long uId = u.getId();
                StringBuilder sb = new StringBuilder();
                if (!map.containsKey(uId)) {
                    String mes = String.format("%s, нет активных проектов", u.getName());
                    log.info("Дневной отчет для {} отправлен, но проектов нет", u.getName());
                    bot.execute(new SendMessage(uId, mes ));
                    bigBuilder.append(mes).append('\n');
                }
                else {
                    sb.append(String.format("%s, активные проекты:\n", u.getName()));
                    for (Project p : map.get(uId)) sb.append(String.format("- %s\n", p.getName()));
                    bot.execute(new SendMessage(uId, sb.toString()));
                    log.info("Дневной отчет для {} отправлен", u.getName());
                    bigBuilder.append(sb).append('\n');
                }
            }
            //высылаем директору общий отчет по всем проектам работников
            bot.execute(new SendMessage(generalId,String.format("Сан Саныч, вот активные проекты по работникам:\n%s", bigBuilder.toString())));

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
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
        text = IceUtility.deleteSpaces(text);
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

                "/new название/проект - сам создает новый проект в базе.\n" +
                "/existed название путь - сам создает новый проект в базе. Допустимы пока буквы русского, латинского алфавита, пробел и дефис -\n" +
                "/addnew название проекта/название подпроекта - создает подпроект в существующей директории\n" +
                "/addtag цифра тэг - добавляет к проекту под этим id этот тэг\n" +
                "/addworker - инструкция по добавлению участника к проекту\n" +
                "РЕДАКТИРОВАНИЕ:\n" +
                "/name имя - изменяет ваше имя пользователя\n" +
                "/finish цифра - заканчивает проект, он больше не активен\n" +
                "/projectname цифра название - меняет имя указанного проекта\n" +
                "/wakeup цифра - возвращает проект в активное состояние\n" +
                //"/delete цифра - удаляет проект с этим id\n" +

                "ПОИСК:\n" +
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
            String[] messages = response.body().split("#");
            for(String mes : messages) bot.execute(new SendMessage(chatId, mes));
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
            String[] messages = response.body().split("#");
            for(String mess : messages) bot.execute(new SendMessage(chatId, mess));

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
        String error = "Не удалось создать проект...\nПричины могут быть:\n-такое название проекта уже используется\n-создатель проекта отсутствует в базе данных";
        log.info("создание нового проекта {}........", text);

        text = IceUtility.nameChecker(text);

        if (!text.equals(error)){
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("http://localhost:%s/api/v1/projects?author=%d", port, chatId)))
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(text))
                    .build();
            try {
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                bot.execute(new SendMessage(chatId, response.body()));
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        } else bot.execute(new SendMessage(chatId, error));

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
