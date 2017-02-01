package com.octo.mbo;

import java.util.function.Consumer;

public interface Appender<T, U> extends Consumer<T> {

    U getContent();
}
