package test.app

import cats.effect.Sync
import cats.implicits._
import org.http4s._
import org.http4s.{Headers, ResponseCookie, UrlForm}
import doobie._
import doobie.implicits._
import com.github.t3hnar.bcrypt._
import org.reactormonk.{CryptoBits, PrivateKey}
import java.time._

case class Session(username: String, password: String) extends Queries {
  def findUser[F[_]: Sync](implicit XA: Transactor[F]): F[Option[User]] =
    sql"""select * from test_app_user where name = ${username}"""
      .query[User]
      .option
      .transact(XA)

  def auth[F[_]: Sync](user: User): Option[User] =
    if (password.isBcrypted(user.password)) Some(user)
    else None
}
object Session extends Model {
  def fromUrlForm[F[_]: Sync](form: UrlForm): F[Session] =
    for {
      name <- getValueOrRaiseError[F](form, "name")
      password <- getValueOrRaiseError[F](form, "password")
    } yield Session(name, password)

  val COOKIE_NAME = "test_app_cookie"
  private val key = PrivateKey(scala.io.Codec.toUTF8(scala.util.Random.alphanumeric.take(20).mkString("")))
  private val crypto = CryptoBits(key)
  def cookie(user: User): ResponseCookie =
    ResponseCookie(name = COOKIE_NAME, content = crypto.signToken(user.id, Instant.now.getEpochSecond.toString))

  def requestCookie(user: User): RequestCookie =
    RequestCookie(name = COOKIE_NAME, content = crypto.signToken(user.id, Instant.now.getEpochSecond.toString))

  def isLoggedIn(requestHeaders: Headers): Option[UserId] =
    for {
      header <- headers.Cookie.from(requestHeaders)
      cookie <- header.values.toList.find(_.name == COOKIE_NAME)
      token <- crypto.validateSignedToken(cookie.content)
    } yield UserId(token)

  def login = views.html.session.login()
  def loginUrl = "/login"
}
