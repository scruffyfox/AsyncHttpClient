package net.callumtaylor.asynchttp;

import android.net.Uri;

import net.callumtaylor.asynchttp.obj.ClientTaskImpl;
import net.callumtaylor.asynchttp.obj.CountingRequestBody;
import net.callumtaylor.asynchttp.obj.Packet;
import net.callumtaylor.asynchttp.obj.RequestMode;
import net.callumtaylor.asynchttp.response.ResponseHandler;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * // TODO: Add class description
 *
 * @author Callum Taylor
 * @documentation // TODO Reference flow doc
 * @project AsyncHttpClient
 */
public class ClientExecutorTask<F> implements ClientTaskImpl<F>
{
	private static final int BUFFER_SIZE = 1024 * 8;

	protected ResponseHandler response;
	protected Uri requestUri;
	protected Headers requestHeaders;
	protected RequestBody postData;
	protected RequestMode requestMode;
	protected boolean allowRedirect = true;
	protected boolean allowAllSsl = false;
	protected long requestTimeout = 0L;
	protected AtomicBoolean cancelled = new AtomicBoolean(false);

	public ClientExecutorTask(RequestMode mode, Uri request, Headers headers, RequestBody postData, ResponseHandler response, boolean allowRedirect, boolean allowAllSsl, long requestTimeout)
	{
		this.response = response;
		this.requestUri = request;
		this.requestHeaders = headers;
		this.postData = postData;
		this.requestMode = mode;
		this.requestTimeout = requestTimeout;
		this.allowAllSsl = allowAllSsl;
		this.allowRedirect = allowRedirect;
	}

	@Override public boolean isCancelled()
	{
		return cancelled.get();
	}

	@Override public void cancel()
	{
		cancelled.set(true);
	}

	@Override public void preExecute()
	{
		if (this.response != null)
		{
			this.response.getConnectionInfo().connectionUrl = requestUri.toString();
			this.response.getConnectionInfo().connectionTime = System.currentTimeMillis();
			this.response.getConnectionInfo().requestMethod = requestMode;
			this.response.getConnectionInfo().requestHeaders = requestHeaders;
			this.response.onSend();
		}
	}

