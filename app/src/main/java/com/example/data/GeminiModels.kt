package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class TrickySolverResponse(
    val analysis: String,
    val option1Label: String,
    val option1Text: String,
    val option2Label: String,
    val option2Text: String,
    val option3Label: String,
    val option3Text: String
)

@JsonClass(generateAdapter = true)
data class GussaRegulatorResponse(
    val analysis: String,
    val option1Label: String,
    val option1Text: String,
    val option2Label: String,
    val option2Text: String,
    val option3Label: String,
    val option3Text: String
)

@JsonClass(generateAdapter = true)
data class ReplyAnalyzerResponse(
    val analysis: String,
    val option1Label: String,
    val option1Text: String,
    val option2Label: String,
    val option2Text: String,
    val option3Label: String,
    val option3Text: String,
    val option4Label: String,
    val option4Text: String
)

@JsonClass(generateAdapter = true)
data class ThemeColorsResponse(
    val primaryHex: String,
    val secondaryHex: String,
    val tertiaryHex: String,
    val textDarkHex: String,
    val bgLightHex: String,
    val cardBgHex: String,
    val alertBgHex: String,
    val alertBorderHex: String
)
