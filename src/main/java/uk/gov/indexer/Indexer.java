package uk.gov.indexer;

import com.google.common.base.Throwables;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.indexer.dao.DestinationDBUpdateDAO;
import uk.gov.indexer.dao.IndexedEntriesUpdateDAO;
import uk.gov.indexer.dao.SourceDBQueryDAO;
import uk.gov.indexer.monitoring.CloudwatchRecordsProcessedUpdater;

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

            DBI sourceDbi = new DBI(configuration.getProperty(register + ".source.postgres.db.connectionString"));
            sourceDBQueryDAO = sourceDbi.onDemand(SourceDBQueryDAO.class);


            executorService.scheduleAtFixedRate(
                    new IndexerTask(
                            configuration.cloudwatchEnvironmentName().map(e -> new CloudwatchRecordsProcessedUpdater(e, register)),
                            register,
                            sourceDBQueryDAO,
                            destinationDBUpdateDAO
                    ),
                    0,
                    10,
                    TimeUnit.SECONDS
            );

            executorService.scheduleAtFixedRate(
                    new IndexEntryItemTask(
                            register,
                            sourceDBQueryDAO,
                            destinationDBUpdateDAO
                    ),
                    0,
                    10,
                    TimeUnit.SECONDS
            );

            configuration.cloudSearchEndPoint(register).ifPresent(
                    endPoint -> executorService.scheduleAtFixedRate(
                            new CloudSearchDataUploadTask(register, endPoint, configuration.cloudSearchWaterMarkEndPoint(register).get(), destDbi.onDemand(IndexedEntriesUpdateDAO.class)),
                            0,
                            5,
                            TimeUnit.MINUTES
                    )
            );

            configuration.elasticSearchEndPoint(register).ifPresent(
                    endPoint -> executorService.scheduleAtFixedRate(
                            new ElasticSearchDataUploadTask(register, endPoint, destDbi.onDemand(IndexedEntriesUpdateDAO.class)),
                            0,
                            5,
                            TimeUnit.MINUTES
                    )
            );

        } catch (Throwable e) {
            LOGGER.info("Error occurred while setting indexer for register: " + register + ". Error is -> " + e.getMessage());
            LOGGER.error(Throwables.getStackTraceAsString(e));
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
