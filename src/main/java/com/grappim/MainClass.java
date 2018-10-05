package com.grappim;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.logging.BotLogger;

/**
 * IDE: IntelliJ IDEA Created by grigo on Oct, 05, 2018 Project: telegrambottest
 */

public class MainClass {

  public static void main(String[] args) {
    ApiContextInitializer.init();
    TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
    try {
      telegramBotsApi.registerBot(new GrigoriyMBot());
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

}
