package net.apnic.whowas;

import net.apnic.whowas.history.History;
//import net.apnic.whowas.history.ObjectHistory;
//import net.apnic.whowas.history.ObjectIndex;
//import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.loaders.RipeDbLoader;
import net.apnic.whowas.progress.Bar;
//import net.apnic.whowas.types.IP;
//import net.apnic.whowas.types.IpInterval;
//import net.apnic.whowas.types.Tuple;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
//import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Properties;
//import java.util.stream.Stream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

@SpringBootApplication
@EnableScheduling
public class App {
    private final static Logger LOGGER = LoggerFactory.getLogger(App.class);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Autowired
    private History history = null;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(App.class);
        Properties defaultProps = new Properties();

        defaultProps.setProperty(
                "spring.mvc.throw-exception-if-no-handler-found", "true");
        defaultProps.setProperty("spring.resources.add-mappings", "false");
        defaultProps.setProperty("spring.mvc.favicon.enabled", "false");
        defaultProps.setProperty("management.add-application-context-header", "false");
        app.setDefaultProperties(defaultProps);
        app.run(args);
    }

    /*@Bean
    public IntervalTree<IP, ObjectHistory, IpInterval> ipListIntervalTree() {
        return new IntervalTree<IP, ObjectHistory, IpInterval>() {
            @Override
            public Stream<Tuple<IpInterval, ObjectHistory>>
                equalToAndLeastSpecific(IpInterval range) {
                return history.getTree().equalToAndLeastSpecific(range)
                        .flatMap(p -> history
                                .historyForObject(p.snd())
                                .map(Stream::of)
                                .orElse(Stream.empty())
                                .map(h -> new Tuple<>(p.fst(), h)));
            }

            @Override
            public Optional<ObjectHistory> exact(IpInterval range) {
                return history.getTree().exact(range)
                        .flatMap(history::historyForObject);
            }

            @Override
            public Stream<Tuple<IpInterval, ObjectHistory>> intersecting(IpInterval range) {
                return history.getTree().intersecting(range)
                        .flatMap(p -> history
                                .historyForObject(p.snd())
                                .map(Stream::of)
                                .orElse(Stream.empty())
                                .map(h -> new Tuple<>(p.fst(), h)));
            }

            @Override
            public int size() {
                return history.getTree().size();
            }
        };
    }*/

 /*   @Bean
    public ObjectIndex objectIndex() {
        return history;
    }*/

    @Bean
    public TaskExecutor taskScheduler() {
        return new SimpleAsyncTaskExecutor();
    }

    @Value("${snapshot.file:#{null}}")
    private String snapshotFile;

    @Autowired
    ApplicationContext context;

    @Autowired
    private JdbcOperations jdbcOperations;

    private RipeDbLoader dbLoader;
    private Exception lastDbException = null;

    @PostConstruct
    public void initialise() {
        dbLoader = new RipeDbLoader(jdbcOperations, -1L);
        executorService.execute(this::buildTree);
    }

    private void buildTree() {
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

        LOGGER.info("Loading history from database, starting at #{}", dbLoader.getLastSerial());
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
            });
        } catch (Exception ex) {
            LOGGER.error("Failed to load data: {}", ex.getLocalizedMessage(), ex);
            lastDbException = ex;
        }
        finally
        {
            history.commit();
        }
        LOGGER.info("IP interval tree construction completed, {} entries", history.getTree().size());
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

    private Future<Long> asyncLoader = CompletableFuture.completedFuture(-1L);

    /* Every 15 seconds, refresh the database */
    @Scheduled(fixedRate = 15000L)
    public void refreshData() {
        if (asyncLoader.isDone()) {
            LOGGER.debug("CRON triggered refresh begun");
            asyncLoader = executorService.submit(() -> {
                try {
                    dbLoader.loadWith(history::addRevision);
                    lastDbException = null;
                } catch (Exception ex) {
                    LOGGER.error("Error refreshing data: {}", ex.getLocalizedMessage(), ex);
                    lastDbException = ex;
                }
                finally
                {
                    history.commit();
                }
                return dbLoader.getLastSerial();
            });
        }
    }

    @Bean
    HealthIndicator loaderHealthIndicator() {
        return () -> {
            if (lastDbException == null) {
                return Health.up()
                        .withDetail("lastSerial", dbLoader.getLastSerial())
                        .build();
            } else {
                return Health.down(lastDbException).build();
            }
        };
    }

    private void writeSnapshot(String target) throws IOException {
        try (FileOutputStream fileOutput = new FileOutputStream(target);
             DeflaterOutputStream zipOutput = new DeflaterOutputStream(fileOutput);
             FSTObjectOutput objOutput = new FSTObjectOutput(zipOutput)) {
            objOutput.writeLong(dbLoader.getLastSerial());
            objOutput.writeObject(history);
        }
    }
}
