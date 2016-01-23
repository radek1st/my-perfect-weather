package services

import java.net.{URL, HttpURLConnection}

import scala.io.Source

object WeatherRetriever {

  val WEATHER_API_URL = "xxx"
  val WEATHER_AUTH = "xxx"
  val CLOUDANT_DB_URL = "xxx"
  val CLOUDANT_AUTH = "xxx"

  def main (args: Array[String]) {
    updateWeatherDataInCloudant
  }

  def updateWeatherDataInCloudant(): Unit = {
    def shorten(s: String): String = "%.2f".format(s.toDouble)
    def addCityMeta(city: String, airport: String, s: String): String = s.replaceFirst("\\{","\\{\"airport\":\"" + airport + "\",").replaceFirst("\\{","\\{\"_id\":\"" + city + "\",")

    val src = Source.fromFile("data/demo-cities.tab")
    val lines = src.getLines().drop(1).map(_.split("\t"))
    for(line <- lines) {
      val weather = getWeather(shorten(line(3)), shorten(line(4)))
      val weatherWithCity = addCityMeta(line(0), line(2), weather)
      val claudantResponse = postToCloudant(weatherWithCity)
      print(claudantResponse)
    }
  }

  def getWeather(lat: String, lon: String): String = {
    val url = s"${WEATHER_API_URL}/api/weather/v2/forecast/daily/10day?units=m&language=en-US&geocode=$lat,$lon"
    println(s"Getting weather from: $url")
    val connection = getConnection(url, WEATHER_AUTH)
    val inputStream = connection.getInputStream
    val content = Source.fromInputStream(inputStream).mkString
    if (inputStream != null) inputStream.close
    content
  }

  def postToCloudant(s: String): String = {
    val url = s"${CLOUDANT_DB_URL}/weather"
    println(s"Updating weather in Cloudant: $s")
    val connection = getConnection(url, CLOUDANT_AUTH)
    connection.setRequestMethod("POST")
    connection.setRequestProperty("Content-Type","application/json")
    connection.setDoOutput(true)
    connection.getOutputStream().write(s.getBytes)
    val inputStream = connection.getInputStream
    val content = Source.fromInputStream(inputStream).mkString
    if (inputStream != null) inputStream.close
    content
  }

  def searchMatchingWeather(q: String): String = {
    val url = s"${CLOUDANT_DB_URL}/parsed_weather/_design/match/_search/searchAll?q=" + q
    println("SEARCHING: " + url)
    val connection = getConnection(url, CLOUDANT_AUTH)
    val inputStream = connection.getInputStream
    val content = Source.fromInputStream(inputStream).mkString
    if (inputStream != null) inputStream.close
    content
  }

  def searchMatchingCities(q: String): String = {
    val url = s"${CLOUDANT_DB_URL}/parsed_weather/_design/match/_search/searchCities?q=" + q
    println("SEARCHING: " + url)
    val connection = getConnection(url, CLOUDANT_AUTH)
    val inputStream = connection.getInputStream
    val content = Source.fromInputStream(inputStream).mkString
    if (inputStream != null) inputStream.close
    content
  }

  def getConnection(url: String, auth: String): HttpURLConnection = {
    val connection = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
    connection.setConnectTimeout(15000)
    connection.setReadTimeout(15000)
    connection.setRequestProperty ("Authorization", s"Basic $auth")
    connection.setRequestMethod("GET")
    connection
  }
}
