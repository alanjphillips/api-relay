package apirelay.util

import akka.http.scaladsl.model.headers.RawHeader
import apirelay.config.ApplicationConfig.{InstagramConfig, TwitterConfig, UberConfig}


trait ApiUtils extends HttpUtils {

  def createInstagramSubscriptionFormData: Map[String,String] = {
    Map(
      "client_id" -> InstagramConfig.clientId,
      "client_secret" ->  InstagramConfig.clientSecret,
      "object" -> "user",
      "aspect" -> "media",
      "verify_token" -> "myVerifyToken",
      "callback_url" -> InstagramConfig.callbackUrl
    )
  }

  def uberGetProductsRequestParams: Map[String,String] = {
    Map(
      "server_token" -> UberConfig.serverToken,
      "latitude" -> "51.531679",
      "longitude" -> "-0.124400"
    )
  }

  def twitterGetOAuth2BearerTokenFormData: Map[String,String] = {
    Map(
      "grant_type" -> "client_credentials"
    )
  }

  def twitterGetOAuth2BearerTokenHeaders: List[RawHeader] = {
    List (
      RawHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"),
      RawHeader("Authorization", "Basic " + base64Encode(utf8Encode(TwitterConfig.consumerKey) + ":" + utf8Encode(TwitterConfig.consumerSecret)))
    )
  }

  def twitterGetUserTimelineHeaders(token: String): List[RawHeader] = {
    List (
      RawHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"),
      RawHeader("Authorization", "Bearer " + token)
    )
  }

  def twitterGetUserTimelineRequestParams(count: String, screenName: String): Map[String,String] = {
    Map(
      "count" -> count,
      "screen_name" -> screenName
    )
  }

}
