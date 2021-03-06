package org.eyeseetea.malariacare.presentation.executors;

import android.os.Handler;
import android.os.Looper;

import org.eyeseetea.malariacare.domain.boundary.executors.IDelayedMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;


public class UIThreadExecutor implements IMainExecutor, IDelayedMainExecutor {

    private Handler handler;

    public UIThreadExecutor() {
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void run(Runnable runnable) {
        handler.post(runnable);
    }

    public void postDelayed(Runnable runnable, long delayMillis) {
        handler.postDelayed(runnable, delayMillis);
    }
}