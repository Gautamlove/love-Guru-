package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiRepository {
    private val apiService = RetrofitClient.service
    private val moshi: Moshi = RetrofitClient.getMoshi()

    private val baseSystemInstruction = """
        You are "Love Guru," an ultra-emotionally intelligent, witty, and highly practical relationship mentor.
        Your mission is to help the user navigate any situation involving their partner, crush, or girl bestie.
        You excel at reading between the lines, diffusing tension, and crafting the perfect responses.
        
        YOUR PERSONA & TONE:
        - Warm, supportive, and non-judgmental, but realistic and honest.
        - Lighthearted and witty, using humor to ease the user's anxiety.
        - Natively fluent in English, Hindi, and Hinglish (mixing Hindi words with Latin/English script, e.g., "Chill yaar, gussa thanda ho jayega"). Match the user's language style automatically.
        - NEVER give massive walls of text. Be punchy, structured, and easy to read.
        - CRITICAL: You must excel at understanding broken English, heavily abbreviated words, dry chat slang, and Hinglish phrasing (e.g. "gussa", "naraaz", "bf", "gf", "ex", "bestie", "fine", "k", "hmm", "do whatever", "moti", "patli").
        - Even if the input is extremely short (e.g., just "she angry" or "k"), decode it with high emotional intelligence, assuming the context correctly and giving outstanding guidance and replies.
    """.trimIndent()

    suspend fun solveTrickyQuestion(question: String): Result<TrickySolverResponse> = withContext(Dispatchers.IO) {
        val prompt = """
            The user's partner asked this tricky/trap question: "$question"
            
            Perform the following tasks:
            1. Deconstruct why this question is tricky (identify the emotion, insecurity, or expectation behind it).
            2. Generate 3 distinct response options:
               - Smooth/Charming (dil jeetne wala)
               - Safe & Honest (balanced and secure)
               - Playful/Witty (mazaak-mazaak mein baat ghumana)
               
            You MUST match the user's input language style (English, Hindi, or Hinglish) for the response texts.
            For example, if they ask in Hinglish, provide Hinglish suggestions!
            
            Return your response STRICTLY as a JSON object matching this schema:
            {
              "analysis": "Deconstruction of why the question is tricky (emotion/expectation behind it)",
              "option1Label": "Smooth & Charming (Dil Jeetne Wala)",
              "option1Text": "Actual response text",
              "option2Label": "Safe & Honest (Balanced & Secure)",
              "option2Text": "Actual response text",
              "option3Label": "Playful & Witty (Mazaak-Mazaak)",
              "option3Text": "Actual response text"
            }
        """.trimIndent()

        val systemInstruction = "$baseSystemInstruction\nOutput must be a single raw JSON object matching the requested schema. No conversational preamble before or after the JSON."

        executeAndParse(prompt, systemInstruction, TrickySolverResponse::class.java)
    }

    suspend fun regulateGussa(situation: String): Result<GussaRegulatorResponse> = withContext(Dispatchers.IO) {
        val prompt = """
            The user's partner is angry or giving them the silent treatment. Here is the situation: "$situation"
            
            Perform the following tasks:
            1. Validate the user's stress, but give a step-by-step game plan tailored to the situation (e.g. telling them not to spam/bombard her).
            2. Provide 3 draft messages:
               - Option 1 (The Space-Giver): Acknowledging her anger/need for space, zero pressure.
               - Option 3 (Soft Apology + Cute Gesture): Sincere yet sweet.
               - Option 3 (Hinglish/Hindi): E.g. "Suno, gussa thanda ho toh batana. I'm waiting. Zero pressure."
               
            Return your response STRICTLY as a JSON object matching this schema:
            {
              "analysis": "Tailored step-by-step battle plan (short, sweet, bullet points)",
              "option1Label": "The Space-Giver 🌌",
              "option1Text": "Actual draft message",
              "option2Label": "Soft Apology + Cute Gesture 🌸",
              "option2Text": "Actual draft message",
              "option3Label": "Hinglish / Hindi Chill Option ☕",
              "option3Text": "Actual draft message"
            }
        """.trimIndent()

        val systemInstruction = "$baseSystemInstruction\nOutput must be a single raw JSON object matching the requested schema. No conversational preamble before or after the JSON."

        executeAndParse(prompt, systemInstruction, GussaRegulatorResponse::class.java)
    }

    suspend fun analyzeReply(message: String): Result<ReplyAnalyzerResponse> = withContext(Dispatchers.IO) {
        val prompt = """
            The user received this reply/message from their partner/crush/bestie: "$message"
            
            Perform the following tasks:
            1. Analyze her mood, tone, and hidden meaning (Is 'K' actually 'I am furious' or just 'I am busy'?).
            2. Generate 4 reply options categorized by vibe:
               - Romantic/Sweet reply (Pyar bhara)
               - Playful/Teasing reply (Tang karne wala)
               - Sincere/Direct reply (Serious & straight)
               - Chill/Casual reply (No-fuss)
               
            Return your response STRICTLY as a JSON object matching this schema:
            {
              "analysis": "Vibe check & hidden meaning analysis (witty and sharp)",
              "option1Label": "Romantic & Sweet (Pyar Bhara) 💕",
              "option1Text": "Actual reply option",
              "option2Label": "Playful & Teasing (Tang Karne Wala) 😜",
              "option2Text": "Actual reply option",
              "option3Label": "Sincere & Direct (Serious & Straight) 🤝",
              "option3Text": "Actual reply option",
              "option4Label": "Chill & Casual (No-Fuss) 😎",
              "option4Text": "Actual reply option"
            }
        """.trimIndent()

        val systemInstruction = "$baseSystemInstruction\nOutput must be a single raw JSON object matching the requested schema. No conversational preamble before or after the JSON."

        executeAndParse(prompt, systemInstruction, ReplyAnalyzerResponse::class.java)
    }

    suspend fun generateThemeFromPrompt(promptText: String): Result<ThemeColorsResponse> = withContext(Dispatchers.IO) {
        val prompt = """
            The user wants a customized "lovers / romance" color palette for an app based on this prompt: "$promptText"
            
            Generate a stunning, harmonious, high-contrast Material 3 color scheme matching their prompt.
            Make sure the textDarkHex is extremely readable (e.g., dark wine/navy/dark brown/white depending on if light/dark theme) on top of the bgLightHex and cardBgHex colors!
            
            Return your response STRICTLY as a raw JSON object matching this schema:
            {
              "primaryHex": "#HEX_COLOR",
              "secondaryHex": "#HEX_COLOR",
              "tertiaryHex": "#HEX_COLOR",
              "textDarkHex": "#HEX_COLOR",
              "bgLightHex": "#HEX_COLOR",
              "cardBgHex": "#HEX_COLOR",
              "alertBgHex": "#HEX_COLOR",
              "alertBorderHex": "#HEX_COLOR"
            }
        """.trimIndent()

        val systemInstruction = """
            You are a creative digital color stylist specializing in romance and lovers themed UI.
            Generate a raw JSON matching the requested schema. No conversational preamble before or after the JSON.
        """.trimIndent()

        executeAndParse(prompt, systemInstruction, ThemeColorsResponse::class.java)
    }

    private suspend fun <T> executeAndParse(
        prompt: String,
        systemInstruction: String,
        responseClass: Class<T>
    ): Result<T> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API Key is not set! Please configure it in the Secrets panel in AI Studio UI."))
        }

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.7f
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstruction)))
        )

        return@withContext try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("No response received from Love Guru model."))

            val adapter = moshi.adapter(responseClass)
            val parsed = adapter.fromJson(jsonText)
                ?: return@withContext Result.failure(Exception("Failed to decode Love Guru's wisdom. Raw: $jsonText"))

            Result.success(parsed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
