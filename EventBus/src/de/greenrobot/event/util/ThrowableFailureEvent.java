/*
 * Copyright (C) 2012 Markus Junginger, greenrobot (http://greenrobot.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.greenrobot.event.util;

/**
 * A generic failure event, which can be used by apps to propagate thrown exceptions. Also used in conjunction with
 * {@link ErrorDialogManager}.
 */
public class ThrowableFailureEvent implements HasExecutionScope {
    protected final Throwable throwable;
    protected final boolean suppressErrorUi;
    private Object executionContext;

    public ThrowableFailureEvent(Throwable throwable) {
        this.throwable = throwable;
        suppressErrorUi = false;
    }

    /**
     * @param suppressErrorUi
     *            true indicates to the receiver that no error UI (e.g. dialog) should now displayed.
     */
    public ThrowableFailureEvent(Throwable throwable, boolean suppressErrorUi) {
        this.throwable = throwable;
        this.suppressErrorUi = suppressErrorUi;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public boolean isSuppressErrorUi() {
        return suppressErrorUi;
    }

    public Object getExecutionScope() {
        return executionContext;
    }

    public void setExecutionScope(Object executionContext) {
        this.executionContext = executionContext;
    }
    
}
