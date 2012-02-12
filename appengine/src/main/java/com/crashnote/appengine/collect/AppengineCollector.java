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
package com.crashnote.appengine.collect;

import com.crashnote.appengine.collect.impl.*;
import com.crashnote.core.collect.Collector;
import com.crashnote.core.collect.impl.*;
import com.crashnote.core.config.Config;

public class AppengineCollector<C extends Config>
    extends Collector<C> {

    // SETUP ======================================================================================

    public AppengineCollector(final C config) {
        super(config);
    }

    // FACTORY ====================================================================================

    @Override
    protected EnvCollector<C> createEnvColl(final C config) {
        return new AppengineEnvCollector<C>(config);
    }

    @Override
    protected LogCollector<C> createLogColl(final C config) {
        return new AppengineLogCollector<C>(config);
    }
}
