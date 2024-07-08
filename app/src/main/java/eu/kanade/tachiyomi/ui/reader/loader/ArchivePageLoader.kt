package eu.kanade.tachiyomi.ui.reader.loader

import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.ui.reader.model.ReaderChapter
import eu.kanade.tachiyomi.ui.reader.model.ReaderPage
import eu.kanade.tachiyomi.util.lang.compareToCaseInsensitiveNaturalOrder
import eu.kanade.translation.TextTranslations
import eu.kanade.translation.TranslationManager
import mihon.core.common.archive.ArchiveReader
import tachiyomi.core.common.util.system.ImageUtil
import tachiyomi.domain.manga.model.Manga

/**
 * Loader used to load a chapter from an archive file.
 */
internal class ArchivePageLoader(
    private val reader: ArchiveReader, private val chapter: ReaderChapter? = null, private val manga: Manga? = null,
    private val source: Source? = null, private val translationManager: TranslationManager? = null,
) : PageLoader() {
    override var isLocal: Boolean = true

    override suspend fun getPages(): List<ReaderPage> {
        val pageTranslations: Map<String, TextTranslations> =
            if (translationManager != null && chapter != null && manga != null && source != null) {
                translationManager.getChapterTranslation(
                    chapter.chapter.name,
                    chapter.chapter.scanlator,
                    manga.title,
                    source,
                )
            } else emptyMap()

        return reader.useEntries { entries ->
            entries
                .filter { it.isFile && ImageUtil.isImage(it.name) { reader.getInputStream(it.name)!! } }
                .sortedWith { f1, f2 -> f1.name.compareToCaseInsensitiveNaturalOrder(f2.name) }
                .mapIndexed { i, entry ->

                    ReaderPage(i).apply {
                        stream = { reader.getInputStream(entry.name)!! }
                        status = Page.State.READY
                        translations = pageTranslations[entry.name]
                    }
                }
                .toList()
        }
    }

    override suspend fun loadPage(page: ReaderPage) {
        check(!isRecycled)
    }

    override fun recycle() {
        super.recycle()
        reader.close()
    }
}
