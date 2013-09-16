package net.callumtaylor.asynchttp;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.zip.GZIPInputStream;

import net.callumtaylor.asynchttp.obj.HttpsFactory.EasySSLSocketFactory;
import net.callumtaylor.asynchttp.obj.RequestMode;
import net.callumtaylor.asynchttp.obj.RequestUtil;
import net.callumtaylor.asynchttp.obj.entity.ProgressEntityWrapper;
import net.callumtaylor.asynchttp.obj.entity.ProgressEntityWrapper.ProgressListener;
import net.callumtaylor.asynchttp.processor.ByteArrayProcessor;
import net.callumtaylor.asynchttp.processor.Processor;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.net.Uri;
import android.text.TextUtils;

public class SyncHttpClient<E>
{
	private Uri requestUri;
	private long requestTimeout = 0L;
	private boolean allowAllSsl = false;
	private ClientExecutorTask<E> executor;

	/**
	 * Creates a new client using a base Url without a timeout
	 * @param baseUrl The base connection url
	 */
	public SyncHttpClient(String baseUrl)
	{
		this(baseUrl, 0);
	}

	/**
	 * Creates a new client using a base Uri without a timeout
	 * @param baseUrl The base connection uri
	 */
	public SyncHttpClient(Uri baseUri)
	{
		this(baseUri, 0);
	}

	/**
	 * Creates a new client using a base Url with a timeout in MS
	 * @param baseUrl The base connection url
	 * @param timeout The timeout in MS
	 */
	public SyncHttpClient(String baseUrl, long timeout)
	{
		this(Uri.parse(baseUrl), timeout);
	}

	/**
	 * Creates a new client using a base Uri with a timeout in MS
	 * @param baseUrl The base connection uri
	 * @param timeout The timeout in MS
	 */
	public SyncHttpClient(Uri baseUri, long timeout)
	{
		requestUri = baseUri;
		requestTimeout = timeout;
	}

