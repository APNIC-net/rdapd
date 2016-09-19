package net.apnic.whowas;

import net.apnic.whowas.history.History;
import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.intervaltree.avl.AvlTree;
import net.apnic.whowas.loaders.Loader;
import net.apnic.whowas.loaders.RipeDbLoader;
import net.apnic.whowas.types.IP;
import net.apnic.whowas.types.IpInterval;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcOperations;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

@SpringBootApplication
public class App {
    private final static Logger LOGGER = LoggerFactory.getLogger(App.class);

    private volatile IntervalTree<IP, History, IpInterval> tree = new AvlTree<>();

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public IntervalTree<IP, History, IpInterval> ipListIntervalTree() {
        return tree;
    }

    @Bean
    @ConditionalOnProperty(value="from.database")
    public Loader ripeLoader(JdbcOperations operations) {
        return new RipeDbLoader(operations);
    }

    @Bean
    @ConditionalOnProperty(value="from.snapshot")
    @SuppressWarnings("unchecked")
    public Loader fileLoader(@Value("${snapshot.file}") String snapshotFile) {
        return () -> {
            LOGGER.info("Restoring snapshot from file {}", snapshotFile);
            try (InputStream resourceStream = resourceLoader.getResource(snapshotFile).getInputStream();
                 InflaterInputStream zipStream = new InflaterInputStream(resourceStream);
                 FSTObjectInput objStream = new FSTObjectInput(zipStream)) {
                 return (AvlTree<IP, History, IpInterval>)objStream.readObject();
            } catch (IOException|ClassNotFoundException ex) {
                LOGGER.error("Invalid snapshot file");
                throw ex;
            }
        };
    }

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private Loader loader;

    @PostConstruct
    public void buildTree() {
        LOGGER.info("Loading history from configured sources");
        try {
            tree = loader.loadTree();
        } catch (Exception ex) {
            LOGGER.error("Failed to load data with {}", loader, ex);
            throw new RuntimeException(ex);
        }
    }

    private void writeSnapshot(String target) {
        try (FileOutputStream fileOutput = new FileOutputStream(target);
             DeflaterOutputStream zipOutput = new DeflaterOutputStream(fileOutput);
             FSTObjectOutput objOutput = new FSTObjectOutput(zipOutput)) {
            objOutput.writeObject(tree);
        } catch (IOException ioex) {
            LOGGER.error("Could not write snapshot file", ioex);
        }
    }
}
