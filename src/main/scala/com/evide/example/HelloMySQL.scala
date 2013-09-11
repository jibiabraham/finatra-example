package com.evide.example

import com.twitter.app.{App => TwitterApp}
import com.twitter.finatra._
import com.twitter.finatra.{Response => FinatraResponse}

import com.evide.extractors.{MySQLExtractor}
import com.evide.mysql.{db}

import com.twitter.ostrich.stats.Stats
import com.twitter.finagle.exp.mysql._
import com.twitter.util.Future
import java.net.InetSocketAddress
import java.sql.Date
import java.util.logging.{Logger, Level}

object HelloMySQL {


  class HelloMySQL extends Controller {

    override def render = new Response with MySQLExtractor

    /**
     * Basic Example
     *
     * curl http://localhost:7070/ => "hello world"
     */
     get("/") { request =>
      render.plain("hello world").toFuture
    }

    /**
     * Basic MySQL Insertion test
     *
     * curl http://localhost:7070/ => "hello world"
     */
     get("/minsert") { request =>
      val client = db.client

      val resultFuture = for {
        _ <- db.createTable(client)
        _ <- db.insertValues(client)
        r <- db.selectQuery(client)
      } yield r

      render.mysql(resultFuture) ensure {
        client.close()
      }
    }

    /**
     * Rendering views
     *
     * curl http://localhost:7070/posts
     */
     class AnView extends View {
      val template = "an_view.mustache"
      val some_val = "random value here"
    }

    get("/template") { request =>
      val anView = new AnView
      render.view(anView).toFuture
    }

    /**
     * Custom Error Handling with custom Exception
     *
     * curl http://localhost:7070/unautorized
     */
     class Unauthorized extends Exception

     get("/unauthorized") { request =>
      throw new Unauthorized
    }

    error { request =>
      request.error match {
        case Some(e:ArithmeticException) =>
        render.status(500).plain("whoops, divide by zero!").toFuture
        case Some(e:Unauthorized) =>
        render.status(401).plain("Not Authorized!").toFuture
        case Some(e:UnsupportedMediaType) =>
        render.status(415).plain("Unsupported Media Type!").toFuture
        case _ =>
        render.status(500).plain("Something went wrong!").toFuture
      }
    }


    /**
     * Metrics are supported out of the box via Twitter's Ostrich library.
     * More details here: https://github.com/twitter/ostrich
     *
     * curl http://localhost:7070/slow_thing
     *
     * By default a stats server is started on 9990:
     *
     * curl http://localhost:9990/stats.txt
     *
     */

     get("/slow_thing") { request =>
      Stats.incr("slow_thing")
      Stats.time("slow_thing time") {
        Thread.sleep(2000)
      }
      render.plain("slow").toFuture
    }

  }

  val app = new HelloMySQL

  def main(args: Array[String]) = {
    FinatraServer.register(app)
    FinatraServer.start()
  }
}

