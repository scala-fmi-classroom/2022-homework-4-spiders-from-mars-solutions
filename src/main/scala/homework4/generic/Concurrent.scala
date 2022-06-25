package homework4.generic

import homework4.concurrent.IO
import homework4.generic.Concurrent.Callback

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

trait Concurrent[F[_]]:
  // Base operations:
  def pure[A](a: A): F[A]
  def failed[A](e: Throwable): F[A]

  def eval[A](a: => A): F[A]
  def evalOn[A](a: => A, ec: ExecutionContext): F[A]

  def parallelZip[A, B](fa: F[A], fb: F[B]): F[(A, B)]

  extension [A](fa: F[A])
    def flatMap[B](f: A => F[B]): F[B]
    def flatMapError(f: Throwable => F[A]): F[A]

  def async[A](registerCallback: (ExecutionContext, Callback[A]) => Unit): F[A]

  // Derived operations:
  extension [A](fa: F[A])
    def map[B](f: A => B): F[B] = fa.flatMap(a => pure(f(a)))
    def recover(pf: PartialFunction[Throwable, A]): F[A] = fa.recoverWith(pf.andThen(a => pure(a)))
    def recoverWith(pf: PartialFunction[Throwable, F[A]]): F[A] = fa.flatMapError { e =>
      pf.lift(e).getOrElse(failed(e))
    }

    def transformWith[B](f: Try[A] => F[B]): F[B] =
      val fSuccess = flatMap(fa)(a => pure(Try(a)))
      val fTry = flatMapError(fSuccess)(e => pure(Failure(e)))

      flatMap(fTry)(f)

  def parallelZipMap[A, B, R](fa: F[A], fb: F[B])(f: (A, B) => R): F[R] = parallelZip(fa, fb).map(f.tupled)

  def parallelSequence[A](fas: List[F[A]]): F[List[A]] =
    fas.foldRight(pure(List.empty[A]))((next, acc) => parallelZipMap(next, acc)(_ :: _))

  def parallelTraverse[A, B](as: List[A])(f: A => F[B]) = parallelSequence(as.map(f))

  def fromFuture[A](fa: => Future[A]): F[A] = async { (ec, callback) =>
    fa.onComplete(callback)(ec)
  }

  def fromTry[A](t: Try[A]): F[A] = t match
    case Success(value) => pure(value)
    case Failure(e) => failed(e)

  // define everything else you might need here...

object Concurrent:
  type Callback[-A] = Try[A] => Unit

  def apply[F[_]](using f: Concurrent[F]): Concurrent[F] = f

  given Concurrent[IO] with
    def pure[A](a: A): IO[A] = IO.of(a)

    def failed[A](e: Throwable): IO[A] = IO.failed(e)

    def eval[A](a: => A): IO[A] = IO(a)

    def evalOn[A](a: => A, ec: ExecutionContext): IO[A] = IO(a).bindTo(ec)

    def parallelZip[A, B](fa: IO[A], fb: IO[B]): IO[(A, B)] = fa zip fb

    extension [A](fa: IO[A])
      def flatMap[B](f: A => IO[B]): IO[B] = fa.flatMap(f)
      def flatMapError(f: Throwable => IO[A]): IO[A] = fa.flatMapError(f)

    def async[A](registerCallback: (ExecutionContext, Callback[A]) => Unit): IO[A] =
      IO.usingCallback(registerCallback)

  given (using ec: ExecutionContext): Concurrent[Future] with
    def pure[A](a: A): Future[A] = Future.successful(a)

    def failed[A](e: Throwable): Future[A] = Future.failed(e)

    def eval[A](a: => A): Future[A] = Future(a)

    def evalOn[A](a: => A, ec: ExecutionContext): Future[A] = Future(a)(using ec)

    def parallelZip[A, B](fa: Future[A], fb: Future[B]): Future[(A, B)] = fa zip fb

    extension [A](fa: Future[A])
      def flatMap[B](f: A => Future[B]): Future[B] = fa.flatMap(f)
      def flatMapError(f: Throwable => Future[A]): Future[A] = fa.recoverWith { case e =>
        f(e)
      }

    def async[A](registerCallback: (ExecutionContext, Callback[A]) => Unit): Future[A] =
      val p = Promise[A]()

      registerCallback(ec, p.complete)

      p.future
