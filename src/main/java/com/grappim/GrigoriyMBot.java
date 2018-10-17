package com.grappim;

import com.grappim.builders.InlineKeyboardBuilder;
import com.grappim.builders.KeyboardMarkupBuilder;
import com.grappim.constant.FieldConstants;
import com.grappim.handlers.MongoDBHandler;
import com.grappim.menu.MenuManager;
import com.grappim.util.LoadProperties;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.vdurmont.emoji.EmojiParser;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * IDE: IntelliJ IDEA Created by grigo on Oct, 05, 2018 Project: telegrambottest
 */

public class GrigoriyMBot extends TelegramLongPollingBot {

  private static final Logger logger = LoggerFactory.getLogger(GrigoriyMBot.class);
  private long chatId;
  private String ocaCorrectAnswer;
  private String explanation;

  private MenuManager ocaMenuManager = new MenuManager();
  private MenuManager interviewQManager = new MenuManager();

  private LoadProperties loadProperties = new LoadProperties();

  private String previousPage;
  private String currentMenu;

  public GrigoriyMBot() {
    MongoDBHandler.connect();
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
    callbackLogging(update);
    String callData = update.getCallbackQuery().getData();
    long messageId = update.getCallbackQuery().getMessage().getMessageId();

    if (callData.startsWith(FieldConstants.OCA_ANSWER_PREFIX)) {
      String[] tokens = callData.split(" ");
      if (tokens[1].equals(ocaCorrectAnswer)) {
        sendMessage(createMessage(createEmoji(":thumbsup: Correct")));
        ocaCorrectAnswer = "";
      } else {
        sendMessage(createMessage(createEmoji(":poop:") + " Incorrect\n" + explanation));
        explanation = "";
      }
      sendNextQMainPage();
    } else if (callData.startsWith(FieldConstants.OCA_NOT_RANDOM_QUESTION_PREFIX)) {
      String[] tokens = callData.split(" ");
      sendQuestionByIdToDB(tokens[1]);
      createAnswers(FieldConstants.OCA_ANSWER_PREFIX);
    } else if (callData.equals(MenuManager.CANCEL_ACTION)) {
      replaceMessageWithText(chatId, messageId, "Cancelled");
    } else if (callData.startsWith(FieldConstants.INTERVIEW_QUESTION_PREFIX)) {
      String[] tokens = callData.split(" ");
      sendInterviewQuestionByIdToDB(tokens[1]);
    } else if (callData.startsWith(MenuManager.PREV_ACTION) ||
        callData.startsWith(MenuManager.NEXT_ACTION)) {
      String pageNum;
      if (callData.startsWith(MenuManager.PREV_ACTION)) {
        pageNum = callData.replace(MenuManager.PREV_ACTION + ":", "");
      } else {
        pageNum = callData.replace(MenuManager.NEXT_ACTION + ":", "");
      }
      switch (currentMenu) {
        case FieldConstants.LIST_OF_OCA_QUESTIONS: {
          InlineKeyboardBuilder builder = ocaMenuManager
              .createMenuForPage(Integer.parseInt(pageNum), true);
          builder.setChatId(chatId).setText("Choose action:");
          SendMessage message = builder.build();
          replaceMessage(chatId, messageId, message);
          break;
        }
        case FieldConstants.INTERVIEW_QUESTIONS: {
          InlineKeyboardBuilder builder = interviewQManager
              .createMenuForPage(Integer.parseInt(pageNum), true);
          builder.setChatId(chatId).setText("Choose action:");
          SendMessage message = builder.build();
          replaceMessage(chatId, messageId, message);
          break;
        }
      }
    }
  }

