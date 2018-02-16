package net.callumtaylor.asynchttp.processor

import com.google.gson.Gson
import com.google.gson.JsonElement
import java.io.InputStream

/**
 * // TODO: Add class description
 */
open class JsonProcessor : ResponseProcessor<JsonElement>()
{
	private val delegateProcessor = StringProcessor()

	override fun processStream(inputStream: InputStream, contentLength: Long, progressCallback: (Long, Long) -> Unit): JsonElement
	{
		val resp = delegateProcessor.processStream(inputStream, contentLength, progressCallback)
		return Gson().toJsonTree(resp)
	}
}
