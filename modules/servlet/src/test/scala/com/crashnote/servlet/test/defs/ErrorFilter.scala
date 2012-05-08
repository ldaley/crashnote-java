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
package com.crashnote.servlet.test.defs

import javax.servlet._
import http.HttpServletRequest
import org.slf4j.MDC

class ErrorFilter extends Filter {

    def init(filterConfig: FilterConfig) {
        println("BOOT ERROR FILTER")
    }

    def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {

        if (request.asInstanceOf[HttpServletRequest].getRequestURI.contains("/errors")) {

            // add test data to context
            MDC.put("TEST", "data")

            // raise error
            sys.error("default error")
        } else {
            chain.doFilter(request, response);
        }
    }

    def destroy() {
        println("SHUTDOWN ERROR FILTER")
    }
}