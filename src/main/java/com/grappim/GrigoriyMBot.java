package com.grappim;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.vdurmont.emoji.EmojiParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.bson.Document;

import static java.lang.Math.toIntExact;
/**
 * IDE: IntelliJ IDEA Created by grigo on Oct, 05, 2018 Project: telegrambottest
 */

public class GrigoriyMBot extends TelegramLongPollingBot {

  private static final Logger logger = LoggerFactory.getLogger(GrigoriyMBot.class);

  private Properties prop;

  public GrigoriyMBot() {
    loadProperties();
  }

  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage() && update.getMessage().hasText()) {
      logging(update);
      onUpdateReceivedText(update);
    } else if (update.hasCallbackQuery()) {
      String callData = update.getCallbackQuery().getData();
      long messageId = update.getCallbackQuery().getMessage().getMessageId();
      long chatId = update.getCallbackQuery().getMessage().getChatId();
      if (callData.equals("update_msg_text")) {
        String answer = "Updated message text";
        EditMessageText newMessage = new EditMessageText()
            .setChatId(chatId)
            .setMessageId(toIntExact(messageId))
            .setText(answer);
        try {
          execute(newMessage);
        } catch (TelegramApiException e) {
          e.printStackTrace();
        }
      }
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
        SendMessage message = createMessage(chatId, "You send /start");
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("Update message text")
            .setCallbackData("update_msg_text"));
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        sendMessage(message);
        break;
      }
      case "/mongodb":{
        SendMessage msg = createMessage(chatId, "/mongo test");
        sendMessage(msg);
        check(getUserInfo(update));
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
      case "/emoji-test":{
        String textMessage = EmojiParser.parseToUnicode("Some emojis: :smile::relieved:");
        SendMessage msg = createMessage(chatId, textMessage);
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
    InputStream inputStream = getClass().getResourceAsStream("/config.properties");
    prop = new Properties();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      prop.load(reader);
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

  private String check(String[] arr) {
    MongoClientURI connectionString = new MongoClientURI("mongodb://127.0.0.1:27017");
    MongoClient mongoClient = new MongoClient(connectionString);
    MongoDatabase database = mongoClient.getDatabase("telegram-bot-test");
    MongoCollection<Document> collection = database.getCollection("users");
    BasicDBObject searchQuery = new BasicDBObject();
    searchQuery.put("id", arr[2]);
    FindIterable<Document> docs = collection.find(searchQuery);
    int count = 0;
    for (Document doc : docs) {
      if (doc.containsValue(arr[2])) {
        count++;
      }
    }
    if (count == 0) {
      Document doc = new Document("first_name", arr[0])
          .append("last_name", arr[1])
          .append("id", arr[2])
          .append("username", arr[3]);
      collection.insertOne(doc);
      mongoClient.close();
      System.out.println("User not exists in database. Written.");
      return "no_exists";
    } else {
      System.out.println("User exists in database.");
      mongoClient.close();
      return "exists";
    }
  }

  private String[] getUserInfo(Update update) {
    String userFirstName = update.getMessage().getChat().getFirstName();
    String userLastName = update.getMessage().getChat().getLastName();
    String username = update.getMessage().getChat().getUserName();
    String userId = String.valueOf(update.getMessage().getChat().getId());
    return new String[]{userFirstName, userLastName, userId, username};
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
