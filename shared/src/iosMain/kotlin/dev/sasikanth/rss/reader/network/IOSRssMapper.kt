package dev.sasikanth.rss.reader.network

import dev.sasikanth.rss.reader.models.FeedPayload
import dev.sasikanth.rss.reader.models.PostPayload
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import platform.Foundation.NSDateFormatter
import platform.Foundation.timeIntervalSince1970

private val offsetTimezoneDateFormatter =
  NSDateFormatter().apply { dateFormat = "E, d MMM yyyy HH:mm:ss Z" }
private val abbrevTimezoneDateFormatter =
  NSDateFormatter().apply { dateFormat = "E, d MMM yyyy HH:mm:ss z" }

internal fun PostPayload.Companion.mapRssPost(rssMap: Map<String, String>): PostPayload {
  val pubDate = rssMap["pubDate"]
  val link = rssMap["link"]
  val description = rssMap["description"]
  val imageUrl: String? = rssMap["imageUrl"]

  return PostPayload(
    title = FeedParser.cleanText(rssMap["title"])!!,
    link = FeedParser.cleanText(link)!!,
    description = FeedParser.cleanTextCompact(description).orEmpty(),
    imageUrl = imageUrl,
    date = pubDate.rssDateStringToEpochSeconds()
  )
}

internal fun FeedPayload.Companion.mapRssFeed(
  feedUrl: String,
  rssMap: Map<String, String>,
  posts: List<PostPayload>
): FeedPayload {
  val link = rssMap["link"]!!.trim()
  val domain = Url(link)
  val iconUrl =
    FeedParser.feedIcon(
      if (domain.host != "localhost") domain.host
      else domain.pathSegments.first().split(" ").first().trim()
    )

  return FeedPayload(
    name = FeedParser.cleanText(rssMap["title"])!!,
    homepageLink = link,
    link = feedUrl,
    description = FeedParser.cleanText(rssMap["description"])!!,
    icon = iconUrl,
    posts = posts
  )
}

private fun String?.rssDateStringToEpochSeconds(): Long {
  if (this.isNullOrBlank()) return 0L

  val date =
    try {
      offsetTimezoneDateFormatter.dateFromString(this.trim())
    } catch (e: Exception) {
      try {
        abbrevTimezoneDateFormatter.dateFromString(this.trim())
      } catch (e: Exception) {
        Napier.e("Parse date error: ${e.message}")
        null
      }
    }

  return date?.timeIntervalSince1970?.times(1000)?.toLong() ?: 0L
}