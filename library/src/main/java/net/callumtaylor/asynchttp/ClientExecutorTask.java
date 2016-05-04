package net.callumtaylor.asynchttp;

import android.net.Uri;

import net.callumtaylor.asynchttp.obj.ClientTaskImpl;
import net.callumtaylor.asynchttp.obj.Packet;
import net.callumtaylor.asynchttp.obj.RequestMode;
import net.callumtaylor.asynchttp.response.ResponseHandler;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

	protected final ResponseHandler response;
	protected final Uri requestUri;
	protected final Headers requestHeaders;
	protected final RequestBody postData;
	protected final RequestMode requestMode;
	protected boolean allowRedirect = true;
	protected long requestTimeout = 0L;
	protected AtomicBoolean cancelled = new AtomicBoolean(false);

	public ClientExecutorTask(RequestMode mode, Uri request, Headers headers, RequestBody postData, ResponseHandler response, boolean allowRedirect, long requestTimeout)
	{
		this.response = response;
		this.requestUri = request;
		this.requestHeaders = headers;
		this.postData = postData;
		this.requestMode = mode;
		this.requestTimeout = requestTimeout;
	}

	@Override public boolean isCancelled()
	{
		return cancelled.get();
	}

	@Override public void cancel()
	{
		cancelled.set(true);
	}

	@Override public void onPreExecute()
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

	@Override public F doInBackground()
	{
		OkHttpClient httpClient;

//			if (allowAllSsl)
//			{
//				SchemeRegistry schemeRegistry = new SchemeRegistry();
//				schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//				schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
//
//				HttpParams httpParams = new BasicHttpParams();
//				httpParams.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
//				httpParams.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
//				httpParams.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
//				HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
//
//				ClientConnectionManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
//				httpClient = new DefaultHttpClient(cm, httpParams);
//			}
//			else
		{
			httpClient = new OkHttpClient()
				.newBuilder()
				.followRedirects(allowRedirect)
				.followSslRedirects(allowRedirect)
				.connectTimeout(requestTimeout, TimeUnit.MILLISECONDS)
				.build();
		}

		try
		{
			System.setProperty("http.keepAlive", "false");
			Request.Builder request = new Request.Builder()
				.url(requestUri.toString());

			if (requestMode == RequestMode.GET)
			{
				request = request.get();
			}
//				else if (requestMode == RequestMode.POST)
//				{
//					request = new HttpPost(requestUri.toString());
//				}
//				else if (requestMode == RequestMode.PUT)
//				{
//					request = new HttpPut(requestUri.toString());
//				}
//				else if (requestMode == RequestMode.DELETE)
//				{
//					request = new HttpDeleteWithBody(requestUri.toString());
//				}
//				else if (requestMode == RequestMode.HEAD)
//				{
//					request = new HttpHead(requestUri.toString());
//				}
//				else if (requestMode == RequestMode.PATCH)
//				{
//					request = new HttpPatch(requestUri.toString());
//				}
//				else if (requestMode == RequestMode.OPTIONS)
//				{
//					request = new HttpOptions(requestUri.toString());
//				}

			request.header("Connection", "close");

			if (requestHeaders != null)
			{
				request.headers(requestHeaders);
			}

//				if ((requestMode == RequestMode.POST || requestMode == RequestMode.PUT || requestMode == RequestMode.DELETE || requestMode == RequestMode.PATCH) && postData != null)
//				{
//					final long contentLength = postData.getContentLength();
//					if (this.response != null && !isCancelled())
//					{
//						this.response.getConnectionInfo().connectionLength = contentLength;
//					}
//
//					((RequestBodyEnclosingRequestBase)request).setEntity(new ProgressEntityWrapper(postData, new ProgressListener()
//					{
//						@Override public void onBytesTransferred(byte[] buffer, int len, long transferred)
//						{
//							if (response != null)
//							{
//								response.onPublishedUploadProgress(buffer, len, contentLength);
//								response.onPublishedUploadProgress(buffer, len, transferred, contentLength);
//
//								publishProgress(new Packet(transferred, contentLength, false));
//							}
//						}
//					}));
//				}

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
//					String encoding = response.body().getContentEncoding() == null ? "" : response.getEntity().getContentEncoding().getValue();
				long contentLength = response.body().contentLength();
				InputStream responseStream;
				InputStream stream = response.body().byteStream();

//					if ("gzip".equals(encoding))
//					{
//						responseStream = new GZIPInputStream(new BufferedInputStream(stream, BUFFER_SIZE));
//					}
//					else
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

	@Override public void onPostExecute()
	{
		if (this.response != null && !isCancelled())
		{
			this.response.beforeCallback();
			this.response.beforeFinish();
			this.response.onFinish();
			this.response.onFinish(this.response.getConnectionInfo().responseCode >= 400 || this.response.getConnectionInfo().responseCode == 0);
		}
	}

	@Override public void postPublishProgress(Packet... values)
	{
	}

	@Override public void onProgressUpdate(Packet... values)
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
