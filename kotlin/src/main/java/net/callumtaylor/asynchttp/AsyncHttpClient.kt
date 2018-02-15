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
	}

	constructor(baseUrl: String, timeout: Long = 0) : this(Uri.parse(baseUrl), timeout)

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
			var httpClient = OkHttpClient().newBuilder()
				.followRedirects(request.followRedirects)
				.followSslRedirects(request.followRedirects)
				.connectTimeout(timeout, TimeUnit.MILLISECONDS)
				.writeTimeout(timeout, TimeUnit.MILLISECONDS)
				.readTimeout(timeout, TimeUnit.MILLISECONDS)
//				.cache(cache)
				.build()

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

			httpRequest = when (request.type) {
//				"POST" -> httpRequest.post(),
				"GET" -> httpRequest.get()
				else -> httpRequest.method(request.type, null/*, request.body*/)
			}

			request.headers.forEach { header ->
				httpRequest.addHeader(header.first, header.second)
			}

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
