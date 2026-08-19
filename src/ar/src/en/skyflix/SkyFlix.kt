package eu.kanade.tachiyomi.animeextension.en.skyflix

import eu.kanade.tachiyomi.animesource.model.AnimeFilterList
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.animesource.online.ParsedHttpSource
import eu.kanade.tachiyomi.network.GET
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class SkyFlix : ParsedHttpSource() {

    override val name = "SkyFlix"
    override val baseUrl = "https://skyflix.to"
    override val lang = "en"
    override val supportsLatest = true

    override val client: OkHttpClient = network.client

    override fun headersBuilder(): Headers.Builder = Headers.Builder()
        .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        .add("Referer", baseUrl)

    // Popular Content
    override fun popularAnimeRequest(page: Int): Request = GET("$baseUrl/trending?page=$page")
    override fun popularAnimeSelector(): String = "div.flw-item, div.movie-card, article.item"
    override fun popularAnimeFromElement(element: Element): SAnime = SAnime.create().apply {
        setUrlWithoutBaseUrl(element.select("a").attr("href"))
        title = element.select("h2, h3, .film-name, .title").text().trim()
        thumbnail_url = element.select("img").attr("abs:src").ifEmpty { element.select("img").attr("abs:data-src") }
    }
    override fun popularAnimeNextPageSelector(): String = "a[rel=next], ul.pagination li.next a"

    // Latest Content
    override fun latestUpdatesRequest(page: Int): Request = GET("$baseUrl/latest-movies?page=$page")
    override fun latestUpdatesSelector(): String = popularAnimeSelector()
    override fun latestUpdatesFromElement(element: Element): SAnime = popularAnimeFromElement(element)
    override fun latestUpdatesNextPageSelector(): String = popularAnimeNextPageSelector()

    // Search
    override fun searchAnimeRequest(page: Int, query: String, filters: AnimeFilterList): Request =
        GET("$baseUrl/search/$query?page=$page")
    override fun searchAnimeSelector(): String = popularAnimeSelector()
    override fun searchAnimeFromElement(element: Element): SAnime = popularAnimeFromElement(element)
    override fun searchAnimeNextPageSelector(): String = popularAnimeNextPageSelector()

    // Details
    override fun animeDetailsParse(document: Document): SAnime = SAnime.create().apply {
        title = document.select("h1, h2.heading-name").text().trim()
        genre = document.select("div.genres a, div.row-line:contains(Genre) a").joinToString(", ") { it.text() }
        description = document.select("div.description, div.detail-desc").text().trim()
        thumbnail_url = document.select("div.poster img, div.film-poster img").attr("abs:src")
    }

    // Episodes / Streams
    override fun episodeListSelector(): String = "div.episodes-ul a, ul.episodes li a, div.server-item a"
    override fun episodeFromElement(element: Element): SEpisode = SEpisode.create().apply {
        setUrlWithoutBaseUrl(element.attr("href"))
        name = element.text().trim().ifEmpty { "Play Video" }
    }

    // Video Streams
    override fun videoListSelector(): String = "iframe, source"
    override fun videoFromElement(element: Element): Video {
        val streamUrl = element.attr("abs:src")
        return Video(streamUrl, "SkyFlix Server", streamUrl)
    }
    override fun videoUrlParse(document: Document): String = ""
}
