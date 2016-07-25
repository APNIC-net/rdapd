package net.apnic.whowas.loader;

import net.apnic.whowas.loader.IntervalTree.IntervalTree;
import net.apnic.whowas.loader.Types.IP;
import net.apnic.whowas.loader.Types.IpInterval;
import net.apnic.whowas.loader.Types.Parsing;
import net.apnic.whowas.loader.Types.Tuple;
import net.apnic.whowas.loader.history.History;
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
    private final IntervalTree<IP, History, IpInterval> intervalTree;

    @Autowired
    WebController(IntervalTree<IP, History, IpInterval> intervalTree) {
        this.intervalTree = intervalTree;
    }

    @RequestMapping("/v4")
    @CrossOrigin(origins = "*")
    public List<History> v4(@RequestParam("range") IpInterval range) {
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
