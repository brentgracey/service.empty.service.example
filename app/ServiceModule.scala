import java.util.logging.Logger
import javax.inject.{Inject, Singleton}

import com.google.inject.AbstractModule
import com.qordoba.GetQConfig
import play.api.Configuration
import play.api.db.{Database, NamedDatabase}

import scala.concurrent.ExecutionContext


class ServiceModule extends AbstractModule {

  override def configure() = {
    // We bind the implementation to the interface (trait) as an eager singleton,
    // which means it is bound immediately when the application starts.
    bind(classOf[OnStartUp]).to(classOf[PerformOnStartUp]).asEagerSingleton()
  }
}


trait OnStartUp

@Singleton
class PerformOnStartUp @Inject()(
                                  configuration: Configuration,
                                  @NamedDatabase("reads") reads: Database,
                                  @NamedDatabase("updateable") updateable: Database,
                                  qConfig: GetQConfig
                                )(
                                  implicit executionContext: ExecutionContext
                                )
  extends OnStartUp {
  val logger = Logger.getLogger(this.getClass.getName)

  //Subscribe to pub-sub
  val manager = new StartUpConnection(qConfig)
  logger.info(manager.sayHi)

}

class StartUpConnection(
                         qReportingV2Config: GetQConfig
                       ) {

  val logger = Logger.getLogger(this.getClass.getName)

  val sayHi = s"StartUpConnection ... being setup; currently does nothing."

  /*
  // set subscriber id, eg. my-sub
  def subscriptionName: SubscriptionName = SubscriptionName.of(
    qReportingV2Config.config.googleProject,
    "Subscription Name Could Go Here"
  )

  var subscriber: Subscriber = null

  class PubSubMessagePuller extends MessageReceiver {
    def receiveMessage(message: PubsubMessage, consumer: AckReplyConsumer) {

      //TODO - acknowledge after all goes well??? Sounds like fun; until things back up
      consumer.ack()

      logger.info(s"Do something interesting when getting stuff from pub-sub")

    }
  }

  // create a subscriber bound to the asynchronous message receiver
  subscriber = Subscriber.newBuilder(subscriptionName, new PubSubMessagePuller).build
  subscriber.startAsync.awaitRunning()

  logger.info(s"${subscriber.toString}")

  try {
    lifecycle.addStopHook { () =>
      Future.successful {
        if (subscriber != null) subscriber.stopAsync
      }
    }
  } catch { case e: Exception =>
    Logger.error("Caught in ``- subscriber about to shut DOWN!!", e)
    if (subscriber != null) subscriber.stopAsync
  }*/

}
