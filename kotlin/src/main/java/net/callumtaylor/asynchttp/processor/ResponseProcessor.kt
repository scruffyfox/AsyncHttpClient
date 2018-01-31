package net.callumtaylor.asynchttp.processor

import android.support.annotation.UiThread
import android.support.annotation.WorkerThread
import net.callumtaylor.asynchttp.obj.Packet
import net.callumtaylor.asynchttp.obj.Request
import java.io.InputStream

/**
 * // TODO: Add class description
 */
interface ResponseProcessor<T>
{
	@UiThread
	fun onChunkProcessed(request: Request, length: Long, total: Long)

	@WorkerThread
	fun processStream(inputStream: InputStream, contentLength: Long, progressCallback: (Long, Long) -> Unit): T
}
