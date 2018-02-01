package net.callumtaylor.asynchttp.processor

import net.callumtaylor.asynchttp.AsyncHttpClient
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * // TODO: Add class description
 */
open class ByteProcessor : ResponseProcessor<ByteArray>()
{
	override fun processStream(inputStream: InputStream, contentLength: Long, progressCallback: (Long, Long) -> Unit): ByteArray
	{
		val buffer = ByteArray(AsyncHttpClient.BUFFER_SIZE)
		val out = ByteArrayOutputStream()

		var len = 0
		var readCount = 0L
		try
		{
			inputStream.use { stream ->
				len = stream.read(buffer)

				if (len > -1)
				{
					progressCallback(readCount, contentLength)
					readCount += len

					out.write(buffer, 0, len)
				}
			}

			inputStream.close()
		}
		catch (e: Exception){}

		progressCallback(readCount, readCount)

		return out.toByteArray()
	}
}
