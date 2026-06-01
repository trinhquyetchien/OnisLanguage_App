package com.onislanguage.app.data.repository

import android.content.ContentValues
import com.onislanguage.app.data.api.OnisApiService
import com.onislanguage.app.data.api.PracticeExamServerDto
import com.onislanguage.app.data.model.*
import com.onislanguage.app.data.local.OnisLocalDbHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.UUID

class PracticeRepository(
    private val apiService: OnisApiService,
    private val dbHelper: OnisLocalDbHelper
) {
    suspend fun getLocalExams(): List<PracticeExam> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(OnisLocalDbHelper.TABLE_EXAMS, null, null, null, null, null, null)
        cursor.use {
            buildList {
                while (it.moveToNext()) {
                    val examId = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_EXAM_ID))
                    val questions = getLocalQuestions(db, examId)
                    add(
                        PracticeExam(
                            exam_id = examId,
                            title = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_EXAM_TITLE)),
                            topic = "Đề cá nhân",
                            level = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_EXAM_LEVEL)) ?: "N5",
                            tags = listOf("local"),
                            question_count = questions.size,
                            questions = questions
                        )
                    )
                }
            }
        }
    }

    suspend fun getRemoteExams(): Result<List<PracticeExam>> {
        return try {
            val response = apiService.getPracticeExams()
            Result.success(response.exams.map(::mapRemoteExam))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFullExam(examId: String): Result<PracticeExam> {
        return try {
            val response = apiService.getPracticeExam(examId)
            Result.success(mapRemoteExam(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadRemoteExam(examId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val remoteExam = mapRemoteExam(apiService.getPracticeExam(examId))
            val localExamId = "remote_exam_$examId"
            val db = dbHelper.writableDatabase
            db.beginTransaction()
            try {
                val examValues = ContentValues().apply {
                    put(OnisLocalDbHelper.COLUMN_EXAM_ID, localExamId)
                    put(OnisLocalDbHelper.COLUMN_EXAM_TITLE, remoteExam.title)
                    put(OnisLocalDbHelper.COLUMN_EXAM_LEVEL, remoteExam.level)
                }
                db.insertWithOnConflict(
                    OnisLocalDbHelper.TABLE_EXAMS,
                    null,
                    examValues,
                    android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
                )

                db.delete(
                    OnisLocalDbHelper.TABLE_QUESTIONS,
                    "${OnisLocalDbHelper.COLUMN_Q_EXAM_ID} = ?",
                    arrayOf(localExamId)
                )

                remoteExam.questions.forEachIndexed { index, question ->
                    val questionValues = ContentValues().apply {
                        put(
                            OnisLocalDbHelper.COLUMN_Q_ID,
                            question.question_id.ifBlank { "remote_q_${examId}_$index" }
                        )
                        put(OnisLocalDbHelper.COLUMN_Q_EXAM_ID, localExamId)
                        put(OnisLocalDbHelper.COLUMN_PROMPT, question.prompt)
                        put(OnisLocalDbHelper.COLUMN_OPTIONS, JSONArray(question.options).toString())
                        put(OnisLocalDbHelper.COLUMN_ANSWER, question.correct_answer)
                    }
                    db.insertWithOnConflict(
                        OnisLocalDbHelper.TABLE_QUESTIONS,
                        null,
                        questionValues,
                        android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
                    )
                }

                db.setTransactionSuccessful()
                Result.success(localExamId)
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLocalExam(examId: String): Result<PracticeExam> = withContext(Dispatchers.IO) {
        try {
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                OnisLocalDbHelper.TABLE_EXAMS,
                null,
                "${OnisLocalDbHelper.COLUMN_EXAM_ID} = ?",
                arrayOf(examId),
                null,
                null,
                null,
                "1"
            )
            cursor.use {
                if (!it.moveToFirst()) {
                    return@withContext Result.failure(IllegalArgumentException("Không tìm thấy đề thi local"))
                }
                val questions = getLocalQuestions(db, examId)
                Result.success(
                    PracticeExam(
                        exam_id = examId,
                        title = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_EXAM_TITLE)),
                        topic = "Đề cá nhân",
                        level = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_EXAM_LEVEL)) ?: "N5",
                        tags = listOf("local"),
                        question_count = questions.size,
                        questions = questions
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitExam(examId: String, answers: Map<String, String>): Result<PracticeSubmissionResponse> {
        return try {
            val request = PracticeSubmissionRequest(answers)
            val response = apiService.submitExam(examId, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitLocalExam(examId: String, answers: Map<String, String>): Result<PracticeSubmissionResponse> =
        withContext(Dispatchers.IO) {
            try {
                val exam = getLocalExam(examId).getOrThrow()
                val results = exam.questions.map { question ->
                    val userAnswer = answers[question.question_id].orEmpty()
                    PracticeQuestionResult(
                        question_id = question.question_id,
                        is_correct = userAnswer == question.correct_answer,
                        correct_answer = question.correct_answer.orEmpty(),
                        user_answer = userAnswer,
                        explanation = question.explanation
                    )
                }
                val correctCount = results.count { it.is_correct }
                Result.success(
                    PracticeSubmissionResponse(
                        exam_id = examId,
                        score = if (results.isEmpty()) 0f else (correctCount * 100f / results.size),
                        total_questions = results.size,
                        correct_answers = correctCount,
                        results = results
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun generateAIExam(topic: String, count: Int): Result<List<PracticeQuestion>> {
        return try {
            val response = apiService.generateExamAI(AIExamGenerateRequest(topic, count))
            Result.success(response.questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveLocalExam(
        title: String,
        level: String,
        questions: List<PracticeQuestion>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val examId = "local_exam_${UUID.randomUUID()}"
            val db = dbHelper.writableDatabase
            db.beginTransaction()
            try {
                val examValues = ContentValues().apply {
                    put(OnisLocalDbHelper.COLUMN_EXAM_ID, examId)
                    put(OnisLocalDbHelper.COLUMN_EXAM_TITLE, title)
                    put(OnisLocalDbHelper.COLUMN_EXAM_LEVEL, level)
                }
                db.insert(OnisLocalDbHelper.TABLE_EXAMS, null, examValues)

                questions.forEachIndexed { index, question ->
                    val questionValues = ContentValues().apply {
                        put(OnisLocalDbHelper.COLUMN_Q_ID, question.question_id.ifBlank { "q_${UUID.randomUUID()}" })
                        put(OnisLocalDbHelper.COLUMN_Q_EXAM_ID, examId)
                        put(OnisLocalDbHelper.COLUMN_PROMPT, question.prompt)
                        put(OnisLocalDbHelper.COLUMN_OPTIONS, JSONArray(question.options).toString())
                        put(OnisLocalDbHelper.COLUMN_ANSWER, question.correct_answer)
                    }
                    db.insert(OnisLocalDbHelper.TABLE_QUESTIONS, null, questionValues)
                }
                db.setTransactionSuccessful()
                Result.success(examId)
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateAIExamFromText(
        title: String,
        text: String,
        level: String,
        count: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        val normalizedText = text.trim()
        if (normalizedText.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("File không có nội dung"))
        }
        generateAIExam(
            topic = "Tạo đề thi tiếng Nhật từ nội dung sau:\n${normalizedText.take(4000)}",
            count = count
        ).fold(
            onSuccess = { questions ->
                saveLocalExam(
                    title = title.ifBlank { "Đề AI từ file" },
                    level = level,
                    questions = questions.mapIndexed { index, question ->
                        question.copy(
                            question_id = question.question_id.ifBlank { "q_${UUID.randomUUID()}" },
                            position = index + 1
                        )
                    }
                )
            },
            onFailure = { Result.failure(it) }
        )
    }

    private fun getLocalQuestions(
        db: android.database.sqlite.SQLiteDatabase,
        examId: String
    ): List<PracticeQuestion> {
        val cursor = db.query(
            OnisLocalDbHelper.TABLE_QUESTIONS,
            null,
            "${OnisLocalDbHelper.COLUMN_Q_EXAM_ID} = ?",
            arrayOf(examId),
            null,
            null,
            null
        )
        return cursor.use {
            buildList {
                var position = 1
                while (it.moveToNext()) {
                    add(
                        PracticeQuestion(
                            question_id = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_Q_ID)),
                            kind = "multiple_choice",
                            prompt = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_PROMPT)),
                            options = jsonArrayToList(
                                it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_OPTIONS)) ?: "[]"
                            ),
                            position = position++,
                            correct_answer = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_ANSWER))
                        )
                    )
                }
            }
        }
    }

    private fun jsonArrayToList(json: String): List<String> {
        val array = JSONArray(json)
        return buildList {
            for (index in 0 until array.length()) {
                add(array.optString(index))
            }
        }
    }

    private fun mapRemoteExam(remoteExam: PracticeExamServerDto): PracticeExam {
        val questions = remoteExam.questions.mapIndexed { index, question ->
            PracticeQuestion(
                question_id = question.question_id,
                kind = question.kind,
                prompt = question.prompt,
                imageUrl = question.image_url,
                audioUrl = question.audio_url,
                options = question.options,
                position = index + 1,
                correct_answer = question.answer,
                explanation = question.explanation
            )
        }
        return PracticeExam(
            exam_id = remoteExam.exam_id,
            title = remoteExam.title,
            topic = remoteExam.topic,
            level = remoteExam.level,
            tags = remoteExam.tags.ifEmpty { listOf(remoteExam.source) },
            question_count = questions.size,
            questions = questions
        )
    }
}
