package jaci.openrio.toast.lib.crash;

import jaci.openrio.toast.core.Environment;
import jaci.openrio.toast.core.Toast;
import jaci.openrio.toast.core.ToastBootstrap;
import jaci.openrio.toast.core.io.usb.MassStorageDevice;
import jaci.openrio.toast.core.io.usb.USBMassStorage;
import jaci.openrio.toast.lib.Assets;
import jaci.openrio.toast.lib.log.SplitStream;
import jaci.openrio.toast.lib.log.SysLogProxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * Handles crashes when the robot encounters an uncaught-exception. This simply adds details to the Logger
 * and reports the StackTrace to a separate file under toast/crash/. Classes implementing
 * {@link jaci.openrio.toast.lib.crash.CrashInfoProvider} are able to add custom data to the log
 *
 * @author Jaci
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    static Vector<CrashInfoProvider> providers;
    static File crashDir;
    static DateFormat dateFormat;
    static CrashHandler instance;

    static int exception_fms_count = 0;
    static long exception_fms_start = 0;

    /**
     * Initialize the handler. This is handled by Toast.
     */
    public static void init() {
        try {
            providers = new Vector<CrashInfoProvider>();
            crashDir = new File(ToastBootstrap.toastHome, "crash");
            crashDir.mkdirs();
        } catch (Exception e) {}
        dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
        instance = new CrashHandler();
        Thread.setDefaultUncaughtExceptionHandler(instance);
        Thread.currentThread().setUncaughtExceptionHandler(instance);

        providers.add(new CrashInfoToast());
        providers.add(new CrashInfoEnvironment());
        providers.add(new CrashInfoModules());
    }

    /**
     * Register a provider for the Crash Handler
     */
    public static void registerProvider(CrashInfoProvider prov) {
        providers.add(prov);
    }

    /**
     * Handle an uncaught exception
     */
    public static void handle(Throwable t) {
        try {
            boolean shouldShutdown = !Environment.isCompetition();
            exception_fms_count++;
            if (exception_fms_start == 0) exception_fms_start = System.currentTimeMillis();

            String fn = "crash-" + dateFormat.format(new Date());
            File file = new File(crashDir, fn + ".txt");
            SplitStream split = new SplitStream(System.err, new FileOutputStream(file));
            PrintStream out = new PrintStream(split);

            out.println("**** CRASH LOG ****");
            if (!shouldShutdown && exception_fms_count > 10 && exception_fms_start < 10000) {     // 10 Exceptions in 10 Seconds
                out.println("NOTICE: More than 10 Exceptions have occured in 10 seconds. This is a good sign there is something majorly wrong, and even though you are connected to FMS, we're going to shut down anyway. Goodbye, World.");
                shouldShutdown = true;
            } else if (!shouldShutdown)
                out.println("NOTICE: Your robot is connected to FMS! To save your robot, we won't shut down your robot unless this exception is repeated.");
            out.println("Your robot has crashed. Following is a crash log and more details.");
            out.println("This log has been saved to: " + file.getCanonicalPath());
            out.println("This log will also be duplicated to USB devices, with the filename: " + fn + ".txt");

            out.println(Assets.getAscii("crash"));

            for (CrashInfoProvider provider : providers) {
                String info = provider.getCrashInfoPre(t);
                if (info != null)
                    out.println("\t" + provider.getCrashInfoPre(t) + "\n");
            }
            t.printStackTrace(out);
            out.println();
            out.println("Crash Information: ");
            for (CrashInfoProvider provider : providers) {
                try {
                    out.println("\t" + provider.getName() + ": ");
                    List<String> info = provider.getCrashInfo(t);
                    if (info != null)
                        for (String s : info) {
                            out.println("\t\t" + s);
                        }
                    out.println();
                } catch (Throwable e) {
                    out.println("Provider: " + provider.getClass().getCanonicalName() + " could not finalize output. (" + e.getLocalizedMessage() + ")");
                }
            }
            out.println();
            out.println("*******************");

            out.flush();
            out.close();

            File recentLog = SysLogProxy.recentOut;
            File cpFile = new File(crashDir, fn + "-FULL.txt");
            Files.copy(recentLog.toPath(), cpFile.toPath());

            duplicate(cpFile);
            duplicate(file);

            if (shouldShutdown)
                Toast.getToast().shutdownCrash();
        } catch (Exception e) {
        }
    }

    /**
     * Duplicates the given Crash File across all USB Mass Storage devices
     */
    public static void duplicate(File f) {
        String fn = f.getName();
        boolean copied = false;
        for (MassStorageDevice device : USBMassStorage.connectedDevices) {
            File crash = new File(device.toast_directory, "crash");
            crash.mkdirs();
            copied = true;
            try {
                Files.copy(f.toPath(), new File(crash, fn).toPath());
            } catch (IOException e) { }
        }

        if (copied) {
            f.delete();
        }
    }

    /**
     * Handles an UncaughtException by logging the crash and shutting down the Robot safely. THis is
     * automatically called by the JVM when an exception is uncaught on any thread.
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        handle(e);
    }
}
