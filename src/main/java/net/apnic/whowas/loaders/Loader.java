package net.apnic.whowas.loaders;

import net.apnic.whowas.history.History;

import java.util.function.Consumer;

public interface Loader {
    void loadInto(Consumer<History> consumer);
}
