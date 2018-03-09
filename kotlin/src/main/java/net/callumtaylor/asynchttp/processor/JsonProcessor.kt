package net.callumtaylor.asynchttp.processor

import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonParser
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
		return try { JsonParser().parse(resp) } catch (e: Exception) { JsonNull.INSTANCE }
	}
}
