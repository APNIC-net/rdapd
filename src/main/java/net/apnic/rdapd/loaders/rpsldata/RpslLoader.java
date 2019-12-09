package net.apnic.rdapd.loaders.rpsldata;

import net.apnic.rdapd.history.History;
import net.apnic.rdapd.history.ObjectClass;
import net.apnic.rdapd.history.ObjectKey;
import net.apnic.rdapd.history.Revision;
import net.apnic.rdapd.loaders.LoaderStatus;
import net.apnic.rdapd.loaders.LoaderStatusProvider;
import net.apnic.rdapd.rdap.RdapObject;
import net.apnic.rdapd.rpsl.RpslObject;
import net.apnic.rdapd.rpsl.rdap.RpslToRdap;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.*;
import org.codehaus.jparsec.error.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Pattern;

import static net.apnic.rdapd.loaders.LoaderStatus.Status.*;

/**
 * Loads RPSl data from a file. It provided an alternative to the database loader
 * ({@link net.apnic.rdapd.loaders.ripedb.RipeDbLoader}) with the downside of not supporting domains.
 */
@EnableScheduling
@Profile("rpsl-data")
@Component
public class RpslLoader implements LoaderStatusProvider {
    private final static Logger LOGGER = LoggerFactory.getLogger(RpslLoader.class);
    private static final Pattern RPSL_OBJECT_PATTERN = Pattern.compile("(^(.+)\\n)*(^\\w.+\\n)(^(.+)\\n)*", Pattern.MULTILINE);
    private static final Map<String, ObjectClass> ATTR_TO_CLASS_MAP;

    private final History history;
    private final RpslConfig rpslConfig;
    private String previousProcessedFileMD5;
    private LoaderStatus loaderStatus;

    @Autowired
    public RpslLoader(History history, RpslConfig rpslConfig) {
        this.history = history;
        this.rpslConfig = rpslConfig;
        this.loaderStatus = new LoaderStatus(INITIALISING, Optional.empty());
    }

    @PostConstruct
    void initialise() {
        updateHistory();
    }

    @Scheduled(cron = "${rpslData.updateCronExpr}")
    void periodicUpdate() {
        updateHistory();
    }

    public LoaderStatus getLoaderStatus() {
        return loaderStatus;
    }

