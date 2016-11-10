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
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcOperations;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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
    @Order(value=2)
    @ConditionalOnProperty(value="spring.datasource.url")
    public Loader ripeLoader(JdbcOperations operations) {
        return new RipeDbLoader(operations);
    }

    @Bean
    @Order(value=1)
    @ConditionalOnProperty(value="snapshot.file")
    @SuppressWarnings("unchecked")
    public Loader fileLoader(@Value("${snapshot.file}") String snapshotFile, ApplicationContext context) {
        return () -> {
            LOGGER.info("Restoring snapshot from file {}", snapshotFile);
            try (InputStream resourceStream = context.getResource("file:///" + snapshotFile).getInputStream();
                 InflaterInputStream zipStream = new InflaterInputStream(resourceStream);
                 FSTObjectInput objStream = new FSTObjectInput(zipStream)) {
                 return (AvlTree<IP, History, IpInterval>)objStream.readObject();
            }
        };
    }

    @Autowired(required=false)
    private List<Loader> loaders;

    @PostConstruct
    public void buildTree() {
        LOGGER.info("Loading history from configured sources");

        // Try loaders in order until one succeeds
        for (Loader loader : loaders) {
            LOGGER.info("Invoking loader: {}", loader);
            try {
                tree = loader.loadTree();
                break;
            } catch (Exception ex) {
                LOGGER.error("Failed to load data with {}: {}", loader, ex.getLocalizedMessage(), ex);
            }
        }
        LOGGER.info("Tree construction completed, {} entries", tree.size());
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
            objOutput.writeObject(tree);
        }
    }
}
