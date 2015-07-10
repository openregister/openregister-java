package uk.gov;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.postgresql.ds.PGSimpleDataSource;
import org.skife.jdbi.v2.DBI;
import uk.gov.mint.LoadHandler;
import uk.gov.mint.MintService;
import uk.gov.store.*;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Application {

    @SuppressWarnings("FieldCanBeLocal")
    private static Application notToBeGCed;
    private final Properties configuration;
    private MintService mintService;

    public Application(String... args) throws IOException {
        Map<String, String> propertiesMap = createConfigurationMap(args);

        Properties properties = new Properties();
        properties.load(configurationPropertiesStream(propertiesMap.get("config.file")));
        properties.putAll(propertiesMap);
        configuration = properties;
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        notToBeGCed = new Application(args);
        notToBeGCed.startup();

        Thread.currentThread().join();
    }

    public void startup() {
        String pgConnectionString = configuration.getProperty("postgres.connection.string");
        consoleLog("Connecting to Postgres database: " + pgConnectionString);
        DataSource ds = createDataSource(configuration.getProperty("postgres.database"));
        DBI dbi = new DBI(ds);
        HighWaterMarkDAO highWaterMarkDAO = dbi.onDemand(HighWaterMarkDAO.class);
        EntriesQueryDAO entriesQueryDAO = dbi.onDemand(EntriesQueryDAO.class);
        dataStore = new PostgresDataStore(resolveConnectionString(pgConnectionString));

        String kafkaString = resolveKafkaConnectionString(configuration.getProperty("kafka.bootstrap.servers"));
        consoleLog("Connecting to Kafka: " + kafkaString);
        LogStream logStream = new LogStream(highWaterMarkDAO, entriesQueryDAO, createKafkaProducer(kafkaString));

        LoadHandler loadHandler = new LoadHandler(dataStore, logStream);
        mintService = new MintService(loadHandler);
        mintService.init();

        consoleLog("Application started...");
    }

    private String resolveKafkaConnectionString(final String kafkaString) {
        final Map<String, String> env = System.getenv();
        if(!env.containsKey("KAFKA_1_PORT_9092_TCP_ADDR")) {
            System.out.println("KAFKA_1_PORT_9092_TCP_ADDR _NOT_ defined - using default: " + kafkaString);
            return kafkaString;
        }

        final String kafkaConnectionString = env.get("KAFKA_1_PORT_9092_TCP_ADDR") + ":"
                + env.get("KAFKA_1_PORT_9092_TCP_PORT");

        System.out.println("KAFKA_1_PORT_9092_TCP_ADDR defined - using: " + kafkaConnectionString);

        return kafkaConnectionString;
    }

    private String resolveConnectionString(String pgConnectionString) {
        final Map<String, String> env = System.getenv();
        if(!env.containsKey("POSTGRES_PORT_5432_TCP_ADDR")) {
            System.out.println("POSTGRES_PORT_5432_TCP_ADDR _NOT_ defined - using default: " + pgConnectionString);
            return pgConnectionString;
        }

        final String dockerConnectionString = "jdbc:postgresql://"
                + env.get("POSTGRES_PORT_5432_TCP_ADDR") + ":"
                + env.get("POSTGRES_PORT_5432_TCP_PORT") + "/"
                + "postgres"
                + "?user=" + env.get("POSTGRES_ENV_POSTGRES_USER")
                + "&password=" + env.get("POSTGRES_ENV_POSTGRES_PASSWORD");
        System.out.println("POSTGRES_PORT_5432_TCP_ADDR defined - using: " + dockerConnectionString);

        return dockerConnectionString;
    }

    private KafkaProducer<String, byte[]> createKafkaProducer(String kafkaString) {
        return new KafkaProducer<>(ImmutableMap.of(
                "bootstrap.servers", kafkaString,
                "key.serializer", "org.apache.kafka.common.serialization.StringSerializer",
                "value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer"
        ));
    }

    private DataSource createDataSource(String databaseName) {
        PGSimpleDataSource source = new PGSimpleDataSource();
        source.setDatabaseName(databaseName);
        return source;
    }

    public void shutdown() throws Exception {
        consoleLog("Shutting application down...");
        mintService.shutdown();
    }

    private InputStream configurationPropertiesStream(String fileName) throws IOException {
        if (Strings.isNullOrEmpty(fileName)) {
            consoleLog("Configuration properties file not provided, using default application.properties file");
            return Application.class.getResourceAsStream("/application.properties");
        } else {
            consoleLog("Loading properties file: " + fileName);
            return new FileInputStream(new File(fileName));
        }
    }

    private Map<String, String> createConfigurationMap(String[] args) {
        Map<String, String> appParams = new HashMap<>();
        for (int i = 0; args != null && i < args.length; i++) {
            if (args[i].contains("=")) {
                String[] kv = args[i].split("=", 2);
                appParams.put(kv[0], kv[1]);
            }
        }
        return appParams;
    }

    private void consoleLog(String logMessage) {
        System.out.println(logMessage);
    }

    private static void dumpEnv() {
        final Map<String, String> env = System.getenv();
        for(final String key : env.keySet()) {
            final String val = env.get(key);

            System.out.println(key + ": " + val);
        }
    }


    @SuppressWarnings("FieldCanBeLocal")
    private static Application notToBeGCed;

    public static void main(String[] args) throws InterruptedException, IOException {
        // Wait for all the containers to start
        Thread.sleep(5000);

        dumpEnv();

        notToBeGCed = new Application(args);
        notToBeGCed.startup();

        Thread.currentThread().join();
    }
}

