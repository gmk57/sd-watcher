package gmk57.sdwatcher;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class SdCheckService extends JobService {
    private static final String TAG = "SdCheckService";
    private static final int JOB_ID = 35723;

    private ServiceSdCheckTask mCurrentTask;

    static void setSchedule(Context context, boolean isOn) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        if (isOn) {
            JobInfo jobInfo = new JobInfo.Builder(JOB_ID,
                    new ComponentName(context, SdCheckService.class))
                    .setPeriodic(1000 * 60 * 60)  // Once per hour
                    .setPersisted(true)
                    .build();
            jobScheduler.schedule(jobInfo);
        } else {
            jobScheduler.cancel(JOB_ID);
        }
    }

    static boolean isScheduled(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        for (JobInfo jobInfo : jobScheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == JOB_ID) return true;
        }
        return false;
    }

    static boolean isSdAvailable(Context context) {
        for (File path : context.getExternalFilesDirs(null)) {
            try {
                if (path != null &&
                        Environment.getExternalStorageState(path).equals(Environment.MEDIA_MOUNTED)
                        && Environment.isExternalStorageRemovable(path)) {
                    return true;
                }
            } catch (RuntimeException e) {
                Log.e(TAG, "Exception while checking path: " + path, e);
            }
        }
        return false;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        mCurrentTask = new ServiceSdCheckTask();
        mCurrentTask.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (mCurrentTask != null) {
            mCurrentTask.cancel(false);
        }
        return false;
    }

    private class ServiceSdCheckTask extends AsyncTask<JobParameters, Void, Boolean> {

        @Override
        protected Boolean doInBackground(JobParameters... params) {
            boolean sdAvailable = isSdAvailable(SdCheckService.this);
            jobFinished(params[0], false);
            return sdAvailable;
        }

        @Override
        protected void onPostExecute(Boolean sdAvailable) {
            if (!sdAvailable) {
                Intent intent = new Intent(SdCheckService.this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(SdCheckService.this, 0,
                        intent, 0);

                Notification notification = new Notification.Builder(SdCheckService.this)
                        .setSmallIcon(R.drawable.ic_sd_card)
                        .setContentTitle(getString(R.string.notification_title))
                        .setContentText(getString(R.string.notification_text))
                        .setTicker(getString(R.string.notification_title))
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(0, notification);
            }
        }
    }
}
