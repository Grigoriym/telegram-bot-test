package com.grappim;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * IDE: IntelliJ IDEA Created by grigo on Oct, 05, 2018 Project: telegrambottest
 */

public class GrigoriyMBot extends TelegramLongPollingBot {

  private Properties prop;
  private String pathToConfig = "src/main/resources/config.properties";

  public GrigoriyMBot() {
    loadProperties();
  }

  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage() && update.getMessage().hasText()) {
      String messageText = update.getMessage().getText();
      long chatId = update.getMessage().getChatId();
      if (messageText.equals("/start")) {
        SendMessage message = createMessage(chatId, messageText);
        sendMessage(message);
      } else if (messageText.equals("/pic")) {
        SendPhoto msg = new SendPhoto()
            .setChatId(chatId)
            .setPhoto("AgADAgAD86kxG2sUwEkIqJt6s1isVVj1tw4ABJ3Z1DUKfZBh9q4AAgI")
            .setCaption("Photo");
        sendPhoto(msg);
      }else{
        SendMessage message = createMessage(chatId, "Unknown command");
        sendMessage(message);
      }
    } else if (update.hasMessage() && update.getMessage().hasPhoto()) {
      long chatId = update.getMessage().getChatId();
      List<PhotoSize> photos = update.getMessage().getPhoto();
      String fId = photos.stream()
          .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
          .findFirst()
          .orElse(null)
          .getFileId();
      int f_width = photos.stream()
          .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
          .findFirst()
          .orElse(null)
          .getWidth();
      int f_height = photos.stream()
          .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
          .findFirst()
          .orElse(null)
          .getHeight();
      String caption = "file_id: " + fId + "\nwidth: "
          + Integer.toString(f_width) + "\nheoght: "
          + Integer.toString(f_height);
      SendPhoto msg = new SendPhoto()
          .setChatId(chatId)
          .setPhoto(fId)
          .setCaption(caption);
      sendPhoto(msg);
    }
  }

  private void loadProperties() {
    prop = new Properties();
    try (InputStream input = new FileInputStream(pathToConfig)) {
      prop.load(input);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private SendMessage createMessage(long chatId, String text) {
    return new SendMessage()
        .setChatId(chatId)
        .setText(text);
  }

  private void sendPhoto(SendPhoto sendPhoto) {
    try {
      execute(sendPhoto);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  private void sendMessage(SendMessage sendMessage) {
    try {
      execute(sendMessage);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getBotUsername() {
    return prop.getProperty("botName");
  }

  @Override
  public String getBotToken() {
    return prop.getProperty("botToken");
  }
}