  private void callbackLogging(Update update) {
    String msg = update.getCallbackQuery().getData();
    DateFormat dateFormat = new SimpleDateFormat("dd/MM//yyyy HH:mm::ss");
    Date date = new Date();
    logger.info(dateFormat.format(date) + "\n"
        + "Text - " + msg);
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
    if (messageText.equals(FieldConstants.BACK)) {
      messageText = previousPage;
    }
    switch (messageText) {
      case FieldConstants.START_COMMAND:
      case FieldConstants.MAIN_PAGE: {
        currentMenu = "";
        homePage();
        break;
      }
      case FieldConstants.ABOUT_ME_COMMAND: {
        String text = "I am a bot who wants to help you with Java.\n" +
            "The source code you can find here: https://github.com/Grigoriym/telegram-bot-test.\n";
        sendMessage(createMessage(text));
        break;
      }
      case FieldConstants.OCA_TEST: {
        currentMenu = "";
        previousPage = FieldConstants.TESTS_AND_QUESTIONS;
        ocaTestCommand();
        break;
      }
      case FieldConstants.PSEUDO_RANDOM_QUESTION:
      case FieldConstants.NEXT_RANDOM_QUESTION: {
        sendPseudoRandomQuestion();
        break;
      }
      case "/addq": {
        break;
      }
      case "/update": {

        break;
      }
      case FieldConstants.ARTICLES: {

        break;
      }
      case FieldConstants.LIST_OF_OCA_QUESTIONS: {
        previousPage = FieldConstants.OCA_TEST;
        currentMenu = FieldConstants.LIST_OF_OCA_QUESTIONS;
        listOfOcaQuestionsCommand();
        break;
      }
      case FieldConstants.TESTS_AND_QUESTIONS: {
        previousPage = FieldConstants.MAIN_PAGE;
        testsCommand();
        break;
      }
      case FieldConstants.BOOKS: {
        previousPage = FieldConstants.MAIN_PAGE;
        booksCommand();
        break;
      }

      case FieldConstants.WEBSITES: {

        break;
      }
      case FieldConstants.JAVA_CORE: {
        currentMenu = "";
        previousPage = FieldConstants.TESTS_AND_QUESTIONS;
        javaCoreCommand();
        break;
      }
      case FieldConstants.INTERVIEW_QUESTIONS: {
        currentMenu = FieldConstants.INTERVIEW_QUESTIONS;
        previousPage = FieldConstants.JAVA_CORE;
        interviewQuestionsCommand();
        break;
      }
      case FieldConstants.JAVA_CORE_COLLECTIONS: {

        break;
      }
      case FieldConstants.OCA_OCP_BOOKS: {
        previousPage = FieldConstants.BOOKS;
        ocaOcpBooksCommand();
        break;
      }
      case FieldConstants.TDD: {
        previousPage = FieldConstants.BOOKS;
        tddBooksCommand();
        break;
      }
      case "/mongodb": {
//        sendMessage(createMessage("/mongo test"));
//        check(getUserInfo(update));
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

  private void interviewQuestionsCommand() {
    FindIterable<Document> docs = MongoDBHandler
        .findAllDocumentsInCollection(MongoDBHandler.COLLECTION_NAME_INTERVIEW_Q);
    interviewQManager.setColumnsCount(1);
    for (Document doc : docs) {
      interviewQManager.addMenuItem(
          (String) doc.get("question"),
          FieldConstants.INTERVIEW_QUESTION_PREFIX + " " +
              doc.get("_id").toString());
    }
    interviewQManager.init();
    InlineKeyboardBuilder builder = interviewQManager.createMenuForPage(0, true);
    builder.setChatId(chatId).setText("Choose question: ");
    sendMessage(builder.build());
  }

  private void createOCAQuestion(String question, String a, String b,
      String c, String d, String explanation, String correctAnswer) {
    MongoDBHandler.addDocumentToCollection(
        addQuestionToOcaCollection(question, a, b, c, d, explanation,
            correctAnswer), MongoDBHandler.COLLECTION_NAME_OCA_QA_TEST);
  }

  private void createInterviewQuestion(String question) {
    MongoDBHandler.addDocumentToCollection(addQuestionToInterviewCollection(question),
        MongoDBHandler.COLLECTION_NAME_INTERVIEW_Q);
  }

  private String createEmoji(String emoji) {
    return EmojiParser.parseToUnicode(emoji);
  }

  private void sendPseudoRandomQuestion() {
    Document document = MongoDBHandler.getRandomQuestion();
    sendMessage(createMessage(createOCAquestionByDocument(document)));
    createAnswers(FieldConstants.OCA_ANSWER_PREFIX);
  }

  private void sendQuestionByIdToDB(String id) {
    Document document = MongoDBHandler
        .getDocumentByIdInCollection(id, MongoDBHandler.COLLECTION_NAME_OCA_QA_TEST);
    sendMessage(createMessage(createOCAquestionByDocument(document)));
  }

  private void sendInterviewQuestionByIdToDB(String id) {
    Document document = MongoDBHandler.getDocumentByIdInCollection(
        id, MongoDBHandler.COLLECTION_NAME_INTERVIEW_Q
    );
    sendMessage(createMessage(createInterviewQuestionByDocument(document)));
  }

  private String createInterviewQuestionByDocument(Document document) {
    StringBuilder sb = new StringBuilder();
    Object[] arr = document.values().toArray();
    sb.append(arr[1]);
    return sb.toString();
  }

  private void createAnswers(String prefix) {
    SendMessage msg = InlineKeyboardBuilder.create(chatId)
        .setText("Your answer is: ")
        .row()
        .button("a", prefix + " a")
        .button("b", prefix + " b")
        .button("c", prefix + " c")
        .button("d", prefix + " d")
        .endRow()
        .build();
    sendMessage(msg);
  }

  private String createOCAquestionByDocument(Document document) {
    StringBuilder sb = new StringBuilder();
    Object[] arr = document.values().toArray();
    sb.append(arr[1]).append("\n")
        .append("a: ").append(arr[2]).append("\n")
        .append("b: ").append(arr[3]).append("\n")
        .append("c: ").append(arr[4]).append("\n")
        .append("d: ").append(arr[5]);
    ocaCorrectAnswer = (String) arr[6];
    explanation = (String) arr[7];
    return sb.toString();
  }

  private void ocaTestCommand() {
    SendMessage msg = KeyboardMarkupBuilder.create(chatId)
        .setText("Let's test you")
        .row()
        .button(FieldConstants.PSEUDO_RANDOM_QUESTION)
        .button(FieldConstants.LIST_OF_OCA_QUESTIONS)
        .endRow()
        .row()
        .button(FieldConstants.BACK)
        .endRow()
        .build();
    sendMessage(msg);
  }

  private void onUpdateReceivedPhoto(Update update) {
    List<PhotoSize> photos = update.getMessage().getPhoto();
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

  private void ocaOcpBooksCommand() {
//    String text = "Books for OCA/OCP.\n" +
//        "Scott Selikoff - OCA  OCP Java SE 8 Programmer Practice Tests.\n" +
//        "Sierra K., Bates B., Robson E. - OCP Java SE 8 Programmer II Exam Guide (Exam 1Z0-809) - 2018\n";
//    SendMessage msg = InlineKeyboardBuilder.create(chatId)
//        .setText("Books for OCA/OCP.")
//        .row()
//        .button(
//            "Scott Selikoff - OCA  OCP Java SE 8 Programmer Practice Tests",
//            FieldConstants.BOOKS_PREFIX + loadProperties.getProp().getProperty("ScottSOCAOCP2017"))
//
//    SendMessage message = KeyboardMarkupBuilder.create(chatId)
//        .setText(text)
//        .row()
//        .button(FieldConstants.BACK)
//        .endRow()
//        .build();
//    sendMessage(message);
  }

  private void tddBooksCommand() {
    String text = "Boks for TDD.\n" +
        "Shekhar Gulati - Java Unit Testing with JUnit 5 Test Driven Development with JUnit 5.\n";
    SendMessage message = KeyboardMarkupBuilder.create(chatId)
        .setText(text)
        .row()
        .button(FieldConstants.BACK)
        .endRow()
        .build();
    sendMessage(message);
  }

  private void booksCommand() {
    SendMessage message =
        KeyboardMarkupBuilder.create(chatId)
            .setText("Books")
            .row()
            .button(FieldConstants.OCA_OCP_BOOKS)
            .button(FieldConstants.TDD)
            .endRow()
            .row()
            .button(FieldConstants.BACK)
            .endRow()
            .build();
    sendMessage(message);
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
    sendMessage(createMessage("Type /start if you stuck somewhere"));
    SendMessage message = KeyboardMarkupBuilder.create(chatId)
        .setText("Home page")
        .row()
        .button(FieldConstants.TESTS_AND_QUESTIONS)
        .button(FieldConstants.BOOKS)
        .button(FieldConstants.ARTICLES)
        .endRow()
        .row()
        .button(FieldConstants.WEBSITES)
        .endRow()
        .build(true, true);
    sendMessage(message);
  }

  private void javaCoreCommand() {
    SendMessage message = KeyboardMarkupBuilder.create(chatId)
        .setText(FieldConstants.JAVA_CORE)
        .row()
        .button(FieldConstants.JAVA_CORE_CONCURRENCY)
        .button(FieldConstants.JAVA_CORE_COLLECTIONS)
        .endRow()
        .row()
        .button(FieldConstants.INTERVIEW_QUESTIONS)
        .endRow()
        .row()
        .button(FieldConstants.BACK)
        .endRow()
        .build();
    sendMessage(message);
  }

  private void testsCommand() {
    SendMessage msg = KeyboardMarkupBuilder.create(chatId)
        .setText("Types of tests")
        .row()
        .button(FieldConstants.OCA_TEST)
        .button(FieldConstants.OCP_TEST)
        .button(FieldConstants.SPRING_TEST)
        .endRow()
        .row()
        .button(FieldConstants.JAVA_CORE)
        .endRow()
        .row()
        .button(FieldConstants.BACK)
        .endRow()
        .build();
    sendMessage(msg);
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

  private void sendNextQMainPage() {
    SendMessage message = KeyboardMarkupBuilder.create(chatId)
        .setText("Choose: ")
        .row()
        .button(createEmoji(FieldConstants.NEXT_RANDOM_QUESTION))
        .button(createEmoji(FieldConstants.MAIN_PAGE))
        .endRow()
        .build(true, true);
    sendMessage(message);
  }

  private void listOfOcaQuestionsCommand() {
    FindIterable<Document> docs = MongoDBHandler
        .findAllDocumentsInCollection(MongoDBHandler.COLLECTION_NAME_OCA_QA_TEST);
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
  }

  private String[] getUserInfo(Update update) {
    String userFirstName = update.getMessage().getChat().getFirstName();
    String userLastName = update.getMessage().getChat().getLastName();
    String username = update.getMessage().getChat().getUserName();
    String userId = String.valueOf(update.getMessage().getChat().getId());
    return new String[]{userFirstName, userLastName, userId, username};
  }

  private Document addQuestionToOcaCollection(String question, String a, String b, String c,
      String d, String explanation, String correct) {
    return new Document("question", question)
        .append("a", a)
        .append("b", b)
        .append("c", c)
        .append("d", d)
        .append("correct", correct)
        .append("explanation", explanation);
  }

  private Document addQuestionToInterviewCollection(String question) {
    return new Document("question", question);
  }

  @Override
  public String getBotUsername() {
    return loadProperties.getProp().getProperty("testBotName");
  }

  @Override
  public String getBotToken() {
    return loadProperties.getProp().getProperty("testBotToken");
  }
}
