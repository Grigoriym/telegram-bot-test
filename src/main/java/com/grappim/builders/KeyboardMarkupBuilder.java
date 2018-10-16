package com.grappim.builders;

import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

/**
 * IDE: IntelliJ IDEA Created by grigo on Oct, 15, 2018 Project: telegram-bot-test
 */

public class KeyboardMarkupBuilder {

  private Long chatId;
  private String text;

  private List<KeyboardRow> keyboard = new ArrayList<>();
  private KeyboardRow row = null;

  public KeyboardMarkupBuilder() {
  }

  public static KeyboardMarkupBuilder create() {
    return new KeyboardMarkupBuilder();
  }

  public static KeyboardMarkupBuilder create(Long chatId) {
    KeyboardMarkupBuilder builder = new KeyboardMarkupBuilder();
    builder.setChatId(chatId);
    return builder;
  }

  public KeyboardMarkupBuilder setChatId(Long chatId) {
    this.chatId = chatId;
    return this;
  }

  public KeyboardMarkupBuilder setText(String text) {
    this.text = text;
    return this;
  }

  public KeyboardMarkupBuilder row() {
    this.row = new KeyboardRow();
    return this;
  }

  public KeyboardMarkupBuilder button(String text) {
    row.add(text);
    return this;
  }

  public KeyboardMarkupBuilder endRow() {
    this.keyboard.add(this.row);
    this.row = null;
    return this;
  }

  public SendMessage build(boolean isOneTimeKeyboard, boolean isResizeKeyboard) {
    SendMessage message = new SendMessage();
    message.setChatId(chatId);
    message.setText(text);
    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    replyKeyboardMarkup.setKeyboard(keyboard);
    replyKeyboardMarkup.setResizeKeyboard(isResizeKeyboard);
    replyKeyboardMarkup.setOneTimeKeyboard(isOneTimeKeyboard);
    message.setReplyMarkup(replyKeyboardMarkup);
    return message;
  }

  public SendMessage build() {
    SendMessage message = new SendMessage();
    message.setChatId(chatId);
    message.setText(text);
    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    replyKeyboardMarkup.setKeyboard(keyboard);
    replyKeyboardMarkup.setResizeKeyboard(true);
    replyKeyboardMarkup.setOneTimeKeyboard(true);
    message.setReplyMarkup(replyKeyboardMarkup);
    return message;
  }
}
