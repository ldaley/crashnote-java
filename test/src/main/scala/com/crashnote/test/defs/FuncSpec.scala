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
package com.crashnote.test.defs

import org.specs2.specification._

import cc.spray._
import http.{StatusCode, StatusCodes}
import StatusCodes._
import com.crashnote.test.util.MockServer

trait FuncSpec
    extends UnitSpec {

    val serverPort = nextFreePort()

    // SETUP ======================================================================================

    override def map(fs: => Fragments) =
        Step(startServer()) ^ fs ^ Step(stopServer())

    def startServer() {
        MockServer.boot(serverPort, onReceive)
    }

    def stopServer() {
        MockServer.shutdown()
    }

    // SHARED ======================================================================================

    protected def onReceive(key: String, data: String): (StatusCode, String) =
        (OK, "Okay")

    protected def nextFreePort() = {
        val s = new java.net.ServerSocket(0)
        val p = s.getLocalPort
        s.close()
        p
    }
}