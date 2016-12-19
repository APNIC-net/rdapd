package net.apnic.whowas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.apnic.whowas.history.*;
import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.rdap.*;
import net.apnic.whowas.types.IP;
import net.apnic.whowas.types.IpInterval;
import net.apnic.whowas.types.Parsing;
import net.apnic.whowas.types.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@EnableConfigurationProperties({ WebController.ServerNotices.class })
public class WebController {
    private final static Logger LOGGER = LoggerFactory.getLogger(WebController.class);

    private final IntervalTree<IP, ObjectHistory, IpInterval> intervalTree;
    private final ObjectIndex objectIndex;
    private final BiFunction<Object, HttpServletRequest, TopLevelObject> makeResponse;

    @Autowired
    WebController(IntervalTree<IP, ObjectHistory, IpInterval> intervalTree, ObjectIndex objectIndex, BiFunction<Object, HttpServletRequest, TopLevelObject> responseMaker) {
        this.intervalTree = intervalTree;
        this.objectIndex = objectIndex;
        this.makeResponse = responseMaker;
    }

    @RequestMapping("/history/ip/**")
    public TopLevelObject ipHistory(HttpServletRequest request) {
        String param = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        LOGGER.info("IP history query for {}", param);
        IpInterval range = Parsing.parseInterval(param.substring(12));

        int pfxCap = range.prefixSize() + (range.low().getAddressFamily() == IP.AddressFamily.IPv4 ? 8 : 16);
        return makeResponse.apply(
                new RdapHistory(intervalTree
                        .intersecting(range)
                        .filter(t -> t.fst().prefixSize() <= pfxCap)
                        .sorted(Comparator.comparing(Tuple::fst))
                        .map(Tuple::snd)
                        .collect(Collectors.toList())),
                request);
    }

    @RequestMapping("/history/autnum/{handle}")
    public ResponseEntity<TopLevelObject> autnumHistory(HttpServletRequest request, @PathVariable("handle") String handle) {
        LOGGER.info("AutNum history query for {}", handle);
        return historyOf(request, new ObjectKey(ObjectClass.AUT_NUM, handle));
    }

    @RequestMapping("/history/entity/{handle}")
    public ResponseEntity<TopLevelObject> entityHistory(HttpServletRequest request, @PathVariable("handle") String handle) {
        LOGGER.info("Entity history query for {}", handle);
        return historyOf(request, new ObjectKey(ObjectClass.ENTITY, handle));
    }

    @RequestMapping("/history/domain/{handle:.+}")
    public ResponseEntity<TopLevelObject> domainHistory(HttpServletRequest request, @PathVariable("handle") String handle) {
        LOGGER.info("Domain history query for {}", handle);
        return historyOf(request, new ObjectKey(ObjectClass.DOMAIN, handle));
    }

    private ResponseEntity<TopLevelObject> historyOf(HttpServletRequest request, ObjectKey objectKey) {
        return objectIndex.historyForObject(objectKey)
                .map(RdapHistory::new)
                .map(h -> makeResponse.apply(h, request))
                .map(r -> new ResponseEntity<>(r, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping("/ip/**")
    public ResponseEntity<TopLevelObject> ip(HttpServletRequest request) {
//        String param = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
//        IpInterval range = Parsing.parseInterval(param.substring(4));

        // TODO: closest match
        //intervalTree.closest(range)
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping("/autnum/{handle}")
    public ResponseEntity<TopLevelObject> autnum(HttpServletRequest request, @PathVariable("handle") String handle) {
        return mostRecent(request, new ObjectKey(ObjectClass.AUT_NUM, handle));
    }

    @RequestMapping("/domain/{handle:.+}")
    public ResponseEntity<TopLevelObject> domain(HttpServletRequest request, @PathVariable("handle") String handle) {
        return mostRecent(request, new ObjectKey(ObjectClass.DOMAIN, handle));
    }

    @RequestMapping("/entity/{handle}")
    public ResponseEntity<TopLevelObject> entity(HttpServletRequest request, @PathVariable("handle") String handle) {
        return mostRecent(request, new ObjectKey(ObjectClass.ENTITY, handle));
    }

    private ResponseEntity<TopLevelObject> mostRecent(HttpServletRequest request, ObjectKey objectKey) {
        return objectIndex.historyForObject(objectKey)
                .flatMap(ObjectHistory::mostRecent)
                .map(Revision::getContents)
                .map(o -> makeResponse.apply(o, request))
                .map(r -> new ResponseEntity<>(r, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Bean
    public Converter<String, IpInterval> convertIpInterval() {
        return new Converter<String, IpInterval>() {
            @Override
            public IpInterval convert(String source) {
                return Parsing.parseInterval(source);
            }
        };
    }

    @Primary
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new RdapObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        return objectMapper;
    }

    @Bean
    public static BiFunction<Object, HttpServletRequest, TopLevelObject> responseMaker(ServerNotices serverNotices) {
        return (o, r) -> {
            String url = serverNotices.baseUrl == null
                    ? r.getRequestURL().toString()
                    : serverNotices.baseUrl + r.getRequestURI();
            List<Link> link = serverNotices.getCopyrightUrl() == null
                    ? null
                    : Collections.singletonList(new Link(url, "terms-of-service", serverNotices.getCopyrightUrl(), "text/html"));
            Notice notice = serverNotices.getTerms() == null
                    ? null
                    : new Notice("Terms and Conditions", Collections.singletonList(serverNotices.getTerms()), link);
            return TopLevelObject.of(o, notice);
        };
    }

    @ConfigurationProperties("rdap.server")
    static class ServerNotices {
        private String terms;
        private String copyrightUrl;
        private String baseUrl;

        public String getTerms() {
            return terms;
        }

        public void setTerms(String terms) {
            this.terms = terms;
        }

        public String getCopyrightUrl() {
            return copyrightUrl;
        }

        public void setCopyrightUrl(String copyrightUrl) {
            this.copyrightUrl = copyrightUrl;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
