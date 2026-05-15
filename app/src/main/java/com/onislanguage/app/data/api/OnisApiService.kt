package com.onislanguage.app.data.api

import com.onislanguage.app.data.model.*
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class AuthRequest(
    val email: String, 
    val password: String, 
    val confirm_password: String? = null,
    val display_name: String? = null
)
data class AuthUser(val user_id: String, val email: String, val display_name: String)
data class AuthResponse(val access_token: String, val token_type: String, val user: AuthUser)

data class OtpResponse(val message: String, val email: String)
data class OtpVerifyRequest(val email: String, val otp: String)

data class ProfileUpdateRequestDto(
    val display_name: String? = null,
    val email: String? = null,
    val avatar_url: String? = null
)

data class EmailChangeVerifyRequestDto(
    val new_email: String,
    val otp: String
)
data class PasswordChangeRequestDto(
    val current_password: String,
    val new_password: String,
    val confirm_new_password: String
)
data class PasswordChangeVerifyRequestDto(
    val otp: String
)

data class TranslationRequest(val text: String, val source_language: String, val target_language: String)
data class ChatMessageDto(val role: String, val content: String)
data class ChatRequestDto(val messages: List<ChatMessageDto>)
data class ChatResponseDto(val response: String)

data class PracticeQuestionDto(
    val question_id: String,
    val kind: String,
    val prompt: String,
    val image_url: String? = null,
    val audio_url: String? = null,
    val options: List<String>,
    val position: Int
)

data class PracticeQuestionServerDto(
    val question_id: String,
    val kind: String,
    val prompt: String,
    val image_url: String? = null,
    val audio_url: String? = null,
    val options: List<String> = emptyList(),
    val difficulty: String? = null,
    val explanation: String? = null,
    val answer: String? = null
)

data class PracticeExamServerDto(
    val exam_id: String,
    val title: String,
    val topic: String,
    val level: String,
    val source: String = "backend",
    val tags: List<String> = emptyList(),
    val questions: List<PracticeQuestionServerDto> = emptyList()
)

data class VocabularyItemDto(
    val surface: String,
    val reading: String?,
    val meaning_vi: String,
    val part_of_speech: String?,
    val level: String?
)

data class GrammarPointDto(
    val pattern: String,
    val explanation_vi: String,
    val example_ja: String?,
    val level: String?
)

data class KanjiItemDto(
    val kanji: String,
    val reading: String? = null,
    val meaning_vi: String? = null
)

data class TextAnalysisDto(
    val summary_vi: String,
    val vocabulary: List<VocabularyItemDto> = emptyList(),
    val grammar_points: List<GrammarPointDto> = emptyList(),
    val normalized_text: String
)

data class FuriganaTokenDto(
    val surface: String,
    val reading: String? = null,
    val has_kanji: Boolean = false,
    val part_of_speech: String? = null
)

data class JapaneseTextDisplayDto(
    val text: String,
    val translation_vi: String? = null,
    val furigana_text: String? = null,
    val tokens: List<FuriganaTokenDto> = emptyList()
)

data class TranslationResponse(
    val source_text: String,
    val translated_text: String,
    val source_language: String,
    val target_language: String,
    val analysis: TextAnalysisDto,
    val text_display: JapaneseTextDisplayDto? = null
)

data class AnalyzeTextRequestDto(
    val text: String,
    val language: String
)

data class AnalyzedSentenceDto(
    val sentence_id: Int,
    val text_display: JapaneseTextDisplayDto
)

data class AnalyzeTextResponseDto(
    val text: String,
    val language: String,
    val normalized_text: String,
    val sentences: List<AnalyzedSentenceDto> = emptyList(),
    val analysis: TextAnalysisDto,
    val kanji: List<KanjiItemDto> = emptyList()
)

data class TranscriptWordDto(
    val start: Float,
    val end: Float,
    val text_ja: String,
    val confidence: Float? = null,
    val text_display: JapaneseTextDisplayDto? = null
)

data class TranscriptSegmentDto(
    val segment_id: Int,
    val start: Float,
    val end: Float,
    val text_ja: String,
    val text_vi: String?,
    val text_display: JapaneseTextDisplayDto? = null,
    val words: List<TranscriptWordDto> = emptyList()
)
data class OcrBlockDto(
    val text: String,
    val confidence: Float,
    val box: List<List<Float>>
)
data class YouTubeTranscriptionRequestDto(val url: String)
data class TranscriptionResponseDto(
    val full_text_ja: String,
    val full_text_vi: String?,
    val duration: Float,
    val segments: List<TranscriptSegmentDto>,
    val text_display: JapaneseTextDisplayDto? = null,
    val analysis: TextAnalysisDto? = null,
    val media_url: String? = null,
    val media_kind: String? = null,
    val media_title: String? = null
)

data class ServerTranscriptHistoryDto(
    val history_id: Long,
    val title: String,
    val source_type: String,
    val source_uri: String? = null,
    val media_url: String? = null,
    val media_kind: String? = null,
    val duration: Float,
    val full_text_ja: String,
    val full_text_vi: String? = null,
    val text_display: JapaneseTextDisplayDto? = null,
    val segments: List<TranscriptSegmentDto>,
    val created_at: String
)

