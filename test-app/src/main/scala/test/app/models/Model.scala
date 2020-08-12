package test.app

import cats.effect.Sync
import doobie._
import java.time.{LocalDateTime, Month}
import java.util.UUID
import org.http4s._
import org.http4s.UrlForm

trait Model {
  protected def getValueOrRaiseError[F[_]: Sync](form: UrlForm, value: String): F[String] =
    form
      .getFirst(value)
      .fold(Sync[F].raiseError[String](MalformedMessageBodyFailure(s"forgot $value")))(Sync[F].pure(_))
}

trait Queries {
  implicit val uuidGet: Get[UUID] = Get[String].map(UUID.fromString(_))
  implicit val uuidPut: Put[UUID] = Put[String].contramap(_.toString)
}

trait Views {
  def default: String

  protected def getUrlOrDefault[A](id: Option[A], s: A => String) =
    id.map(s).getOrElse(default)
}
case class Date(value: LocalDateTime)

object Date extends doobie.util.meta.TimeMetaInstances with doobie.util.meta.MetaConstructors {
  def apply(month: Int, day: Int, year: Int) =
    LocalDateTime.of(year, Month.of(month), day, now.value.getHour, now.value.getMinute)

  def now: Date = Date(LocalDateTime.now)

  implicit val get: Get[Date] = Get[LocalDateTime].map(Date(_))
  implicit val put: Put[Date] = Put[LocalDateTime].contramap(_.value)
}
