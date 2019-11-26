package net.apnic.rdapd.loaders;

/**
 * Defines common interface for loaders that provide {@link LoaderStatus}.
 */
public interface LoaderStatusProvider {
    LoaderStatus getLoaderStatus();
}
