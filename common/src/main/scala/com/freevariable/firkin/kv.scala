/*
 * Copyright (c) 2015 William C. Benton and Red Hat, Inc.
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
 * limitations under the License.c
 */

package com.freevariable.firkin

import akka.actor._
import scala.concurrent.Promise

import java.security.MessageDigest

class KV extends Actor {
  import KV._

  val db = collection.mutable.Map[String, String]()
  val tag_db = collection.mutable.Map[String, String]()

  def receive = {
    case GET(key, promise) => promise.success(db.get(key))
    case PUT(value, promise) => {
      val hash = digestify(value)
      db(hash) = value
      promise.success(hash)
    }
    case LIST(promise) => {
      promise.success(db.keys.toList)
    }
    
    case GET_TAG(tag, promise) => promise.success(tag_db.get(tag)/* .flatMap {key => db.get(key)}*/)
    case RESOLVE_TAG(tag, promise) => promise.success(tag_db.get(tag).flatMap {key => db.get(key).map { doc => (key, doc)}} )
    case PUT_TAG(tag, hash, promise) => {
      promise.success(db.get(hash).flatMap(ignored => {tag_db(tag) = hash; Some(tag)}))
    }
    case LIST_TAGS(promise) => {
      promise.success(tag_db.keys.toList)
    }
  
  }
}

object KV {
  case class GET(key: String, promise: Promise[Option[String]] = Promise())
  case class PUT(value: String, promise: Promise[String] = Promise())
  case class LIST(promise: Promise[List[String]] = Promise())

  case class GET_TAG(tag: String, promise: Promise[Option[String]] = Promise())
  case class RESOLVE_TAG(tag: String, promise: Promise[Option[Pair[String,String]]] = Promise())
  case class PUT_TAG(tag: String, hash: String, promise: Promise[Option[String]] = Promise())
  case class LIST_TAGS(promise: Promise[List[String]] = Promise())

  def digestify(s: String) = {
    MessageDigest.getInstance("SHA1").digest(s.getBytes).map("%02x".format(_)).mkString
  }
}
