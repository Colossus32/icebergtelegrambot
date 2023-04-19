package ru.iceberg.projects.telegrambot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
@Slf4j
public class ProjectBot {
    TelegramBot bot;
    private int port;

    public ProjectBot() {
        try{
            BufferedReader reader = new BufferedReader(new FileReader("${bot.id}"));
            String token = reader.readLine();
            reader.close();

            this.bot = new TelegramBot(token);
            this.port = 8080;

        } catch (IOException e) {
            log.error("bot initialisation error");
            e.printStackTrace();
        }
    }

    public void listen(){

        bot.setUpdatesListener(element -> {
            element.forEach(pock -> {

                if (pock.message() != null && pock.message().text() != null){
                    String text = pock.message().text().toLowerCase();
                    Long chatId = pock.message().chat().id();

                    switch (text){
                        case "/start" :
                            //saveUserToDBonStart(chatId);
                            break;
                    }
                }

            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    //нужно будет сконфигурировать юзера и передать его в теле... по идее id = chatID, спросить разве что имя
    private void saveUserToDBonStart(Long chatId, String name) {

        bot.execute(new SendMessage(chatId, "Добро пожаловать в Телеграмбот \"Айсберг\". Как вас называть?\n"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://localhost:%d/api/v1/users/id=%d&name=%s", port, chatId, name)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
            log.error("error on a save user to database");
            e.printStackTrace();
        }


        //добавить метод отрисовки помощи
    }
}
