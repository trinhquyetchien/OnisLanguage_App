package com.onislanguage.app.di

import com.onislanguage.app.data.api.OnisApiClient
import com.onislanguage.app.data.repository.AuthRepository
import com.onislanguage.app.ui.viewmodel.AuthViewModel

import android.content.Context
import com.onislanguage.app.data.local.OnisLocalDbHelper
import com.onislanguage.app.data.repository.AiRepository
import com.onislanguage.app.data.repository.FlashcardRepository
import com.onislanguage.app.ui.viewmodel.AiViewModel
import com.onislanguage.app.ui.viewmodel.FlashcardViewModel

import kotlinx.coroutines.runBlocking

import com.onislanguage.app.data.repository.PracticeRepository
import com.onislanguage.app.ui.viewmodel.PracticeViewModel

/**
 * A simple Service Locator to manage dependencies for now.
 * In a real-world app, use Hilt or Koin.
 */
object ServiceLocator {
    private var dbHelper: OnisLocalDbHelper? = null
    private var appContext: Context? = null

    fun init(context: Context) {
        if (dbHelper == null) {
            appContext = context.applicationContext
            dbHelper = OnisLocalDbHelper(context.applicationContext)
            
            // Link token from repository to API client
            OnisApiClient.setTokenProvider {
                runBlocking { authRepository.getAccessToken() }
            }
        }
    }

    private val apiService by lazy { OnisApiClient.create() }
    
    val authRepository by lazy { 
        AuthRepository(apiService, appContext ?: throw IllegalStateException("ServiceLocator not initialized")) 
    }
    val aiRepository by lazy { AiRepository(apiService) }
    val flashcardRepository by lazy { 
        FlashcardRepository(apiService, dbHelper ?: throw IllegalStateException("ServiceLocator not initialized")) 
    }
    val practiceRepository by lazy {
        PracticeRepository(apiService, dbHelper ?: throw IllegalStateException("ServiceLocator not initialized"))
    }
    
    fun provideAuthViewModel(): AuthViewModel {
        return AuthViewModel(authRepository)
    }

    fun provideAiViewModel(): AiViewModel {
        return AiViewModel(aiRepository)
    }

    fun provideFlashcardViewModel(): FlashcardViewModel {
        return FlashcardViewModel(flashcardRepository)
    }

    fun providePracticeViewModel(): PracticeViewModel {
        return PracticeViewModel(practiceRepository)
    }
}
