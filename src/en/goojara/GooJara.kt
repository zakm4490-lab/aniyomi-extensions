package eu.kanade.tachiyomi.animeextension.en.goojara

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

class GooJara : ParsedHttpSource() {

    override val name = "GooJara"
    override val baseUrl = "https://ww1.goojara.to"
    override val lang = "en"
    override val supportsLatest = true

    override val client: OkHttpClient = network.client

    override fun headersBuilder(): Headers.Builder = Headers.Builder()
        .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        .add("Referer", baseUrl)

    // Popular Movies/Shows
    override fun popularAnimeRequest(page: Int): Request = GET("$baseUrl/watch-popular?page=$page")
    override fun popularAnimeSelector(): String = "div.df, div.mitem"
    override fun popularAnimeFromElement(element: Element): SAnime = SAnime.create().apply {
        setUrlWithoutBaseUrl(element.select("a").attr("href"))
        title = element.select("a strong, .title, a").text().trim()
        thumbnail_url = element.select("img").attr("abs:src").ifEmpty { element.select("img").attr("abs:data-src") }
    }
    override fun popularAnimeNextPageSelector(): String = "a.next, div.mop a"

    // Latest Content
    override fun latestUpdatesRequest(page: Int): Request = GET("$baseUrl/watch-recent?page=$page")
    override fun latestUpdatesSelector(): String = popularAnimeSelector()
    override fun latestUpdatesFromElement(element: Element): SAnime = popularAnimeFromElement(element)
    override fun latestUpdatesNextPageSelector(): String = popularAnimeNextPageSelector()

    // Search Content
    override fun searchAnimeRequest(page: Int, query: String, filters: AnimeFilterList): Request =
        GET("$baseUrl/search?q=$query")
    override fun searchAnimeSelector(): String = popularAnimeSelector()
    override fun searchAnimeFromElement(element: Element): SAnime = popularAnimeFromElement(element)
    override fun searchAnimeNextPageSelector(): String = popularAnimeNextPageSelector()

    // Details
    override fun animeDetailsParse(document: Document): SAnime = SAnime.create().apply {
        title = document.select("h1, div.mhd h1").text().trim()
        genre = document.select("div.mft a").joinToString(", ") { it.text() }
        description = document.select("div.mdesc, p").text().trim()
        thumbnail_url = document.select("div.mimg img").attr("abs:src")
    }

    // Episodes / Streams List
    override fun episodeListSelector(): String = "div.seasons a, ul.episodes li a, div.se-c a"
    override fun episodeFromElement(element: Element): SEpisode = SEpisode.create().apply {
        setUrlWithoutBaseUrl(element.attr("href"))
        name = element.text().trim().ifEmpty { "Play Movie / Episode" }
    }

    // Video Embeds
    override fun videoListSelector(): String = "iframe, source, video"
    override fun videoFromElement(element: Element): Video {
        val streamUrl = element.attr("abs:src")
        return Video(streamUrl, "GooJara Server", streamUrl)
    }
    override fun videoUrlParse(document: Document): String = ""
}
