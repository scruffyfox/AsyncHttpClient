package net.callumtaylor.asynchttp

import android.net.Uri
import android.os.AsyncTask
import net.callumtaylor.asynchttp.obj.CountingRequestBody
import net.callumtaylor.asynchttp.obj.Request
import net.callumtaylor.asynchttp.obj.Response
import net.callumtaylor.asynchttp.processor.ResponseProcessor
import net.callumtaylor.asynchttp.processor.StringProcessor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import java.io.BufferedInputStream
import java.io.InputStream
import java.lang.Exception
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class AsyncHttpClient(
	private var baseUri: Uri = Uri.EMPTY,
	private var timeout: Long = 0
)
{
	private lateinit var networkTask: AsyncHttpClient.ExecutorTask<*>

	companion object
	{
		public var BUFFER_SIZE: Int = 8192
		public var OKHTTP_CLIENT = OkHttpClient().newBuilder().build()
	}

	constructor(baseUrl: String, timeout: Long = 0) : this(Uri.parse(baseUrl), timeout)

	/**
	 * Performs a http request with default String response body. Request type should be defined in the [request] param
	 * @param request The request object to be made
	 * @param response The response callback
	 */
	fun request(
		request: Request? = null,
		response: (response: Response<String>) -> Unit
	)
	{
		get(request, StringProcessor(), response)
	}

	/**
	 * Performs a http request. Request type should be defined in the [request] param
	 * @param request The request object to be made
	 * @param processor The response body processor
	 * @param response The response callback
	 */
	fun <T> request(
		request: Request? = null,
		processor: ResponseProcessor<T>? = null,
		response: (response: Response<T>) -> Unit
	)
	{
		var request = process(request ?: Request())

		networkTask = ExecutorTask<T>(
			request = request,
			timeout = timeout,
			processor = processor,
			responseCallback = response
		)
		networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 *  Performs a http GET request with default String response body.
	 * @param request The request object to be made
	 * @param response The response callback
	 */
	fun get(
		request: Request? = null,
		response: (response: Response<String>) -> Unit
	)
	{
		get(request, StringProcessor(), response)
	}

	/**
	 * Performs a http GET request
	 * @param request The request object to be made
	 * @param processor The response body processor
	 * @param response The response callback
	 */
	fun <T> get(
		request: Request? = null,
		processor: ResponseProcessor<T>? = null,
		response: (response: Response<T>) -> Unit
	)
	{
		var request = process(request ?: Request())
		request.type = "GET"

		networkTask = ExecutorTask<T>(
			request = request,
			timeout = timeout,
			processor = processor,
			responseCallback = response
		)
		networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * Performs a http POST request with default String response body. Post body should be part of [request]
	 * @param request The request object to be made
	 * @param response The response callback
	 */
	fun post(
		request: Request? = null,
		response: (response: Response<String>) -> Unit
	)
	{
		post(request, StringProcessor(), response)
	}

	/**
	 * Performs a http POST request. Post body should be part of [request]
	 * @param request The request object to be made
	 * @param processor The response body processor
	 * @param response The response callback
	 */
	fun <T> post(
		request: Request? = null,
		processor: ResponseProcessor<T>? = null,
		response: (response: Response<T>) -> Unit
	)
	{
		var request = process(request ?: Request())
		request.type = "POST"

		networkTask = ExecutorTask<T>(
			request = request,
			timeout = timeout,
			processor = processor,
			responseCallback = response
		)
		networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * Performs a http DELETE request with default String response body. Delete body should be part of [request]
	 * @param request The request object to be made
	 * @param response The response callback
	 */
	fun delete(
		request: Request? = null,
		response: (response: Response<String>) -> Unit
	)
	{
		delete(request, StringProcessor(), response)
	}

	/**
	 * Performs a http DELETE request. Delete body should be part of [request]
	 * @param request The request object to be made
	 * @param processor The response body processor
	 * @param response The response callback
	 */
	fun <T> delete(
		request: Request? = null,
		processor: ResponseProcessor<T>? = null,
		response: (response: Response<T>) -> Unit
	)
	{
		var request = process(request ?: Request())
		request.type = "DELETE"

		networkTask = ExecutorTask<T>(
			request = request,
			timeout = timeout,
			processor = processor,
			responseCallback = response
		)
		networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * Performs a http PUT request with default String response body. Put body should be part of [request]
	 * @param request The request object to be made
	 * @param response The response callback
	 */
	fun put(
		request: Request? = null,
		response: (response: Response<String>) -> Unit
	)
	{
		put(request, StringProcessor(), response)
	}

	/**
	 * Performs a http PUT request. Put body should be part of [request]
	 * @param request The request object to be made
	 * @param processor The response body processor
	 * @param response The response callback
	 */
	fun <T> put(
		request: Request? = null,
		processor: ResponseProcessor<T>? = null,
		response: (response: Response<T>) -> Unit
	)
	{
		var request = process(request ?: Request())
		request.type = "PUT"

		networkTask = ExecutorTask<T>(
			request = request,
			timeout = timeout,
			processor = processor,
			responseCallback = response
		)
		networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * Cancels the pending request
	 */
	fun cancel()
	{
		networkTask.cancel(true)
	}

	/**
	 * Processes the base request into a usable request object by the network task
	 */
	private fun process(request: Request): Request
	{
		// ensure request is built
		request.path = baseUri.toString() + request.path
		return request
	}

	class ExecutorTask<T>(
		private val request: Request,
		private val timeout: Long = 0,
		private val processor: ResponseProcessor<T>?,
		private val responseCallback: (response: Response<T>) -> Unit
	) : AsyncTask<Void, Long, Response<T>>()
	{
		companion object
		{
			const val REQUEST_DOWNSTREAM = 0L;
			const val REQUEST_UPSTREAM = 1L;
		}

		private lateinit var responseStream: InputStream
		private val response = Response<T>(request = request)

		override fun onPreExecute()
		{
			request.time = System.currentTimeMillis()
			processor?.onRequest()
		}

		override fun doInBackground(vararg params: Void): Response<T>
		{
			var httpClient = OKHTTP_CLIENT.newBuilder().also { builder ->
				builder.followRedirects(request.followRedirects)
				builder.followSslRedirects(request.followRedirects)

				if (timeout != 0L)
				{
					builder.connectTimeout(timeout, TimeUnit.MILLISECONDS)
					builder.writeTimeout(timeout, TimeUnit.MILLISECONDS)
					builder.readTimeout(timeout, TimeUnit.MILLISECONDS)
				}
			}.build()

			if (request.allowAllSSl)
			{
				try
				{
					// Create a trust manager that does not validate certificate chains
					val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager
					{
						@Throws(java.security.cert.CertificateException::class)
						override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String){}
						@Throws(java.security.cert.CertificateException::class)
						override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String){}
						override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate>? = arrayOf()
					})

					val sslContext = SSLContext.getInstance("SSL")
					sslContext.init(null, trustAllCerts, java.security.SecureRandom())
					val sslSocketFactory = sslContext.socketFactory

					httpClient = httpClient.newBuilder()
						.sslSocketFactory(sslSocketFactory)
						.hostnameVerifier { hostname, session -> true }
						.build()
				}
				catch (e: NoSuchAlgorithmException)
				{
					e.printStackTrace()
				}
				catch (e: KeyManagementException)
				{
					e.printStackTrace()
				}
			}

			var httpRequest: okhttp3.Request.Builder = okhttp3.Request.Builder()
				.url(request.path)

			request.body = request.body ?: RequestBody.create(null, ByteArray(0))
			request.body = CountingRequestBody(request.body!!, { buffer, bufferCount, bytesWritten, contentLength ->
				publishProgress(REQUEST_UPSTREAM, bytesWritten, contentLength)
			})

			httpRequest = when (request.type) {
				"POST" -> httpRequest.post(request.body!!)
				"PUT" -> httpRequest.put(request.body!!)
				"DELETE" -> httpRequest.delete(request.body)
				"GET" -> httpRequest.get()
				else -> httpRequest.method(request.type, request.body)
			}

			httpRequest.addHeader("Connection", "close")
			request.headers.forEach { header ->
				httpRequest.addHeader(header.first, header.second)
			}

			request.length = (request.body as CountingRequestBody).contentLength()
			val httpCall = httpClient.newCall(httpRequest.build())
			val httpResponse = httpCall.execute()

			httpResponse.body()?.let {
				val encoding = httpResponse.header("Content-Encoding", "")

				responseStream = BufferedInputStream(it.byteStream(), BUFFER_SIZE)
				if (encoding == "gzip")
				{
					responseStream = GZIPInputStream(responseStream)
				}

				val contentLength = httpResponse.body()?.contentLength() ?: 0
				val responseBody = processor?.processStream(responseStream, contentLength, { length, total ->
					publishProgress(REQUEST_DOWNSTREAM, length, total)
				})

				response.body = responseBody
				response.length = contentLength
			}

			for (index in 0 until httpResponse.headers().size())
			{
				response.headers.add(Pair(httpResponse.headers().name(index), httpResponse.headers()[httpResponse.headers().name(index)] ?: ""))
			}

			response.time = System.currentTimeMillis()
			response.code = httpResponse.code()

			return response
		}

		override fun onCancelled()
		{
			try
			{
				// force close the stream
				responseStream.close()
				response.code = 0
			}
			catch (e: Exception){}

			responseCallback.invoke(response)
		}

		override fun onProgressUpdate(vararg values: Long?)
		{
			when (values[0])
			{
				REQUEST_DOWNSTREAM -> processor?.onChunkReceived(request, values[1] ?: 0, values[2] ?: 0)
				REQUEST_UPSTREAM -> processor?.onChunkSent(request, values[1] ?: 0, values[2] ?: 0)
			}
		}

		override fun onPostExecute(result: Response<T>)
		{
			responseCallback.invoke(result)
		}
	}
}
