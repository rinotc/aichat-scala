import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.{Config, ConfigFactory}
import io.cequence.openaiscala.domain.{ChatRole, MessageSpec}
import io.cequence.openaiscala.service.{
  OpenAIService,
  OpenAIServiceFactory,
  OpenAIServiceStreamedExtra,
  OpenAIServiceStreamedFactory
}
import wvlet.airframe.launcher.*

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor}
import scala.util.control.Breaks.*
import scala.util.{Failure, Success}

@main def aichat(args: String*): Unit = {
  Launcher.execute[AiChatImpl](args.toArray)
}

class AiChatImpl(
    @option(prefix = "-h,--help", description = "display help messages", isHelp = true)
    help: Boolean = false
):
  implicit val ec: ExecutionContext       = ExecutionContext.global
  private val actorSystem: ActorSystem    = ActorSystem()
  implicit val materializer: Materializer = Materializer(actorSystem)

  private val config: Config         = ConfigFactory.load("application")
  private val service: OpenAIService = OpenAIServiceFactory(config)
  @command(isDefault = true)
  def default(): Unit =
    breakable {
      while (true)
        val input: String = scala.io.StdIn.readLine("aichat > ")
        if input == "q" || input == "quit" then
          println("quit aichat...")
          break()
        else if input.isEmpty then ()
        else
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
        end if
      end while
    }
    actorSystem.terminate().foreach(_ => "terminated...")
    exit()
  end default

  @command(description = "say hello.")
  def hello(): Unit =
    println("Hello World")
    exit()

  private def exit(): Unit = System.exit(0)
end AiChatImpl
