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
    lazy val twitterHost = twitterConfig.getString("host")
    lazy val twitterPort = twitterConfig.getInt("port")
    lazy val oauthTokenUrl = twitterConfig.getString("oauth2-token-url")
    lazy val userTimelineUrl = twitterConfig.getString("user-timeline-url")
    lazy val enabled = twitterConfig.getBoolean("enabled")
    lazy val consumerKey = twitterConfig.getString("consumer-key")
    lazy val consumerSecret = twitterConfig.getString("consumer-secret")
    lazy val accessToken = twitterConfig.getString("access-token")
    lazy val accessTokenSecret = twitterConfig.getString("access-token-secret")
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

  object UberConfig {
    private val uberConfig = config.getConfig("uber")
    lazy val serverToken = uberConfig.getString("server-token")
    lazy val uberHost = uberConfig.getString("host")
    lazy val uberPort = uberConfig.getInt("port")
    lazy val productsUrl = uberConfig.getString("products-url")
  }

}
