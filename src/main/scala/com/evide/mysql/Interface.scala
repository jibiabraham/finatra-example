package com.evide.mysql

import com.twitter.app.{App => TwitterApp}
import com.twitter.util.Future
import com.twitter.finagle.exp.mysql._
import com.evide.mysql.config.Config
import com.evide.mysql.models.{SwimmingRecord}
import java.util.logging.{Logger, Level}

object db extends TwitterApp {
  val mysqlConfig = Config("localhost", 3306, "root", "root", "test")

  def client = {
    Client(mysqlConfig.host().getHostName + ":" + mysqlConfig.host().getPort, mysqlConfig.username(), mysqlConfig.password(), mysqlConfig.dbname(), Level.OFF)
  }

  def createTable(client: Client): Future[Result] = {
    client.query(SwimmingRecord.createTableSQL)
  }


  def insertValues(client: Client): Future[Seq[Result]] = {
    val insertSQL = "INSERT INTO `finagle-mysql-example` (`event`, `time`, `name`, `nationality`, `date`) VALUES (?,?,?,?,?)"
    client.prepare(insertSQL) flatMap { ps =>
      val insertResults = SwimmingRecord.records map { r =>
        ps.parameters = Array(r.event, r.time, r.name, r.nationality, r.date)
        client.execute(ps)
      }
      Future.collect(insertResults) ensure {
        client.closeStatement(ps)
      }
    }
  }

  def selectQuery(client: Client): Future[Seq[_]] = {
    val query = "SELECT * FROM `finagle-mysql-example` WHERE `date` BETWEEN '2009-06-01' AND '2009-8-31'"
    client.select(query) { row =>
      val StringValue(event) = row("event").get
      val DateValue(date) = row("date").get
      val StringValue(name) = row("name").get
      val time = row("time") map {
        case FloatValue(f) => f
        case _ => 0.0F
      } get

      (name, time)
    }
  }
}