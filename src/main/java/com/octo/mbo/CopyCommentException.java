package com.octo.mbo;

@SuppressWarnings("ALL")
public class CopyCommentException extends Exception {

    CopyCommentException(String message) {
        super(message);
    }

    CopyCommentException(String message, Throwable t) {
        super(message, t);
    }

}
