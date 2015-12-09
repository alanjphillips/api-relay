package apirelay.util

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64


trait HttpUtils {

  def toQueryString(queryMap: Map[String,String]) = "?"+queryMap.map{
    case (key,value) => s"$key=" + URLEncoder.encode(s"$value", "UTF-8")
  }.mkString("&")

  def base64Encode(toEncode: String) = {
    val encoder:Base64.Encoder = Base64.getEncoder()
    encoder.encodeToString(toEncode.getBytes(StandardCharsets.UTF_8))
  }

  def utf8Encode(value: String): String = URLEncoder.encode(value, "UTF-8")

}
