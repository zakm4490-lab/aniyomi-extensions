package eu.kanade.tachiyomi.animeextension.en.sample

import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.animesource.online.ParsedHttpSource
import eu.kanade.tachiyomi.network.GET
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.Locale

abstract class BaseScraper : ParsedHttpSource() {

    override val supportsLatest = true

    override val client: OkHttpClient = network.client.newBuilder().build()

    override fun headersBuilder(): Headers.Builder = Headers.Builder()
        .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
        .add("Referer", baseUrl)

    // Popular
    override fun popularAnimeRequest(page: Int): Request = GET("$baseUrl/popular?page=$page", headers)
    override fun popularAnimeSelector(): String = "div.video-grid > div.video-card"
    override fun popularAnimeFromElement(element: Element): SAnime {
        val anime = SAnime.create()
        anime.setUrlWithoutDomain(element.select("a.card-link").attr("href"))
        anime.title = element.select("h3.card-title").text()
        anime.thumbnail_url = element.select("img.card-thumb").attr("abs:src")
        return anime
    }
    override fun popularAnimeNextPageSelector(): String? = "a.pagination-next:not(.disabled)"

    // Search
    override fun searchAnimeRequest(page: Int, query: String, filters: eu.kanade.tachiyomi.animesource.model.AnimeFilterList): Request {
        return GET("$baseUrl/search?q=$query&page=$page", headers)
    }
    override fun searchAnimeSelector(): String = "div.search-results > div.video-card"
    override fun searchAnimeFromElement(element: Element): SAnime = popularAnimeFromElement(element)
    override fun searchAnimeNextPageSelector(): String? = popularAnimeNextPageSelector()

    // Details
    override fun animeDetailsParse(document: Document): SAnime {
        val anime = SAnime.create()
        val infoBox = document.selectFirst("div.video-info")
        anime.title = infoBox?.selectFirst("h1.title")?.text() ?: ""
        anime.description = infoBox?.selectFirst("div.description")?.text()
        anime.genre = infoBox?.select("div.tags > a")?.joinToString(", ") { it.text() }
        anime.thumbnail_url = document.selectFirst("meta[property=og:image]")?.attr("content")
        anime.status = SAnime.COMPLETED
        return anime
    }

    // Episodes
    override fun episodeListSelector(): String = "ul.episode-list > li.episode-item"
    override fun episodeFromElement(element: Element): SEpisode {
        val episode = SEpisode.create()
        val link = element.select("a.episode-link")
        episode.setUrlWithoutDomain(link.attr("href"))
        episode.name = link.select("span.episode-title").text().ifBlank { "Episode 1" }
        return episode
    }

    // Video Links
    override fun videoListSelector(): String = "div.player-container"
    override fun videoFromElement(element: Element): Video {
        val src = element.attr("abs:src")
        return Video(src, "Default", src, headers = headers)
    }
    override fun videoUrlParse(document: Document): String = throw Exception("Not used")

    override fun videoListParse(response: Response): List<Video> {
        val document = response.asJsoup()
        val videoList = mutableListOf<Video>()
        document.select("iframe[src], video source[src]").forEach { tag ->
            val url = tag.attr("abs:src")
            if (url.isNotBlank()) videoList.add(Video(url, "Default", url, headers = headers))
        }
        return videoList
    }

    // Latest
    override fun latestUpdatesRequest(page: Int): Request = popularAnimeRequest(page)
    override fun latestUpdatesSelector(): String = popularAnimeSelector()
    override fun latestUpdatesFromElement(element: Element): SAnime = popularAnimeFromElement(element)
    override fun latestUpdatesNextPageSelector(): String? = popularAnimeNextPageSelector()
}
