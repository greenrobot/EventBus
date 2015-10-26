package de.greenrobot.event;

/**
 * Implement this interface to provide custom logging.
 */
public interface CustomLogger {

    /**
     * Log a message at verbose level.
     *
     * @param tag tag of the log message
     * @param text log message text
     * @param t include throwable in log message
     */
    void v(String tag, String text, Throwable t);

    /**
     * Log a message at debug level.
     *
     * @param tag tag of the log message
     * @param text log message text
     * @param t include throwable in log message
     */
    void d(String tag, String text, Throwable t);

    /**
     * Log a message at info level.
     *
     * @param tag tag of the log message
     * @param text log message text
     * @param t include throwable in log message
     */
    void i(String tag, String text, Throwable t);

    /**
     * Log a message at warning level.
     *
     * @param tag tag of the log message
     * @param text log message text
     * @param t include throwable in log message
     */
    void w(String tag, String text, Throwable t);

    /**
     * Log a message at error level.
     *
     * @param tag tag of the log message
     * @param text log message text
     * @param t include throwable in log message
     */
    void e(String tag, String text, Throwable t);
}
