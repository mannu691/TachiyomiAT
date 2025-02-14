package eu.kanade.translation

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import eu.kanade.tachiyomi.R

class WebtoonTranslationsView : AbstractComposeView {

    private val translations: TextTranslations
    private val font: FontFamily
    private val translationOffset: TranslationOffset

    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : super(context, attrs, defStyleAttr) {
        this.translations = TextTranslations.EMPTY
        this.translationOffset = TranslationOffset()
        this.font = Font(
            resId = R.font.animeace, // Resource ID of the font file
            weight = FontWeight.Bold, // Weight of the font
        ).toFontFamily()
    }

    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        translations: TextTranslations,
        translationOffset: TranslationOffset? = null,
        font: FontFamily? = null,
    ) : super(context, attrs, defStyleAttr) {
        this.translations = translations
        this.translationOffset = translationOffset ?: TranslationOffset()
        this.font = font ?: Font(
            resId = R.font.animeace, // Resource ID of the font file
            weight = FontWeight.Bold, // Weight of the font
        ).toFontFamily()
    }

    @Composable
    override fun Content() {
        TranslationsContent(translations)
    }

    @Composable
    fun TranslationsContent(translations: TextTranslations) {
        var size by remember { mutableStateOf(IntSize.Zero) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    size = it
                    if (size == IntSize.Zero) hide()
                    else show()
                },
        ) {
            if (size == IntSize.Zero) return
            val scaleFactor = size.width / translations.imgWidth

            val offsetX = translationOffset.x.toFloat() / 100 - translationOffset.width.toFloat() / 200
            val offsetY = translationOffset.y.toFloat() / 100 - translationOffset.height.toFloat() / 200
            val heightMultiplier = 1 + translationOffset.height.toFloat() / 100
            val widthMultiplier = 1 + translationOffset.width.toFloat() / 100

            translations.translations.forEach { translation ->
                val xPx =
                    (translation.x - (translation.symWidth / 2) + (offsetX * (translation.width + translation.symWidth))) * scaleFactor
                val yPx =
                    (translation.y - (translation.symHeight / 2) + (offsetY * (translation.height + translation.symHeight))) * scaleFactor
                val width = (translation.width + translation.symWidth) * scaleFactor * widthMultiplier
                val height = (translation.height + translation.symHeight) * scaleFactor * heightMultiplier
                val bgWidth = (translation.width + translation.symWidth / 2) * scaleFactor
                val bgHeight = (translation.height + translation.symHeight / 2) * scaleFactor
                val bgX = (translation.x - translation.symWidth / 4) * scaleFactor
                val bgY = (translation.y - translation.symHeight / 4) * scaleFactor
                Box(
                    modifier = Modifier
                        .absoluteOffset(pxToDp(bgX), pxToDp(bgY))
                        .size(pxToDp(bgWidth), pxToDp(bgHeight))
                        .rotate(if (translation.angle < 88) translation.angle else 0f)
                        .background(Color.White, shape = RoundedCornerShape(4.dp)),

                    )
                TextBlock(
                    translation = translation,
                    modifier = Modifier
                        .absoluteOffset(pxToDp(xPx), pxToDp(yPx))
                        .size(pxToDp(width), pxToDp(height)),
                )
            }
        }
    }

    @Composable
    fun TextBlock(translation: BlockTranslation, modifier: Modifier) {
        Box(modifier = modifier) {
            AutoSizeText(
                text = translation.translated,
                color = Color.Black,
                softWrap = true, fontFamily = font,
                lineSpacingRatio = 1.2f,
                overflow = TextOverflow.Clip,
                alignment = Alignment.Center,
                modifier = Modifier
                    .rotate(if (translation.angle < 88) translation.angle else 0f),
            )
        }
    }

    fun show() {
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }

    private fun pxToDp(px: Float): Dp {
        return Dp(px / (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT))
    }
}