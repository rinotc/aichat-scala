import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import io.cequence.openaiscala.domain.{ChatRole, MessageSpec}
import io.cequence.openaiscala.service.{
  OpenAIService,
  OpenAIServiceFactory,
  OpenAIServiceStreamedExtra,
  OpenAIServiceStreamedFactory
}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor}
import scala.util.control.Breaks.*
import scala.util.{Failure, Success}

@main
def main(): Unit = {
  implicit val ec: ExecutionContext       = ExecutionContext.global
  val actorSystem: ActorSystem            = ActorSystem()
  implicit val materializer: Materializer = Materializer(actorSystem)

  val config  = ConfigFactory.load("application.conf")
  val service = OpenAIServiceFactory(config)
  breakable {
    while (true) {
      val input: String = scala.io.StdIn.readLine("please input prompt >")
      if (input == "q" || input == "quit") {
        println("quit aichat...")
        break()
      } else {
        val future = service
          .createChatCompletion(
            messages = Seq(
              MessageSpec(ChatRole.User, input)
            )
          )
          .map { response =>
            response.choices.map(_.message.content).foreach(println)
            println("request completed.")
          }
          .recover(_.printStackTrace())
        Await.result(future, Duration.Inf)
      }
    }
  }

  actorSystem.terminate().foreach(_ => println("terminated."))
  System.exit(0)
}
