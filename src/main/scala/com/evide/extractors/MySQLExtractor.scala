package com.evide.extractors

import com.twitter.finatra.{Response => FinatraResponse}
import com.twitter.util.Future

trait MySQLExtractor {
  def mysql(future: Future[Seq[Any]]): Future[FinatraResponse] = {
    val resp = new FinatraResponse()

    future flatMap { 
      case seq: Seq[Any] => resp.body(seq.mkString).status(200).toFuture
      case _ => resp.body("Something went wrong again. Chill, it'll take time").status(200).toFuture
    }

  }
}