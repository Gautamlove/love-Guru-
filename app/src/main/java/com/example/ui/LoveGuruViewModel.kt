package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.LoveThemeColors
import com.example.ui.theme.ThemePresets

sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class LoveGuruViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val savedGemRepository = SavedGemRepository(db.savedGemDao())
    private val geminiRepository = GeminiRepository()

    private val _currentTheme = MutableStateFlow<LoveThemeColors>(ThemePresets.RoseRomance)
    val currentTheme: StateFlow<LoveThemeColors> = _currentTheme.asStateFlow()

    private val _themeGenerationState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val themeGenerationState: StateFlow<UiState<String>> = _themeGenerationState.asStateFlow()

    init {
        val prefs = application.getSharedPreferences("love_guru_prefs", android.content.Context.MODE_PRIVATE)
        val savedName = prefs.getString("selected_theme_name", ThemePresets.RoseRomance.name) ?: ThemePresets.RoseRomance.name
        
        if (savedName.startsWith("AI:") || savedName.startsWith("Custom:")) {
            val primary = prefs.getInt("custom_primary", ThemePresets.RoseRomance.primary.value.toInt())
            val secondary = prefs.getInt("custom_secondary", ThemePresets.RoseRomance.secondary.value.toInt())
            val tertiary = prefs.getInt("custom_tertiary", ThemePresets.RoseRomance.tertiary.value.toInt())
            val textDark = prefs.getInt("custom_text_dark", ThemePresets.RoseRomance.textDark.value.toInt())
            val bgLight = prefs.getInt("custom_bg_light", ThemePresets.RoseRomance.bgLight.value.toInt())
            val cardBg = prefs.getInt("custom_card_bg", ThemePresets.RoseRomance.cardBg.value.toInt())
            val alertBg = prefs.getInt("custom_alert_bg", ThemePresets.RoseRomance.alertBg.value.toInt())
            val alertBorder = prefs.getInt("custom_alert_border", ThemePresets.RoseRomance.alertBorder.value.toInt())
            
            _currentTheme.value = LoveThemeColors(
                name = savedName,
                primary = Color(primary.toLong() and 0xFFFFFFFFL),
                secondary = Color(secondary.toLong() and 0xFFFFFFFFL),
                tertiary = Color(tertiary.toLong() and 0xFFFFFFFFL),
                textDark = Color(textDark.toLong() and 0xFFFFFFFFL),
                bgLight = Color(bgLight.toLong() and 0xFFFFFFFFL),
                cardBg = Color(cardBg.toLong() and 0xFFFFFFFFL),
                alertBg = Color(alertBg.toLong() and 0xFFFFFFFFL),
                alertBorder = Color(alertBorder.toLong() and 0xFFFFFFFFL)
            )
        } else {
            _currentTheme.value = ThemePresets.getThemeByName(savedName)
        }
    }

    fun selectTheme(theme: LoveThemeColors) {
        _currentTheme.value = theme
        val prefs = getApplication<Application>().getSharedPreferences("love_guru_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("selected_theme_name", theme.name)
            if (theme.name.startsWith("AI:") || theme.name.startsWith("Custom:")) {
                putInt("custom_primary", theme.primary.value.toInt())
                putInt("custom_secondary", theme.secondary.value.toInt())
                putInt("custom_tertiary", theme.tertiary.value.toInt())
                putInt("custom_text_dark", theme.textDark.value.toInt())
                putInt("custom_bg_light", theme.bgLight.value.toInt())
                putInt("custom_card_bg", theme.cardBg.value.toInt())
                putInt("custom_alert_bg", theme.alertBg.value.toInt())
                putInt("custom_alert_border", theme.alertBorder.value.toInt())
            }
            apply()
        }
    }

    fun generateThemeFromPrompt(prompt: String, onComplete: (String) -> Unit) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _themeGenerationState.value = UiState.Loading
            if (!isApiKeyConfigured) {
                kotlinx.coroutines.delay(1000)
                val generated = ThemePresets.generateLocalSemanticTheme(prompt)
                selectTheme(generated)
                _themeGenerationState.value = UiState.Success("Theme generated offline!")
                onComplete("Theme generated locally! Add a GEMINI_API_KEY to unlock advanced AI palettes.")
                return@launch
            }
            
            val result = geminiRepository.generateThemeFromPrompt(prompt)
            result.onSuccess { response ->
                try {
                    val parsed = LoveThemeColors(
                        name = "AI: ${if (prompt.length > 15) prompt.take(15) + "..." else prompt}",
                        primary = Color(android.graphics.Color.parseColor(response.primaryHex)),
                        secondary = Color(android.graphics.Color.parseColor(response.secondaryHex)),
                        tertiary = Color(android.graphics.Color.parseColor(response.tertiaryHex)),
                        textDark = Color(android.graphics.Color.parseColor(response.textDarkHex)),
                        bgLight = Color(android.graphics.Color.parseColor(response.bgLightHex)),
                        cardBg = Color(android.graphics.Color.parseColor(response.cardBgHex)),
                        alertBg = Color(android.graphics.Color.parseColor(response.alertBgHex)),
                        alertBorder = Color(android.graphics.Color.parseColor(response.alertBorderHex))
                    )
                    selectTheme(parsed)
                    _themeGenerationState.value = UiState.Success("Theme generated successfully!")
                    onComplete("Dynamic AI theme created successfully! 🎨")
                } catch (e: Exception) {
                    _themeGenerationState.value = UiState.Error("Failed to parse colors: ${e.message}")
                    onComplete("Failed to parse AI colors. Reverted to local model.")
                    val generated = ThemePresets.generateLocalSemanticTheme(prompt)
                    selectTheme(generated)
                }
            }.onFailure {
                _themeGenerationState.value = UiState.Error(it.message ?: "Unknown error")
                onComplete("Gemini Error: ${it.message}. Using offline semantic analyzer.")
                val generated = ThemePresets.generateLocalSemanticTheme(prompt)
                selectTheme(generated)
            }
        }
    }

    val savedGems: StateFlow<List<SavedGem>> = savedGemRepository.allGems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isApiKeyConfigured: Boolean
        get() {
            val key = BuildConfig.GEMINI_API_KEY
            return key.isNotEmpty() && key != "MY_GEMINI_API_KEY"
        }

    private val _trickyState = MutableStateFlow<UiState<TrickySolverResponse>>(UiState.Idle)
    val trickyState: StateFlow<UiState<TrickySolverResponse>> = _trickyState.asStateFlow()

    private val _gussaState = MutableStateFlow<UiState<GussaRegulatorResponse>>(UiState.Idle)
    val gussaState: StateFlow<UiState<GussaRegulatorResponse>> = _gussaState.asStateFlow()

    private val _replyState = MutableStateFlow<UiState<ReplyAnalyzerResponse>>(UiState.Idle)
    val replyState: StateFlow<UiState<ReplyAnalyzerResponse>> = _replyState.asStateFlow()

    fun solveTrickyQuestion(question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            _trickyState.value = UiState.Loading
            if (!isApiKeyConfigured) {
                kotlinx.coroutines.delay(600) // Simulated delay
                val q = question.lowercase()
                val response = when {
                    q.contains("fat") || q.contains("dress") || q.contains("moti") || q.contains("motu") || q.contains("patli") || q.contains("kapde") || q.contains("kapda") || q.contains("suit") -> TrickySolverResponse(
                        analysis = "Psychology Check: This is a classic trap seeking reassurance and validation. She doesn't want a geometry analysis of volume or shape; she is asking, 'Do you find me stunning and attractive right now?'",
                        option1Label = "Smooth & Charming (Dil Jeetne Wala) 💕",
                        option1Text = "Moti? Kahape? All I see is my gorgeous partner looking stunning as always. Stop playing with my heart! 😍",
                        option2Label = "Safe & Honest (Balanced & Secure) 🌸",
                        option2Text = "That dress looks incredibly beautiful on you and fits you perfectly. You look amazing, and I love seeing you wear it.",
                        option3Label = "Playful & Witty (Mazaak-Mazaak) 😜",
                        option3Text = "Only if my eyes can get fat from looking at too much beauty at once! 😜 Come here, you look gorgeous."
                    )
                    q.contains("beautiful") || q.contains("friend") || q.contains("bestie") || q.contains("sundar") || q.contains("ladki") || q.contains("ladka") || q.contains("ex") || q.contains("gf") || q.contains("bf") || q.contains("pyaar") || q.contains("love") -> TrickySolverResponse(
                        analysis = "Psychology Check: High hazard trap! She is assessing her status and hierarchy in your emotional space. Comparison is the enemy. Never, ever rank anyone above or even close to her.",
                        option1Label = "Smooth & Charming (Dil Jeetne Wala) 💕",
                        option1Text = "Are you really comparing a candle to the sun? ☀️ You are the most beautiful person in my universe. No competition, ever.",
                        option2Label = "Safe & Honest (Balanced & Secure) 🌸",
                        option2Text = "My best friend is a great friend, but you are the person I'm completely in love with. To me, you are the most beautiful person inside and out.",
                        option3Label = "Playful & Witty (Mazaak-Mazaak) 😜",
                        option3Text = "I think you need to clean your glasses! 👓 Or wait, are you fishing for compliments? Because it's working—you are ridiculously gorgeous."
                    )
                    else -> TrickySolverResponse(
                        analysis = "Psychology Check: This question comes from a place of curiosity, playfulness, or seeking a declaration of care. Focus on making her feel special, secure, and loved.",
                        option1Label = "Smooth & Charming (Dil Jeetne Wala) 💕",
                        option1Text = "Whatever the scenario, my absolute priority is making sure you are happy, safe, and loved. You come first, always.",
                        option2Label = "Safe & Honest (Balanced & Secure) 🌸",
                        option2Text = "That is an interesting question! Honestly, I can't imagine my life without you, and I will always choose what is best for us and your happiness.",
                        option3Label = "Playful & Witty (Mazaak-Mazaak) 😜",
                        option3Text = "Aww! Look at you asking deep questions. 🌸 Tell you what, I will trade you a perfect answer for a hug right now. Deal?"
                    )
                }
                _trickyState.value = UiState.Success(response)
                return@launch
            }

            val result = geminiRepository.solveTrickyQuestion(question)
            result.onSuccess {
                _trickyState.value = UiState.Success(it)
            }.onFailure {
                _trickyState.value = UiState.Error(it.message ?: "An unknown error occurred")
            }
        }
    }

    fun regulateGussa(situation: String) {
        if (situation.isBlank()) return
        viewModelScope.launch {
            _gussaState.value = UiState.Loading
            if (!isApiKeyConfigured) {
                kotlinx.coroutines.delay(600) // Simulated delay
                val s = situation.lowercase()
                val response = when {
                    s.contains("ignore") || s.contains("silent") || s.contains("call") || s.contains("baat") || s.contains("block") || s.contains("msg ni") || s.contains("msg nahi") || s.contains("cut") || s.contains("gussa") || s.contains("naraaz") || s.contains("naraz") -> GussaRegulatorResponse(
                        analysis = "Battle Plan: Silence is often a self-protective shield or an outcry for you to notice and pursue, not just sheer anger. Rule #1: DO NOT text bomb or spam her with questions. Send a calm, loving anchor message to show you are there, and then step back to give her space.",
                        option1Label = "The Space-Giver 🌌",
                        option1Text = "Hey, I can sense that you're upset right now and need some quiet time to yourself. I fully understand and respect that. I'm right here whenever you feel ready to talk. No pressure, take your time. 🌸",
                        option2Label = "Soft Apology + Cute Gesture 🌸",
                        option2Text = "I'm really sorry if I upset you. I've sent a small treat your way to brighten your day. Let's talk whenever you're ready. I miss you. 🥺",
                        option3Label = "Hinglish / Hindi Chill Option ☕",
                        option3Text = "Suno, gussa thanda ho toh batana. I am waiting. Zero pressure, take your time! ☕"
                    )
                    s.contains("late") || s.contains("reply") || s.contains("der se") || s.contains("deri") || s.contains("slow") || s.contains("seen") || s.contains("unread") || s.contains("hmm") || s.contains("k") -> GussaRegulatorResponse(
                        analysis = "Battle Plan: Replying late feels like deprioritization to her. Validate her frustration immediately. Reassure her that she is always on your mind and explain what kept you busy.",
                        option1Label = "The Space-Giver 🌌",
                        option1Text = "Hey, so sorry I was disconnected! I had to run some errands, but I hate leaving you hanging. I'm all yours now. Tell me about your day!",
                        option2Label = "Soft Apology + Cute Gesture 🌸",
                        option2Text = "Hey, I'm so sorry for replying late. I know it's frustrating. Sending you a virtual hug and a promise to keep you updated next time. Forgive me? 🥺",
                        option3Label = "Hinglish / Hindi Chill Option ☕",
                        option3Text = "Sorry yaar, thoda busy ho gaya tha. Gussa mat ho, ab free hoon. Batao, kya chal raha hai? 💕"
                    )
                    else -> GussaRegulatorResponse(
                        analysis = "Battle Plan: Diffuse tension using immediate validation and zero defensiveness. Do not argue about who is right. Focus on resolving the emotion first, then discuss the details later.",
                        option1Label = "The Space-Giver 🌌",
                        option1Text = "I see why you are upset, and I want to apologize for making you feel this way. I value our connection more than being right. Let's take a breath and talk when we are both ready.",
                        option2Label = "Soft Apology + Cute Gesture 🌸",
                        option2Text = "I'm sorry for my part in this argument. I hate when we fight. Can we declare a ceasefire and grab some ice cream? My treat! 🍦",
                        option3Label = "Hinglish / Hindi Chill Option ☕",
                        option3Text = "Chalo ab bas gussa thanda karo. Maaf kar do na please? Agli baar se dhyaan rakhunga. 🤝"
                    )
                }
                _gussaState.value = UiState.Success(response)
                return@launch
            }

            val result = geminiRepository.regulateGussa(situation)
            result.onSuccess {
                _gussaState.value = UiState.Success(it)
            }.onFailure {
                _gussaState.value = UiState.Error(it.message ?: "An unknown error occurred")
            }
        }
    }

    fun analyzeReply(message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            _replyState.value = UiState.Loading
            if (!isApiKeyConfigured) {
                kotlinx.coroutines.delay(600) // Simulated delay
                val m = message.lowercase()
                val response = when {
                    m.contains("fine") || m.contains("nothing") || m.contains("kuch nahi") || m.contains("kuch ni") || m.contains("theek h") || m.contains("thik h") || m.contains("thik hai") || m.contains("theek") -> ReplyAnalyzerResponse(
                        analysis = "Mood Decoder: Code Red! 🚨 'Fine' or 'Nothing' is relationship speak for 'I am definitely upset, but I want to see if you care enough to notice and ask.' Sarcasm level: 90%. Do not take it literally!",
                        option1Label = "Romantic & Sweet (Pyar Bhara) 💕",
                        option1Text = "I know that 'fine' usually means you have a lot on your mind. Tell me what's bothering you, I'm all ears and here to listen. 💕",
                        option2Label = "Playful & Teasing (Tang Karne Wala) 😜",
                        option2Text = "Uh oh, the dreaded 'I'm fine'! 🚨 Code red! What can I do to bring that sweet smile back? Spilling the tea is highly recommended. 😜",
                        option3Label = "Sincere & Direct (Serious & Straight) 🤝",
                        option3Text = "I can sense you are a bit upset or down. If you want some space, I respect that, but I'm here to listen and help whenever you want to talk.",
                        option4Label = "Chill & Casual (No-Fuss) 😎",
                        option4Text = "Alright, take your time! Let me know if you want to chat later or grab some quick dessert to lift the mood."
                    )
                    m.contains("k") || m == "k." || m.contains("hmm") || m.contains("ok") || m.contains("acha") || m.contains("accha") || m.contains("achha") || m.contains("kk") -> ReplyAnalyzerResponse(
                        analysis = "Mood Decoder: The dreaded single-letter 'K'. 🥶 It signals emotional distance, frustration, or that she is extremely busy. It's safe to assume minor annoyance. Keep your reply warm, engaging, and easy to respond to.",
                        option1Label = "Romantic & Sweet (Pyar Bhara) 💕",
                        option1Text = "A single 'K'? Someone is sounding cold! 🥶 Let me warm up your screen with a quick 'I love you' and a warm hug. What are you up to?",
                        option2Label = "Playful & Teasing (Tang Karne Wala) 😜",
                        option2Text = "Whoa, that 'K' almost gave me brain freeze! ❄️ Do I need to report this cold wave to the weather department, or are you just teasing me? 😉",
                        option3Label = "Sincere & Direct (Serious & Straight) 🤝",
                        option3Text = "Hey, everything okay? You seem a bit brief. Let me know if you are busy or if we can chat about something later.",
                        option4Label = "Chill & Casual (No-Fuss) 😎",
                        option4Text = "Got it! Hit me up when you are free. Talk to you soon!"
                    )
                    else -> ReplyAnalyzerResponse(
                        analysis = "Mood Decoder: This text shows mixed signals or standard casual communication. You can match her energy while maintaining a playful, loving, and supportive tone.",
                        option1Label = "Romantic & Sweet (Pyar Bhara) 💕",
                        option1Text = "Reading your message just made my day. You're always on my mind. Can't wait to see you soon! 💕",
                        option2Label = "Playful & Teasing (Tang Karne Wala) 😜",
                        option2Text = "Oh, is that so? I think someone is missing me extra today! 😉 Don't worry, I miss you too.",
                        option3Label = "Sincere & Direct (Serious & Straight) 🤝",
                        option3Text = "Thanks for letting me know! I appreciate you updating me. Let's catch up tonight and talk more.",
                        option4Label = "Chill & Casual (No-Fuss) 😎",
                        option4Text = "Sounds good! Enjoy your time and talk to you in a bit."
                    )
                }
                _replyState.value = UiState.Success(response)
                return@launch
            }

            val result = geminiRepository.analyzeReply(message)
            result.onSuccess {
                _replyState.value = UiState.Success(it)
            }.onFailure {
                _replyState.value = UiState.Error(it.message ?: "An unknown error occurred")
            }
        }
    }

    fun saveGem(gem: SavedGem) {
        viewModelScope.launch {
            savedGemRepository.insert(gem)
        }
    }

    fun deleteGem(id: Int) {
        viewModelScope.launch {
            savedGemRepository.delete(id)
        }
    }

    fun resetTrickyState() {
        _trickyState.value = UiState.Idle
    }

    fun resetGussaState() {
        _gussaState.value = UiState.Idle
    }

    fun resetReplyState() {
        _replyState.value = UiState.Idle
    }
}
