package com.onislanguage.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.onislanguage.app.ui.layout.AppLayout
import com.onislanguage.app.di.ServiceLocator
import com.onislanguage.app.utils.LanguageManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageManager.applySavedLanguage(this)
        super.onCreate(savedInstanceState)
        ServiceLocator.init(this)
        setContent {
            AppLayout()
        }
    }
}
