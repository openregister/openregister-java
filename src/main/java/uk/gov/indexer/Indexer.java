package uk.gov.indexer;

import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.indexer.ctserver.CTServer;
import uk.gov.indexer.dao.DestinationDBUpdateDAO;
import uk.gov.indexer.dao.IndexedEntriesUpdateDAO;
import uk.gov.indexer.dao.SourceDBQueryDAO;
import uk.gov.indexer.fetchers.CTFetcher;
import uk.gov.indexer.fetchers.PGFetcher;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Indexer {
    private final Logger LOGGER = LoggerFactory.getLogger(Indexer.class);

    private final Configuration configuration;
    private final String register;
    private DestinationDBUpdateDAO destinationDBUpdateDAO;
    private SourceDBQueryDAO sourceDBQueryDAO;

    public Indexer(Configuration configuration, String register) {
        this.configuration = configuration;
        this.register = register;
    }

    public synchronized void start(ScheduledExecutorService executorService) {
        try {
            LOGGER.info("setting up register " + register);

            DBI destDbi = new DBI(configuration.getProperty(register + ".destination.postgres.db.connectionString"));
            destinationDBUpdateDAO = destDbi.open().attach(DestinationDBUpdateDAO.class);

            if (configuration.getCTServerEndpointForRegister(register).isPresent()) {
                executorService.scheduleAtFixedRate(
                        new IndexerTask(register, new CTFetcher(new CTServer(configuration.getCTServerEndpointForRegister(register).get())), destinationDBUpdateDAO),
                        0,
                        10,
                        TimeUnit.SECONDS);
            } else {
                DBI sourceDbi = new DBI(configuration.getProperty(register + ".source.postgres.db.connectionString"));
                sourceDBQueryDAO = sourceDbi.onDemand(SourceDBQueryDAO.class);
                executorService.scheduleAtFixedRate(
                        new IndexerTask(register, new PGFetcher(sourceDBQueryDAO), destinationDBUpdateDAO),
                        0,
                        10,
                        TimeUnit.SECONDS
                );
            }

            configuration.cloudSearchEndPoint(register).ifPresent(
                    endPoint -> executorService.scheduleAtFixedRate(
                            new CloudSearchDataUploadTask(register, endPoint, configuration.cloudSearchWaterMarkEndPoint(register).get(), destDbi.onDemand(IndexedEntriesUpdateDAO.class)),
                            0,
                            5,
                            TimeUnit.MINUTES
                    )
            );

            configuration.elasticSerachEndPoint(register).ifPresent(
                    endPoint -> executorService.scheduleAtFixedRate(
                            new ElasticSearchDataUploadTask(register, endPoint, destDbi.onDemand(IndexedEntriesUpdateDAO.class)),
                            0,
                            5,
                            TimeUnit.MINUTES
                    )
            );

        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.info("Error occurred while setting indexer for register: " + register + ". Error is -> " + e.getMessage());
        }

    }

    public synchronized void stop() {
        if (destinationDBUpdateDAO != null) {
            destinationDBUpdateDAO.close();
        }
        if (sourceDBQueryDAO != null) {
            sourceDBQueryDAO.close();
        }
    }
}
