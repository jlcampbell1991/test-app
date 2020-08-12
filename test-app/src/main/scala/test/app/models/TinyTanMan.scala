package test.app

import cats.implicits._
import cats.effect.Sync
import doobie._
import doobie.implicits._
import org.http4s._
import org.http4s.UrlForm
import play.twirl.api.Html
import java.util.UUID

final case class TinyTanManId(value: UUID)
object TinyTanManId {
  def apply(id: String): TinyTanManId = TinyTanManId(UUID.fromString(id))
  def random: TinyTanManId = TinyTanManId(UUID.randomUUID)
}

final case class TinyTanMan(
    name: String,
    size: Int,
    createdAt: Option[Date],
    updatedAt: Option[Date],
    id: Option[TinyTanManId],
    userId: Option[UserId]
) {
  def save[F[_]: Sync: Transactor](userId: UserId): F[TinyTanMan] = TinyTanMan.create[F](this, userId)

  def update[F[_]: Sync: Transactor](userId: UserId): F[TinyTanMan] = TinyTanMan.update[F](this, userId)

  def destroy[F[_]: Sync: Transactor](userId: UserId): F[Int] = TinyTanMan.destroy[F](this.id, userId)

  def show: Html = TinyTanMan.show(this)

  def showUrl: String = TinyTanMan.showUrl(this.id)

  def edit: Html = TinyTanMan.edit(this)

  def editUrl: String = TinyTanMan.editUrl(this.id)

  def updateUrl: String = TinyTanMan.updateUrl(this.id)

  def destroyUrl: String = TinyTanMan.destroyUrl(this.id)
}

object TinyTanMan extends Model with TinyTanManQueries with TinyTanManViews {

  def fromUrlForm[F[_]: Sync](form: UrlForm): F[TinyTanMan] =
    for {
      name <- getValueOrRaiseError[F](form, "name")
      size <- getValueOrRaiseError[F](form, "size")
    } yield TinyTanMan(name, size.toInt, None, None, None, None)

}

trait TinyTanManQueries extends Queries {
  def all[F[_]: Sync](userId: UserId)(implicit XA: Transactor[F]): F[List[TinyTanMan]] =
    sql"""
      select * from test_app_tiny_tan_man where user_id = ${userId}
    """.query[TinyTanMan].to[List].transact(XA)

  def find[F[_]: Sync](tinyTanManId: TinyTanManId, userId: UserId)(implicit XA: Transactor[F]): F[TinyTanMan] =
    sql"""
     select * from test_app_tiny_tan_man where id = ${tinyTanManId.value} and user_id = ${userId.id}
    """.query[TinyTanMan].unique.transact(XA)

  def create[F[_]: Sync](tinyTanMan: TinyTanMan, userId: UserId)(implicit XA: Transactor[F]): F[TinyTanMan] =
    sql"""
      insert into test_app_tiny_tan_man (name, size, created_at, id, user_id)
      values
      (
        ${tinyTanMan.name},
        ${tinyTanMan.size},
        ${Date.now},
        ${TinyTanManId.random},
        ${userId.id}
      );
    """.update
      .withUniqueGeneratedKeys[TinyTanMan]("name", "size", "created_at", "updated_at", "id", "user_id")
      .transact(XA)

  def update[F[_]: Sync](tinyTanMan: TinyTanMan, userId: UserId)(implicit XA: Transactor[F]): F[TinyTanMan] =
    sql"""
      update test_app_tiny_tan_man set
        name = ${tinyTanMan.name},
        size = ${tinyTanMan.size},
        updated_at = ${Date.now}
      where id = ${tinyTanMan.id.map(_.value)}
      and user_id = ${userId.id}
    """.update
      .withUniqueGeneratedKeys[TinyTanMan]("name", "size", "created_at", "updated_at", "id", "user_id")
      .transact(XA)

  def destroy[F[_]: Sync](id: Option[TinyTanManId], userId: UserId)(implicit XA: Transactor[F]): F[Int] =
    sql"""delete from test_app_tiny_tan_man where id = ${id} and user_id = ${userId.id}""".update.run.transact(XA)
}

trait TinyTanManViews extends Views {
  def default: String = indexUrl

  def index(tinyTanMans: List[TinyTanMan]): Html = views.html.tiny_tan_man.index(tinyTanMans)

  def indexUrl: String = s"""/tiny-tan-mans"""

  def show(tinyTanMan: TinyTanMan): Html = views.html.tiny_tan_man.show(tinyTanMan)

  def showUrl(maybeId: Option[TinyTanManId]): String =
    getUrlOrDefault[TinyTanManId](maybeId, id => s"""/tiny-tan-man/${id.value.toString}""")

  def add: Html = views.html.tiny_tan_man.add()

  def addUrl: String = s"""/tiny-tan-man/add"""

  def createUrl: String = s"""/tiny-tan-man/create"""

  def edit(tinyTanMan: TinyTanMan): Html = views.html.tiny_tan_man.edit(tinyTanMan)

  def editUrl(maybeId: Option[TinyTanManId]): String =
    getUrlOrDefault[TinyTanManId](maybeId, id => s"""/tiny-tan-man/${id.value.toString}/edit""")

  def updateUrl(maybeId: Option[TinyTanManId]): String =
    getUrlOrDefault[TinyTanManId](maybeId, id => s"""/tiny-tan-man/${id.value.toString}/update""")

  def destroyUrl(maybeId: Option[TinyTanManId]): String =
    getUrlOrDefault[TinyTanManId](maybeId, id => s"""/tiny-tan-man/${id.value.toString}/destroy""")
}
