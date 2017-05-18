package com.apps.dave.davepluxandroid;

/**
 * Created by David on 16/05/2017.
 */

public class BITalinoException extends Exception{

    private final int code;

    public BITalinoException(final BITalinoErrorTypes errorType) {
        super(errorType.getDescription());
        code = errorType.getValue();
    }

    public int getCode() {
        return code;
    }
}
