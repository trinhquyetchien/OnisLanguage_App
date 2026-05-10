package com.onislanguage.app.data.api

import com.onislanguage.app.data.model.*
import okhttp3.MultipartBody
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

data class TranslationRequest(val text: String, val source_language: String, val target_language: String)
data class TranslationResponse(val source_text: String, val translated_text: String)

data class TranscriptSegmentDto(val segment_id: Int, val start: Float, val end: Float, val text_ja: String, val text_vi: String?)
data class TranscriptionResponseDto(
    val full_text_ja: String,
    val full_text_vi: String?,
    val duration: Float,
    val segments: List<TranscriptSegmentDto>
)

data class OcrResponseDto(val full_text: String, val translated_text_vi: String?)
data class KanjiPredictionDto(val kanji: String, val confidence: Float, val meaning_vi: String?)
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

data class PracticeExamListResponse(val exams: List<PracticeExam>)

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

    @POST("api/v1/language/translate")
    suspend fun translate(@Body request: TranslationRequest): TranslationResponse

    @retrofit2.http.GET("api/v1/flashcards/decks")
    suspend fun getFlashcardDecks(): List<FlashcardDeckDto>

    @retrofit2.http.GET("api/v1/flashcards/decks/{deck_id}")
    suspend fun getFlashcardDeck(@retrofit2.http.Path("deck_id") deckId: String): FlashcardDeckDto

    // Practice Exams
    @retrofit2.http.GET("api/v1/practice/exams")
    suspend fun getPracticeExams(): PracticeExamListResponse

    @retrofit2.http.GET("api/v1/practice/exams/{exam_id}")
    suspend fun getPracticeExam(@retrofit2.http.Path("exam_id") examId: String): PracticeExam

    @POST("api/v1/practice/exams/{exam_id}/submit")
    suspend fun submitExam(
        @retrofit2.http.Path("exam_id") examId: String,
        @Body request: PracticeSubmissionRequest
    ): PracticeSubmissionResponse

    @Multipart
    @POST("api/v1/ai/transcribe/media-to-text")
    suspend fun transcribeMedia(@Part file: MultipartBody.Part): TranscriptionResponseDto

    @Multipart
    @POST("api/v1/ai/ocr/image-to-text")
    suspend fun ocrImage(@Part file: MultipartBody.Part): OcrResponseDto

    @Multipart
    @POST("api/v1/ai/kanji/draw-and-recognize")
    suspend fun recognizeKanji(@Part file: MultipartBody.Part): KanjiResponseDto
}
