package com.onislanguage.app.data.api

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer

class ProgressRequestBody(
    private val delegate: RequestBody,
    private val onProgress: (sentBytes: Long, totalBytes: Long) -> Unit
) : RequestBody() {
    override fun contentType(): MediaType? = delegate.contentType()

    override fun contentLength(): Long = delegate.contentLength()

    override fun writeTo(sink: BufferedSink) {
        val totalBytes = contentLength()
        var sentBytes = 0L
        val forwardingSink = object : ForwardingSink(sink) {
            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                sentBytes += byteCount
                onProgress(sentBytes, totalBytes)
            }
        }.buffer()

        delegate.writeTo(forwardingSink)
        forwardingSink.flush()
    }
}
