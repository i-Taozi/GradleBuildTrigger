package org.ea.sqrl.services;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;

import org.ea.sqrl.processors.SQRLStorage;

/**
 *
 * @author Daniel Persson
 */
@TargetApi(21)
public class ClearIdentityService extends JobService {
    public static final int JOB_NUMBER = 1;

    @Override
    public boolean onStartJob(JobParameters params) {
        SQRLStorage.getInstance(this).clearQuickPass();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return SQRLStorage.getInstance(this).hasQuickPass();
    }
}
