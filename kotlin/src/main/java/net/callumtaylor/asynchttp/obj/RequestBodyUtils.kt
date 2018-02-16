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

operator fun MultipartBody.plus(other: MultipartBody): MultipartBody
{
	return MultipartBody.Builder().also { builder ->
		this@plus.parts().forEach { part ->
			builder.addPart(part)
		}
	}
	.addPart(other)
	.build()
}

fun JsonElement.toRequestBody(): RequestBody = RequestBody.create(MediaType.parse("application/json"), toString())
fun String.asJsonBody(): RequestBody = RequestBody.create(MediaType.parse("application/json"), this)
fun String.toRequestBody(): RequestBody = RequestBody.create(MediaType.parse("text/plain"), this)
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
