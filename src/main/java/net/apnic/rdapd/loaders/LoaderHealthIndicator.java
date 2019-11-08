package net.apnic.rdapd.loaders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * {@link HealthIndicator} implementation for {@link LoaderStatusProvider}s.
 */
@Component
public class LoaderHealthIndicator implements HealthIndicator {

    private final LoaderStatusProvider loaderStatusProvider;

    @Autowired
    public LoaderHealthIndicator(LoaderStatusProvider loaderStatusProvider) {
        this.loaderStatusProvider = loaderStatusProvider;
    }

    @Override
    public Health health() {
        return Health.up().withDetail("Loader:", toJson(loaderStatusProvider.getLoaderStatus())).build();
    }

    private String toJson(LoaderStatus loaderStatus) {
        return "{" +
                "lastSuccessfulDateTime=" + (loaderStatus.getLastSuccessfulDateTime().isPresent()
                                                ? loaderStatus.getLastSuccessfulDateTime().get() : "never") +
                ", status=" + loaderStatus.getStatus() +
                '}';
    }
}
