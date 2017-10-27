package net.apnic.whowas.history.config;

import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.apnic.whowas.history.History;
import net.apnic.whowas.history.ObjectHistory;
import net.apnic.whowas.history.ObjectIndex;
import net.apnic.whowas.history.Revision;
import net.apnic.whowas.intervaltree.Interval;
import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.ip.IpService;
import net.apnic.whowas.types.IP;
import net.apnic.whowas.types.IpInterval;
import net.apnic.whowas.types.Tuple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HistoryConfiguration
{
    @Bean
    public History history()
    {
        return new History();
    }

    @Autowired
    @Bean
    public IntervalTree<IP, ObjectHistory, IpInterval> ipListIntervalTree(History history)
    {
        return lazyMap(history::getTree, objectKey -> history().historyForObject(objectKey).orElse(null));
    }

    @Autowired
    @Bean
    public ObjectIndex objectIndex(History history)
    {
        return history;
    }


    @Autowired
    @Bean
    public IpService ipService(History history) {
        final IntervalTree<IP, ObjectHistory, IpInterval> tree = lazyMap(
                history::getTree,
                objectKey -> history.historyForObject(objectKey).orElse(null)
        );

        final BinaryOperator<Tuple<IpInterval, ObjectHistory>> mostSpecific =
                (a, b) -> a.first().compareTo(b.first()) >= 0 ? b : a;

        return ipInterval -> tree.equalToAndLeastSpecific(ipInterval)
                .filter(t -> t.second().mostCurrent().isPresent())
                .reduce(mostSpecific)
                .flatMap(t -> t.second().mostCurrent())
                .map(Revision::getContents);
    }

    private <K extends Comparable<K>, V, V2, I extends Interval<K>> IntervalTree<K, V2, I> lazyMap(
            Supplier<IntervalTree<K, V, I>> treeSupplier, Function<V, V2> mapper) {
        return new IntervalTree<K, V2, I>()
        {
            @Override
            public Stream<Tuple<I, V2>>
            equalToAndLeastSpecific(I range) {
                return treeSupplier.get().equalToAndLeastSpecific(range)
                        .flatMap(tuple -> Optional.ofNullable(mapper.apply(tuple.second()))
                                .map(Stream::of)
                                .orElse(Stream.empty())
                                .map(v2 -> new Tuple<>(tuple.first(), v2)));
            }

            @Override
            public Optional<V2> exact(I range) {
                return treeSupplier.get().exact(range).map(mapper);
            }

            @Override
            public Stream<Tuple<I, V2>> intersecting(I range) {
                return treeSupplier.get().intersecting(range)
                        .flatMap(tuple -> Optional.ofNullable(mapper.apply(tuple.second()))
                                .map(Stream::of)
                                .orElse(Stream.empty())
                                .map(v2 -> new Tuple<>(tuple.first(), v2)));
            }

            @Override
            public int size() {
                return treeSupplier.get().size();
            }
        };
    }
}
