package com.onislanguage.app.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteException

class OnisLocalDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private data class FlashcardSeed(
        val id: String,
        val front: String,
        val back: String,
        val reading: String? = null,
        val example: String? = null
    )

    private data class DeckSeed(
        val id: String,
        val title: String,
        val description: String,
        val languageFocus: String = "Japanese",
        val cards: List<FlashcardSeed>
    )

    private data class QuestionSeed(
        val id: String,
        val prompt: String,
        val options: List<String>,
        val answer: String
    )

    private data class ExamSeed(
        val id: String,
        val title: String,
        val level: String,
        val questions: List<QuestionSeed>
    )

    companion object {
        const val DATABASE_NAME = "onis_local.db"
        const val DATABASE_VERSION = 8

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

        // Media transcript history
        const val TABLE_MEDIA_TRANSCRIPTS = "media_transcripts"
        const val COLUMN_MEDIA_ID = "media_id"
        const val COLUMN_MEDIA_TITLE = "title"
        const val COLUMN_MEDIA_SOURCE_TYPE = "source_type"
        const val COLUMN_MEDIA_SOURCE_URI = "source_uri"
        const val COLUMN_MEDIA_LOCAL_PATH = "local_media_path"
        const val COLUMN_MEDIA_REMOTE_URL = "remote_media_url"
        const val COLUMN_MEDIA_KIND = "media_kind"
        const val COLUMN_MEDIA_DURATION = "duration"
        const val COLUMN_MEDIA_FULL_TEXT_JA = "full_text_ja"
        const val COLUMN_MEDIA_FULL_TEXT_VI = "full_text_vi"
        const val COLUMN_MEDIA_SEGMENTS_JSON = "segments_json"
        const val COLUMN_MEDIA_CREATED_AT = "created_at"

        // OCR image history
        const val TABLE_OCR_HISTORY = "ocr_history"
        const val COLUMN_OCR_ID = "ocr_id"
        const val COLUMN_OCR_TITLE = "title"
        const val COLUMN_OCR_SOURCE_URI = "source_uri"
        const val COLUMN_OCR_IMAGE_URL = "image_url"
        const val COLUMN_OCR_FULL_TEXT = "full_text"
        const val COLUMN_OCR_TRANSLATED_TEXT = "translated_text_vi"
        const val COLUMN_OCR_TEXT_DISPLAY_JSON = "text_display_json"
        const val COLUMN_OCR_CREATED_AT = "created_at_ocr"

        // Translation history
        const val TABLE_TRANSLATION_HISTORY = "translation_history"
        const val COLUMN_TRANSLATION_ID = "translation_id"
        const val COLUMN_TRANSLATION_SOURCE_TEXT = "source_text"
        const val COLUMN_TRANSLATION_RESULT_TEXT = "result_text"
        const val COLUMN_TRANSLATION_DIRECTION = "direction"
        const val COLUMN_TRANSLATION_CREATED_AT = "created_at_translation"

        // Study usage events
        const val TABLE_STUDY_USAGE_EVENTS = "study_usage_events"
        const val COLUMN_USAGE_ID = "usage_id"
        const val COLUMN_USAGE_FEATURE_KEY = "feature_key"
        const val COLUMN_USAGE_FEATURE_LABEL = "feature_label"
        const val COLUMN_USAGE_CREATED_AT = "created_at_usage"

        // Chat prompt stats
        const val TABLE_CHAT_PROMPT_STATS = "chat_prompt_stats"
        const val COLUMN_CHAT_PROMPT = "prompt"
        const val COLUMN_CHAT_COUNT = "usage_count"
        const val COLUMN_CHAT_LAST_USED_AT = "last_used_at"
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

        db.execSQL("""
            CREATE TABLE $TABLE_MEDIA_TRANSCRIPTS (
                $COLUMN_MEDIA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_MEDIA_TITLE TEXT NOT NULL,
                $COLUMN_MEDIA_SOURCE_TYPE TEXT NOT NULL,
                $COLUMN_MEDIA_SOURCE_URI TEXT,
                $COLUMN_MEDIA_LOCAL_PATH TEXT,
                $COLUMN_MEDIA_REMOTE_URL TEXT,
                $COLUMN_MEDIA_KIND TEXT,
                $COLUMN_MEDIA_DURATION REAL DEFAULT 0,
                $COLUMN_MEDIA_FULL_TEXT_JA TEXT,
                $COLUMN_MEDIA_FULL_TEXT_VI TEXT,
                $COLUMN_MEDIA_SEGMENTS_JSON TEXT NOT NULL,
                $COLUMN_MEDIA_CREATED_AT INTEGER NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_OCR_HISTORY (
                $COLUMN_OCR_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_OCR_TITLE TEXT NOT NULL,
                $COLUMN_OCR_SOURCE_URI TEXT,
                $COLUMN_OCR_IMAGE_URL TEXT,
                $COLUMN_OCR_FULL_TEXT TEXT NOT NULL,
                $COLUMN_OCR_TRANSLATED_TEXT TEXT,
                $COLUMN_OCR_TEXT_DISPLAY_JSON TEXT,
                $COLUMN_OCR_CREATED_AT INTEGER NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_TRANSLATION_HISTORY (
                $COLUMN_TRANSLATION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TRANSLATION_SOURCE_TEXT TEXT NOT NULL,
                $COLUMN_TRANSLATION_RESULT_TEXT TEXT NOT NULL,
                $COLUMN_TRANSLATION_DIRECTION TEXT NOT NULL,
                $COLUMN_TRANSLATION_CREATED_AT INTEGER NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_STUDY_USAGE_EVENTS (
                $COLUMN_USAGE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USAGE_FEATURE_KEY TEXT NOT NULL,
                $COLUMN_USAGE_FEATURE_LABEL TEXT NOT NULL,
                $COLUMN_USAGE_CREATED_AT INTEGER NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_CHAT_PROMPT_STATS (
                $COLUMN_CHAT_PROMPT TEXT PRIMARY KEY,
                $COLUMN_CHAT_COUNT INTEGER NOT NULL DEFAULT 0,
                $COLUMN_CHAT_LAST_USED_AT INTEGER NOT NULL
            )
        """)

        seedSampleLearningContent(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            ensureColumn(
                db,
                TABLE_MEDIA_TRANSCRIPTS,
                COLUMN_MEDIA_LOCAL_PATH,
                "ALTER TABLE $TABLE_MEDIA_TRANSCRIPTS ADD COLUMN $COLUMN_MEDIA_LOCAL_PATH TEXT"
            )
        }
        if (oldVersion < 5) {
            ensureTable(
                db,
                TABLE_TRANSLATION_HISTORY,
                """
                CREATE TABLE $TABLE_TRANSLATION_HISTORY (
                    $COLUMN_TRANSLATION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_TRANSLATION_SOURCE_TEXT TEXT NOT NULL,
                    $COLUMN_TRANSLATION_RESULT_TEXT TEXT NOT NULL,
                    $COLUMN_TRANSLATION_DIRECTION TEXT NOT NULL,
                    $COLUMN_TRANSLATION_CREATED_AT INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
        if (oldVersion < 6) {
            ensureColumn(
                db,
                TABLE_OCR_HISTORY,
                COLUMN_OCR_TEXT_DISPLAY_JSON,
                "ALTER TABLE $TABLE_OCR_HISTORY ADD COLUMN $COLUMN_OCR_TEXT_DISPLAY_JSON TEXT"
            )
        }
        if (oldVersion < 7) {
            ensureTable(
                db,
                TABLE_STUDY_USAGE_EVENTS,
                """
                CREATE TABLE $TABLE_STUDY_USAGE_EVENTS (
                    $COLUMN_USAGE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_USAGE_FEATURE_KEY TEXT NOT NULL,
                    $COLUMN_USAGE_FEATURE_LABEL TEXT NOT NULL,
                    $COLUMN_USAGE_CREATED_AT INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
        if (oldVersion < 8) {
            ensureTable(
                db,
                TABLE_CHAT_PROMPT_STATS,
                """
                CREATE TABLE $TABLE_CHAT_PROMPT_STATS (
                    $COLUMN_CHAT_PROMPT TEXT PRIMARY KEY,
                    $COLUMN_CHAT_COUNT INTEGER NOT NULL DEFAULT 0,
                    $COLUMN_CHAT_LAST_USED_AT INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        ensureColumn(
            db,
            TABLE_MEDIA_TRANSCRIPTS,
            COLUMN_MEDIA_LOCAL_PATH,
            "ALTER TABLE $TABLE_MEDIA_TRANSCRIPTS ADD COLUMN $COLUMN_MEDIA_LOCAL_PATH TEXT"
        )
        ensureTable(
            db,
            TABLE_TRANSLATION_HISTORY,
            """
            CREATE TABLE $TABLE_TRANSLATION_HISTORY (
                $COLUMN_TRANSLATION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TRANSLATION_SOURCE_TEXT TEXT NOT NULL,
                $COLUMN_TRANSLATION_RESULT_TEXT TEXT NOT NULL,
                $COLUMN_TRANSLATION_DIRECTION TEXT NOT NULL,
                $COLUMN_TRANSLATION_CREATED_AT INTEGER NOT NULL
            )
            """.trimIndent()
        )
        ensureTable(
            db,
            TABLE_CHAT_PROMPT_STATS,
            """
            CREATE TABLE $TABLE_CHAT_PROMPT_STATS (
                $COLUMN_CHAT_PROMPT TEXT PRIMARY KEY,
                $COLUMN_CHAT_COUNT INTEGER NOT NULL DEFAULT 0,
                $COLUMN_CHAT_LAST_USED_AT INTEGER NOT NULL
            )
            """.trimIndent()
        )
        ensureColumn(
            db,
            TABLE_OCR_HISTORY,
            COLUMN_OCR_TEXT_DISPLAY_JSON,
            "ALTER TABLE $TABLE_OCR_HISTORY ADD COLUMN $COLUMN_OCR_TEXT_DISPLAY_JSON TEXT"
        )
        ensureTable(
            db,
            TABLE_STUDY_USAGE_EVENTS,
            """
            CREATE TABLE $TABLE_STUDY_USAGE_EVENTS (
                $COLUMN_USAGE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USAGE_FEATURE_KEY TEXT NOT NULL,
                $COLUMN_USAGE_FEATURE_LABEL TEXT NOT NULL,
                $COLUMN_USAGE_CREATED_AT INTEGER NOT NULL
            )
            """.trimIndent()
        )
        seedSampleLearningContent(db)
    }

    private fun seedSampleLearningContent(db: SQLiteDatabase) {
        seedFlashcardDecks(db)
        seedPracticeExams(db)
    }

    private fun seedFlashcardDecks(db: SQLiteDatabase) {
        sampleDeckSeeds().forEach { deck ->
            if (!recordExists(db, TABLE_DECKS, COLUMN_DECK_ID, deck.id)) {
                db.execSQL(
                    "INSERT INTO $TABLE_DECKS ($COLUMN_DECK_ID, $COLUMN_TITLE, $COLUMN_DESCRIPTION, $COLUMN_LANG, $COLUMN_IS_REMOTE) VALUES (?, ?, ?, ?, 0)",
                    arrayOf(deck.id, deck.title, deck.description, deck.languageFocus)
                )
            }
            deck.cards.forEach { card ->
                if (!recordExists(db, TABLE_CARDS, COLUMN_CARD_ID, card.id)) {
                    db.execSQL(
                        "INSERT INTO $TABLE_CARDS ($COLUMN_CARD_ID, $COLUMN_CARD_DECK_ID, $COLUMN_FRONT, $COLUMN_BACK, $COLUMN_READING, $COLUMN_EXAMPLE) VALUES (?, ?, ?, ?, ?, ?)",
                        arrayOf(card.id, deck.id, card.front, card.back, card.reading, card.example)
                    )
                }
            }
        }
    }

    private fun seedPracticeExams(db: SQLiteDatabase) {
        sampleExamSeeds().forEach { exam ->
            if (!recordExists(db, TABLE_EXAMS, COLUMN_EXAM_ID, exam.id)) {
                db.execSQL(
                    "INSERT INTO $TABLE_EXAMS ($COLUMN_EXAM_ID, $COLUMN_EXAM_TITLE, $COLUMN_EXAM_LEVEL) VALUES (?, ?, ?)",
                    arrayOf(exam.id, exam.title, exam.level)
                )
            }
            exam.questions.forEach { question ->
                if (!recordExists(db, TABLE_QUESTIONS, COLUMN_Q_ID, question.id)) {
                    db.execSQL(
                        "INSERT INTO $TABLE_QUESTIONS ($COLUMN_Q_ID, $COLUMN_Q_EXAM_ID, $COLUMN_PROMPT, $COLUMN_OPTIONS, $COLUMN_ANSWER) VALUES (?, ?, ?, ?, ?)",
                        arrayOf(
                            question.id,
                            exam.id,
                            question.prompt,
                            toJsonArray(question.options),
                            question.answer
                        )
                    )
                }
            }
        }
    }

    private fun recordExists(
        db: SQLiteDatabase,
        table: String,
        idColumn: String,
        idValue: String
    ): Boolean {
        val cursor = db.rawQuery(
            "SELECT 1 FROM $table WHERE $idColumn = ? LIMIT 1",
            arrayOf(idValue)
        )
        return cursor.use { it.moveToFirst() }
    }

    private fun toJsonArray(options: List<String>): String {
        return options.joinToString(prefix = "[", postfix = "]") {
            "\"${it.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        }
    }

    private fun sampleDeckSeeds(): List<DeckSeed> {
        return listOf(
            DeckSeed(
                id = "local-deck-core-n5",
                title = "JLPT N5 Cốt lõi",
                description = "Bộ từ vựng mở đầu để ôn hằng ngày.",
                cards = listOf(
                    FlashcardSeed("card-n5-001", "学校", "Trường học", "がっこう", "学校へ行きます。"),
                    FlashcardSeed("card-n5-002", "先生", "Giáo viên", "せんせい", "先生は日本人です。"),
                    FlashcardSeed("card-n5-003", "学生", "Học sinh", "がくせい", "学生が図書館で勉強しています。"),
                    FlashcardSeed("card-n5-004", "友達", "Bạn bè", "ともだち", "友達と映画を見ます。"),
                    FlashcardSeed("card-n5-005", "時間", "Thời gian", "じかん", "時間がありますか。"),
                    FlashcardSeed("card-n5-006", "今日", "Hôm nay", "きょう", "今日は雨です。"),
                    FlashcardSeed("card-n5-007", "明日", "Ngày mai", "あした", "明日テストがあります。"),
                    FlashcardSeed("card-n5-008", "電車", "Tàu điện", "でんしゃ", "電車で会社へ行きます。"),
                    FlashcardSeed("card-n5-009", "駅", "Nhà ga", "えき", "駅の前で待ちます。"),
                    FlashcardSeed("card-n5-010", "本", "Sách", "ほん", "日本語の本を読みます。"),
                    FlashcardSeed("card-n5-011", "水", "Nước", "みず", "水を一杯ください。"),
                    FlashcardSeed("card-n5-012", "食べる", "Ăn", "たべる", "朝ごはんを食べます。")
                )
            ),
            DeckSeed(
                id = "local-deck-verb-daily",
                title = "Động từ hằng ngày",
                description = "Các động từ xuất hiện nhiều trong hội thoại.",
                cards = listOf(
                    FlashcardSeed("card-verb-001", "行く", "Đi", "いく", "毎日歩いて駅へ行きます。"),
                    FlashcardSeed("card-verb-002", "来る", "Đến", "くる", "友達が家に来ます。"),
                    FlashcardSeed("card-verb-003", "帰る", "Trở về", "かえる", "九時に帰ります。"),
                    FlashcardSeed("card-verb-004", "聞く", "Nghe/Hỏi", "きく", "先生に聞いてください。"),
                    FlashcardSeed("card-verb-005", "話す", "Nói chuyện", "はなす", "日本語で話したいです。"),
                    FlashcardSeed("card-verb-006", "書く", "Viết", "かく", "名前をここに書きます。"),
                    FlashcardSeed("card-verb-007", "読む", "Đọc", "よむ", "毎晩ニュースを読みます。"),
                    FlashcardSeed("card-verb-008", "見る", "Xem/Nhìn", "みる", "映画を見ました。"),
                    FlashcardSeed("card-verb-009", "使う", "Sử dụng", "つかう", "この辞書を使います。"),
                    FlashcardSeed("card-verb-010", "作る", "Làm/Tạo", "つくる", "母が夕飯を作ります。"),
                    FlashcardSeed("card-verb-011", "待つ", "Chờ", "まつ", "ここで少し待ってください。"),
                    FlashcardSeed("card-verb-012", "始める", "Bắt đầu", "はじめる", "授業を始めます。")
                )
            ),
            DeckSeed(
                id = "local-deck-adj-n4",
                title = "Tính từ N4",
                description = "Nhóm tính từ miêu tả người và sự vật.",
                cards = listOf(
                    FlashcardSeed("card-adj-001", "忙しい", "Bận rộn", "いそがしい", "今週はとても忙しいです。"),
                    FlashcardSeed("card-adj-002", "嬉しい", "Vui mừng", "うれしい", "合格して嬉しいです。"),
                    FlashcardSeed("card-adj-003", "寂しい", "Cô đơn", "さびしい", "一人だと少し寂しいです。"),
                    FlashcardSeed("card-adj-004", "丁寧", "Lịch sự", "ていねい", "丁寧な言い方を覚えましょう。"),
                    FlashcardSeed("card-adj-005", "静か", "Yên tĩnh", "しずか", "この図書館は静かです。"),
                    FlashcardSeed("card-adj-006", "便利", "Tiện lợi", "べんり", "このアプリは便利ですね。"),
                    FlashcardSeed("card-adj-007", "苦手", "Không giỏi", "にがて", "私は漢字が少し苦手です。"),
                    FlashcardSeed("card-adj-008", "必要", "Cần thiết", "ひつよう", "パスポートが必要です。"),
                    FlashcardSeed("card-adj-009", "安全", "An toàn", "あんぜん", "夜でも安全な町です。"),
                    FlashcardSeed("card-adj-010", "危険", "Nguy hiểm", "きけん", "そこは危険です。"),
                    FlashcardSeed("card-adj-011", "複雑", "Phức tạp", "ふくざつ", "この文法は複雑です。"),
                    FlashcardSeed("card-adj-012", "大切", "Quan trọng", "たいせつ", "家族は大切です。")
                )
            ),
            DeckSeed(
                id = "local-deck-office",
                title = "Tiếng Nhật công sở",
                description = "Từ thường gặp trong email, họp, trao đổi công việc.",
                cards = listOf(
                    FlashcardSeed("card-office-001", "会議", "Cuộc họp", "かいぎ", "午後に会議があります。"),
                    FlashcardSeed("card-office-002", "資料", "Tài liệu", "しりょう", "資料を共有しました。"),
                    FlashcardSeed("card-office-003", "締切", "Hạn chót", "しめきり", "締切は金曜日です。"),
                    FlashcardSeed("card-office-004", "確認", "Xác nhận", "かくにん", "内容をご確認ください。"),
                    FlashcardSeed("card-office-005", "依頼", "Yêu cầu", "いらい", "依頼メールを送りました。"),
                    FlashcardSeed("card-office-006", "対応", "Xử lý/Phản hồi", "たいおう", "すぐに対応します。"),
                    FlashcardSeed("card-office-007", "担当", "Phụ trách", "たんとう", "私は営業を担当しています。"),
                    FlashcardSeed("card-office-008", "連絡", "Liên lạc", "れんらく", "あとで連絡します。"),
                    FlashcardSeed("card-office-009", "報告", "Báo cáo", "ほうこく", "進捗を報告してください。"),
                    FlashcardSeed("card-office-010", "相談", "Trao đổi/Tham khảo", "そうだん", "上司に相談しました。"),
                    FlashcardSeed("card-office-011", "承知しました", "Tôi đã nắm được", null, "内容、承知しました。"),
                    FlashcardSeed("card-office-012", "恐れ入ります", "Xin lỗi đã làm phiền", null, "恐れ入りますが、ご確認ください。")
                )
            ),
            DeckSeed(
                id = "local-deck-travel",
                title = "Du lịch và di chuyển",
                description = "Từ vựng dùng khi đi tàu, sân bay, khách sạn.",
                cards = listOf(
                    FlashcardSeed("card-travel-001", "空港", "Sân bay", "くうこう", "空港までバスで行きます。"),
                    FlashcardSeed("card-travel-002", "搭乗券", "Thẻ lên máy bay", "とうじょうけん", "搭乗券を見せてください。"),
                    FlashcardSeed("card-travel-003", "予約", "Đặt chỗ", "よやく", "ホテルを予約しました。"),
                    FlashcardSeed("card-travel-004", "改札", "Cổng soát vé", "かいさつ", "改札の前で会いましょう。"),
                    FlashcardSeed("card-travel-005", "片道", "Một chiều", "かたみち", "大阪まで片道でお願いします。"),
                    FlashcardSeed("card-travel-006", "往復", "Khứ hồi", "おうふく", "往復切符を買いました。"),
                    FlashcardSeed("card-travel-007", "出発", "Khởi hành", "しゅっぱつ", "飛行機は七時に出発します。"),
                    FlashcardSeed("card-travel-008", "到着", "Đến nơi", "とうちゃく", "電車は十分後に到着します。"),
                    FlashcardSeed("card-travel-009", "荷物", "Hành lý", "にもつ", "荷物を預けます。"),
                    FlashcardSeed("card-travel-010", "地図", "Bản đồ", "ちず", "駅までの地図がありますか。"),
                    FlashcardSeed("card-travel-011", "観光", "Tham quan", "かんこう", "京都で観光しました。"),
                    FlashcardSeed("card-travel-012", "温泉", "Suối nước nóng", "おんせん", "温泉に入りたいです。")
                )
            ),
            DeckSeed(
                id = "local-deck-counter",
                title = "Bộ đếm thông dụng",
                description = "Các counter cơ bản dễ nhầm khi nói số lượng.",
                cards = listOf(
                    FlashcardSeed("card-counter-001", "一つ", "Một cái", "ひとつ", "りんごを一つください。"),
                    FlashcardSeed("card-counter-002", "二枚", "Hai tờ", "にまい", "切符を二枚買います。"),
                    FlashcardSeed("card-counter-003", "三本", "Ba chai/cây", "さんぼん", "ペンが三本あります。"),
                    FlashcardSeed("card-counter-004", "四人", "Bốn người", "よにん", "家族は四人です。"),
                    FlashcardSeed("card-counter-005", "五冊", "Năm quyển", "ごさつ", "本を五冊読みました。"),
                    FlashcardSeed("card-counter-006", "六匹", "Sáu con vật nhỏ", "ろっぴき", "猫が六匹います。"),
                    FlashcardSeed("card-counter-007", "七回", "Bảy lần", "ななかい", "同じ映画を七回見ました。"),
                    FlashcardSeed("card-counter-008", "八時", "Tám giờ", "はちじ", "授業は八時に始まります。"),
                    FlashcardSeed("card-counter-009", "九階", "Tầng chín", "きゅうかい", "会議室は九階です。"),
                    FlashcardSeed("card-counter-010", "十台", "Mười chiếc máy/xe", "じゅうだい", "駐車場に車が十台あります。"),
                    FlashcardSeed("card-counter-011", "何歳", "Bao nhiêu tuổi", "なんさい", "お子さんは何歳ですか。"),
                    FlashcardSeed("card-counter-012", "何分", "Bao nhiêu phút", "なんぷん", "駅まで何分かかりますか。")
                )
            ),
            DeckSeed(
                id = "local-deck-katakana",
                title = "Katakana đời sống",
                description = "Từ mượn xuất hiện nhiều khi mua sắm và công nghệ.",
                cards = listOf(
                    FlashcardSeed("card-kata-001", "アプリ", "Ứng dụng", null, "新しいアプリを入れました。"),
                    FlashcardSeed("card-kata-002", "メール", "Thư điện tử", null, "メールを確認してください。"),
                    FlashcardSeed("card-kata-003", "スケジュール", "Lịch trình", null, "今週のスケジュールは忙しいです。"),
                    FlashcardSeed("card-kata-004", "プロジェクト", "Dự án", null, "このプロジェクトを担当します。"),
                    FlashcardSeed("card-kata-005", "ホテル", "Khách sạn", null, "駅前のホテルに泊まります。"),
                    FlashcardSeed("card-kata-006", "チケット", "Vé", null, "ライブのチケットを買いました。"),
                    FlashcardSeed("card-kata-007", "スーパー", "Siêu thị", null, "スーパーで牛乳を買います。"),
                    FlashcardSeed("card-kata-008", "メニュー", "Thực đơn", null, "おすすめのメニューは何ですか。"),
                    FlashcardSeed("card-kata-009", "コピー", "Bản sao", null, "資料をコピーしてください。"),
                    FlashcardSeed("card-kata-010", "サイズ", "Kích cỡ", null, "このサイズで大丈夫です。"),
                    FlashcardSeed("card-kata-011", "エラー", "Lỗi", null, "エラーが出ました。"),
                    FlashcardSeed("card-kata-012", "サポート", "Hỗ trợ", null, "サポートに連絡します。")
                )
            ),
            DeckSeed(
                id = "local-deck-grammar-phrases",
                title = "Mẫu câu thực chiến",
                description = "Các mẫu câu ngắn để phản xạ nhanh trong lớp và công việc.",
                cards = listOf(
                    FlashcardSeed("card-phrase-001", "お願いします", "Làm ơn/Xin nhờ", null, "もう一度お願いします。"),
                    FlashcardSeed("card-phrase-002", "大丈夫です", "Ổn/Không sao", null, "私は大丈夫です。"),
                    FlashcardSeed("card-phrase-003", "わかりました", "Tôi hiểu rồi", null, "はい、わかりました。"),
                    FlashcardSeed("card-phrase-004", "少々お待ちください", "Xin chờ một chút", null, "少々お待ちください。"),
                    FlashcardSeed("card-phrase-005", "どういう意味ですか", "Nghĩa là gì", null, "この単語はどういう意味ですか。"),
                    FlashcardSeed("card-phrase-006", "もう一度言ってください", "Hãy nói lại một lần nữa", null, "すみません、もう一度言ってください。"),
                    FlashcardSeed("card-phrase-007", "あとで確認します", "Tôi sẽ xác nhận sau", null, "あとで確認します。"),
                    FlashcardSeed("card-phrase-008", "少し難しいです", "Hơi khó", null, "この問題は少し難しいです。"),
                    FlashcardSeed("card-phrase-009", "時間がかかります", "Mất thời gian", null, "修正には時間がかかります。"),
                    FlashcardSeed("card-phrase-010", "問題ありません", "Không có vấn đề gì", null, "その方法で問題ありません。"),
                    FlashcardSeed("card-phrase-011", "よろしくお願いします", "Mong được giúp đỡ", null, "本日もよろしくお願いします。"),
                    FlashcardSeed("card-phrase-012", "失礼します", "Xin phép/Thất lễ", null, "では、失礼します。")
                )
            )
        )
    }

    private fun sampleExamSeeds(): List<ExamSeed> {
        return listOf(
            ExamSeed(
                id = "local-exam-n5-01",
                title = "Đề từ vựng N5 số 1",
                level = "N5",
                questions = listOf(
                    QuestionSeed("q-n5-001", "「先生」の意味はどれですか。", listOf("Giáo viên", "Bác sĩ", "Kỹ sư", "Nhà báo"), "Giáo viên"),
                    QuestionSeed("q-n5-002", "「駅」に行きます。 đọc là gì?", listOf("えき", "いき", "えぎ", "いち"), "えき"),
                    QuestionSeed("q-n5-003", "Chọn từ đúng cho nghĩa: nước", listOf("みず", "かぜ", "くるま", "いぬ"), "みず"),
                    QuestionSeed("q-n5-004", "「明日」 nghĩa là gì?", listOf("Hôm qua", "Hôm nay", "Ngày mai", "Tuần sau"), "Ngày mai"),
                    QuestionSeed("q-n5-005", "「食べる」 thuộc nhóm nghĩa nào?", listOf("Ăn", "Uống", "Đi", "Ngủ"), "Ăn"),
                    QuestionSeed("q-n5-006", "「かばん」を chọn đúng nghĩa.", listOf("Túi xách", "Bút", "Cửa", "Sách"), "Túi xách"),
                    QuestionSeed("q-n5-007", "「友達」 đọc là gì?", listOf("ともだち", "ともたい", "ゆうたち", "ともたち"), "ともだち"),
                    QuestionSeed("q-n5-008", "「会社」 nghĩa là gì?", listOf("Công ty", "Trường học", "Ngân hàng", "Bưu điện"), "Công ty"),
                    QuestionSeed("q-n5-009", "「読む」 nghĩa là gì?", listOf("Viết", "Đọc", "Nghe", "Nói"), "Đọc"),
                    QuestionSeed("q-n5-010", "Từ nào là phương tiện giao thông?", listOf("電車", "机", "花", "靴"), "電車")
                )
            ),
            ExamSeed(
                id = "local-exam-n5-02",
                title = "Trợ từ cơ bản N5",
                level = "N5",
                questions = listOf(
                    QuestionSeed("q-particle-001", "私は学校___行きます。", listOf("へ", "を", "に", "で"), "へ"),
                    QuestionSeed("q-particle-002", "毎朝パン___食べます。", listOf("を", "に", "で", "と"), "を"),
                    QuestionSeed("q-particle-003", "図書館___勉強します。", listOf("で", "を", "へ", "も"), "で"),
                    QuestionSeed("q-particle-004", "友達___映画を見ます。", listOf("と", "で", "が", "は"), "と"),
                    QuestionSeed("q-particle-005", "田中さん___学生です。", listOf("は", "を", "に", "へ"), "は"),
                    QuestionSeed("q-particle-006", "七時___起きます。", listOf("に", "で", "を", "と"), "に"),
                    QuestionSeed("q-particle-007", "日本語___英語を勉強しています。", listOf("と", "や", "も", "が"), "と"),
                    QuestionSeed("q-particle-008", "これは私___本です。", listOf("の", "を", "で", "へ"), "の"),
                    QuestionSeed("q-particle-009", "猫___好きです。", listOf("が", "で", "を", "へ"), "が"),
                    QuestionSeed("q-particle-010", "コーヒー___飲みません。", listOf("は", "が", "の", "も"), "は")
                )
            ),
            ExamSeed(
                id = "local-exam-n5-03",
                title = "Ngữ pháp mẫu câu N5",
                level = "N5",
                questions = listOf(
                    QuestionSeed("q-grammar-001", "日曜日に公園へ___。", listOf("行きます", "行きたい", "行って", "行くです"), "行きます"),
                    QuestionSeed("q-grammar-002", "これは日本語___本です。", listOf("の", "を", "と", "や"), "の"),
                    QuestionSeed("q-grammar-003", "私は寿司___好きです。", listOf("が", "に", "を", "へ"), "が"),
                    QuestionSeed("q-grammar-004", "今、雨が___。", listOf("降っています", "降りますか", "降りたい", "降るです"), "降っています"),
                    QuestionSeed("q-grammar-005", "部屋をきれい___してください。", listOf("に", "で", "を", "と"), "に"),
                    QuestionSeed("q-grammar-006", "日本へ行ったこと___あります。", listOf("が", "は", "を", "に"), "が"),
                    QuestionSeed("q-grammar-007", "今日は昨日___寒いです。", listOf("より", "ほど", "しか", "でも"), "より"),
                    QuestionSeed("q-grammar-008", "音楽を聞き___勉強します。", listOf("ながら", "しか", "まで", "ほど"), "ながら"),
                    QuestionSeed("q-grammar-009", "この問題は簡単___ありません。", listOf("では", "しか", "ほど", "など"), "では"),
                    QuestionSeed("q-grammar-010", "宿題を忘れ___。", listOf("ました", "ないです", "させる", "られる"), "ました")
                )
            ),
            ExamSeed(
                id = "local-exam-n4-01",
                title = "Từ vựng N4 mở rộng",
                level = "N4",
                questions = listOf(
                    QuestionSeed("q-n4v-001", "「必要」の意味はどれですか。", listOf("Cần thiết", "Đắt đỏ", "An toàn", "Nổi tiếng"), "Cần thiết"),
                    QuestionSeed("q-n4v-002", "「連絡する」 nghĩa là gì?", listOf("Liên lạc", "Chờ đợi", "Mở ra", "Vay mượn"), "Liên lạc"),
                    QuestionSeed("q-n4v-003", "「予約」 đọc là gì?", listOf("よやく", "ようやく", "よくやく", "ゆやく"), "よやく"),
                    QuestionSeed("q-n4v-004", "Từ nào nghĩa là “tiện lợi”?", listOf("便利", "不便", "複雑", "危険"), "便利"),
                    QuestionSeed("q-n4v-005", "「相談」 nghĩa là gì?", listOf("Trao đổi", "Lựa chọn", "Mua sắm", "Báo thức"), "Trao đổi"),
                    QuestionSeed("q-n4v-006", "「寂しい」 dùng để diễn tả gì?", listOf("Cô đơn", "No bụng", "Mệt mỏi", "Bực bội"), "Cô đơn"),
                    QuestionSeed("q-n4v-007", "「締切」 là gì?", listOf("Hạn chót", "Biên lai", "Tín hiệu", "Cửa sổ"), "Hạn chót"),
                    QuestionSeed("q-n4v-008", "「複雑」 đọc là gì?", listOf("ふくざつ", "ふくさつ", "ふっざつ", "ふくたつ"), "ふくざつ"),
                    QuestionSeed("q-n4v-009", "「担当」 nghĩa là gì?", listOf("Phụ trách", "Bắt đầu", "Giải thích", "Dừng lại"), "Phụ trách"),
                    QuestionSeed("q-n4v-010", "Từ nào liên quan đến cuộc họp?", listOf("会議", "冷蔵庫", "運動場", "薬局"), "会議")
                )
            ),
            ExamSeed(
                id = "local-exam-n4-02",
                title = "Đọc hiểu ngắn N4",
                level = "N4",
                questions = listOf(
                    QuestionSeed("q-read-001", "「田中さんは毎朝七時に家を出て、電車で会社へ行きます。」田中さんはどうやって会社へ行きますか。", listOf("電車", "バス", "自転車", "徒歩"), "電車"),
                    QuestionSeed("q-read-002", "「今日は雨なので、試合は中止になりました。」何が中止になりましたか。", listOf("試合", "授業", "会議", "旅行"), "試合"),
                    QuestionSeed("q-read-003", "「図書館は九時から六時までです。」何時に閉まりますか。", listOf("六時", "九時", "五時", "七時"), "六時"),
                    QuestionSeed("q-read-004", "「この店は安いですが、駅から少し遠いです。」この店について正しいものはどれですか。", listOf("安い", "駅に近い", "高い", "新しい"), "安い"),
                    QuestionSeed("q-read-005", "「山本さんは忙しくて、昼ごはんを食べる時間がありませんでした。」山本さんはどうしましたか。", listOf("昼ごはんを食べなかった", "早く帰った", "休みを取った", "電車に乗った"), "昼ごはんを食べなかった"),
                    QuestionSeed("q-read-006", "「京都では有名なお寺を三つ見ました。」いくつ見ましたか。", listOf("三つ", "二つ", "四つ", "一つ"), "三つ"),
                    QuestionSeed("q-read-007", "「資料はメールで送りました。」資料はどうやって送りましたか。", listOf("メール", "電話", "郵便", "手渡し"), "メール"),
                    QuestionSeed("q-read-008", "「この薬は食後に飲んでください。」いつ飲みますか。", listOf("食後", "食前", "寝る前", "朝だけ"), "食後"),
                    QuestionSeed("q-read-009", "「明日の会議は十時ではなく、十一時に始まります。」会議は何時に始まりますか。", listOf("十一時", "十時", "九時", "十二時"), "十一時"),
                    QuestionSeed("q-read-010", "「すみません、この席は空いていますか。」話している人は何を知りたいですか。", listOf("席が使えるか", "時間は何時か", "注文の方法", "道のり"), "席が使えるか")
                )
            ),
            ExamSeed(
                id = "local-exam-n4-03",
                title = "Ngữ pháp N4 tổng hợp",
                level = "N4",
                questions = listOf(
                    QuestionSeed("q-n4g-001", "時間があれば、映画を___つもりです。", listOf("見に行く", "見に行き", "見に行って", "見に行け"), "見に行く"),
                    QuestionSeed("q-n4g-002", "日本へ行く___、京都を訪れたいです。", listOf("なら", "しか", "まで", "ほど"), "なら"),
                    QuestionSeed("q-n4g-003", "この仕事は一人ではでき___。", listOf("ません", "ます", "ました", "たい"), "ません"),
                    QuestionSeed("q-n4g-004", "毎日練習すれば、上手に___。", listOf("なります", "します", "あります", "いきます"), "なります"),
                    QuestionSeed("q-n4g-005", "先生は学生に本を読ま___ました。", listOf("せ", "れ", "さ", "ら"), "せ"),
                    QuestionSeed("q-n4g-006", "この店は料理がおいしい___、いつも込んでいます。", listOf("ので", "でも", "しか", "ほど"), "ので"),
                    QuestionSeed("q-n4g-007", "山田さんは来る___でしたが、来られなくなりました。", listOf("はず", "ほど", "だけ", "ぐらい"), "はず"),
                    QuestionSeed("q-n4g-008", "雨が降りそう___、傘を持って行きます。", listOf("なので", "でも", "しか", "ほど"), "なので"),
                    QuestionSeed("q-n4g-009", "宿題を出すのを忘れて___。", listOf("しまいました", "おきました", "みました", "あります"), "しまいました"),
                    QuestionSeed("q-n4g-010", "部長に資料を見て___ました。", listOf("いただき", "あげ", "もらわ", "くれ"), "いただき")
                )
            ),
            ExamSeed(
                id = "local-exam-kanji-01",
                title = "Kanji thường gặp số 1",
                level = "N5-N4",
                questions = listOf(
                    QuestionSeed("q-kanji-001", "Chọn cách đọc đúng của 「毎日」.", listOf("まいにち", "まにち", "まいじつ", "まにじつ"), "まいにち"),
                    QuestionSeed("q-kanji-002", "「会社」 nghĩa là gì?", listOf("Công ty", "Hội trường", "Bệnh viện", "Công viên"), "Công ty"),
                    QuestionSeed("q-kanji-003", "Chọn kanji đúng cho từ でんしゃ.", listOf("電車", "電話", "電気", "電池"), "電車"),
                    QuestionSeed("q-kanji-004", "「図書館」 là nơi nào?", listOf("Thư viện", "Bảo tàng", "Nhà ga", "Nhà hàng"), "Thư viện"),
                    QuestionSeed("q-kanji-005", "「時間」 đọc là gì?", listOf("じかん", "しかん", "じげん", "しげん"), "じかん"),
                    QuestionSeed("q-kanji-006", "Kanji nào nghĩa là “núi”?", listOf("山", "川", "田", "口"), "山"),
                    QuestionSeed("q-kanji-007", "「出口」 là gì?", listOf("Lối ra", "Lối vào", "Cửa sổ", "Vé vào"), "Lối ra"),
                    QuestionSeed("q-kanji-008", "Chọn cách đọc của 「友達」.", listOf("ともだち", "ゆうだち", "ともたち", "ゆうたち"), "ともだち"),
                    QuestionSeed("q-kanji-009", "「火曜日」 là thứ mấy?", listOf("Thứ ba", "Thứ hai", "Thứ tư", "Chủ nhật"), "Thứ ba"),
                    QuestionSeed("q-kanji-010", "Kanji nào nghĩa là “tiền”?", listOf("お金", "天気", "先生", "病気"), "お金")
                )
            ),
            ExamSeed(
                id = "local-exam-office-01",
                title = "Tình huống công sở",
                level = "N4-N3",
                questions = listOf(
                    QuestionSeed("q-office-001", "Câu nào phù hợp để xác nhận đã hiểu email?", listOf("承知しました。", "行きません。", "食べました。", "高いです。"), "承知しました。"),
                    QuestionSeed("q-office-002", "「資料をご確認ください。」 nghĩa gần nhất là gì?", listOf("Vui lòng kiểm tra tài liệu", "Hãy đóng tài liệu", "Đừng gửi tài liệu", "Tôi sẽ viết tài liệu"), "Vui lòng kiểm tra tài liệu"),
                    QuestionSeed("q-office-003", "Từ nào nghĩa là “hạn chót”?", listOf("締切", "担当", "相談", "確認"), "締切"),
                    QuestionSeed("q-office-004", "Trong họp, từ nào dùng để nói “báo cáo tiến độ”?", listOf("進捗を報告する", "切符を買う", "注文を取る", "薬を飲む"), "進捗を報告する"),
                    QuestionSeed("q-office-005", "「恐れ入りますが」 thường mang sắc thái gì?", listOf("Lịch sự nhờ vả", "Ra lệnh mạnh", "Nói đùa", "Từ chối thẳng"), "Lịch sự nhờ vả"),
                    QuestionSeed("q-office-006", "「対応します」 nghĩa là gì?", listOf("Tôi sẽ xử lý", "Tôi sẽ nghỉ", "Tôi sẽ mượn", "Tôi sẽ ăn"), "Tôi sẽ xử lý"),
                    QuestionSeed("q-office-007", "Từ nào chỉ người chịu trách nhiệm?", listOf("担当", "乗客", "医者", "店員"), "担当"),
                    QuestionSeed("q-office-008", "Câu nào tự nhiên khi muốn xin người khác chờ?", listOf("少々お待ちください。", "そこに座ります。", "あとで帰ります。", "雨が降ります。"), "少々お待ちください。"),
                    QuestionSeed("q-office-009", "「相談する」 nghĩa gần nhất là gì?", listOf("Trao đổi để xin ý kiến", "Đi mua hàng", "Nộp báo cáo", "Gọi taxi"), "Trao đổi để xin ý kiến"),
                    QuestionSeed("q-office-010", "Từ nào liên quan trực tiếp đến cuộc họp?", listOf("会議", "公園", "温泉", "空港"), "会議")
                )
            ),
            ExamSeed(
                id = "local-exam-travel-01",
                title = "Tình huống du lịch",
                level = "N5-N4",
                questions = listOf(
                    QuestionSeed("q-travel-001", "「片道」 nghĩa là gì?", listOf("Một chiều", "Khứ hồi", "Đường vòng", "Đi bộ"), "Một chiều"),
                    QuestionSeed("q-travel-002", "Ở sân bay, 「搭乗券」 là gì?", listOf("Thẻ lên máy bay", "Hộ chiếu", "Vali", "Cửa ra"), "Thẻ lên máy bay"),
                    QuestionSeed("q-travel-003", "「改札」 thường ở đâu?", listOf("Nhà ga", "Nhà bếp", "Lớp học", "Hiệu thuốc"), "Nhà ga"),
                    QuestionSeed("q-travel-004", "Câu nào phù hợp khi muốn đặt phòng khách sạn?", listOf("部屋を予約したいです。", "切符をなくしました。", "薬をください。", "道を教えます。"), "部屋を予約したいです。"),
                    QuestionSeed("q-travel-005", "「往復」 nghĩa là gì?", listOf("Khứ hồi", "Một chiều", "Đổi tàu", "Hủy chuyến"), "Khứ hồi"),
                    QuestionSeed("q-travel-006", "Từ nào nghĩa là “hành lý”?", listOf("荷物", "案内", "出口", "予定"), "荷物"),
                    QuestionSeed("q-travel-007", "「到着」 là gì?", listOf("Đến nơi", "Khởi hành", "Đặt vé", "Tham quan"), "Đến nơi"),
                    QuestionSeed("q-travel-008", "Bạn muốn hỏi bản đồ, chọn câu đúng.", listOf("地図がありますか。", "薬を飲みますか。", "机がありますか。", "宿題がありますか。"), "地図がありますか。"),
                    QuestionSeed("q-travel-009", "「温泉」 là gì?", listOf("Suối nước nóng", "Nhà ga", "Siêu thị", "Nhà hàng"), "Suối nước nóng"),
                    QuestionSeed("q-travel-010", "Từ nào nghĩa là “tham quan”?", listOf("観光", "連絡", "予約", "対応"), "観光")
                )
            ),
            ExamSeed(
                id = "local-exam-mixed-01",
                title = "Tổng hợp phản xạ nhanh",
                level = "N5-N4",
                questions = listOf(
                    QuestionSeed("q-mixed-001", "「よろしくお願いします」 thường dùng khi nào?", listOf("Khi mở đầu hoặc nhờ giúp đỡ", "Khi nổi giận", "Khi xin nghỉ phép", "Khi gọi món"), "Khi mở đầu hoặc nhờ giúp đỡ"),
                    QuestionSeed("q-mixed-002", "「少し難しいです」 nghĩa là gì?", listOf("Hơi khó", "Rất rẻ", "Mau lên", "Không cần"), "Hơi khó"),
                    QuestionSeed("q-mixed-003", "Chọn câu đúng để hỏi lại lịch sự.", listOf("もう一度言ってください。", "すぐ行け。", "そこに書け。", "早く食べろ。"), "もう一度言ってください。"),
                    QuestionSeed("q-mixed-004", "「大丈夫です」 có thể mang nghĩa nào?", listOf("Ổn, không sao", "Tôi đến rồi", "Đắt quá", "Muộn rồi"), "Ổn, không sao"),
                    QuestionSeed("q-mixed-005", "「あとで確認します」 nghĩa là gì?", listOf("Tôi sẽ kiểm tra sau", "Tôi đã xác nhận rồi", "Tôi muốn hủy", "Tôi đang chờ"), "Tôi sẽ kiểm tra sau"),
                    QuestionSeed("q-mixed-006", "Cụm nào phù hợp khi kết thúc cuộc trò chuyện lịch sự?", listOf("失礼します", "いくらですか", "どこですか", "食べます"), "失礼します"),
                    QuestionSeed("q-mixed-007", "「時間がかかります」 nghĩa là gì?", listOf("Mất thời gian", "Không kịp nữa", "Tôi bận", "Tôi không biết"), "Mất thời gian"),
                    QuestionSeed("q-mixed-008", "「問題ありません」 nghĩa là gì?", listOf("Không có vấn đề gì", "Có vấn đề lớn", "Khó hiểu", "Cần sửa ngay"), "Không có vấn đề gì"),
                    QuestionSeed("q-mixed-009", "Câu nào phù hợp khi chưa hiểu nghĩa của từ?", listOf("どういう意味ですか。", "何歳ですか。", "どこへ行きますか。", "いくらですか。"), "どういう意味ですか。"),
                    QuestionSeed("q-mixed-010", "「お願いします」 là mẫu câu dùng để?", listOf("Nhờ vả hoặc yêu cầu lịch sự", "Từ chối mạnh", "Chúc mừng", "Than phiền"), "Nhờ vả hoặc yêu cầu lịch sự")
                )
            )
        )
    }

    private fun ensureColumn(
        db: SQLiteDatabase,
        table: String,
        column: String,
        alterSql: String
    ) {
        if (!hasColumn(db, table, column)) {
            db.execSQL(alterSql)
        }
    }

    private fun ensureTable(
        db: SQLiteDatabase,
        table: String,
        createSql: String
    ) {
        if (!hasTable(db, table)) {
            db.execSQL(createSql)
        }
    }

    private fun hasColumn(db: SQLiteDatabase, table: String, column: String): Boolean {
        val cursor = db.rawQuery("PRAGMA table_info($table)", null)
        return cursor.use {
            val nameIndex = it.getColumnIndex("name")
            while (it.moveToNext()) {
                if (it.getString(nameIndex) == column) {
                    return true
                }
            }
            false
        }
    }

    private fun hasTable(db: SQLiteDatabase, table: String): Boolean {
        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
            arrayOf(table)
        )
        return cursor.use { it.moveToFirst() }
    }
}
