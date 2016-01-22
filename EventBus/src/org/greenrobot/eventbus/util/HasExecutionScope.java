package org.greenrobot.eventbus.util;

public interface HasExecutionScope {
    Object getExecutionScope();

    void setExecutionScope(Object executionScope);

}
