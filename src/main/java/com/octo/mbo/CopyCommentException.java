package com.octo.mbo;

@SuppressWarnings("ALL")
public class CopyCommentException extends Exception {
    public CopyCommentException() {
    }

    CopyCommentException(String message) {
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
