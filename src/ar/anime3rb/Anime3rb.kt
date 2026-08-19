package eu.kanade.tachiyomi.animeextension.ar.anime3rb

import eu.kanade.tachiyomi.animesource.model.AnimeFilterList
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

class Anime3rb : ParsedHttpSource() {

    override val name = "Anime3rb"
    override val baseUrl = "https://anime3rb.com"
    override val lang = "ar"
    override val supportsLatest = true

    override val client: OkHttpClient = network.client

    override fun headersBuilder(): Headers.Builder = Headers.Builder()
        .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        .add("Referer", baseUrl)

    // Popular Anime
    override fun popularAnimeRequest(page: Int): Request = GET("$baseUrl/anime-list?page=$page")
    override fun popularAnimeSelector(): String = "div.anime-card, div.col-md-3, article.card"
    override fun popularAnimeFromElement(element: Element): SAnime = SAnime.create().apply {
        setUrlWithoutBaseUrl(element.select("a").attr("href"))
        title = element.select("h2, h3, .title, .card-title").text().trim()
        thumbnail_url = element.select("img").attr("abs:src").ifEmpty { element.select("img").attr("abs:data-src") }
    }
    override fun popularAnimeNextPageSelector(): String = "a[rel=next], li.next a"

    // Latest Updates
    override fun latestUpdatesRequest(page: Int): Request = GET("$baseUrl/episodes?page=$page")
    override fun latestUpdatesSelector(): String = popularAnimeSelector()
    override fun latestUpdatesFromElement(element: Element): SAnime = popularAnimeFromElement(element)
    override fun latestUpdatesNextPageSelector(): String = popularAnimeNextPageSelector()

    // Search Anime
    override fun searchAnimeRequest(page: Int, query: String, filters: AnimeFilterList): Request =
        GET("$baseUrl/search?q=$query")
    override fun searchAnimeSelector(): String = popularAnimeSelector()
    override fun searchAnimeFromElement(element: Element): SAnime = popularAnimeFromElement(element)
    override fun searchAnimeNextPageSelector(): String = popularAnimeNextPageSelector()

    // Anime Details
    override fun animeDetailsParse(document: Document): SAnime = SAnime.create().apply {
        title = document.select("h1, h1.title").text().trim()
        genre = document.select("div.genres a, span.badge").joinToString(", ") { it.text() }
        description = document.select("p.story, div.description, p.synopsis").text().trim()
        thumbnail_url = document.select("div.poster img, img.img-fluid").attr("abs:src")
    }

    // Episodes
    override fun episodeListSelector(): String = "div.episodes-list a, ul.episodes li a"
    override fun episodeFromElement(element: Element): SEpisode = SEpisode.create().apply {
        setUrlWithoutBaseUrl(element.attr("href"))
        name = element.text().trim()
    }

    // Video Streams
    override fun videoListSelector(): String = "iframe, source"
    override fun videoFromElement(element: Element): Video {
        val streamUrl = element.attr("abs:src")
        return Video(streamUrl, "Anime3rb Server", streamUrl)
    }
    override fun videoUrlParse(document: Document): String = ""
}
