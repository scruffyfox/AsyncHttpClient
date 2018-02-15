package net.callumtaylor.asynchttp.processor

import android.support.annotation.UiThread
import android.support.annotation.WorkerThread
import net.callumtaylor.asynchttp.obj.Request
import java.io.InputStream

/**
 * // TODO: Add class description
 */
abstract class ResponseProcessor<out T>
{
	@UiThread
	open fun onRequest(){}

	@UiThread
	open fun onChunkReceived(request: Request, length: Long, total: Long){}

	@UiThread
	open fun onChunkSent(request: Request, length: Long, total: Long){}

	@WorkerThread
	abstract fun processStream(inputStream: InputStream, contentLength: Long, progressCallback: (Long, Long) -> Unit): T
}
