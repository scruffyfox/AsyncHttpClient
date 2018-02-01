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
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

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
