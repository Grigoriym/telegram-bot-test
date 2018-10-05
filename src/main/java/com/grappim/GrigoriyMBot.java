package com.grappim;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * IDE: IntelliJ IDEA Created by grigo on Oct, 05, 2018 Project: telegrambottest
 */

public class GrigoriyMBot extends TelegramLongPollingBot {

  private static final Logger logger = LoggerFactory.getLogger(GrigoriyMBot.class);

  private Properties prop;
  private String pathToConfig = "src/main/resources/config.properties";

  public GrigoriyMBot() {
    loadProperties();
  }

  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage() && update.getMessage().hasText()) {
      logging(update);
      onUpdateReceivedText(update);
    } else if (update.hasMessage() && update.getMessage().hasPhoto()) {
      logging(update);
      onUpdateReceivedPhoto(update);
    }
  }

  private void logging(Update update) {
    String userFirstName = update.getMessage().getChat().getFirstName();
    String userLastName = update.getMessage().getChat().getLastName();
    String username = update.getMessage().getChat().getUserName();
    String messageText = update.getMessage().getText();
    long chatId = update.getMessage().getChatId();

    DateFormat dateFormat = new SimpleDateFormat("dd/MM//yyyy HH:mm::ss");
    Date date = new Date();

    if (messageText == null) {
      messageText = update.getMessage().getPhoto().get(0).getFileId();
    }

    logger.info(dateFormat.format(date) +
        "\nMessage from " + userFirstName + " " + userLastName + " (@" + username + ")(chat_id = "
        + chatId +
        ") Text - " + messageText);
  }

  private void onUpdateReceivedText(Update update) {
    String messageText = update.getMessage().getText();
    long chatId = update.getMessage().getChatId();
    switch (messageText) {
      case "/start": {
        SendMessage message = createMessage(chatId, messageText);
        sendMessage(message);
        break;
      }
      case "/pic": {
        SendPhoto msg = new SendPhoto()
            .setChatId(chatId)
            .setPhoto("AgADAgAD86kxG2sUwEkIqJt6s1isVVj1tw4ABJ3Z1DUKfZBh9q4AAgI")
            .setCaption("Photo");
        sendPhoto(msg);
        break;
      }
      case "/markup": {
        SendMessage message = createMessage(chatId, "Here is your keyboard");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Row 1 Btn 1");
        row.add("Row 1 Btn 2");
        row.add("Row 1 Btn 3");
        keyboard.add(row);
        row = new KeyboardRow();
        row.add("Row 2 Btn 1");
        row.add("Row 2 Btn 2");
        row.add("Row 2 Btn 3");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
        sendMessage(message);
        break;
      }
      case "Row 1 Btn 1": {
        SendPhoto msg = new SendPhoto()
            .setChatId(chatId)
            .setPhoto("AgADAgAD86kxG2sUwEkIqJt6s1isVVj1tw4ABJ3Z1DUKfZBh9q4AAgI")
            .setCaption("Photo");
        sendPhoto(msg);
        break;
      }
      case "/hide": {
        SendMessage msg = createMessage(chatId, "Keyboard hidden");
        ReplyKeyboardRemove keyboardMarkup = new ReplyKeyboardRemove();
        msg.setReplyMarkup(keyboardMarkup);
        sendMessage(msg);
        break;
      }
      default: {
        SendMessage message = createMessage(chatId, "Unknown command");
        sendMessage(message);
        break;
      }
    }
  }

  private void onUpdateReceivedPhoto(Update update) {
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
