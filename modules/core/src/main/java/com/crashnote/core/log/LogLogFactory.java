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

import com.crashnote.core.config.CrashConfig;

/**
 * Factory class that can instantiate an implementation of {@link LogLog}.
 */
public class LogLogFactory<C extends CrashConfig<C>> {

    // VARS =======================================================================================

    private final boolean debug;


    // SETUP ======================================================================================

    public LogLogFactory(final C config) {
        this.debug = config.isDebug();
    }


    // INTERFACE ==================================================================================

    public LogLog getLogger(final Class clazz) {
        return getLogger(clazz.getName());
    }

    public LogLog getLogger(final String name) {
        return new LogLog(name, debug);
    }

    public LogLog getLogger() {
        return getLogger("CRASHNOTE");
    }


    // GET ========================================================================================

    public boolean isDebug() {
        return debug;
    }
}
