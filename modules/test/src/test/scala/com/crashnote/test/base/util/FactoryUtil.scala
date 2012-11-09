/**
 * Copyright (C) 2011 - 101loops.com <dev@101loops.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.crashnote.test.base.util

import java.util.Properties
import scala.collection.JavaConversions._

trait FactoryUtil {

    type javaEnum[T] = java.util.Enumeration[T]

    def toEnum[T](seq: Seq[T]): java.util.Enumeration[T] =
        seq.iterator

    def toProps(map: Map[String, String], fn: ((String, String)) => (String, String)): Properties = {
        val p = new Properties()
        map.map(fn(_)).foreach(kv => p.setProperty(kv._1, kv._2))
        p
    }

    def toProps(map: Map[String, String]): Properties =
        toProps(map, tp => tp)

    def toConfProps(map: Map[String, String]): Properties =
        toProps(map, tp => ("crashnote." + tp._1, tp._2))
}