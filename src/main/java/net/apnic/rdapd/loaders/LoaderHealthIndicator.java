package net.apnic.rdapd.loaders;

import io.prometheus.client.Gauge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * {@link HealthIndicator} implementation for {@link LoaderStatusProvider}s.
 */
@Component
public class LoaderHealthIndicator implements HealthIndicator {

    private static final Gauge PROMETHEUS_LOADER_STATUS =
            Gauge.build()
                    .name("rdapd_loader_status")
                    .help("RDAP loader status (" + getLoaderStatusesString() + ")")
                    .register();

    private final LoaderStatusProvider loaderStatusProvider;

    @Autowired
    public LoaderHealthIndicator(LoaderStatusProvider loaderStatusProvider) {
        this.loaderStatusProvider = loaderStatusProvider;
        updatePrometheusStatus();
    }

    @Override
    public Health health() {
        LoaderStatus loaderStatus = loaderStatusProvider.getLoaderStatus();
        return new Health.Builder()
                .status(loaderStatus.getStatus().toString())
                .withDetail("lastSuccessfulDateTime", (loaderStatus.getLastSuccessfulDateTime().isPresent()
                        ? DateTimeFormatter.ISO_DATE_TIME.format(loaderStatus.getLastSuccessfulDateTime().get())
                        : "never"))
                .build();
    }

    @Scheduled(fixedRate = 60 * 1000 /* 1 min */)
    public void updatePrometheusStatus() {
        PROMETHEUS_LOADER_STATUS.set(loaderStatusProvider.getLoaderStatus().getStatus().ordinal());
    }

    private static String getLoaderStatusesString() {
        LoaderStatus.Status[] statuses = LoaderStatus.Status.values();
        String[] strings = new String[statuses.length];

        for (int counter = 0; counter < statuses.length; counter++) {
            strings[counter] = counter + ": " + statuses[counter];
        }

        return String.join(", ", strings);
    }
}
