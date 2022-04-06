package com.canoo.platform.client.swing;

import com.canoo.platform.client.PlatformClient;
import com.canoo.platform.client.Toolkit;
import org.apiguardian.api.API;

import javax.swing.*;
import java.util.concurrent.Executor;

import static org.apiguardian.api.API.Status.MAINTAINED;

@API(since = "1.0.0-RC5", status = MAINTAINED)
public class SwingToolkit implements Toolkit {

    private final static String NAME = "Swing Toolkit";

    @Override
    public Executor getUiExecutor() {
        return new Executor() {
            @Override
            public void execute(final Runnable command) {
                SwingUtilities.invokeLater(command);
            }
        };
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static void init() {
        PlatformClient.init(new SwingToolkit());
    }
}
