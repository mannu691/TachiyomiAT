package eu.kanade.tachiyomi.ui.reader.model

import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.translation.TextTranslations
import java.io.InputStream

open class ReaderPage(
    index: Int,
    url: String = "",
    imageUrl: String? = null,
    var translations: TextTranslations? = null,
    var stream: (() -> InputStream)? = null,

    ) : Page(index, url, imageUrl, null) {

    open lateinit var chapter: ReaderChapter
}