	@Override public F executeTask()
	{
		OkHttpClient httpClient;

		httpClient = new OkHttpClient()
			.newBuilder()
			.followRedirects(allowRedirect)
			.followSslRedirects(allowRedirect)
			.connectTimeout(requestTimeout, TimeUnit.MILLISECONDS)
			.build();

		if (allowAllSsl)
		{
			try
			{
				// Create a trust manager that does not validate certificate chains
				final TrustManager[] trustAllCerts = new TrustManager[]
				{
					new X509TrustManager()
					{
						@Override public void checkClientTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException{}
						@Override public void checkServerTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException{}

						@Override public java.security.cert.X509Certificate[] getAcceptedIssuers()
						{
							return new java.security.cert.X509Certificate[]{};
						}
					}
				};

				final SSLContext sslContext = SSLContext.getInstance("SSL");
				sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
				final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

				httpClient = httpClient.newBuilder()
					.sslSocketFactory(sslSocketFactory)
					.hostnameVerifier(new HostnameVerifier()
					{
						@Override public boolean verify(String hostname, SSLSession session)
						{
							return true;
						}
					})
					.build();
			}
			catch (NoSuchAlgorithmException e)
			{
				e.printStackTrace();
			}
			catch (KeyManagementException e)
			{
				e.printStackTrace();
			}
		}

		try
		{
			System.setProperty("http.keepAlive", "false");
			Request.Builder request = new Request.Builder()
				.url(requestUri.toString());

			if (postData != null)
			{
				postData = new CountingRequestBody(postData, new CountingRequestBody.Listener()
				{
					@Override public void onRequestProgress(byte[] buffer, long bufferCount, long bytesWritten, long contentLength)
					{
						if (response != null)
						{
							response.onPublishedUploadProgress(buffer, bufferCount, contentLength);
							response.onPublishedUploadProgress(buffer, bufferCount, bytesWritten, contentLength);

							transferProgress(new Packet(bytesWritten, contentLength, false));
						}
					}
				});
			}

			if (requestMode == RequestMode.GET)
			{
				request = request.get();
			}
			else if (requestMode == RequestMode.POST)
			{
				request = request.post(postData);
			}
			else if (requestMode == RequestMode.PUT)
			{
				request = request.put(postData);
			}
			else if (requestMode == RequestMode.DELETE)
			{
				if (postData != null)
				{
					request = request.delete(postData);
				}
				else
				{
					request = request.delete();
				}
			}
			else if (requestMode == RequestMode.HEAD)
			{
				request = request.head();
			}
			else if (requestMode == RequestMode.PATCH)
			{
				request = request.patch(postData);
			}
			else if (requestMode == RequestMode.OPTIONS)
			{
				request = request.method("OPTIONS", null);
			}

			request.header("Connection", "close");

			if (requestHeaders != null)
			{
				request.headers(requestHeaders);
			}

			if ((requestMode == RequestMode.POST || requestMode == RequestMode.PUT || requestMode == RequestMode.DELETE || requestMode == RequestMode.PATCH) && postData != null)
			{
				final long contentLength = postData.contentLength();
				if (this.response != null && !isCancelled())
				{
					this.response.getConnectionInfo().connectionLength = contentLength;
				}
			}

			// Get the response
			Call call = httpClient.newCall(request.build());
			Response response = call.execute();

			int responseCode = response.code();

			if (response.headers() != null && this.response != null)
			{
				this.response.getConnectionInfo().responseHeaders = response.headers();
			}

			if (response.body() != null)
			{
				String encoding = response.header("Content-Encoding", "");
				long contentLength = response.body().contentLength();
				InputStream responseStream;
				InputStream stream = response.body().byteStream();

				if ("gzip".equalsIgnoreCase(encoding))
				{
					responseStream = new GZIPInputStream(new BufferedInputStream(stream, BUFFER_SIZE));
				}
				else
				{
					responseStream = new BufferedInputStream(stream, BUFFER_SIZE);
				}

				if (this.response != null && !isCancelled())
				{
					this.response.getConnectionInfo().responseCode = responseCode;
				}

				try
				{
					if (this.response != null && contentLength != 0 && !isCancelled())
					{
						this.response.onBeginPublishedDownloadProgress(responseStream, this, contentLength);
						this.response.generateContent();
					}
				}
				catch (SocketTimeoutException timeout)
				{
					responseCode = 0;
					timeout.printStackTrace();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					responseStream.close();
				}
			}

			if (this.response != null && !isCancelled())
			{
				this.response.getConnectionInfo().responseCode = responseCode;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (this.response != null && !isCancelled())
		{
			this.response.getConnectionInfo().responseTime = System.currentTimeMillis();

			if (this.response.getConnectionInfo().responseCode < 400 && this.response.getConnectionInfo().responseCode > 100)
			{
				this.response.onSuccess();
			}
			else
			{
				this.response.onFailure();
			}
		}

		return (F)this.response.getContent();
	}

	@Override public void postExecute()
	{
		if (this.response != null && !isCancelled())
		{
			this.response.beforeCallback();
			this.response.beforeFinish();
			this.response.onFinish();
			this.response.onFinish(this.response.getConnectionInfo().responseCode >= 400 || this.response.getConnectionInfo().responseCode == 0);
		}
	}

	@Override public void transferProgress(Packet... values)
	{
		if (this.response != null && !isCancelled())
		{
			if (values[0].isDownload)
			{
				this.response.onPublishedDownloadProgressUI(values[0].length, values[0].total);
			}
			else
			{
				this.response.onPublishedUploadProgressUI(values[0].length, values[0].total);
			}
		}
	}
}
