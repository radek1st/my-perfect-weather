package models

import play.api.libs.json.Json

case class WeatherRequest(minTemp: Option[Int],
                          maxTemp: Option[Int],
                          maxWspd: Option[Int],
                          minWspd: Option[Int],
                          minPop: Option[Int],
                          maxPop: Option[Int],
                          precipType: Option[String])

object WeatherJsonFormats {
  implicit val weatherFormat = Json.format[WeatherRequest]
}