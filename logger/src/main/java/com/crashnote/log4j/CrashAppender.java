/**
 * Copyright (C) 2011 - 101loops.com <dev@101loops.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.crashnote.log4j;

import com.crashnote.ICrashAppender;
import com.crashnote.core.model.types.LogLevel;
import com.crashnote.log4j.impl.Log4jEvt;
import com.crashnote.logger.config.*;
import com.crashnote.logger.report.LoggerReporter;
import org.apache.log4j.*;
import org.apache.log4j.spi.*;

/**
 * Appender that writes logs from 'Log4J' to the cloud
 */
public class CrashAppender
    extends AppenderSkeleton implements ICrashAppender {

    private boolean started;

    private LoggerReporter<LoggerConfig> reporter;

    // config
    private LoggerConfig config;
    private final LoggerConfigFactory configFactory;

    // SETUP ======================================================================================

    public CrashAppender() {
        this(new LoggerConfigFactory());
        start();
    }

    public CrashAppender(final LoggerConfigFactory configFactory) {
        this.configFactory = configFactory;
        start();
    }

    public CrashAppender(final LoggerConfig config, final LoggerReporter reporter) {
        this.config = config;
        this.reporter = reporter;
        this.configFactory = null;
        start();
    }

    // INTERFACE ==================================================================================

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    public void close() {
        if (started) {
            getReporter().stop();
            started = false;
        }
    }

    public static Logger getTargetLogger(final Class clazz) {
        return Logger.getLogger(clazz);
    }

    public boolean isStarted() {
        return started;
    }

    public void setLogLevel(final LogLevel lvl) {
        if (lvl == LogLevel.DEBUG)
            setThreshold(Level.DEBUG);
        else if (lvl == LogLevel.INFO)
            setThreshold(Level.INFO);
        if (lvl == LogLevel.WARN)
            setThreshold(Level.WARN);
        else
            setThreshold(Level.ERROR);
    }

    // SHARED =====================================================================================

    @Override
    protected void append(final LoggingEvent event) {
        if (isStarted())
            getReporter().reportLog(new Log4jEvt(event, MDC.getContext()));
    }

    // INTERNALS ==================================================================================

    private void start() {
        if (!started) {
            setLogLevel(getConfig().getLogLevel());
            getReporter().start();

            addFilter(new Filter() {
                @Override
                public int decide(final LoggingEvent event) {
                    final boolean res = getReporter().doAcceptLog(event.getLoggerName());
                    return res ? ACCEPT : DENY;
                }
            });

            started = true;
        }
    }

    private LoggerConfig getConfig() {
        if (config == null) {
            config = (LoggerConfig) configFactory.get();
        }
        return config;
    }

    private LoggerReporter<LoggerConfig> getReporter() {
        if (reporter == null) reporter = getConfig().getReporter();
        return reporter;
    }

    // PARAMETERS =================================================================================

    public void setPort(final String port) {
        configFactory.setPort(port);
    }

    public void setHost(final String host) {
        configFactory.setHost(host);
    }

    public void setKey(final String key) {
        configFactory.setKey(key);
    }

    public void setEnabled(final String enabled) {
        configFactory.setEnabled(enabled);
    }

    public void setSslPort(final String sslPort) {
        configFactory.setSslPort(sslPort);
    }

    public void setSecure(final String secure) {
        configFactory.setSecure(secure);
    }

    public void setSync(final String on) {
        configFactory.setSync(on);
    }
}