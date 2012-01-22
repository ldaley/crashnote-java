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
package com.crashnote.core.log;

import com.crashnote.core.config.Config;

/**
 * The log within the log: allows the library to issue log statements without them ending up in
 * the user's log history. By default is uses the stdout / stderr to convey
 * important messages to the user (like initialization problems).
 */
public class LogLog {

    private final String name;
    private final boolean debug;

    // SETUP ======================================================================================

    public LogLog(final String name, final boolean debug) {
        this.name = Config.LIB_NAME;
        this.debug = debug;
    }

    public LogLog(final String name) {
        this(name, false);
    }

    // INTERFACE ==================================================================================

    public void debug(final String msg, final Object... args) {
        if (debug) System.out.println(name + " [DEBUG] - " + buildMsg(msg, args));
    }

    public void info(final String msg, final Object... args) {
        System.out.println(name + " - " + buildMsg(msg, args));
    }

    public void warn(final String msg, final Object... args) {
        System.err.println(name + " WARN - " + buildMsg(msg, args));
    }

    public void warn(final String msg, final Throwable th) {
        System.err.println(name + " WARN - " + msg + ": " + th.getMessage());
    }

    public void warn(final String msg, final Throwable th, final Object... args) {
        System.err.println(name + " WARN - " + buildMsg(msg, args) + ": " + th.getMessage());
    }

    public void error(final String msg, final Object... args) {
        System.err.println(name + " ERROR - " + buildMsg(msg, args));
    }

    public void error(final String msg, final Throwable th, final Object... args) {
        System.err.println(name + " ERROR - " + buildMsg(msg, args) + ": " + th.getMessage());
    }

    // SHARED =====================================================================================

    protected String buildMsg(final String msg, final Object... args) {
        String res = msg;
        for (final Object arg : args)
            res = res.replaceFirst("\\{\\}", arg.toString());
        return res;
    }

    // GET ========================================================================================

    public boolean isDebug() {
        return debug;
    }

    public String getName() {
        return name;
    }
}
