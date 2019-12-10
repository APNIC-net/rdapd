package net.apnic.rdapd.loaders;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Encapsulates the status for the loader.
 */
public class LoaderStatus {
    private final Status status;
    private final Optional<LocalDateTime> lastSuccessfulDateTime;

    public LoaderStatus(Status status, Optional<LocalDateTime> lastSuccessfulDateTime) {
        this.status = status;
        this.lastSuccessfulDateTime = lastSuccessfulDateTime;
    }

    public enum Status {UP_TO_DATE, OUT_OF_DATE, INITIALISING, INITIALISATION_FAILED}

    public Status getStatus() {
        return status;
    }

    public Optional<LocalDateTime> getLastSuccessfulDateTime() {
        return lastSuccessfulDateTime;
    }
}
