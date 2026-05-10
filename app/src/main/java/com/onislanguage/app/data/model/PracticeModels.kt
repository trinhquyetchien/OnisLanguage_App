package com.onislanguage.app.data.model

data class PracticeQuestion(
    val question_id: String,
    val kind: String,
    val prompt: String,
    val options: List<String>,
    val position: Int
)

data class PracticeExam(
    val exam_id: String,
    val title: String,
    val topic: String,
    val level: String,
    val tags: List<String>,
    val question_count: Int,
    val questions: List<PracticeQuestion> = emptyList()
)

data class PracticeSubmissionRequest(
    val answers: Map<String, String>
)

data class PracticeQuestionResult(
    val question_id: String,
    val is_correct: Boolean,
    val correct_answer: String,
    val user_answer: String,
    val explanation: String?
)

data class PracticeSubmissionResponse(
    val exam_id: String,
    val score: Float,
    val total_questions: Int,
    val correct_answers: Int,
    val results: List<PracticeQuestionResult>
)
