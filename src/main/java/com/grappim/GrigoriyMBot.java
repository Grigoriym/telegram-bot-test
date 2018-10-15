package com.grappim;

import com.grappim.builders.InlineKeyboardBuilder;
import com.grappim.constant.FieldConstants;
import com.grappim.handlers.MongoDBHandler;
import com.grappim.menu.MenuManager;
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
import java.util.Arrays;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * IDE: IntelliJ IDEA Created by grigo on Oct, 05, 2018 Project: telegrambottest
 */

public class GrigoriyMBot extends TelegramLongPollingBot {

  private static final Logger logger = LoggerFactory.getLogger(GrigoriyMBot.class);

  private Properties prop;
  private long chatId;

  private String correctAnswer;
  private String explanation;

  private int currentPageInList = 1;
  private int numberOfPagesInList = 0;

  private MenuManager ocaMenuManager = new MenuManager();

  public GrigoriyMBot() {
    MongoDBHandler.connect();
    loadProperties();
  }

  @Override
  public void onUpdateReceived(Update update) {
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
    if (callData.startsWith(FieldConstants.OCA_RANDOM_QUESTION_ANSWER_PREFIX)) {
      String[] tokens = callData.split(" ");
      System.out.println(Arrays.toString(tokens));
      if (tokens[1].equals(correctAnswer)) {
        sendMessage(createMessage(createEmoji(":thumbsup: Correct")));
        correctAnswer = "";
      } else {
        sendMessage(createMessage(createEmoji(":poop:") + " Incorrect\n" + explanation));
        explanation = "";
      }
      ReplyKeyboardMarkup keyboardMarkup = createReplyKeyboardMarkup(
          new String[]{createEmoji(FieldConstants.NEXT_RANDOM_QUESTION),
              createEmoji(FieldConstants.MAIN_PAGE)});
      sendMessage(
          createMessage(createEmoji(":question: Choose:")).setReplyMarkup(keyboardMarkup));
    } else if (callData.startsWith(FieldConstants.OCA_NOT_RANDOM_QUESTION_PREFIX)) {
      String[] tokens = callData.split(" ");
      sendQuestionById(tokens[1]);
      createAnswers();
    } else if (callData.equals(MenuManager.CANCEL_ACTION)) {
      replaceMessageWithText(chatId, messageId, "Cancelled");
    } else if (callData.startsWith(MenuManager.PREV_ACTION) ||
        callData.startsWith(MenuManager.NEXT_ACTION)) {
      String pageNum;
      if (callData.startsWith(MenuManager.PREV_ACTION)) {
        pageNum = callData.replace(MenuManager.PREV_ACTION + ":", "");
      } else {
        pageNum = callData.replace(MenuManager.NEXT_ACTION + ":", "");
      }
      InlineKeyboardBuilder builder = ocaMenuManager
          .createMenuForPage(Integer.parseInt(pageNum), true);
      builder.setChatId(chatId).setText("Choose action:");
      SendMessage message = builder.build();
      replaceMessage(chatId, messageId, message);
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
    logging(update);
    chatId = update.getMessage().getChatId();
    String messageText = EmojiParser.parseToAliases(update.getMessage().getText());
    switch (messageText) {
      case FieldConstants.START_COMMAND: {
        homePage();
        break;
      }
      case FieldConstants.ABOUT_ME_COMMAND: {
        String text = "I am a  bot who wants to help you with Java.\n" +
            "The source code you can find here: https://github.com/Grigoriym/telegram-bot-test.\n";
        sendMessage(createMessage(text));
        break;
      }
      case FieldConstants.OCA_TEST: {
        testMeCommand();
        break;
      }
      case FieldConstants.PSEUDO_RANDOM_QUESTION: {
        sendPseudoRandomQuestion();
        break;
      }
      case FieldConstants.MAIN_PAGE: {
        homePage();
        break;
      }
      case FieldConstants.NEXT_RANDOM_QUESTION: {
        sendPseudoRandomQuestion();
        break;
      }
      case "/addq": {
        break;
      }
      case FieldConstants.ARTICLES: {

        break;
      }
      case FieldConstants.LIST_OF_OCA_QUESTIONS: {
        FindIterable<Document> docs = MongoDBHandler
            .findAllDocumentsinCollection(MongoDBHandler.COLLECTION_NAME_OCA_QA_TEST);
        ocaMenuManager.setColumnsCount(1);
        for (Document doc : docs) {
          ocaMenuManager.addMenuItem(
              (String) doc.get("question"),
              FieldConstants.OCA_NOT_RANDOM_QUESTION_PREFIX + " " + doc.get("_id").toString());
        }
        ocaMenuManager.init();
        InlineKeyboardBuilder builder = ocaMenuManager.createMenuForPage(0, true);
        builder.setChatId(chatId).setText("Choose question: ");
        sendMessage(builder.build());
        break;
      }
      case FieldConstants.TESTS: {
        testsCommand();
        break;
      }
      case FieldConstants.BOOKS: {
        String text = "Java books";
        SendMessage message = createMessage(text);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(FieldConstants.OCA_OCP_BOOKS);
        row.add(FieldConstants.TDD);
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
        sendMessage(message);
        break;
      }

      case FieldConstants.WEBSITES: {

        break;
      }
      case FieldConstants.OCA_OCP_BOOKS: {
        String text = "Books for OCA/OCP.\n" +
            "Scott Selikoff - OCA  OCP Java SE 8 Programmer Practice Tests.\n" +
            "Sierra K., Bates B., Robson E. - OCP Java SE 8 Programmer II Exam Guide (Exam 1Z0-809) - 2018\n";
        sendMessage(createMessage(text));
        break;
      }
      case FieldConstants.TDD: {
        String text = "Boks for TDD.\n" +
            "Shekhar Gulati - Java Unit Testing with JUnit 5 Test Driven Development with JUnit 5.\n";
        sendMessage(createMessage(text));
        break;
      }
      case "/mongodb": {
        sendMessage(createMessage("/mongo test"));
        check(getUserInfo(update));
        break;
      }
      case "/hide": {
        SendMessage msg = createMessage("Keyboard hidden");
        ReplyKeyboardRemove keyboardMarkup = new ReplyKeyboardRemove();
        msg.setReplyMarkup(keyboardMarkup);
        sendMessage(msg);
        break;
      }
      default: {
        sendMessage(createMessage("Unknown command"));
        break;
      }
    }
  }

  private void createQuestion(String question, String a, String b,
      String c, String d, String explanation, String correctAnswer) {
    MongoDBHandler.addDocumentToCollection(addQuestionToDB(question, a, b, c, d, explanation,
        correctAnswer), MongoDBHandler.COLLECTION_NAME_OCA_QA_TEST);
  }

  private String createEmoji(String emoji) {
    return EmojiParser.parseToUnicode(emoji);
  }

  private void sendPseudoRandomQuestion() {
    Document document = MongoDBHandler.getRandomQuestion();
    sendMessage(createMessage(createMessageByDocument(document)));
    createAnswers();
  }

  private void sendQuestionById(String id) {
    Document document = MongoDBHandler
        .getQuestionById(id, MongoDBHandler.COLLECTION_NAME_OCA_QA_TEST);
    sendMessage(createMessage(createMessageByDocument(document)));
  }

  private void createAnswers() {
    SendMessage msg = InlineKeyboardBuilder.create(chatId)
        .setText("Your answer is: ")
        .row()
        .button("a", FieldConstants.OCA_RANDOM_QUESTION_ANSWER_PREFIX + " a")
        .button("b", FieldConstants.OCA_RANDOM_QUESTION_ANSWER_PREFIX + " b")
        .button("c", FieldConstants.OCA_RANDOM_QUESTION_ANSWER_PREFIX + " c")
        .button("d", FieldConstants.OCA_RANDOM_QUESTION_ANSWER_PREFIX + " d")
        .endRow()
        .build();
    sendMessage(msg);
  }

  private String createMessageByDocument(Document document) {
    StringBuilder sb = new StringBuilder();
    Object[] arr = document.values().toArray();
    sb.append(arr[1]).append("\n")
        .append("a: ").append(arr[2]).append("\n")
        .append("b: ").append(arr[3]).append("\n")
        .append("c: ").append(arr[4]).append("\n")
        .append("d: ").append(arr[5]);
    correctAnswer = (String) arr[6];
    explanation = (String) arr[7];
    return sb.toString();
  }

  private void testMeCommand() {
    String text = "Let's test you";
    SendMessage message = createMessage(text);
    ReplyKeyboardMarkup keyboardMarkup = createReplyKeyboardMarkup(
        new String[]{FieldConstants.PSEUDO_RANDOM_QUESTION,
            FieldConstants.LIST_OF_OCA_QUESTIONS});
    message.setReplyMarkup(keyboardMarkup);
    sendMessage(message);
  }

  private void onUpdateReceivedPhoto(Update update) {
    List<PhotoSize> photos = update.getMessage().getPhoto();
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

  private void sendEditMessage(EditMessageText editMessageText) {
    try {
      execute(editMessageText);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  private String check(String[] arr) {
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
      logger.info("User not exists in database. Written.");
      return "no_exists";
    } else {
      logger.info("User exists in database.");
      MongoDBHandler.disconnect();
      return "exists";
    }
  }

  private void homePage() {
    SendMessage msg = createMessage("Home page");
    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    List<KeyboardRow> keyboard = new ArrayList<>();
    KeyboardRow row = new KeyboardRow();
    row.add(FieldConstants.TESTS);
    row.add(FieldConstants.BOOKS);
    row.add(FieldConstants.ARTICLES);
    keyboard.add(row);
    row = new KeyboardRow();
    row.add(FieldConstants.WEBSITES);
    keyboard.add(row);
    keyboardMarkup.setKeyboard(keyboard);
    keyboardMarkup.setOneTimeKeyboard(true);
    keyboardMarkup.setResizeKeyboard(true);
    msg.setReplyMarkup(keyboardMarkup);
    sendMessage(msg);
  }

  private void testsCommand() {
    SendMessage msg = createMessage("Types of tests");
    ReplyKeyboardMarkup replyKeyboardMarkup = createReplyKeyboardMarkup(
        new String[]{FieldConstants.OCA_TEST,
            FieldConstants.OCP_TEST, FieldConstants.SPRING_TEST});
    msg.setReplyMarkup(replyKeyboardMarkup);
    sendMessage(msg);
  }

  private ReplyKeyboardMarkup createReplyKeyboardMarkup(String[] rows) {
    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    List<KeyboardRow> keyboardRows = new ArrayList<>();
    KeyboardRow keyboardRow = new KeyboardRow();
    for (String row : rows) {
      keyboardRow.add(row);
    }
    keyboardRows.add(keyboardRow);
    replyKeyboardMarkup.setKeyboard(keyboardRows);
    replyKeyboardMarkup.setOneTimeKeyboard(true);
    replyKeyboardMarkup.setResizeKeyboard(true);
    return replyKeyboardMarkup;
  }

  private void replaceMessage(long chatId, long messageId, SendMessage message) {
    EditMessageText newMessage = new EditMessageText()
        .setChatId(chatId)
        .setMessageId(Math.toIntExact(messageId))
        .setText(message.getText())
        .setReplyMarkup((InlineKeyboardMarkup) message.getReplyMarkup());
    sendEditMessage(newMessage);
  }

  private void replaceMessageWithText(long chatId, long messageId, String text) {
    EditMessageText newMessage = new EditMessageText()
        .setChatId(chatId)
        .setMessageId(Math.toIntExact(messageId))
        .setText(text);
    sendEditMessage(newMessage);
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
