package com.crashnote.appengine.test.unit.util

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

import com.crashnote.test.defs.UnitSpec
import com.crashnote.appengine.util.AppengineUtil
import com.google.appengine.api.urlfetch.HTTPMethod
import java.net.URL

class AppengineUtilSpec
    extends UnitSpec {

    "AppEngine Util" should {

        val util = new AppengineUtil

        "know if app is running on app engine" >> {
            util.isRunningOnAppengine === false
        }

        "create a HTTP request" >> {
            val r = util.createRequest("http://test.com", HTTPMethod.PUT, null)
            r.getMethod === HTTPMethod.PUT
            r.getURL === new URL("http://test.com")
        }
    }

}