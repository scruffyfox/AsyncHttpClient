package net.callumtaylor.asynchttp.processor

import java.io.InputStream

/**
 * // TODO: Add class description
 */
class StringProcessor : ResponseProcessor<String>
{
	override fun processStream(inputStream: InputStream, contentLength: Long): String
	{
		val string = inputStream.reader().readText()
		inputStream.close()

		return string
	}

	override fun onChunkReceived()
	{

	}
}
