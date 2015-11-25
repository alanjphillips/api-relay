package apirelay.config

import com.typesafe.config.{Config, ConfigFactory}

object ApplicationConfig {

  val config: Config = ConfigFactory.load()

  object HttpConfig {
    private val httpConfig = config.getConfig("http")

    lazy val interface = httpConfig.getString("interface")
    lazy val port = httpConfig.getInt("port")
  }

  object TwitterConfig {
    private val twitterConfig = config.getConfig("twitter")
    lazy val url = twitterConfig.getString("url")
    lazy val enabled = twitterConfig.getBoolean("enabled")
    lazy val consumerKey = twitterConfig.getString("consumer-key")
    lazy val consumerSecret = twitterConfig.getString("consumer-secret")
    lazy val userKey = twitterConfig.getString("user-key")
    lazy val userSecret = twitterConfig.getString("user-secret")
    lazy val userName = twitterConfig.getString("user-name")
  }

  object InstagramConfig {
    private val instagramConfig = config.getConfig("instagram")
    lazy val instagramHost = instagramConfig.getString("host")
    lazy val instagramPort = instagramConfig.getInt("port")
    lazy val callbackUrl = instagramConfig.getString("callback-url")
    lazy val recentMediaUrl = instagramConfig.getString("recent-media-url")
    lazy val subscriptionsUrl = instagramConfig.getString("subscriptions-url ")
    lazy val clientId = instagramConfig.getString("client-id")
    lazy val clientSecret = instagramConfig.getString("client-secret")
  }

  object SoundcloudConfig {
    private val soundcloudConfig = config.getConfig("soundcloud")
    lazy val soundcloudHost = soundcloudConfig.getString("host")
    lazy val soundcloudPort = soundcloudConfig.getInt("port")
    lazy val callbackUrl = soundcloudConfig.getString("callback-url")
    lazy val searchUrl = soundcloudConfig.getString("search-url")
    lazy val clientId = soundcloudConfig.getString("client-id")
    lazy val clientSecret = soundcloudConfig.getString("client-secret")

  }

}
