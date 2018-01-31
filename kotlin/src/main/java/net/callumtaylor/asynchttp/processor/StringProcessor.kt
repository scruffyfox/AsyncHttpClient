package net.callumtaylor.asynchttp.processor

import net.callumtaylor.asynchttp.AsyncHttpClient
import net.callumtaylor.asynchttp.obj.Packet
import net.callumtaylor.asynchttp.obj.Request
import java.io.InputStream

/**
 * // TODO: Add class description
 */
open class StringProcessor : ResponseProcessor<String>
{
	override fun onChunkProcessed(request: Request, length: Long, total: Long)
	{

	}

	override fun processStream(inputStream: InputStream, contentLength: Long, progressCallback: (Long, Long) -> Unit): String
	{
		val buffer = ByteArray(AsyncHttpClient.BUFFER_SIZE)
		val string = StringBuffer()

		var len = 0
		var readCount = 0L
		inputStream.use { stream ->
			len = stream.read(buffer)

			if (len > -1)
			{
				progressCallback(readCount, contentLength)
				readCount += len

				string.append(String(buffer, 0, len))
			}
		}

		progressCallback(readCount, contentLength)

		inputStream.close()
		return string.toString()
	}
}
