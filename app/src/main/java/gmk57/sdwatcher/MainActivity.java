package gmk57.sdwatcher;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView mSdStatusTextView;
    private ActivitySdCheckTask mCurrentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSdStatusTextView = (TextView) findViewById(R.id.sd_status);

        Switch notifySwitch = (Switch) findViewById(R.id.notify_switch);
        notifySwitch.setChecked(SdCheckService.isScheduled(this));
        notifySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SdCheckService.setSchedule(MainActivity.this, isChecked);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCurrentTask = new ActivitySdCheckTask();
        mCurrentTask.execute();
    }

    @Override
    protected void onStop() {
        if (mCurrentTask != null) {
            mCurrentTask.cancel(false);
        }
        super.onStop();
    }

    private class ActivitySdCheckTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            return SdCheckService.isSdAvailable(MainActivity.this);
        }

        @Override
        protected void onPostExecute(Boolean sdAvailable) {
            if (sdAvailable) {
                mSdStatusTextView.setText(R.string.sd_status_present);
                mSdStatusTextView.setTextColor(0xff007f00);
            } else {
                mSdStatusTextView.setText(R.string.sd_status_missing);
                mSdStatusTextView.setTextColor(0xffbf0000);
            }
        }
    }
}
