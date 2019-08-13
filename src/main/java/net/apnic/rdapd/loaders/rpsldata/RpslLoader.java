package net.apnic.rdapd.loaders.rpsldata;

import net.apnic.rdapd.history.History;
import net.apnic.rdapd.history.ObjectClass;
import net.apnic.rdapd.history.ObjectKey;
import net.apnic.rdapd.history.Revision;
import net.apnic.rdapd.rdap.RdapObject;
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
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EnableScheduling
@Profile("rpsl-data")
@Component
public class RpslLoader {
    private final static Logger LOGGER = LoggerFactory.getLogger(RpslLoader.class);
    private static final Pattern RPSL_OBJECT_PATTERN = Pattern.compile("((.+)\n)+", Pattern.MULTILINE);
    private static final Pattern FIRST_ATTR_PATTERN = Pattern.compile("((\\w|-)*):");
    private static final Pattern RPSL_COMMENT_PATTERN = Pattern.compile("^#(.)*\n", Pattern.MULTILINE);
    private static final Map<String, ObjectClass> ATTR_TO_CLASS_MAP;
    private static final Map<ObjectClass, Pattern> OBJ_CLASS_TO_KEY_PATTERN;

    private final History history;
    private final RpslConfig rpslConfig;
    private String previousProcessedFileMD5;

    @Autowired
    public RpslLoader(History history, RpslConfig rpslConfig) {
        this.history = history;
        this.rpslConfig = rpslConfig;
    }

    @PostConstruct
    void initialise() {
        updateHistory();
    }

    @Scheduled(cron = "${rpslData.updateCronExpr}")
    void periodicUpdate() {
        updateHistory();
    }

    private synchronized void updateHistory() {
        LOGGER.info("Initialising RPSL data update.");
        final History newHistory;
        final String md5;
        File localFile = null;

        try {
            // We could not use Apache Commons VSF to retrieve the file from FTP, that would be ideal since it would
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
                LOGGER.error("Error retrieving RPSL file from FTP.", e);
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
            throw (e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
        } finally {
            if (localFile != null && localFile.exists()) {
                boolean deletionSucceded = localFile.delete();

                if (!deletionSucceded) {
                    LOGGER.warn("Error deleting temporary file: " + localFile.getPath());
                }
            }
        }

        previousProcessedFileMD5 = md5;
        history.overwriteHistory(newHistory);
        LOGGER.info("RPSL data updated.");
    }

    private void loadFile(FileObject dbFileObj, History newHistory) {
        LOGGER.info("Loading file {}", dbFileObj.getName());
        try {
            Scanner scanner = new Scanner(dbFileObj.getContent().getInputStream(), Charsets.UTF_8.toString());
            String match;

            while ((match = scanner.findWithinHorizon(RPSL_OBJECT_PATTERN, 0) ) != null) {
                String sanitisedMatch = RPSL_COMMENT_PATTERN.matcher(match).replaceAll("");

                if (sanitisedMatch.isEmpty()) {
                    continue;
                }

                Matcher firstAttributeMatcher = FIRST_ATTR_PATTERN.matcher(sanitisedMatch);

                if (!firstAttributeMatcher.find()) {
                    LOGGER.error("Can't find first attribute for RPSL object:\n{}", match);
                    throw new RuntimeException("Error loading RPSL data.");
                }

                ObjectClass objectClass = ATTR_TO_CLASS_MAP.get(firstAttributeMatcher.group(1));

                if (objectClass == null) {
                    System.out.println("here");
                }
                Matcher keyMatcher = OBJ_CLASS_TO_KEY_PATTERN.get(objectClass).matcher(sanitisedMatch);

                if (!keyMatcher.find()) {
                    LOGGER.error("Can't find object key for RPSL object:\n{}", match);
                    throw new RuntimeException("Error loading RPSL data.");
                }

                ObjectKey key = new ObjectKey(objectClass, keyMatcher.group(1));
                RdapObject rdapObject;

                try {
                    rdapObject = RpslToRdap.rpslToRdap(key, sanitisedMatch.getBytes(Charsets.UTF_8));
                } catch (RuntimeException ex) {  // unfortunately this is what is thrown by the method
                    if (ex.getCause() instanceof ParserException) {
                        LOGGER.warn("Error parsing RPSL object:" + key.toString()
                                + ". This object will be ignored.", ex);
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
        ATTR_TO_CLASS_MAP.put("aut-num", ObjectClass.AUT_NUM);
        ATTR_TO_CLASS_MAP.put("inetnum", ObjectClass.IP_NETWORK);
        ATTR_TO_CLASS_MAP.put("inet6num", ObjectClass.IP_NETWORK);

        OBJ_CLASS_TO_KEY_PATTERN = new HashMap<>();
        OBJ_CLASS_TO_KEY_PATTERN.put(ObjectClass.ENTITY, Pattern.compile("(?:^|\n)(?:nic-hdl:(?:\\s)+)(.*)(?:\n)"));
        OBJ_CLASS_TO_KEY_PATTERN.put(ObjectClass.AUT_NUM, Pattern.compile("^(?:aut-num:(?:\\s)+)(.*)(?:\n)"));
        OBJ_CLASS_TO_KEY_PATTERN.put(ObjectClass.IP_NETWORK, Pattern.compile("^(?:inet(?:6)?num:(?:\\s)+)(.*)(?:\n)"));
    }
}
