package net.callumtaylor.asynchttp.obj

import com.google.gson.JsonElement
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.internal.Util
import okio.BufferedSink
import okio.Okio
import okio.Source
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Adds one multipart body to another
 */
operator fun MultipartBody.plus(other: MultipartBody): MultipartBody
{
	return MultipartBody.Builder().also { builder ->
		this@plus.parts().forEach { part ->
			builder.addPart(part)
		}

		other.parts().forEach { part ->
			builder.addPart(part)
		}
	}
	.build()
}

/**
 * Converts a json element to standard request body
 */
fun JsonElement.toRequestBody(): RequestBody = RequestBody.create(MediaType.parse("application/json"), toString())

/**
 * Creates a json body from a json string
 */
fun String.asJsonBody(): RequestBody = RequestBody.create(MediaType.parse("application/json"), this)

/**
 * Converts a string to a text/plain request body
 */
fun String.toRequestBody(): RequestBody = RequestBody.create(MediaType.parse("text/plain"), this)

/**
 * Converts a file to request body using input stream.
 * @param formName optional form field name, providing this will add as a form data part instead of standard part
 */
fun File.toRequestBody(formName: String? = null): MultipartBody
{
	return MultipartBody.Builder().also { builder ->
		if ((formName ?: "").isEmpty())
		{
			builder.addPart(FileInputStream(this).toRequestBody())
		}
		else
		{
			builder.addFormDataPart(formName, this.name, FileInputStream(this).toRequestBody())
		}
	}.build();
}

/**
 * Converts an input stream to request body
 * @param mediaType optional media type, defaults to application/octet-stream
 */
fun InputStream.toRequestBody(mediaType: MediaType? = MediaType.parse("application/octet-stream")): RequestBody
{
	return object : RequestBody()
	{
		override fun contentType(): MediaType?
		{
			return mediaType
		}

		override fun contentLength(): Long = try
		{
			this@toRequestBody.available().toLong()
		}
		catch (e: IOException)
		{
			0
		}

		@Throws(IOException::class)
		override fun writeTo(sink: BufferedSink)
		{
			var source: Source? = null

			try
			{
				source = Okio.source(this@toRequestBody)
				sink.writeAll(source!!)
			}
			finally
			{
				Util.closeQuietly(source)
			}
		}
	}
}
