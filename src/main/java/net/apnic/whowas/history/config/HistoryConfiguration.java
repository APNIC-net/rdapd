package net.apnic.whowas.history.config;

import java.util.Optional;
import java.util.stream.Stream;

import net.apnic.whowas.history.History;
import net.apnic.whowas.history.ObjectHistory;
import net.apnic.whowas.history.ObjectIndex;
import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.search.SearchEngine;
import net.apnic.whowas.types.IP;
import net.apnic.whowas.types.IpInterval;
import net.apnic.whowas.types.Tuple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HistoryConfiguration
{
    @Autowired
    @Bean
    public History history(SearchEngine searchEngine)
    {
        return new History(searchEngine);
    }

    @Autowired
    @Bean
    public IntervalTree<IP, ObjectHistory, IpInterval> ipListIntervalTree(History history)
    {
        return new IntervalTree<IP, ObjectHistory, IpInterval>()
        {
            @Override
            public Stream<Tuple<IpInterval, ObjectHistory>>
                equalToAndLeastSpecific(IpInterval range) {
                return history.getTree().equalToAndLeastSpecific(range)
                        .flatMap(p -> history
                                .historyForObject(p.second())
                                .map(Stream::of)
                                .orElse(Stream.empty())
                                .map(h -> new Tuple<>(p.first(), h)));
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
                                .historyForObject(p.second())
                                .map(Stream::of)
                                .orElse(Stream.empty())
                                .map(h -> new Tuple<>(p.first(), h)));
            }

            @Override
            public int size() {
                return history.getTree().size();
            }
        };
    }

    @Autowired
    @Bean
    public ObjectIndex objectIndex(History history)
    {
        return history;
    }
}
