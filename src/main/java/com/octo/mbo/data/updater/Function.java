package com.octo.mbo.data.updater;

import com.octo.mbo.exceptions.CopyNotesException;

@FunctionalInterface
public interface Function<T, U> {
    U apply(T arg) throws CopyNotesException;
}
