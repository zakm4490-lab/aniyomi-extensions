package eu.kanade.tachiyomi.animeextension.ar.animedar

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

class AnimeDar : ParsedHttpSource() {

    override val name = "AnimeDar"
    override val baseUrl = "https://animedar.tv"
    override val lang = "ar"
    override val supportsLatest = true

    override val client: OkHttpClient = network.client

    override fun headersBuilder(): Headers.Builder = Headers.Builder()
        .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        .add("Referer", baseUrl)

    // Popular Anime
    override fun popularAnimeRequest(page: Int): Request = GET("$baseUrl/anime-list/page/$page/")
    override fun popularAnimeSelector(): String = "div.anime-card, div.poster-card, article.item"
    override fun popularAnimeFromElement(element: Element): SAnime = SAnime.create().apply {
        setUrlWithoutBaseUrl(element.select("a").attr("href"))
        title = element.select("h2, h3, .title").text().trim()
        thumbnail_url = element.select("img").attr("abs:src").ifEmpty { element.select("img").attr("abs:data-src") }
    }
    override fun popularAnimeNextPageSelector(): String = "a.next, div.pagination a:contains(Next)"

    // Latest Updates
    override fun latestUpdatesRequest(page: Int): Request = GET("$baseUrl/episodes/page/$page/")
    override fun latestUpdatesSelector(): String = popularAnimeSelector()
    override fun latestUpdatesFromElement(element: Element): SAnime = popularAnimeFromElement(element)
    override fun latestUpdatesNextPageSelector(): String = popularAnimeNextPageSelector()

    // Search Anime
    override fun searchAnimeRequest(page: Int, query: String, filters: AnimeFilterList): Request =
        GET("$baseUrl/?s=$query")
    override fun searchAnimeSelector(): String = popularAnimeSelector()
    override fun searchAnimeFromElement(element: Element): SAnime = popularAnimeFromElement(element)
    override fun searchAnimeNextPageSelector(): String = popularAnimeNextPageSelector()

    // Anime Details
    override fun animeDetailsParse(document: Document): SAnime = SAnime.create().apply {
        title = document.select("h1.title, h1.entry-title").text().trim()
        genre = document.select("div.genres a, span.genre a").joinToString(", ") { it.text() }
        description = document.select("div.description, div.story, p.synopsis").text().trim()
        thumbnail_url = document.select("div.poster img, img.thumbnail").attr("abs:src")
    }

    // Episodes
    override fun episodeListSelector(): String = "ul.episodes-list li, div.episode-card"
    override fun episodeFromElement(element: Element): SEpisode = SEpisode.create().apply {
        setUrlWithoutBaseUrl(element.select("a").attr("href"))
        name = element.select("span.ep-num, a").text().trim()
    }

    // Video Streams
    override fun videoListSelector(): String = "iframe, source"
    override fun videoFromElement(element: Element): Video {
        val streamUrl = element.attr("abs:src")
        return Video(streamUrl, "AnimeDar Server", streamUrl)
    }
    override fun videoUrlParse(document: Document): String = ""
}
