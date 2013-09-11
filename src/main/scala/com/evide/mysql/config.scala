package com.evide.mysql.config

import com.twitter.app.{App => TwitterApp}
import java.net.InetSocketAddress

case class Config(ip: String, port: Int, usr: String, pwd: String, database: String) extends TwitterApp {
  val host = flag("server", new InetSocketAddress(ip, port), "mysql server address")
  val username = flag("username", usr, "mysql username")
  val password = flag("password", pwd, "mysql password")
  val dbname = flag("database", database, "default database to connect to")	

  override def toString = {
  	host().getHostName + ":" + host().getPort + " " + username() + " " + password() + " " + dbname() + " " + "Level.OFF"
  }
}
