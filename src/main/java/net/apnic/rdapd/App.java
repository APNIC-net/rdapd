package net.apnic.rdapd;

import java.util.Properties;

import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnablePrometheusEndpoint
public class App {

    public static void main(String[] args)
    {
        SpringApplication app = new SpringApplication(App.class);
        Properties defaultProps = new Properties();

        defaultProps.setProperty(
                "spring.mvc.throw-exception-if-no-handler-found", "true");
        defaultProps.setProperty("spring.resources.add-mappings", "false");
        defaultProps.setProperty("spring.mvc.favicon.enabled", "false");
        defaultProps.setProperty("management.add-application-context-header", "false");
        app.setDefaultProperties(defaultProps);
        app.run(args);
        // initialise prometheus hotspot metrics
        DefaultExports.initialize();
    }
}
