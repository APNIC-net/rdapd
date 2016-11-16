package net.apnic.whowas;

import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.types.IP;
import net.apnic.whowas.types.IpInterval;
import net.apnic.whowas.types.Parsing;
import net.apnic.whowas.types.Tuple;
import net.apnic.whowas.history.ObjectHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class WebController {
    private final IntervalTree<IP, ObjectHistory, IpInterval> intervalTree;

    @Autowired
    WebController(IntervalTree<IP, ObjectHistory, IpInterval> intervalTree) {
        this.intervalTree = intervalTree;
    }

    @RequestMapping("/v4")
    @CrossOrigin(origins = "*")
    public List<ObjectHistory> v4(@RequestParam("range") IpInterval range) {
        int pfxCap = range.prefixSize() + 8;
        return intervalTree.intersecting(range).filter(t -> t.fst().prefixSize() <= pfxCap)
                .sorted(Comparator.comparing(Tuple::fst)).map(Tuple::snd).collect(Collectors.toList());
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
}
