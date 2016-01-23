package controllers

import play.api.libs.json._
import models.WeatherRequest
import models.WeatherJsonFormats.weatherFormat
import play.api.mvc.{Action, Controller}
import services.WeatherRetriever
import scala.collection.mutable.ListBuffer

class Weather extends Controller {

  def search = Action { implicit request =>
    request.body.asJson.map { json =>
      json.validate[WeatherRequest].map {
        case w: WeatherRequest => {
          val citiesMatched = toCitiesMatched(WeatherRetriever.searchMatchingCities(toSearchString(w)))
          if(citiesMatched.size == 0) Ok("")
          else {
            val topHits = WeatherRetriever.searchMatchingWeather(toSearchString(citiesMatched.keys))
            val topHitsProccesed = Json.toJson(toWeatherMatched(topHits, citiesMatched))
            println(topHitsProccesed)
            Ok(topHitsProccesed)
          }
        }
      }.recoverTotal {
        e => BadRequest("Detected error:" + JsError.toFlatForm(e))
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }

  }

  def toWeatherMatched(s: String, daysMatched: Map[String,Seq[Int]]): ListBuffer[JsValue] = {
    val json = Json.parse(s)
    val cities = (json \\ "rows")
    val citiesBuffer = new ListBuffer[JsValue]
    for(city <- cities) {
      val rows = (city \\ "fields")
      val fieldsBuffer = new ListBuffer[JsObject]
      var count = 0
      for(row <- rows) {
        val cityField = (row \ "city").as[String]
        val numField = (row \ "num").as[Int]
        val isPerfectDay = if(daysMatched(cityField).contains(numField)) true else false
        if(isPerfectDay) count += 1
        fieldsBuffer += row.as[JsObject] + ("perfect_weather" -> Json.toJson(isPerfectDay))
      }
      val fieldsWithCounts: JsValue = Json.obj(
        "perfect_count" -> count,
        "city" -> (fieldsBuffer(0) \ "city").as[String],
        "fields" -> fieldsBuffer)
      citiesBuffer += fieldsWithCounts
    }
    citiesBuffer
  }

  def toCitiesMatched(s: String): Map[String,Seq[Int]] = {
    val json = Json.parse(s)
    val ids = (json \\ "id")
    val m = for {
      id <- ids
      x = id.as[String].split("-")
    } yield (x(0), x(1))
    m.groupBy(x => x._1).map(x => (x._1, x._2.map(y => y._2.toInt)))
  }

  def toSearchString(cities: Iterable[String]): String = {
    cities.map(c => c.split(" ")(0)). mkString("city:",",","&group_field=city")
  }

  def toSearchString(w: WeatherRequest): String = {
    val sb = new StringBuilder

    if(w.precipType.isDefined) {
      if (w.precipType.get.equalsIgnoreCase("snow")) {
        //'precip' seems to be snow with rain, so treat it as snow
        sb.append(s"precip_type:snow,precip")
      } else sb.append(s"precip_type:${w.precipType.get}")
    } else sb.append(s"precip_type:rain,snow,precip")
    if(w.maxTemp.isDefined) sb.append(s" AND max_temp:[-Infinity TO ${w.maxTemp.get}]")
    if(w.minTemp.isDefined) sb.append(s" AND min_temp:[${w.minTemp.get} TO Infinity]")
    if(w.minPop.isDefined) sb.append(s" AND pop:[${w.minPop.get} TO 100]")
    if(w.maxPop.isDefined) sb.append(s" AND pop:[0 TO ${w.maxPop.get}]")
    val minWind = if(w.minWspd.isDefined) w.minWspd.get else "0"
    val maxWind = if(w.maxWspd.isDefined) w.maxWspd.get else "Infinity"
    sb.append(s" AND wspd:[${minWind} TO ${maxWind}]")
    sb.append("&group_field=city")
    println(sb.replaceAllLiterally(" ", "%20"))
    sb.replaceAllLiterally(" ", "%20")
  }
}
