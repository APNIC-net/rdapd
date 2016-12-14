package net.apnic.whowas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.apnic.whowas.rdap.RdapHistory;
import net.apnic.whowas.rdap.RdapObjectMapper;
import net.apnic.whowas.rdap.TopLevelObject;
import net.apnic.whowas.history.*;
import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.types.IP;
import net.apnic.whowas.types.IpInterval;
import net.apnic.whowas.types.Parsing;
import net.apnic.whowas.types.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Comparator;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
public class WebController {
    private final IntervalTree<IP, ObjectHistory, IpInterval> intervalTree;
    private final ObjectIndex objectIndex;

    @Autowired
    WebController(IntervalTree<IP, ObjectHistory, IpInterval> intervalTree, ObjectIndex objectIndex) {
        this.intervalTree = intervalTree;
        this.objectIndex = objectIndex;
    }

    @RequestMapping("/history/ip/**")
    public TopLevelObject ipHistory(HttpServletRequest request) {
        String param = (String)request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        IpInterval range = Parsing.parseInterval(param.substring(12));

        int pfxCap = range.prefixSize() + (range.low().getAddressFamily() == IP.AddressFamily.IPv4 ? 8 : 16);
        return TopLevelObject.of(
                new RdapHistory(intervalTree
                        .intersecting(range)
                        .filter(t -> t.fst().prefixSize() <= pfxCap)
                        .sorted(Comparator.comparing(Tuple::fst))
                        .map(Tuple::snd)
                        .collect(Collectors.toList())));
    }

    @RequestMapping("/history/autnum/{handle}")
    public ResponseEntity<TopLevelObject> autnumHistory(@PathVariable("handle") String handle) {
        return historyOf(new ObjectKey(ObjectClass.AUT_NUM, handle));
    }

    @RequestMapping("/history/entity/{handle}")
    public ResponseEntity<TopLevelObject> entityHistory(@PathVariable("handle") String handle) {
        return historyOf(new ObjectKey(ObjectClass.ENTITY, handle));
    }

    @RequestMapping("/history/domain/{handle:.+}")
    public ResponseEntity<TopLevelObject> domainHistory(@PathVariable("handle") String handle) {
        return historyOf(new ObjectKey(ObjectClass.DOMAIN, handle));
    }

    private ResponseEntity<TopLevelObject> historyOf(ObjectKey objectKey) {
        return objectIndex.historyForObject(objectKey)
                .map(RdapHistory::new)
                .map(TopLevelObject::of)
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
    public ResponseEntity<TopLevelObject> autnum(@PathVariable("handle") String handle) {
        return mostRecent(new ObjectKey(ObjectClass.AUT_NUM, handle));
    }

    @RequestMapping("/domain/{handle:.+}")
    public ResponseEntity<TopLevelObject> domain(@PathVariable("handle") String handle) {
        return mostRecent(new ObjectKey(ObjectClass.DOMAIN, handle));
    }

    @RequestMapping("/entity/{handle}")
    public ResponseEntity<TopLevelObject> entity(@PathVariable("handle") String handle) {
        return mostRecent(new ObjectKey(ObjectClass.ENTITY, handle));
    }

    private ResponseEntity<TopLevelObject> mostRecent(ObjectKey objectKey) {
        return objectIndex.historyForObject(objectKey)
                .flatMap(ObjectHistory::mostRecent)
                .map(Revision::getContents)
                .map(TopLevelObject::of)
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
}
