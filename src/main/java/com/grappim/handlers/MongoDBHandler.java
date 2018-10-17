package com.grappim.handlers;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import java.util.Collections;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * IDE: IntelliJ IDEA Created by grigo on Oct, 06, 2018 Project: telegram-bot-test
 */

public class MongoDBHandler {

  public static final String COLLECTION_NAME_USERS = "users";
  public static final String COLLECTION_NAME_OCA_QA_TEST = "oca_qa_test";
  public static final String COLLECTION_NAME_OCP_QA_TEST = "ocp_qa_test";
  public static final String COLLECTION_NAME_SPRING_QA_TEST = "spring_qa_test";
  public static final String COLLECTION_NAME_INTERVIEW_Q = "interview_q";

  private static final String databaseName = "telegram-bot-test";
  private static final String mongoClientURI = "mongodb://127.0.0.1:27017";

  private static MongoClient mongoClient;
  private static MongoDatabase database;
  private static MongoCollection<Document> usersCollection;
  private static MongoCollection<Document> ocaQaTestCollection;
  private static MongoCollection<Document> interviewQCollection;

  private static MongoClientURI connectionString = new MongoClientURI(mongoClientURI);

  public static void addDocumentToCollection(Document document, String collectionName) {
    switch (collectionName) {
      case COLLECTION_NAME_OCA_QA_TEST: {
        ocaQaTestCollection.insertOne(document);
        break;
      }
      case COLLECTION_NAME_INTERVIEW_Q:{
        interviewQCollection.insertOne(document);
        break;
      }
    }
  }

  public static Document getRandomQuestion() {
    return ocaQaTestCollection.aggregate(Collections.singletonList(Aggregates.sample(1))).first();
  }

  public static Document getDocumentByIdInCollection(String id, String collectionName) {
    switch (collectionName) {
      case COLLECTION_NAME_OCA_QA_TEST: {
        return ocaQaTestCollection.find(Filters.eq("_id", new ObjectId(id))).first();
      }
      case COLLECTION_NAME_INTERVIEW_Q:{
        return interviewQCollection.find(Filters.eq("_id", new ObjectId(id))).first();
      }
      default: {
        return null;
      }
    }
  }

  public static FindIterable<Document> findAllDocumentsInCollection(String collectionName) {
    switch (collectionName) {
      case COLLECTION_NAME_OCA_QA_TEST: {
        return ocaQaTestCollection.find();
      }
      case COLLECTION_NAME_INTERVIEW_Q:{
        return interviewQCollection.find();
      }
      default: {
        return null;
      }
    }
  }

  public static FindIterable<Document> findInCollection(BasicDBObject searchQuery,
      String collectionName) {
    switch (collectionName) {
      case COLLECTION_NAME_OCA_QA_TEST: {
        return ocaQaTestCollection.find(searchQuery);
      }
      default: {
        return null;
      }
    }
  }

  public static void replaceQuestion(Document document, String id, String collectionName) {
    switch (collectionName) {
      case (COLLECTION_NAME_OCA_QA_TEST): {
        ocaQaTestCollection.replaceOne(Filters.eq("_id", new ObjectId(id)),
            document);
        break;
      }
    }
  }

  public static void connect() {
    mongoClient = new MongoClient(connectionString);
    database = mongoClient.getDatabase(databaseName);
    ocaQaTestCollection = database.getCollection(COLLECTION_NAME_OCA_QA_TEST);
    interviewQCollection = database.getCollection(COLLECTION_NAME_INTERVIEW_Q);
  }

  public static void disconnect() {
    mongoClient.close();
  }

}
