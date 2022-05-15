package homework4.generic

import homework4.concurrent.IO
import homework4.generic.Concurrent.Callback

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

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
    def recover(pf: PartialFunction[Throwable, A]): F[A] = ???
    def recoverWith(pf: PartialFunction[Throwable, F[A]]): F[A] = ???
    def transformWith[B](f: Try[A] => F[B]): F[B] = ???

  def parallelZipMap[A, B, R](fa: F[A], fb: F[B])(f: (A, B) => R): F[R] = parallelZip(fa, fb).map(f.tupled)

  def parallelSequence[A](fas: List[F[A]]): F[List[A]] = ???

  def fromFuture[A](fa: => Future[A]): F[A] = ???

  // define everything else you might need here...

object Concurrent:
  type Callback[-A] = Try[A] => Unit

  def apply[F[_]](using f: Concurrent[F]): Concurrent[F] = f

  given Concurrent[IO] with
    def pure[A](a: A): IO[A] = ???

    def failed[A](e: Throwable): IO[A] = ???

    def eval[A](a: => A): IO[A] = ???

    def evalOn[A](a: => A, ec: ExecutionContext): IO[A] = ???

    def parallelZip[A, B](fa: IO[A], fb: IO[B]): IO[(A, B)] = ???

    extension [A](fa: IO[A])
      def flatMap[B](f: A => IO[B]): IO[B] = ???
      def flatMapError(f: Throwable => IO[A]): IO[A] = ???

    def async[A](registerCallback: (ExecutionContext, Callback[A]) => Unit): IO[A] = ???

  given (using ec: ExecutionContext): Concurrent[Future] with
    def pure[A](a: A): Future[A] = ???

    def failed[A](e: Throwable): Future[A] = ???

    def eval[A](a: => A): Future[A] = ???

    def evalOn[A](a: => A, ec: ExecutionContext): Future[A] = ???

    def parallelZip[A, B](fa: Future[A], fb: Future[B]): Future[(A, B)] = ???

    extension [A](fa: Future[A])
      def flatMap[B](f: A => Future[B]): Future[B] = ???
      def flatMapError(f: Throwable => Future[A]): Future[A] = ???

    def async[A](registerCallback: (ExecutionContext, Callback[A]) => Unit): Future[A] = ???
