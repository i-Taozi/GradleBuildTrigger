package com.canoo.dp.impl.platform.projector.server.update;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.platform.core.functional.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UpdateHandler<T> {

    private final List<Consumer<T>> consumers;

    public UpdateHandler() {
        this.consumers = new ArrayList<>();
    }

    public void update(final T instance) {
        consumers.forEach(c -> c.accept(instance));
    }

    public Subscription addHandler(final Consumer<T> handler) {
        Assert.requireNonNull(handler, "handler");
        consumers.add(handler);
        return () -> consumers.remove(handler);
    }
}
