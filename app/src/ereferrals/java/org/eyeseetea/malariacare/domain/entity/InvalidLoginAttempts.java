package org.eyeseetea.malariacare.domain.entity;

import org.eyeseetea.malariacare.domain.exception.ActionNotAllowed;

import java.util.Date;

public class InvalidLoginAttempts {

    private static final int NUMBER_ATTEMPTS = 3;
    private static final int DISABLE_TIME = 30000;

    private int failedLoginAttempts;
    private long enableLoginTime;

    public InvalidLoginAttempts(int failedLoginAttempts, long enableLoginTime) {
        this.failedLoginAttempts = failedLoginAttempts;
        this.enableLoginTime = enableLoginTime;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public long getEnableLoginTime() {
        return enableLoginTime;
    }

    public void setEnableLoginTime(long enableLoginTime) {
        this.enableLoginTime = enableLoginTime;
    }

    public void addFailedAttempts() throws ActionNotAllowed {
        if (isLoginEnabled()) {
            failedLoginAttempts++;
            if (failedLoginAttempts >= NUMBER_ATTEMPTS) {
                enableLoginTime = new Date().getTime() + DISABLE_TIME;
                failedLoginAttempts = 0;
            }
        } else {
            throw new ActionNotAllowed("Add new attempt if login is disable is not allowed.");
        }
    }

    public boolean isLoginEnabled() {
        return enableLoginTime < new Date().getTime();
    }


}
