package net.callumtaylor.asynchttp.processor

import java.io.InputStream

/**
 * // TODO: Add class description
 */
interface ResponseProcessor<T>
{
	fun onChunkReceived()
	fun processStream(inputStream: InputStream, contentLength: Long): T
}
