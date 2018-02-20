package net.apnic.rdapd.loaders.config;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import javax.annotation.PostConstruct;

import net.apnic.rdapd.history.History;
import net.apnic.rdapd.loaders.health.LoaderHealthIndicator;
import net.apnic.rdapd.loaders.RipeDbLoader;
import net.apnic.rdapd.progress.Bar;
import net.apnic.rdapd.search.SearchEngine;

import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class LoaderConfiguration
{
    private final static Logger LOGGER = LoggerFactory.getLogger(LoaderConfiguration.class);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Future<Long> asyncLoader = CompletableFuture.completedFuture(-1L);

    @Autowired
    private ApplicationContext context;
    private RipeDbLoader dbLoader;
    private LoaderHealthIndicator loaderHealthIndicator = new LoaderHealthIndicator();

    @Value("${snapshot.file:#{null}}")
    private String snapshotFile;

    @Autowired
    History history;

    @Autowired
    private JdbcOperations jdbcOperations;

    @Autowired
    SearchEngine searchEngine;

    private void buildTree()
    {
        if (snapshotFile != null) {
            LOGGER.info("Attempting to deserialise from {}", snapshotFile);
            try (InputStream resourceStream = context.getResource("file:///" + snapshotFile).getInputStream();
                InflaterInputStream zipStream = new InflaterInputStream(resourceStream);
                FSTObjectInput objStream = new FSTObjectInput(zipStream)) {
                long serial = objStream.readLong();
                history.deserialize((History)objStream.readObject());
                dbLoader.setLastSerial(serial);
            } catch (FileNotFoundException ex) {
                LOGGER.warn("snapshot file \"{}\" does not exist", snapshotFile);
            } catch (IOException | ClassNotFoundException ex) {
                LOGGER.error("Exception during load", ex);
            }
        }

        LOGGER.info("Loading history from database, starting at #{}",
                    dbLoader.getLastSerial());
        try {
            Bar bar = new Bar(107, LOGGER::info);
            final ZonedDateTime lastDate[] = { ZonedDateTime.of(2008, 1, 1, 1, 1, 1,1, ZoneId.systemDefault()) };
            lastDate[0] = lastDate[0].truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1);
            dbLoader.loadWith((k, r) -> {
                ZonedDateTime x = r.getValidFrom().truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1);
                if (x.isAfter(lastDate[0])) {
                    lastDate[0] = x;
                    bar.inc();
                }
                history.addRevision(k, r);
                searchEngine.putIndexEntry(r, k);
            });
        } catch (Exception ex) {
            LOGGER.error("Failed to load data: {}", ex.getLocalizedMessage(), ex);
        }
        finally
        {
            searchEngine.commit();
            loaderHealthIndicator.setFinishedLoading();
        }
        LOGGER.info("IP interval tree construction completed, {} entries", history.getTree().size());
    }

    @PostConstruct
    public void initialise()
    {
        dbLoader = new RipeDbLoader(jdbcOperations, -1L);
        executorService.execute(this::buildTree);
    }

    @Bean
    public LoaderHealthIndicator loaderHealthIndicator()
    {
        return loaderHealthIndicator;
    }

    @Scheduled(fixedRate = 15000L)
    public void refreshData()
    {
        if (asyncLoader.isDone()) {
            LOGGER.info("CRON triggered refresh begun");
            asyncLoader = executorService.submit(() -> {
                try {
                    dbLoader.loadWith((key, revision) ->
                    {
                        history.addRevision(key, revision);
                        searchEngine.putIndexEntry(revision, key);
                    });
                } catch (Exception ex) {
                    LOGGER.error("Error refreshing data: {}", ex.getLocalizedMessage(), ex);
                }
                finally
                {
                    searchEngine.commit();
                }
                return dbLoader.getLastSerial();
            });
        }
    }

    @Bean
    @ConditionalOnProperty(value="snapshot.file")
    Endpoint<Boolean> snapshotEndpoint(@Value("${snapshot.file}") String snapshotFile) {
        return new Endpoint<Boolean>() {
            @Override
            public String getId() {
                return "snapshot";
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public boolean isSensitive() {
                return true;
            }

            @Override
            public Boolean invoke() {
                try {
                    writeSnapshot(snapshotFile);
                } catch (IOException ioex) {
                    LOGGER.error("Could not write snapshot file", ioex);
                    return false;
                }
                return true;
            }
        };
    }

    private void writeSnapshot(String target) throws IOException
    {
        try (FileOutputStream fileOutput = new FileOutputStream(target);
             DeflaterOutputStream zipOutput = new DeflaterOutputStream(fileOutput);
             FSTObjectOutput objOutput = new FSTObjectOutput(zipOutput))
        {
            objOutput.writeLong(dbLoader.getLastSerial());
            objOutput.writeObject(history);
        }
    }
}
