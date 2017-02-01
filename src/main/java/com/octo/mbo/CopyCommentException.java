package com.octo.mbo;

public class CopyCommentException extends Exception {
    public CopyCommentException() {
    }

    public CopyCommentException(String message) {
        super(message);
    }

    public CopyCommentException(String message, Throwable cause) {
        super(message, cause);
    }

    public CopyCommentException(Throwable cause) {
        super(cause);
    }

    public CopyCommentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
