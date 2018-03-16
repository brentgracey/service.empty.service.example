package com.qordoba

import javax.inject.{Inject, Singleton}

import org.joda.time.DateTime
import play.api.mvc._
import play.api.{Configuration, Logger}


@Singleton
class Version @Inject()(configuration: Configuration,
                        qReportingV2Config: GetQConfig) extends InjectedController {
  def version() = Action { implicit request =>

    val nowIs = DateTime.now()
    //Touch config to encourage failing early - helps dev ops smoke test.
    Logger.info(s"We are up - `version` api; re-checking config settings: ${qReportingV2Config.config}")

    Ok(
      s"""
        |CHECK YOUR LOGS FOR CONFIG DETAILS - search for: `GetQConfig`, `QConfigSettings`
        |
        |  toString:    `${nowIs.toString}`
        |  toLocalDate: `${nowIs.toLocalDate}`
        |  getMillis:   `${nowIs.getMillis}`
        |
        | `the start`
        |
        | ${qReportingV2Config.whoami} <- this must be the same for the lifecycle of a server (@Singleton)
        |
      """.stripMargin)
  }
}

