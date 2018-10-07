package com.grappim;

import static java.lang.Math.toIntExact;

import com.grappim.constant.FieldConstants;
import com.grappim.handlers.MongoDBHandler;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
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
import org.bson.Document;
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

/**
 * IDE: IntelliJ IDEA Created by grigo on Oct, 05, 2018 Project: telegrambottest
 */

public class GrigoriyMBot extends TelegramLongPollingBot {

  private static final Logger logger = LoggerFactory.getLogger(GrigoriyMBot.class);

  private Properties prop;
  private long chatId;

  public GrigoriyMBot() {
    loadProperties();
  }

  @Override
  public void onUpdateReceived(Update update) {
    chatId = update.getMessage().getChatId();
    logging(update);
    if (update.hasMessage() && update.getMessage().hasText()) {
      onUpdateReceivedText(update);
    } else if (update.hasCallbackQuery()) {
      onUpdateReceivedCallbackQuery(update);
    } else if (update.hasMessage() && update.getMessage().hasPhoto()) {
      onUpdateReceivedPhoto(update);
    }
  }

  private void onUpdateReceivedCallbackQuery(Update update) {
    String callData = update.getCallbackQuery().getData();
    long messageId = update.getCallbackQuery().getMessage().getMessageId();
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
  }

  private void logging(Update update) {
    String messageText = update.getMessage().getText();
    String[] userInfo = getUserInfo(update);
    DateFormat dateFormat = new SimpleDateFormat("dd/MM//yyyy HH:mm::ss");
    Date date = new Date();
    if (messageText == null) {
      messageText = update.getMessage().getPhoto().get(0).getFileId();
    }
    logger.info(dateFormat.format(date) +
        "\nMessage from " + userInfo[0] + " " + userInfo[1] + " (@" + userInfo[3] + ")(chat_id = "
        + userInfo[2] +
        ") Text - " + messageText);
  }

  private void onUpdateReceivedText(Update update) {
    String messageText = update.getMessage().getText();
    switch (messageText) {
      case FieldConstants.START_COMMAND: {
        SendMessage message = createMessage("You send /start");
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
      case FieldConstants.ABOUT_ME_COMMAND: {
        String text = "I am a  bot who wants to help you with Java.\n" +
            "The source code you can find here: https://github.com/Grigoriym/telegram-bot-test.\n";
        SendMessage message = createMessage(text);
        sendMessage(message);
        break;
      }
      case FieldConstants.TEST_ME_COMMAND: {
        String text = "Let's test you";
        SendMessage message = createMessage(text);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(FieldConstants.PSEUDO_RANDOM);
        row.add(FieldConstants.LIST_OF_QUESTIONS);
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
        sendMessage(message);
        break;
      }
      case "/addq": {
        MongoDBHandler.connect();
//        String question = "Which of the following method signatures is a valid declaration of an entry point in a Java application";
//        String a = "public void main(String[] args)";
//        String b = "public static void main()";
//        String c =
//            "private static void start(String[] mydata)";
//        String d = "public static final void main(String[] mydata)";
//        String explanation = "An entry point in a Java application of a main() method with a single String[] argument, return type of void, and modifiers public and static. The name of the variable in the input argument does not matter. Option A is missing the static modifier, Option B is missing the String[] argument, and Option C has the wrong access nmodifier and method name. Only D option fulfills these requirements. Note that the modifier final is optional and may be added to an entry point method.";
//        MongoDBHandler
//            .addDocumentToCollection(addQuestionToDB(question, a, b, c, d, explanation, "d"),
//                MongoDBHandler.COLLECTION_NAME_QA_TEST);
        MongoDBHandler.disconnect();
        break;
      }
      case FieldConstants.BOOKS_COMMAND: {
        String text = "Java books";
        SendMessage message = createMessage(text);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(FieldConstants.OCA_OCP);
        row.add(FieldConstants.TDD);
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
        sendMessage(message);
        break;
      }
      case FieldConstants.OCA_OCP: {
        String text = "Books for OCA/OCP.\n" +
            "Scott Selikoff - OCA  OCP Java SE 8 Programmer Practice Tests.\n" +
            "https://yadi.sk/i/vF_PpZ0L6qTG5Q\n" +
            "Sierra K., Bates B., Robson E. - OCP Java SE 8 Programmer II Exam Guide (Exam 1Z0-809) - 2018\n"
            + "https://yadi.sk/i/dvhqCjvKZ1yhug";
        sendMessage(createMessage(text));
        break;
      }
      case FieldConstants.TDD: {
        String text = "Boks for TDD.\n" +
            "Shekhar Gulati - Java Unit Testing with JUnit 5 Test Driven Development with JUnit 5.\n"
            +
            "https://yadi.sk/i/CE6T_ZahMeWENg";
        sendMessage(createMessage(text));
        break;
      }
      case "/mongodb": {
        sendMessage(createMessage("/mongo test"));
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
        SendMessage message = createMessage("Here is your keyboard");
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
        sendMessage(createMessage("Row 1 Btn 1"));
        break;
      }
      case "/hide": {
        SendMessage msg = createMessage("Keyboard hidden");
        ReplyKeyboardRemove keyboardMarkup = new ReplyKeyboardRemove();
        msg.setReplyMarkup(keyboardMarkup);
        sendMessage(msg);
        break;
      }
      case "/emoji-test": {
        String textMessage = EmojiParser.parseToUnicode("Some emojis: :smile::relieved:");
        sendMessage(createMessage(textMessage));
        break;
      }
      default: {
        sendMessage(createMessage("Unknown command"));
        break;
      }
    }
  }

  private void onUpdateReceivedPhoto(Update update) {
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

  private SendMessage createMessage(String text) {
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
    MongoDBHandler.connect();
    BasicDBObject searchQuery = new BasicDBObject();
    searchQuery.put("id", arr[2]);
    FindIterable<Document> docs = MongoDBHandler
        .findInCollection(searchQuery, MongoDBHandler.COLLECTION_NAME_USERS);
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
      MongoDBHandler.addDocumentToCollection(doc, MongoDBHandler.COLLECTION_NAME_USERS);
      MongoDBHandler.disconnect();
      logger.info("User not exists in database. Written.");
      return "no_exists";
    } else {
      logger.info("User exists in database.");
      MongoDBHandler.disconnect();
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

  private Document addQuestionToDB(String question, String a, String b, String c,
      String d, String explanation, String correct) {
    return new Document("question", question)
        .append("a", a)
        .append("b", b)
        .append("c", c)
        .append("d", d)
        .append("correct", correct)
        .append("explanation", explanation);
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
