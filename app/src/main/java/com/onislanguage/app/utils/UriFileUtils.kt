package com.onislanguage.app.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.Locale
import java.util.zip.ZipFile

object UriFileUtils {
    fun getDisplayName(context: Context, uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        }
    }

    fun uriToFile(context: Context, uri: Uri, fileName: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createTempImageUri(context: Context): Uri {
        val file = File(context.cacheDir, "ocr_capture_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun extractTextFromUri(context: Context, uri: Uri): String {
        val fileName = (getDisplayName(context, uri) ?: "").lowercase(Locale.ROOT)
        return when {
            fileName.endsWith(".pdf") -> extractTextFromPdf(context, uri)
            fileName.endsWith(".docx") -> extractTextFromDocx(context, uri)
            else -> context.contentResolver.openInputStream(uri)?.use { stream ->
                InputStreamReader(stream).readText()
            }.orEmpty()
        }.trim()
    }

    private fun extractTextFromPdf(context: Context, uri: Uri): String {
        val tempFile = uriToFile(context, uri, "import_${System.currentTimeMillis()}.pdf") ?: return ""
        return try {
            PDDocument.load(tempFile).use { document ->
                PDFTextStripper().getText(document).orEmpty()
            }
        } catch (_: Exception) {
            ""
        } finally {
            tempFile.delete()
        }
    }

    private fun extractTextFromDocx(context: Context, uri: Uri): String {
        val tempFile = uriToFile(context, uri, "import_${System.currentTimeMillis()}.docx") ?: return ""
        return try {
            ZipFile(tempFile).use { zip ->
                val entry = zip.getEntry("word/document.xml") ?: return ""
                zip.getInputStream(entry).bufferedReader().use { reader ->
                    reader.readText()
                        .replace(Regex("<w:tab[^>]*/>"), "\t")
                        .replace(Regex("</w:p>"), "\n")
                        .replace(Regex("<[^>]+>"), " ")
                        .replace(Regex("\\s+"), " ")
                        .trim()
                }
            }
        } catch (_: Exception) {
            ""
        } finally {
            tempFile.delete()
        }
    }
}
