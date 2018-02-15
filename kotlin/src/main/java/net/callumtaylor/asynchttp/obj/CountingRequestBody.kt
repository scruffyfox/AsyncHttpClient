package net.callumtaylor.asynchttp.obj

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import java.io.IOException

/**
 * // TODO: Add class description
 */
class CountingRequestBody(
	private var delegate: RequestBody,
	private var listener: (buffer: ByteArray, bufferCount: Long, bytesWritten: Long, contentLength: Long) -> Unit
) : RequestBody()
{
	private lateinit var countingSink: CountingSink

	override fun contentType(): MediaType? = delegate.contentType()

	override fun contentLength(): Long
	{
		try
		{
			return delegate.contentLength()
		}
		catch (e: IOException)
		{
			e.printStackTrace()
		}

		return 0
	}

	@Throws(IOException::class)
	override fun writeTo(sink: BufferedSink)
	{
		countingSink = CountingSink(sink)
		val bufferedSink = Okio.buffer(countingSink)

		delegate.writeTo(bufferedSink)

		bufferedSink.flush()
	}

	private inner class CountingSink(delegate: Sink) : ForwardingSink(delegate)
	{
		private var bytesWritten: Long = 0

		@Throws(IOException::class)
		override fun write(source: Buffer, byteCount: Long)
		{
			val copy = Buffer()
			source.copyTo(copy, 0, byteCount)

			super.write(source, byteCount)

			bytesWritten += byteCount
			listener.invoke(copy.readByteArray(), byteCount, bytesWritten, contentLength())
		}
	}
}
