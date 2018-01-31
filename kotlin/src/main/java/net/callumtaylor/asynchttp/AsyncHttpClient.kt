package net.callumtaylor.asynchttp

import android.net.Uri
import android.os.AsyncTask
import net.callumtaylor.asynchttp.obj.Request
import net.callumtaylor.asynchttp.obj.Response
import net.callumtaylor.asynchttp.processor.ResponseProcessor
import okhttp3.OkHttpClient
import java.io.BufferedInputStream
import java.io.InputStream
import java.lang.Exception
import java.util.concurrent.TimeUnit

class AsyncHttpClient(
	private var baseUri: Uri = Uri.EMPTY,
	private var timeout: Long = 0
)
{
	companion object
	{
		public var BUFFER_SIZE: Int = 8192
	}

	constructor(baseUrl: String, timeout: Long = 0) : this(Uri.parse(baseUrl), timeout)

	fun <T> get(
		request: Request? = null,
		processor: ResponseProcessor<T>,
		response: (response: Response<T>) -> Unit
	)
	{
		var request = process(request ?: Request())
		ExecutorTask(
			request = request,
			timeout = timeout,
			processor = processor,
			response = response
		).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private fun process(request: Request): Request
	{
		// ensure request is built
		request.path = baseUri.toString() + request.path
		return request
	}

	class ExecutorTask<T>(
		private val request: Request,
		private val timeout: Long = 0,
		private val processor: ResponseProcessor<T>,
		private val response: (response: Response<T>) -> Unit
	) : AsyncTask<Void, Long, Response<T>>()
	{
		private lateinit var responseStream: InputStream

		override fun doInBackground(vararg params: Void): Response<T>
		{
			var httpClient = OkHttpClient().newBuilder()
				.followRedirects(request.followRedirects)
				.followSslRedirects(request.followRedirects)
				.connectTimeout(timeout, TimeUnit.MILLISECONDS)
				.writeTimeout(timeout, TimeUnit.MILLISECONDS)
				.readTimeout(timeout, TimeUnit.MILLISECONDS)
//				.cache(cache)
				.build()

			var httpRequest: okhttp3.Request.Builder = okhttp3.Request.Builder()
				.url(request.path)

			httpRequest = httpRequest.get()
			val httpCall = httpClient.newCall(httpRequest.build())
			val httpResponse = httpCall.execute()
			val response = Response<T>(request = request)

			httpResponse.body()?.let {
				responseStream = BufferedInputStream(it.byteStream(), BUFFER_SIZE)

				val contentLength = httpResponse.body()?.contentLength() ?: 0
				val responseBody = processor.processStream(responseStream, contentLength, { length, total ->
					publishProgress(length, total)
				})

				response.body = responseBody
				response.length = contentLength
			}

			return response
		}

		override fun onCancelled()
		{
			try
			{
				// force close the stream
				responseStream.close()
			}
			catch (e: Exception){}
		}

		override fun onProgressUpdate(vararg values: Long?)
		{
			processor.onChunkProcessed(request, values[0] ?: 0, values[1] ?: 0)
		}

		override fun onPostExecute(result: Response<T>)
		{
			response.invoke(result)
		}
	}
}
