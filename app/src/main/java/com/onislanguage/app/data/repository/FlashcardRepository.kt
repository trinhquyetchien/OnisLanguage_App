package com.onislanguage.app.data.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.onislanguage.app.data.api.FlashcardDeckDto
import com.onislanguage.app.data.api.FlashcardDto
import com.onislanguage.app.data.api.OnisApiService
import com.onislanguage.app.data.local.OnisLocalDbHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FlashcardRepository(
    private val apiService: OnisApiService,
    private val dbHelper: OnisLocalDbHelper
) {
    suspend fun getLocalDecks(): List<FlashcardDeckDto> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(OnisLocalDbHelper.TABLE_DECKS, null, null, null, null, null, null)
        val decks = mutableListOf<FlashcardDeckDto>()
        while (cursor.moveToNext()) {
            val deckId = cursor.getString(cursor.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_DECK_ID))
            decks.add(
                FlashcardDeckDto(
                    deck_id = deckId,
                    title = cursor.getString(cursor.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_TITLE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_DESCRIPTION)),
                    language_focus = cursor.getString(cursor.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_LANG)),
                    cards = getLocalCardsSync(db, deckId)
                )
            )
        }
        cursor.close()
        decks
    }

    suspend fun getLocalCards(deckId: String): List<FlashcardDto> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val selection = "${OnisLocalDbHelper.COLUMN_CARD_DECK_ID} = ?"
        val selectionArgs = arrayOf(deckId)
        val cursor = db.query(OnisLocalDbHelper.TABLE_CARDS, null, selection, selectionArgs, null, null, null)
        val cards = mutableListOf<FlashcardDto>()
        while (cursor.moveToNext()) {
            cards.add(
                FlashcardDto(
                    card_id = cursor.getString(cursor.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_CARD_ID)),
                    front = cursor.getString(cursor.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_FRONT)),
                    back = cursor.getString(cursor.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_BACK)),
                    example_sentence = cursor.getString(cursor.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_EXAMPLE))
                )
            )
        }
        cursor.close()
        cards
    }

    suspend fun getRemoteDecks(): Result<List<FlashcardDeckDto>> {
        return try {
            val response = apiService.getFlashcardDecks()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadDeck(deckId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val deck = apiService.getFlashcardDeck(deckId)
            val db = dbHelper.writableDatabase
            db.beginTransaction()
            try {
                val values = ContentValues().apply {
                    put(OnisLocalDbHelper.COLUMN_DECK_ID, deck.deck_id)
                    put(OnisLocalDbHelper.COLUMN_TITLE, deck.title)
                    put(OnisLocalDbHelper.COLUMN_DESCRIPTION, deck.description)
                    put(OnisLocalDbHelper.COLUMN_LANG, deck.language_focus)
                    put(OnisLocalDbHelper.COLUMN_IS_REMOTE, 1)
                }
                db.insertWithOnConflict(OnisLocalDbHelper.TABLE_DECKS, null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)

                deck.cards.forEach { card ->
                    val cardValues = ContentValues().apply {
                        put(OnisLocalDbHelper.COLUMN_CARD_ID, card.card_id)
                        put(OnisLocalDbHelper.COLUMN_CARD_DECK_ID, deck.deck_id)
                        put(OnisLocalDbHelper.COLUMN_FRONT, card.front)
                        put(OnisLocalDbHelper.COLUMN_BACK, card.back)
                        put(OnisLocalDbHelper.COLUMN_EXAMPLE, card.example_sentence)
                    }
                    db.insertWithOnConflict(OnisLocalDbHelper.TABLE_CARDS, null, cardValues, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
                }
                db.setTransactionSuccessful()
                Result.success(Unit)
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createLocalDeck(title: String, description: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(OnisLocalDbHelper.COLUMN_DECK_ID, java.util.UUID.randomUUID().toString())
                put(OnisLocalDbHelper.COLUMN_TITLE, title)
                put(OnisLocalDbHelper.COLUMN_DESCRIPTION, description)
                put(OnisLocalDbHelper.COLUMN_IS_REMOTE, 0)
            }
            db.insert(OnisLocalDbHelper.TABLE_DECKS, null, values)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addLocalCard(deckId: String, front: String, back: String, example: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(OnisLocalDbHelper.COLUMN_CARD_ID, java.util.UUID.randomUUID().toString())
                put(OnisLocalDbHelper.COLUMN_CARD_DECK_ID, deckId)
                put(OnisLocalDbHelper.COLUMN_FRONT, front)
                put(OnisLocalDbHelper.COLUMN_BACK, back)
                put(OnisLocalDbHelper.COLUMN_EXAMPLE, example)
            }
            db.insert(OnisLocalDbHelper.TABLE_CARDS, null, values)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addAnalysisCards(
        deckTitle: String,
        cards: List<FlashcardDto>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = dbHelper.writableDatabase
            db.beginTransaction()
            try {
                val deckId = ensureDeckId(db, deckTitle)
                cards.forEach { card ->
                    val values = ContentValues().apply {
                        put(OnisLocalDbHelper.COLUMN_CARD_ID, java.util.UUID.randomUUID().toString())
                        put(OnisLocalDbHelper.COLUMN_CARD_DECK_ID, deckId)
                        put(OnisLocalDbHelper.COLUMN_FRONT, card.front)
                        put(OnisLocalDbHelper.COLUMN_BACK, card.back)
                        put(OnisLocalDbHelper.COLUMN_EXAMPLE, card.example_sentence)
                    }
                    db.insert(OnisLocalDbHelper.TABLE_CARDS, null, values)
                }
                db.setTransactionSuccessful()
                Result.success(Unit)
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ensureDeckId(db: SQLiteDatabase, title: String): String {
        val cursor = db.query(
            OnisLocalDbHelper.TABLE_DECKS,
            arrayOf(OnisLocalDbHelper.COLUMN_DECK_ID),
            "${OnisLocalDbHelper.COLUMN_TITLE} = ?",
            arrayOf(title),
            null,
            null,
            null,
            "1"
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_DECK_ID))
            }
        }

        val deckId = java.util.UUID.randomUUID().toString()
        val deckValues = ContentValues().apply {
            put(OnisLocalDbHelper.COLUMN_DECK_ID, deckId)
            put(OnisLocalDbHelper.COLUMN_TITLE, title)
            put(OnisLocalDbHelper.COLUMN_DESCRIPTION, "Flashcard được tạo từ phân tích văn bản")
            put(OnisLocalDbHelper.COLUMN_IS_REMOTE, 0)
        }
        db.insert(OnisLocalDbHelper.TABLE_DECKS, null, deckValues)
        return deckId
    }

    suspend fun renameLocalDeck(deckId: String, title: String, description: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val values = ContentValues().apply {
                put(OnisLocalDbHelper.COLUMN_TITLE, title)
                if (description != null) {
                    put(OnisLocalDbHelper.COLUMN_DESCRIPTION, description)
                }
            }
            dbHelper.writableDatabase.update(
                OnisLocalDbHelper.TABLE_DECKS,
                values,
                "${OnisLocalDbHelper.COLUMN_DECK_ID} = ?",
                arrayOf(deckId)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteLocalDeck(deckId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = dbHelper.writableDatabase
            db.beginTransaction()
            try {
                db.delete(
                    OnisLocalDbHelper.TABLE_CARDS,
                    "${OnisLocalDbHelper.COLUMN_CARD_DECK_ID} = ?",
                    arrayOf(deckId)
                )
                db.delete(
                    OnisLocalDbHelper.TABLE_DECKS,
                    "${OnisLocalDbHelper.COLUMN_DECK_ID} = ?",
                    arrayOf(deckId)
                )
                db.setTransactionSuccessful()
                Result.success(Unit)
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllLocalDecks(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = dbHelper.writableDatabase
            db.beginTransaction()
            try {
                db.delete(OnisLocalDbHelper.TABLE_CARDS, null, null)
                db.delete(OnisLocalDbHelper.TABLE_DECKS, null, null)
                db.setTransactionSuccessful()
                Result.success(Unit)
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getLocalCardsSync(db: SQLiteDatabase, deckId: String): List<FlashcardDto> {
        val cursor = db.query(
            OnisLocalDbHelper.TABLE_CARDS,
            null,
            "${OnisLocalDbHelper.COLUMN_CARD_DECK_ID} = ?",
            arrayOf(deckId),
            null,
            null,
            null
        )
        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    add(
                        FlashcardDto(
                            card_id = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_CARD_ID)),
                            front = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_FRONT)),
                            back = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_BACK)),
                            example_sentence = it.getString(it.getColumnIndexOrThrow(OnisLocalDbHelper.COLUMN_EXAMPLE))
                        )
                    )
                }
            }
        }
    }
}