data class OcrResponseDto(
    val full_text: String, 
    val translated_text_vi: String?,
    val text_display: JapaneseTextDisplayDto? = null,
    val sentences: List<AnalyzedSentenceDto> = emptyList(),
    val analysis: TextAnalysisDto? = null,
    val image_url: String? = null,
    val blocks: List<OcrBlockDto> = emptyList()
)

data class KanjiPredictionDto(
    val kanji: String, 
    val confidence: Float, 
    val meaning_vi: String?,
    val reading: String? = null,
    val label_id: Int? = null,
    val analysis: TextAnalysisDto? = null,
    val details: KanjiDetailsDto? = null
)

data class KanjiCommentDto(
    val content_text: String,
    val image_url: String? = null,
    val user_name: String? = null
)

data class KanjiDetailsDto(
    val meaning_vi: String? = null,
    val meaning_en: String? = null,
    val on_readings: List<String> = emptyList(),
    val kun_readings: List<String> = emptyList(),
    val am_han: String? = null,
    val stroke_count: Int? = null,
    val frequency: Int? = null,
    val examples: List<String> = emptyList(),
    val explanation: String? = null,
    val svg_url: String? = null,
    val image_urls: List<String> = emptyList(),
    val comments: List<KanjiCommentDto> = emptyList()
)

data class KanjiResponseDto(val top1: KanjiPredictionDto, val top5: List<KanjiPredictionDto>)

data class FlashcardDto(
    val card_id: String,
    val front: String,
    val back: String,
    val example_sentence: String? = null,
    val tags: List<String> = emptyList()
)

data class FlashcardDeckDto(
    val deck_id: String,
    val title: String,
    val description: String? = null,
    val language_focus: String = "Japanese",
    val cards: List<FlashcardDto> = emptyList()
)

data class PracticeExamListResponse(val exams: List<PracticeExamServerDto>)

interface OnisApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("api/v1/auth/register/initiate")
    suspend fun initiateRegister(@Body request: AuthRequest): OtpResponse

    @POST("api/v1/auth/register/verify")
    suspend fun verifyRegister(@Body request: OtpVerifyRequest): AuthResponse

    @retrofit2.http.PATCH("api/v1/auth/profile")
    suspend fun updateProfile(@Body request: ProfileUpdateRequestDto): retrofit2.Response<okhttp3.ResponseBody>

    @POST("api/v1/auth/profile/verify-email")
    suspend fun verifyEmailChange(@Body request: EmailChangeVerifyRequestDto): AuthUser

    @POST("api/v1/auth/profile/change-password")
    suspend fun initiatePasswordChange(@Body request: PasswordChangeRequestDto): OtpResponse

    @POST("api/v1/auth/profile/verify-password")
    suspend fun verifyPasswordChange(@Body request: PasswordChangeVerifyRequestDto): AuthUser

    @POST("api/v1/language/translate")
    suspend fun translate(@Body request: TranslationRequest): TranslationResponse

    @POST("api/v1/language/analyze")
    suspend fun analyzeText(@Body request: AnalyzeTextRequestDto): AnalyzeTextResponseDto

    @POST("api/v1/ai/chat/")
    suspend fun chat(@Body request: ChatRequestDto): ChatResponseDto

    @retrofit2.http.GET("api/v1/flashcards/decks")
    suspend fun getFlashcardDecks(): List<FlashcardDeckDto>

    @retrofit2.http.GET("api/v1/flashcards/decks/{deck_id}")
    suspend fun getFlashcardDeck(@retrofit2.http.Path("deck_id") deckId: String): FlashcardDeckDto

    // Practice Exams
    @retrofit2.http.GET("api/v1/practice/exams")
    suspend fun getPracticeExams(): PracticeExamListResponse

    @retrofit2.http.GET("api/v1/practice/exams/{exam_id}")
    suspend fun getPracticeExam(@retrofit2.http.Path("exam_id") examId: String): PracticeExamServerDto

    @POST("api/v1/practice/exams/{exam_id}/submit")
    suspend fun submitExam(
        @retrofit2.http.Path("exam_id") examId: String,
        @Body request: PracticeSubmissionRequest
    ): PracticeSubmissionResponse

    @POST("api/v1/practice/generate-ai")
    suspend fun generateExamAI(@Body request: AIExamGenerateRequest): AIExamGenerateResponse

    @Multipart
    @POST("api/v1/ai/transcribe/media-to-text")
    suspend fun transcribeMedia(@Part file: MultipartBody.Part): TranscriptionResponseDto

    @POST("api/v1/ai/transcribe/youtube-to-text")
    suspend fun transcribeYouTube(@Body request: YouTubeTranscriptionRequestDto): TranscriptionResponseDto

    @GET("api/v1/ai/transcribe/history")
    suspend fun getServerTranscriptionHistory(): List<ServerTranscriptHistoryDto>

    @Multipart
    @POST("api/v1/ai/ocr/image-to-text")
    suspend fun ocrImage(@Part file: MultipartBody.Part): OcrResponseDto

    @Multipart
    @POST("api/v1/ai/kanji/draw-and-recognize")
    suspend fun recognizeKanji(@Part file: MultipartBody.Part): KanjiResponseDto
}
