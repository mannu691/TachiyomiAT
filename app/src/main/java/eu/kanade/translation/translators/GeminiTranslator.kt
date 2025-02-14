package eu.kanade.translation.translators

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import eu.kanade.translation.BlockTranslation
import eu.kanade.translation.TextTranslations
import org.json.JSONObject
import tachiyomi.core.common.util.system.logcat
import java.util.Locale

class GeminiTranslator(private val langFrom: ScanLanguage, private val langTo: Locale, private var apiKey: String) :
    LanguageTranslator {
    private var model: GenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-pro",
        apiKey = apiKey,
        generationConfig = generationConfig {
            topK = 30
            topP = 0.5f
            maxOutputTokens = 8192
            responseMimeType = "application/json"
        },
        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE),
        ),
        systemInstruction = content {
            text(
                "## System Prompt for Manhwa/Manga/Manhua Translation\n" +
                    "\n" +
                    "You are a highly skilled AI tasked with translating text from scanned images of comics (manhwa, manga, manhua) while preserving the original structure and removing any watermarks or site links.\n" +
                    "\n" +
                    "**Here's how you should operate:**\n" +
                    "\n" +
                    "1. **Input:** You'll receive a JSON object where keys are image filenames (e.g., \"001.jpg\") and values are lists of text strings extracted from those images.\n" +
                    "\n" +
                    "2. **Translation:** Translate all text strings to the target language `${langTo.displayLanguage}`. Ensure the translation is natural and fluent, adapting idioms and expressions to fit the target language.\n" +
                    "\n" +
                    "3. **Watermark/Site Link Removal:** Replace any watermarks or site links (e.g., \"colamanga.com\") with the placeholder \"RTMTH\".\n" +
                    "\n" +
                    "4. **Structure Preservation:** Maintain the exact same structure as the input JSON. The output JSON should have the same number of keys (image filenames) and the same number of text strings per key.\n" +
                    "\n" +
                    "**Example:**\n" +
                    "\n" +
                    "**Input:**\n" +
                    "\n" +
                    "```json\n" +
                    "{\"001.jpg\":[\"chinese1\",\"chinese2\"],\"002.jpg\":[\"chinese2\",\"colamanga.com\"]}\n" +
                    "```\n" +
                    "\n" +
                    "**Output (for `${langTo.displayLanguage}` = English):**\n" +
                    "\n" +
                    "```json\n" +
                    "{\"001.jpg\":[\"eng1\",\"eng2\"],\"002.jpg\":[\"eng2\",\"RTMTH\"]}\n" +
                    "```\n" +
                    "\n" +
                    "**Key Points:**\n" +
                    "\n" +
                    "* Prioritize accurate and natural-sounding translations.\n" +
                    "* Be meticulous in removing all watermarks and site links.\n" +
                    "* Ensure the output JSON structure perfectly mirrors the input structure."
            )
        },
    )

    override suspend fun translate(pages: HashMap<String, TextTranslations>) {
        try {
            val data = pages.mapValues { (k, v) -> v.translations.map { b -> b.text } }
            val json = JSONObject(data)
            val response = model.generateContent(json.toString())
            val resJson = JSONObject(response.text)
            for ((k, v) in pages) {
                v.translations.forEachIndexed { i, b ->
                    val res = resJson.optJSONArray(k)?.optString(i, "NULL")
                    b.translated = if (res == null || res == "NULL") b.text else res
                }
                v.translations = v.translations.filterNot { it.translated.contains("RTMTH") } as ArrayList<BlockTranslation>
            }

        } catch (e: Exception) {
            logcat { "Image Translation Error : ${e.stackTraceToString()}" }
        }

    }
}