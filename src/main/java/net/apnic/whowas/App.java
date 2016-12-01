package net.apnic.whowas;

import net.apnic.whowas.history.History;
import net.apnic.whowas.history.ObjectHistory;
import net.apnic.whowas.history.ObjectIndex;
import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.loaders.RipeDbLoader;
import net.apnic.whowas.types.IP;
import net.apnic.whowas.types.IpInterval;
import net.apnic.whowas.types.Tuple;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcOperations;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

@SpringBootApplication
public class App {
    private final static Logger LOGGER = LoggerFactory.getLogger(App.class);

    private final Executor executor = Executors.newSingleThreadExecutor();

    private final History history = new History();

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public IntervalTree<IP, ObjectHistory, IpInterval> ipListIntervalTree() {
        return new IntervalTree<IP, ObjectHistory, IpInterval>() {
            @Override
            public Optional<ObjectHistory> exact(IpInterval range) {
                return history.getTree().exact(range)
                        .flatMap(history::getObjectHistory);
            }

            @Override
            public Stream<Tuple<IpInterval, ObjectHistory>> intersecting(IpInterval range) {
                return history.getTree().intersecting(range)
                        .flatMap(p -> history
                                .getObjectHistory(p.snd())
                                .map(Stream::of)
                                .orElse(Stream.empty())
                                .map(h -> new Tuple<>(p.fst(), h)));
            }

            @Override
            public int size() {
                return history.getTree().size();
            }
        };
    }

    @Bean
    public ObjectIndex objectIndex() {
        return history::getObjectHistory;
    }

    @Value("${snapshot.file}")
    private String snapshotFile;

    @Autowired
    ApplicationContext context;

    @Autowired
    private JdbcOperations jdbcOperations;

    private RipeDbLoader dbLoader;

    @PostConstruct
    public void initialise() {
        dbLoader = new RipeDbLoader(jdbcOperations, -1L);
        executor.execute(this::buildTree);
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
            } catch (IOException | ClassNotFoundException ex) {
                LOGGER.error("Exception during load", ex);
            }
        }

        LOGGER.info("Loading history from database, starting at #{}", dbLoader.getLastSerial());
        try {
            dbLoader.loadWith(history::addRevision);
        } catch (Exception ex) {
            LOGGER.error("Failed to load data: {}", ex.getLocalizedMessage(), ex);
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

    private void writeSnapshot(String target) throws IOException {
        try (FileOutputStream fileOutput = new FileOutputStream(target);
             DeflaterOutputStream zipOutput = new DeflaterOutputStream(fileOutput);
             FSTObjectOutput objOutput = new FSTObjectOutput(zipOutput)) {
            objOutput.writeLong(dbLoader.getLastSerial());
            objOutput.writeObject(history);
        }
    }
}
