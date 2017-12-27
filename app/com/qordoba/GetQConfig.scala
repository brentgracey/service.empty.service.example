package com.qordoba

import java.util.UUID
import java.util.logging.Logger
import javax.inject.{Inject, Singleton}

import com.google.cloud.bigtable.hbase.BigtableConfiguration
import org.apache.hadoop.hbase.client.Connection
import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

/**
  * We need `1` of these; just after start-up; available to all controllers & all calls to those
  * 1 and only `1`
  */
class QConfigSettings(
                                  val k8sName: String,
                                  val googleProject: String,
                                  val bigTableConnection: Connection,
                                  val ws: WSClient,
                                  val defaultEc: ExecutionContext
                                ) {
  override def toString() =
    s"""
       | QConfigSettings.k8sName:                 `${k8sName}`,
       | QConfigSettings.googleProject:           `${googleProject}`,
       | QConfigSettings.bigTableConnection:      `${bigTableConnection}`,
       | QConfigSettings.ws:                      `${ws}`,
       | QConfigSettings.defaultEc:               `${defaultEc}`,""".stripMargin
}

object LocalLaptop {
  val development = "local-laptop-development"
}

object GoogleProjects {
  val dev = "qordoba-devel"
  val test = "strange-cosmos-822"
  val marketing = "qordoba-marketing"
  val prod = "qordoba-prod"
}

/**
  * Checks `magic` environment variables set in k8s Service Object definitions
  * -> when doing local laptop based development - these environment variables are note set <- config picks special values to support this
  * -> In a Google Project based K8s cluster; code knows 'who it is' and can setup its own config pretty darn well.
  */
@Singleton
class GetQConfig @Inject()(ws: WSClient, configuration: Configuration) {

  private val logger: Logger = Logger.getLogger(this.getClass.getName)
  //Just for cross-checking we only ever get 1 instance of this
  //Makes it obvious to new dev's in the code ... there is only 1 instance created
  val whoami = s"${UUID.randomUUID().toString}"

  /**
    * THESE ARE THE MAGIC environment variables NAMES - set in K8s Yaml configs
    * https://github.com/Qordobacode/qordoba.k8s/blob/prod-3/deploy/reporting-v2/reporting-v2-dep.yaml#L29 (or latest prod env branch)
    */
  private val qordobaGoogleProjectNameKey = "QORDOBA_GOOGLE_PROJECT_NAME"
  private val qordobaGoogleK8sNameKey = "QORDOBA_K8S_NAME"

  private lazy val googleProject: String = Option(System.getenv(qordobaGoogleProjectNameKey)).getOrElse({
    logger.info(s"No ${qordobaGoogleProjectNameKey} environment variable; defaulting `googleProject` to ${GoogleProjects.dev}")
    GoogleProjects.dev
  })

  private lazy val k8sName: String = Option(System.getenv(qordobaGoogleK8sNameKey)).getOrElse({
    logger.info(s"${qordobaGoogleK8sNameKey} environment variable; defaulting `k8sName` to ${LocalLaptop.development}")
    LocalLaptop.development
  })


  private val bigTableInstanceId: String = configuration.getString("bigTableInstanceId").getOrElse {
    logger.info("No `bigTableInstanceId` (that's fine) - running off sane default of `history`.")

    "qor-dw"
  }

  //Maybe lazy is not needed here ... due to DI + its Singleton annotation
  lazy val config: QConfigSettings= {

    new QConfigSettings(
      k8sName = k8sName,
      googleProject = googleProject,
      bigTableConnection = BigtableConfiguration.connect(googleProject, bigTableInstanceId),
      ws = ws,
      defaultEc = scala.concurrent.ExecutionContext.Implicits.global
    )
  }

  //Leave this down here to avoid null points; really scala? :(
  logger.info(s"${whoami} - REALLY - only once per start-up. \n ${config}")
}
