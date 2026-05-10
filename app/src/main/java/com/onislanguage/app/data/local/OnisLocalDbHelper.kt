package com.onislanguage.app.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class OnisLocalDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "onis_local.db"
        const val DATABASE_VERSION = 1

        // Flashcard Decks
        const val TABLE_DECKS = "flashcard_decks"
        const val COLUMN_DECK_ID = "deck_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_LANG = "language_focus"
        const val COLUMN_IS_REMOTE = "is_remote" // 1 if synced from server

        // Flashcards
        const val TABLE_CARDS = "flashcards"
        const val COLUMN_CARD_ID = "card_id"
        const val COLUMN_CARD_DECK_ID = "deck_id"
        const val COLUMN_FRONT = "front"
        const val COLUMN_BACK = "back"
        const val COLUMN_READING = "reading"
        const val COLUMN_EXAMPLE = "example_sentence"

        // Practice Exams
        const val TABLE_EXAMS = "practice_exams"
        const val COLUMN_EXAM_ID = "exam_id"
        const val COLUMN_EXAM_TITLE = "title"
        const val COLUMN_EXAM_LEVEL = "level"

        // Questions
        const val TABLE_QUESTIONS = "practice_questions"
        const val COLUMN_Q_ID = "question_id"
        const val COLUMN_Q_EXAM_ID = "exam_id"
        const val COLUMN_PROMPT = "prompt"
        const val COLUMN_OPTIONS = "options_json"
        const val COLUMN_ANSWER = "correct_answer"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_DECKS (
                $COLUMN_DECK_ID TEXT PRIMARY KEY,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_LANG TEXT DEFAULT 'Japanese',
                $COLUMN_IS_REMOTE INTEGER DEFAULT 0
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_CARDS (
                $COLUMN_CARD_ID TEXT PRIMARY KEY,
                $COLUMN_CARD_DECK_ID TEXT NOT NULL,
                $COLUMN_FRONT TEXT NOT NULL,
                $COLUMN_BACK TEXT NOT NULL,
                $COLUMN_READING TEXT,
                $COLUMN_EXAMPLE TEXT,
                FOREIGN KEY($COLUMN_CARD_DECK_ID) REFERENCES $TABLE_DECKS($COLUMN_DECK_ID) ON DELETE CASCADE
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_EXAMS (
                $COLUMN_EXAM_ID TEXT PRIMARY KEY,
                $COLUMN_EXAM_TITLE TEXT NOT NULL,
                $COLUMN_EXAM_LEVEL TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_QUESTIONS (
                $COLUMN_Q_ID TEXT PRIMARY KEY,
                $COLUMN_Q_EXAM_ID TEXT NOT NULL,
                $COLUMN_PROMPT TEXT NOT NULL,
                $COLUMN_OPTIONS TEXT,
                $COLUMN_ANSWER TEXT,
                FOREIGN KEY($COLUMN_Q_EXAM_ID) REFERENCES $TABLE_EXAMS($COLUMN_Q_EXAM_ID) ON DELETE CASCADE
            )
        """)

        // Seed Local Data
        val deckId = "local-deck-1"
        db.execSQL("INSERT INTO $TABLE_DECKS ($COLUMN_DECK_ID, $COLUMN_TITLE, $COLUMN_DESCRIPTION, $COLUMN_IS_REMOTE) VALUES ('$deckId', 'Từ vựng quan trọng', 'Các từ mình hay quên', 0)")
        db.execSQL("INSERT INTO $TABLE_CARDS ($COLUMN_CARD_ID, $COLUMN_CARD_DECK_ID, $COLUMN_FRONT, $COLUMN_BACK, $COLUMN_EXAMPLE) VALUES ('card-1', '$deckId', '勉強', 'Học tập', '毎日勉強します。')")
        db.execSQL("INSERT INTO $TABLE_CARDS ($COLUMN_CARD_ID, $COLUMN_CARD_DECK_ID, $COLUMN_FRONT, $COLUMN_BACK, $COLUMN_EXAMPLE) VALUES ('card-2', '$deckId', '先生', 'Giáo viên', '先生は日本人です。')")

        db.execSQL("INSERT INTO $TABLE_EXAMS ($COLUMN_EXAM_ID, $COLUMN_EXAM_TITLE, $COLUMN_EXAM_LEVEL) VALUES ('local-exam-1', 'Đề luyện tập N5', 'N5')")
        db.execSQL("INSERT INTO $TABLE_QUESTIONS ($COLUMN_Q_ID, $COLUMN_Q_EXAM_ID, $COLUMN_PROMPT, $COLUMN_OPTIONS, $COLUMN_ANSWER) VALUES ('q-1', 'local-exam-1', 'これは何ですか？', '[\"ほん\", \"ペン\", \"かばん\"]', 'ほん')")
        }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_QUESTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXAMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CARDS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DECKS")
        onCreate(db)
    }
}
