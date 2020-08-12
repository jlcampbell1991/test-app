package test.app

import cats.effect.IO
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.implicits._
import org.http4s.twirl._
import org.http4s.UrlForm

final class TinyTanManRoutesTest extends BaseTest {
  import DBDriver.XA

  val userId: UserId = UserId.random

  val user: User = User(
    "fecac44c-63b6-4435-8bf5-74ce476788e0",
    Password("9b61172b-3058-4114-ae40-ea3f854f4fe1"),
    userId
  ).save[IO].unsafeRunSync

  val cookie: RequestCookie = Session.requestCookie(user)

  val tinyTanManForm: UrlForm = UrlForm(("name", "d53b7748-3c9a-4002-a141-fa9b1c7eb255"), ("size", "1"))

  val tinyTanMan: TinyTanMan =
    TinyTanMan("cc026e64-4aca-40be-bb43-fcff02eab63f", 1, None, None, Some(TinyTanManId.random), Some(userId))
      .save[IO](userId)
      .unsafeRunSync

  """GET -> Root / "tiny-tan-mans"""" in {
    check[String](
      service.orNotFound
        .run(
          Request(method = Method.GET, uri = Uri.unsafeFromString(TinyTanMan.indexUrl)).addCookie(cookie)
        ),
      Status.Ok,
      None
    )
  }
  """GET -> Root / "tiny-tan-man/ id"""" in {
    check[String](
      service.orNotFound
        .run(
          Request(method = Method.GET, uri = Uri.unsafeFromString(tinyTanMan.showUrl)).addCookie(cookie)
        ),
      Status.Ok,
      None
    )
  }
  """GET -> Root / "tiny-tan-man" / "add"""" in {
    check[String](
      service.orNotFound
        .run(
          Request(method = Method.GET, uri = Uri.unsafeFromString(TinyTanMan.addUrl)).addCookie(cookie)
        ),
      Status.Ok,
      None
    )
  }
  """POST -> Root / "tiny-tan-man" / "create"""" in {
    check[String](
      service.orNotFound
        .run(
          Request(method = Method.POST, uri = Uri.unsafeFromString(TinyTanMan.createUrl))
            .addCookie(cookie)
            .withEntity(
              tinyTanManForm
            )
        ),
      Status.SeeOther,
      None
    )
  }
  """GET -> Root / "tiny-tan-man" / id / "edit"""" in {
    check[String](
      service.orNotFound
        .run(
          Request(method = Method.GET, uri = Uri.unsafeFromString(tinyTanMan.editUrl)).addCookie(cookie)
        ),
      Status.Ok,
      None
    )
  }
  """POST  -> Root / "tiny-tan-man" / id / "update"""" in {
    check[String](
      service.orNotFound
        .run(
          Request(method = Method.POST, uri = Uri.unsafeFromString(tinyTanMan.updateUrl))
            .addCookie(cookie)
            .withEntity(
              tinyTanManForm
            )
        ),
      Status.SeeOther,
      None
    )
  }
  """GET -> Root / "tiny-tan-man" / id / "destroy"""" in {
    check[String](
      service.orNotFound
        .run(
          Request(method = Method.GET, uri = Uri.unsafeFromString(tinyTanMan.destroyUrl))
            .addCookie(cookie)
            .withEntity(
              tinyTanManForm
            )
        ),
      Status.SeeOther,
      None
    )
  }
}
