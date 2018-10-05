package com.grappim;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * IDE: IntelliJ IDEA Created by grigo on Oct, 05, 2018 Project: telegrambottest
 */

public class GrigoriyMBot extends TelegramLongPollingBot {

  private Properties prop;
  private String pathToConfig = "src/main/resources/config.properties";

  public GrigoriyMBot() {
    prop = new Properties();
    try (InputStream input = new FileInputStream(pathToConfig)) {
      prop.load(input);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onUpdateReceived(Update update) {
    String command = update.getMessage().getText();
    SendMessage message = new SendMessage();
    if (command.equals("/myname")) {
      System.out.println("myname");
      message.setText(update.getMessage().getFrom().getFirstName());
    } else if (command.equals("/mylastname")) {
      System.out.println("lastname");
      message.setText(update.getMessage().getFrom().getLastName());
    }
    message.setChatId(update.getMessage().getChatId());
    try {
      execute(message);
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
