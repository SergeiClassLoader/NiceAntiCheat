package pro.cyrent.anticheat.api.database.impl;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.database.DatabaseInterface;
import pro.cyrent.anticheat.api.database.api.InputData;
import pro.cyrent.anticheat.util.json.*;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientURI;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.bson.conversions.Bson;
import pro.cyrent.anticheat.util.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class MongoManager implements DatabaseInterface {

    private MongoDatabase mongoDatabase;
    private MongoClient mongoClient;
    private List<String> databaseCollections;
    private MongoCollection<Document> logsCollection;

    @Override
    public void initManager() {
        Anticheat.INSTANCE.getDatabaseManager().getExecutorService().execute(() -> {
            try {


                Logger logger = Logger.getLogger("org.mongodb.driver");
                logger.setLevel(Level.OFF);

                Logger logger2 = Logger.getLogger("com.mongodb");
                logger2.setLevel(Level.OFF);


                MongoClientSettings.builder()
                        .applyToSocketSettings(builder -> {
                            builder.connectTimeout(1, MILLISECONDS);
                            builder.readTimeout(1, MILLISECONDS);
                        })
                        .applyToClusterSettings( builder -> builder.serverSelectionTimeout(1, MILLISECONDS))
                        .applyConnectionString(new ConnectionString(Anticheat.INSTANCE.getConfigValues().getClientURI()))
                        .build();

                this.mongoClient = new MongoClient(new MongoClientURI(Anticheat.INSTANCE.getConfigValues().getClientURI()));

                this.mongoDatabase = this.mongoClient.getDatabase(Anticheat.INSTANCE.getConfigValues().getDataBaseName());
                this.databaseCollections = this.mongoDatabase.listCollectionNames().into(new ArrayList<>());
                this.createCollection(Anticheat.INSTANCE.getConfigValues().getCollectionsName());

                this.logsCollection = this.mongoDatabase.getCollection(Anticheat.INSTANCE.getConfigValues().getCollectionsName());
                this.createIndexes();

                System.out.println("\nSuccessfully connected to Mongo DB!\n");

            } catch (Exception e) {
                System.out.println("\nFailed to connect to the mongodb!\n");
                e.printStackTrace();
            }
        });
    }

    @Override
    public void shutdown() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
            this.mongoClient = null;
        }
    }

    @Override
    public void dropReset() {
        if (isSetup()) {

            System.out.println("Contacting database...");

            this.logsCollection.drop();
            System.out.println("Dropping collections...");

            this.mongoDatabase.drop();
            System.out.println("Dropping database...");

            Anticheat.INSTANCE.getDatabaseManager().shutdown();
            System.out.println("Disconnecting database...");

            System.out.println("Contacting database...");
            Anticheat.INSTANCE.getDatabaseManager().setup();

            System.out.println("Connected to MongoDB!");
        }
    }

    @Override
    public List<InputData> getLogs(String playerName) {
        List<Document> logs = new ArrayList<>();
        List<Bson> bsonArrayList = new ArrayList<>();

        bsonArrayList.add(Aggregates.match(Filters.eq("name", playerName)));
        AggregateIterable<Document> aggregateIterable = this.logsCollection
                .aggregate(bsonArrayList).allowDiskUse(true);
        aggregateIterable.forEach((Consumer<Document>) logs::add);

        List<InputData> inputData = new ArrayList<>();

        logs.forEach(document -> {
            String uuid = document.getString("uuid");

            String checkDataString = document.getString("checkdata");
            String[] checkDataLines = checkDataString.split("\n");

            for (String checkDataLine : checkDataLines) {
                String[] fields = checkDataLine.split(":");

                String check = fields[0];
                String type = fields[1];
                String vl = fields[2];
                boolean experimental = Boolean.getBoolean(fields[3]);
                String pinger = fields[4];
                String data = fields[5];

                String currentDate = fields[6];
                String loginDate = fields[7];
                String loginTime = fields[8];

                String fixedPing = pinger
                        .replace("(", "")
                        .replace(")", "")
                        .replace("ping", "").replace(" ", "");

                String fixedVL = vl
                        .replace("(", "")
                        .replace(")", "")
                        .replace("x", "").replace(" ", "");

                int ping = Integer.parseInt(fixedPing);

                double violation = Double.parseDouble(fixedVL);

                String ban = fields[9];
                String license = fields[10];

                inputData.add(new InputData(
                        uuid,
                        playerName,
                        check,
                        type,
                        violation,
                        experimental,

                        ping,
                        data,

                        currentDate,
                        loginDate,
                        loginTime,
                        ban,
                        license)
                );
            }
        });

        return inputData;
    }

    @Override
    public boolean isSetup() {
        return this.mongoClient != null && this.mongoDatabase != null && this.logsCollection != null;
    }

    @Override
    public void addViolation(InputData inputData) {

        // TODO: make this like your backend so it doesn't lag and cause high connection use.
        if (this.logsCollection == null) return;

        String uuid = inputData.getUUID();

        // Retrieve the existing document with the given UUID
        Document existingDocument = this.logsCollection.find(new Document("uuid", uuid)).first();
        if (existingDocument == null) {
            // If the document doesn't exist, create a new one
            existingDocument = new Document("uuid", uuid);
        }


        String playerName = inputData.getPlayerName();
        String checkName = inputData.getCheckName();
        String checkType = inputData.getCheckType();

        double violation = inputData.getViolation();

        boolean experimental = inputData.isExperimental();

        int ping = inputData.getPing();

        String currentDate = inputData.getCurrentDate();
        String loginDate = inputData.getLoginDate();
        String loginTime = inputData.getLoginTime();

        String data = checkName + ":" + checkType
                + ":(x" + violation + ")"
                + ":" + experimental + ""
                + ":(ping " + ping + ")"
                + ":checkData=" + inputData.getCheckData()
                + ":date=" + currentDate
                + ":login-date=" + loginDate
                + ":login-time=" + loginTime;


        JSONArray array = new JSONArray();
        array.put(data);


        String existingCheckData = existingDocument.getString("checkdata");


        // Append the new data to the existing document
        existingDocument.append("uuid", uuid)
                .append("name", playerName)
                .append("checkdata", array.getString(0));

        if (existingCheckData != null
                && !existingCheckData.equals(data)) {
            existingDocument.put("checkdata", existingCheckData + "\n" + array.get(0));
        }


        if (existingDocument.containsKey("_id")) {
            // If the document already exists in the collection, update it
            this.logsCollection.replaceOne(new Document("_id", existingDocument.get("_id")), existingDocument);
        } else {
            // If the document is new, insert it into the collection
            this.logsCollection.insertOne(existingDocument);
        }


        array.clear();
    }


    void createCollection(String s) {
        if (!this.databaseCollections.contains(s)) {
            this.mongoDatabase.createCollection(s);
        }
    }

    void createIndexes() {
        if (this.logsCollection != null) {
            this.logsCollection.createIndex(Indexes.ascending("uuid"));
            this.logsCollection.createIndex(Indexes.ascending("name"));
            this.logsCollection.createIndex(Indexes.ascending("checkdata"));
        }
    }
}
