package net.apnic.whowas;

import net.apnic.whowas.history.History;
import net.apnic.whowas.intervaltree.IntervalTree;
import net.apnic.whowas.intervaltree.avl.AVL;
import net.apnic.whowas.loaders.RipeDbLoader;
import net.apnic.whowas.types.IP;
import net.apnic.whowas.types.IpInterval;
import net.apnic.whowas.types.Parsing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcOperations;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class App {
    private final AVL<IP, History, IpInterval> tree = new AVL<>();

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public IntervalTree<IP, History, IpInterval> ipListIntervalTree() {
        return tree;
    }

    @Autowired
    private JdbcOperations operations;

    @PostConstruct
    public void buildTree() {
        RipeDbLoader loader = new RipeDbLoader(operations);
        loader.loadInto(r -> tree.insert(Parsing.parseInterval(r.getPrimaryKey()), r));
    }

}