    private synchronized void updateHistory() {
        LOGGER.info("Initialising RPSL data update.");
        final History newHistory;
        final String md5;
        File localFile = null;

        try {
            // We could not use Apache Commons VFS to retrieve the file from FTP, that would be ideal since it would
            // allow the URI to be completely protocol agnostic including the compressing method.
            try {
                LOGGER.info("Processing file: {}", rpslConfig.getUri());
                localFile = File.createTempFile("rpsl", null);
                URL url = new URL (rpslConfig.getUri());
                URLConnection urlc = url.openConnection();
                InputStream is = urlc.getInputStream();
                FileOutputStream fos = new FileOutputStream(localFile);
                IOUtils.copy(is, fos);

                try (InputStream localFileIS = new FileInputStream(localFile)) {
                    md5 = DigestUtils.md5Hex(localFileIS);
                }
            } catch (IOException e) {
                LOGGER.error("Error retrieving RPSL file.", e);
                throw new RuntimeException(e);
            }

            if (md5.equals(previousProcessedFileMD5)) {
                LOGGER.info("RPSL file retrieved has same contents as the last processed. Update postponed.");
                return;
            }

            newHistory = new History();
            FileSystemManager fsManager = VFS.getManager();
            FileObject fileObject = fsManager.resolveFile(getSchemeForFilename(rpslConfig.getUri()) + ":" +
                                                          localFile.getPath());
            FileObject[] dbFiles = fileObject.findFiles(new FileExtensionSelector("db"));

            if (dbFiles.length > 0) {
                Arrays.stream(dbFiles).forEach(dbFile -> loadFile(dbFile, newHistory));
            } else {
                loadFile(fileObject, newHistory);
            }
        } catch (FileSystemException | RuntimeException e) {
            LOGGER.error("Error loading RPSL data.", e);
            Optional<LocalDateTime> previousSuccessfulLocalDateTime = loaderStatus.getLastSuccessfulDateTime();
            LoaderStatus.Status lastStatus = loaderStatus.getStatus();
            loaderStatus = new LoaderStatus(
                    lastStatus.equals(INITIALISING) ? INITIALISATION_FAILED : OUT_OF_DATE,
                    previousSuccessfulLocalDateTime);
            throw (e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
        } finally {
            if (localFile != null && localFile.exists()) {
                boolean deletionSucceeded = localFile.delete();

                if (!deletionSucceeded) {
                    LOGGER.warn("Error deleting temporary file: " + localFile.getPath());
                }
            }
        }

        previousProcessedFileMD5 = md5;
        history.overwriteHistory(newHistory);
        loaderStatus = new LoaderStatus(UP_TO_DATE, Optional.of(LocalDateTime.now()));
        LOGGER.info("RPSL data updated.");
    }

    private void loadFile(FileObject dbFileObj, History newHistory) {
        LOGGER.info("Loading file {}", dbFileObj.getName());
        try {
            Scanner scanner = new Scanner(dbFileObj.getContent().getInputStream(), Charsets.UTF_8.toString());
            String match;
            ObjectKey key;
            RdapObject rdapObject;

            while ((match = scanner.findWithinHorizon(RPSL_OBJECT_PATTERN, 0) ) != null) {
                if (match.isEmpty()) {
                    continue;
                }

                try {
                    RpslObject rpslObject = new RpslObject(match);
                    String primaryAttributeKey = rpslObject.getPrimaryAttribute().first();
                    ObjectClass objectClass = ATTR_TO_CLASS_MAP.get(primaryAttributeKey);

                    if (objectClass == null) {
                        LOGGER.warn("Object class for RPSL object not supported. this Object will be ignored:\n" +
                                match);
                        continue;
                    }

                    String objectName = getObjectName(rpslObject, primaryAttributeKey).orElseThrow(
                            () -> new IllegalArgumentException("Error retrieving object name."));
                    key = new ObjectKey(objectClass, objectName);
                    rdapObject = RpslToRdap.rpslToRdap(key, rpslObject);
                } catch (RuntimeException ex) {  // unfortunately this is what is thrown by the parser method
                    if (ex.getCause() instanceof ParserException) {
                        LOGGER.warn("Error parsing RPSL object:\n" + match + "\n\nThis object will be ignored.", ex);
                        continue;
                    } else {
                        throw ex;  // rethrow
                    }
                }

                Revision revision = new Revision(ZonedDateTime.now(), null, rdapObject);
                newHistory.addRevision(key, revision);
            }
        } catch (FileSystemException e) {
            throw new RuntimeException(e);  // to be logged by the invoker above
        }
    }

    private Optional<String> getObjectName(RpslObject rpslObject, String primaryAttributeKey) {
        switch (primaryAttributeKey) {
            case "role":
            case "person": return rpslObject.getAttributeFirstValue("nic-hdl");
            case "irt": return rpslObject.getAttributeFirstValue("irt");
            case "inetnum": return rpslObject.getAttributeFirstValue("inetnum");
            case "inet6num": return rpslObject.getAttributeFirstValue("inet6num");
            case "aut-num": return rpslObject.getAttributeFirstValue("aut-num");
            default: throw new IllegalArgumentException("Attribute key not supported: " + primaryAttributeKey);
        }
    }

    private String getSchemeForFilename(String fileName) {
        if (fileName.endsWith(".tar.gz")) {
            return "tgz";
        } else if (fileName.endsWith(".gz")) {
            return "gz";
        } else if (fileName.endsWith(".db")) {
            return "file";
        } else {
            throw new IllegalArgumentException("Can't find scheme for file: " + fileName);
        }
    }

    static {
        ATTR_TO_CLASS_MAP = new HashMap<>();
        ATTR_TO_CLASS_MAP.put("person", ObjectClass.ENTITY);
        ATTR_TO_CLASS_MAP.put("role", ObjectClass.ENTITY);
        ATTR_TO_CLASS_MAP.put("irt", ObjectClass.ENTITY);
        ATTR_TO_CLASS_MAP.put("aut-num", ObjectClass.AUT_NUM);
        ATTR_TO_CLASS_MAP.put("inetnum", ObjectClass.IP_NETWORK);
        ATTR_TO_CLASS_MAP.put("inet6num", ObjectClass.IP_NETWORK);
    }
}