package com.grappim.handlers;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import java.util.Arrays;
import java.util.Collections;
import org.bson.Document;

/**
 * IDE: IntelliJ IDEA Created by grigo on Oct, 06, 2018 Project: telegram-bot-test
 */

public class MongoDBHandler {

  public static final String COLLECTION_NAME_USERS = "users";
  public static final String COLLECTION_NAME_QA_TEST = "qa_test";

  private static final String databaseName = "telegram-bot-test";
  private static final String mongoClientURI = "mongodb://127.0.0.1:27017";

  private static MongoClient mongoClient;
  public static MongoDatabase database;
  public static MongoCollection<Document> usersCollection;
  public static MongoCollection<Document> qaTestCollection;

  private static MongoClientURI connectionString = new MongoClientURI(mongoClientURI);

  public static void addDocumentToCollection(Document document, String collectionName) {
    switch (collectionName){
      case COLLECTION_NAME_QA_TEST:{
        qaTestCollection.insertOne(document);
        break;
      }
      case COLLECTION_NAME_USERS:{
        usersCollection.insertOne(document);
        break;
      }
    }
  }

  public static Document getRandomQuestion() {
    return qaTestCollection.aggregate(Collections.singletonList(Aggregates.sample(1))).first();
  }

  public static FindIterable<Document> findInCollection(BasicDBObject searchQuery, String collectionName) {
    switch (collectionName){
      case COLLECTION_NAME_QA_TEST:{
        return qaTestCollection.find(searchQuery);
      }
      case COLLECTION_NAME_USERS:{
        return usersCollection.find(searchQuery);
      }
      default:{
        return null;
      }
    }
  }

  public static void connect() {
    mongoClient = new MongoClient(connectionString);
    database = mongoClient.getDatabase(databaseName);
    usersCollection = database.getCollection(COLLECTION_NAME_USERS);
    qaTestCollection = database.getCollection(COLLECTION_NAME_QA_TEST);
  }

  public static void disconnect() {
    mongoClient.close();
  }

}