	/**
	 * Cancells the current executor task
	 */
	public void cancel()
	{
		if (executor != null)
		{
			executor.cancel();
		}
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param response The response handler for the request
	 */
	public E get(Processor<?> response)
	{
		return get("", null, null, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param path The path extended from the baseUri
	 */
	public E get(String path)
	{
		return get(path, null, null, new ByteArrayProcessor());
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public E get(String path, Processor<?> response)
	{
		return get(path, null, null, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E get(List<Header> headers, Processor<?> response)
	{
		return get("", null, headers, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E get(List<NameValuePair> params, List<Header> headers, Processor<?> response)
	{
		return get("", params, headers, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E get(String path, List<NameValuePair> params, Processor<?> response)
	{
		return get(path, params, null, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E get(String path, List<NameValuePair> params, List<Header> headers, Processor<?> response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		return executeTask(RequestMode.GET, requestUri, headers, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param response The response handler for the request
	 */
	public E delete(Processor<?> response)
	{
		return delete("", null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 */
	public E delete(String path)
	{
		return delete(path, null, null, new ByteArrayProcessor());
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public E delete(String path, Processor<?> response)
	{
		return delete(path, null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E delete(List<Header> headers, Processor<?> response)
	{
		return delete("", null, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E delete(List<NameValuePair> params, List<Header> headers, Processor<?> response)
	{
		return delete("", params, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E delete(String path, List<NameValuePair> params, Processor<?> response)
	{
		return delete(path, params, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E delete(String path, List<NameValuePair> params, List<Header> headers, Processor<?> response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		return executeTask(RequestMode.DELETE, requestUri, headers, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param response The response handler for the request
	 */
	public E post(Processor<?> response)
	{
		return post("", null, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUr
	 * @param path The path extended from the baseUri
	 */
	public E post(String path)
	{
		return post(path, null, null, null, new ByteArrayProcessor());
	}

	/**
	 * Performs a POST request on the baseUr
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public E post(String path, Processor<?> response)
	{
		return post(path, null, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public E post(List<NameValuePair> params, Processor<?> response)
	{
		return post("", params, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E post(List<NameValuePair> params, List<Header> headers, Processor<?> response)
	{
		return post("", params, null, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E post(HttpEntity postData, Processor<?> response)
	{
		return post("", null, postData, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E post(HttpEntity postData, List<Header> headers, Processor<?> response)
	{
		return post("", null, postData, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E post(List<NameValuePair> params, HttpEntity postData, Processor<?> response)
	{
		return post("", params, postData, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public E post(String path, List<NameValuePair> params, Processor<?> response)
	{
		return post(path, params, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E post(String path, List<NameValuePair> params, List<Header> headers, Processor<?> response)
	{
		return post(path, params, null, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E post(String path, HttpEntity postData, Processor<?> response)
	{
		return post(path, null, postData, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E post(String path, HttpEntity postData, List<Header> headers, Processor<?> response)
	{
		return post(path, null, postData, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E post(String path, List<NameValuePair> params, HttpEntity postData, Processor<?> response)
	{
		return post(path, params, postData, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E post(String path, List<NameValuePair> params, HttpEntity postData, List<Header> headers, Processor<?> response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		return executeTask(RequestMode.POST, requestUri, headers, postData, response);
	}

	/**
	 * Performs a PUT request on the baseUr
	 * @param response The response handler for the request
	 */
	public E put(Processor<?> response)
	{
		return put("", null, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUr
	 * @param path The path extended from the baseUri
	 */
	public E put(String path)
	{
		return put(path, null, null, null, new ByteArrayProcessor());
	}

	/**
	 * Performs a PUT request on the baseUr
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public E put(String path, Processor<?> response)
	{
		return put(path, null, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public E put(List<NameValuePair> params, Processor<?> response)
	{
		return put("", params, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E put(List<NameValuePair> params, List<Header> headers, Processor<?> response)
	{
		return put("", params, null, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E put(HttpEntity postData, Processor<?> response)
	{
		return put("", null, postData, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E put(HttpEntity postData, List<Header> headers, Processor<?> response)
	{
		return put("", null, postData, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E put(List<NameValuePair> params, HttpEntity postData, Processor<?> response)
	{
		return put("", params, postData, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public E put(String path, List<NameValuePair> params, Processor<?> response)
	{
		return put(path, params, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E put(String path, List<NameValuePair> params, List<Header> headers, Processor<?> response)
	{
		return put(path, params, null, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E put(String path, HttpEntity postData, Processor<?> response)
	{
		return put(path, null, postData, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E put(String path, HttpEntity postData, List<Header> headers, Processor<?> response)
	{
		return put(path, null, postData, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E put(String path, List<NameValuePair> params, HttpEntity postData, Processor<?> response)
	{
		return put(path, params, postData, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E put(String path, List<NameValuePair> params, HttpEntity postData, List<Header> headers, Processor<?> response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		return executeTask(RequestMode.PUT, requestUri, headers, postData, response);
	}

	private E executeTask(RequestMode mode, Uri uri, List<Header> headers, HttpEntity sendData, Processor<?> requestProcessor)
	{
		executor = new ClientExecutorTask<E>(mode, uri, headers, sendData, requestProcessor);
		executor.onPreExecute();
		E response = executor.execute();
		executor.onPostExecute();

		return response;
	}

	/**
	 * Sets whether to allow all ssl, trusted or not
	 * @param allow
	 */
	public void setAllowAllSsl(boolean allow)
	{
		this.allowAllSsl = allow;
	}

	private class ClientExecutorTask<F>
	{
		private static final int BUFFER_SIZE = 1 * 1024 * 8;

		private final Processor<?> response;
		private final Uri requestUri;
		private final List<Header> requestHeaders;
		private final HttpEntity postData;
		private final RequestMode requestMode;
		private volatile boolean cancelled = false;

		public ClientExecutorTask(RequestMode mode, Uri request, List<Header> headers, HttpEntity postData, Processor<?> response)
		{
			this.response = response;
			this.requestUri = request;
			this.requestHeaders = headers;
			this.postData = postData;
			this.requestMode = mode;
		}

		public boolean isCancelled()
		{
			return cancelled;
		}

		public void cancel()
		{
			cancelled = true;
		}

		public void onPreExecute()
		{
			if (this.response != null)
			{
				this.response.getConnectionInfo().connectionTime = System.currentTimeMillis();
				this.response.getConnectionInfo().requestMethod = requestMode;
				this.response.onSend();
			}
		}

		@SuppressWarnings("unchecked")
		public F execute()
		{
			HttpClient httpClient;

			if (allowAllSsl)
			{
				SchemeRegistry schemeRegistry = new SchemeRegistry();
				schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
				schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));

				HttpParams httpParams = new BasicHttpParams();
				httpParams.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
				httpParams.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
				httpParams.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
				HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);

				ClientConnectionManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
				httpClient = new DefaultHttpClient(cm, httpParams);
			}
			else
			{
				httpClient = new DefaultHttpClient();
			}

			HttpContext httpContext = new BasicHttpContext();
			HttpRequestBase request = null;

			try
			{
				if (this.response != null)
				{
					this.response.getConnectionInfo().connectionUrl = requestUri.toString();
				}

				System.setProperty("http.keepAlive", "false");

				if (requestMode == RequestMode.GET)
				{
					request = new HttpGet(requestUri.toString());
				}
				else if (requestMode == RequestMode.POST)
				{
					request = new HttpPost(requestUri.toString());
				}
				else if (requestMode == RequestMode.PUT)
				{
					request = new HttpPut(requestUri.toString());
				}
				else if (requestMode == RequestMode.DELETE)
				{
					request = new HttpDelete(requestUri.toString());
				}

				HttpParams p = httpClient.getParams();
				HttpConnectionParams.setConnectionTimeout(p, (int)requestTimeout);
				HttpConnectionParams.setSoTimeout(p, (int)requestTimeout);
				request.setHeader("Connection", "close");

				if (postData != null)
				{
					request.setHeader(postData.getContentType().getName(), postData.getContentType().getValue());
				}

				if (requestHeaders != null)
				{
					for (Header header : requestHeaders)
					{
						request.setHeader(header.getName(), header.getValue());
					}
				}

				if ((requestMode == RequestMode.POST || requestMode == RequestMode.PUT) && postData != null)
				{
					final long contentLength = postData.getContentLength();
					if (this.response != null && !isCancelled())
					{
						this.response.getConnectionInfo().connectionLength = contentLength;
					}

					((HttpEntityEnclosingRequestBase)request).setEntity(new ProgressEntityWrapper(postData, new ProgressListener()
					{
						@Override public void onBytesTransferred(byte[] buffer, int len, long transferred)
						{
							if (response != null)
							{
								response.onPublishedUploadProgress(buffer, len, contentLength);
								response.onPublishedUploadProgress(buffer, len, transferred, contentLength);
							}
						}
					}));
				}

				// Get the response
				HttpResponse response = httpClient.execute(request, httpContext);
				int responseCode = response.getStatusLine().getStatusCode();

				if (response.getEntity() != null)
				{
					String encoding = response.getEntity().getContentEncoding() == null ? "" : response.getEntity().getContentEncoding().getValue();
					long contentLength = response.getEntity().getContentLength();
					InputStream i = response.getEntity().getContent();

					if ("gzip".equals(encoding))
					{
						i = new GZIPInputStream(new BufferedInputStream(i, BUFFER_SIZE));
					}
					else
					{
						i = new BufferedInputStream(i, BUFFER_SIZE);
					}

					if (this.response != null && !isCancelled())
					{
						this.response.getConnectionInfo().responseCode = responseCode;
					}

					try
					{
						if (contentLength != 0)
						{
							byte[] buffer = new byte[BUFFER_SIZE];

							int len = 0;
							int readCount = 0;
							while ((len = i.read(buffer)) > -1 && !isCancelled())
							{
								if (this.response != null)
								{
									this.response.onPublishedDownloadProgress(buffer, len, contentLength);
									this.response.onPublishedDownloadProgress(buffer, len, readCount, contentLength);
								}

								readCount += len;
							}

							if (this.response != null && !isCancelled())
							{
								this.response.getConnectionInfo().responseLength = readCount;

								// we fake the content length, because it can be -1
								this.response.onPublishedDownloadProgress(null, readCount, readCount);
								this.response.onPublishedDownloadProgress(null, readCount, readCount, readCount);
							}

							i.close();
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
				}

				if (this.response != null && !isCancelled())
				{
					this.response.getConnectionInfo().responseTime = System.currentTimeMillis();
					this.response.getConnectionInfo().responseCode = responseCode;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			if (this.response != null && !isCancelled())
			{
				return (F)this.response.getContent();
			}

			return null;
		}

		public void onPostExecute()
		{
		}
	}
}