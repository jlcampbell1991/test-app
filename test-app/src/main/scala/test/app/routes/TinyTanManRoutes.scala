package test.app

import cats.implicits._
import cats.effect.Sync
import org.http4s.dsl.Http4sDsl
import org.http4s._
import org.http4s.twirl._
import doobie._

object TinyTanManRoutes extends Routes {
  def publicRoutes[F[_]: Sync: Transactor](implicit dsl: Http4sDsl[F]): HttpRoutes[F] =
    HttpRoutes.empty

  def authedRoutes[F[_]: Sync: Transactor](implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._
    authedService((userId: UserId) =>
      HttpRoutes.of {
        case GET -> Root / "tiny-tan-mans" =>
          for {
            tinyTanMans <- TinyTanMan.all(userId)
            response <- Ok(TinyTanMan.index(tinyTanMans))
          } yield response
        case GET -> Root / "tiny-tan-man" / "add" => Ok(TinyTanMan.add)
        case GET -> Root / "tiny-tan-man" / id =>
          for {
            tinyTanMan <- TinyTanMan.find(TinyTanManId(id), userId)
            response <- Ok(tinyTanMan.show)
          } yield response

        case req @ POST -> Root / "tiny-tan-man" / "create" =>
          for {
            form <- req.as[UrlForm]
            tinyTanMan <- TinyTanMan.fromUrlForm(form).flatMap(_.save(userId))
            response <- Redirect(tinyTanMan.showUrl)
          } yield response
        case GET -> Root / "tiny-tan-man" / id / "edit" =>
          for {
            tinyTanMan <- TinyTanMan.find(TinyTanManId(id), userId)
            response <- Ok(tinyTanMan.edit)
          } yield response
        case req @ POST -> Root / "tiny-tan-man" / id / "update" =>
          for {
            form <- req.as[UrlForm]
            tinyTanMan <- TinyTanMan
              .fromUrlForm(form)
              .map(_.copy(id = Some(TinyTanManId(id))))
              .flatMap(_.update(userId))
            response <- Redirect(tinyTanMan.showUrl)
          } yield response
        case GET -> Root / "tiny-tan-man" / id / "destroy" =>
          for {
            _ <- TinyTanMan.destroy(Some(TinyTanManId(id)), userId)
            response <- Redirect(TinyTanMan.indexUrl)
          } yield response
      }
    )
  }
}
