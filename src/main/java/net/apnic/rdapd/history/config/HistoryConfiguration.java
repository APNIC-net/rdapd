package net.apnic.rdapd.history.config;

import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.apnic.rdapd.autnum.ASN;
import net.apnic.rdapd.autnum.ASNInterval;
import net.apnic.rdapd.autnum.AutNumSearchService;
import net.apnic.rdapd.history.History;
import net.apnic.rdapd.history.ObjectHistory;
import net.apnic.rdapd.history.ObjectIndex;
import net.apnic.rdapd.history.Revision;
import net.apnic.rdapd.intervaltree.Interval;
import net.apnic.rdapd.intervaltree.IntervalTree;
import net.apnic.rdapd.ip.IpService;
import net.apnic.rdapd.rdap.AutNum;
import net.apnic.rdapd.rdap.IpNetwork;
import net.apnic.rdapd.types.IP;
import net.apnic.rdapd.types.IpInterval;
import net.apnic.rdapd.types.Tuple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration(exclude =  {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class HistoryConfiguration
{
    @Bean
    public History history()
    {
        return new History();
    }

    @Autowired
    @Bean
    public AutNumSearchService autnumSearchService(History history) {
        final IntervalTree<ASN, ObjectHistory, ASNInterval> tree = lazyMap(
            history::getAutNumTree,
            objectKey -> history.historyForObject(objectKey).orElse(null)
        );

        final BinaryOperator<Tuple<ASNInterval, ObjectHistory>> mostSpecific =
                (a, b) -> a.first().compareTo(b.first()) <= 0 ? b : a;

        return new AutNumSearchService()
        {
            @Override
            public Optional<AutNum> findCurrent(ASN asn)
            {
                return tree.equalToAndLeastSpecific(new ASNInterval(asn, asn))
                    .filter(t -> t.second().mostCurrent().isPresent())
                    .reduce(mostSpecific)
                    .flatMap(t -> t.second().mostCurrent())
                    .map(Revision::getContents)
                    .map(rdapObject -> (AutNum)rdapObject);
            }

            @Override
            public Optional<ObjectHistory> findHistory(ASN asn)
            {
                return tree.equalToAndLeastSpecific(new ASNInterval(asn, asn))
                    .reduce(mostSpecific)
                    .flatMap(t -> Optional.ofNullable(t.second()));
            }
        };
    }

    @Autowired
    @Bean
    public IntervalTree<IP, ObjectHistory, IpInterval> ipListIntervalTree(History history)
    {
        return lazyMap(history::getIPNetworkTree, objectKey -> history().historyForObject(objectKey).orElse(null));
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
                history::getIPNetworkTree,
                objectKey -> history.historyForObject(objectKey).orElse(null)
        );

        final BinaryOperator<Tuple<IpInterval, ObjectHistory>> mostSpecific =
                (a, b) -> a.first().compareTo(b.first()) <= 0 ? b : a;

        return ipInterval -> tree.equalToAndLeastSpecific(ipInterval)
                .filter(t -> t.second().mostCurrent().isPresent())
                .reduce(mostSpecific)
                .flatMap(t -> t.second().mostCurrent())
                .map(Revision::getContents)
                .map(rdapObject -> (IpNetwork) rdapObject);
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
