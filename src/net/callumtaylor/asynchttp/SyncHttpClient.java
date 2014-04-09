package net.callumtaylor.asynchttp;

import android.net.Uri;
import android.os.NetworkOnMainThreadException;
import android.text.TextUtils;

import net.callumtaylor.asynchttp.obj.ConnectionInfo;
import net.callumtaylor.asynchttp.obj.HttpDeleteWithBody;
import net.callumtaylor.asynchttp.obj.HttpPatch;
import net.callumtaylor.asynchttp.obj.HttpsFactory;
import net.callumtaylor.asynchttp.obj.HttpsFactory.EasySSLSocketFactory;
import net.callumtaylor.asynchttp.obj.Packet;
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
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * @mainpage
 *
 * This class is a synchronous class which runs on any thread that the code was created
 * on. This will throw a {@link NetworkOnMainThreadException} if ran on the UI thread.
 *
 * <b>Note:</b> Because of the way SyncHttpClient works, only one instance can be created
 * at a time. If one client makes 2 requests, the first request is canceled for the new
 * request. You can either wait for the first to finish before making the second, or you
 * can create two seperate instances.
 *
 * <b>Depends on</b>
 * <ul>
 * <li>{@link Processor}</li>
 * <li>{@link HttpEntity}</li>
 * <li>{@link NameValuePair}</li>
 * <li>{@link ConnectionInfo}</li>
 * <li>{@link Packet}</li>
 * <li>{@link RequestMode}</li>
 * <li>{@link RequestUtil}</li>
 * <li>{@link HttpsFactory}</li>
 * </ul>
 * <h1>Example GET</h1>
 *
 * SyncHttpClient is a paramitized class which means the type you infer to it, is the type
 * that gets returned when calling the method. When supplying a {@link Processor}, that
 * processor must also paramitized with the same type as the SyncHttpClient instance.
 *
 * <pre>
 * SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>(&quot;http://example.com&quot;);
 * List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
 * params.add(new BasicNameValuePair(&quot;key&quot;, &quot;value&quot;));
 *
 * List&lt;Header&gt; headers = new ArrayList&lt;Header&gt;();
 * headers.add(new BasicHeader(&quot;1&quot;, &quot;2&quot;));
 *
 * JsonElement response = client.get(&quot;api/v1/&quot;, params, headers, new JsonProcessor());
 * </pre>
 *
 * <h1>Example DELETE</h1>
 *
 * <pre>
 * SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>(&quot;http://example.com&quot;);
 * List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
 * params.add(new BasicNameValuePair(&quot;key&quot;, &quot;value&quot;));
 *
 * List&lt;Header&gt; headers = new ArrayList&lt;Header&gt;();
 * headers.add(new BasicHeader(&quot;1&quot;, &quot;2&quot;));
 *
 * JsonElement response = client.delete(&quot;api/v1/&quot;, params, headers, new JsonProcessor());
 * </pre>
 *
 * <h1>Example POST - Single Entity</h1>
 *
 * <pre>
 * SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>(&quot;http://example.com&quot;);
 * List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
 * params.add(new BasicNameValuePair(&quot;key&quot;, &quot;value&quot;));
 *
 * List&lt;Header&gt; headers = new ArrayList&lt;Header&gt;();
 * headers.add(new BasicHeader(&quot;1&quot;, &quot;2&quot;));
 *
 * JsonEntity data = new JsonEntity(&quot;{\&quot;key\&quot;:\&quot;value\&quot;}&quot;);
 * GzippedEntity entity = new GzippedEntity(data);
 *
 * JsonElement response = client.post(&quot;api/v1/&quot;, params, entity, headers, new JsonProcessor());
 * </pre>
 *
 * <h1>Example POST - Multiple Entity + file</h1>
 *
 * <pre>
 * SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>(&quot;http://example.com&quot;);
 * List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
 * params.add(new BasicNameValuePair(&quot;key&quot;, &quot;value&quot;));
 *
 * List&lt;Header&gt; headers = new ArrayList&lt;Header&gt;();
 * headers.add(new BasicHeader(&quot;1&quot;, &quot;2&quot;));
 *
 * MultiPartEntity entity = new MultiPartEntity();
 * FileEntity data1 = new FileEntity(new File(&quot;/IMG_6614.JPG&quot;), &quot;image/jpeg&quot;);
 * JsonEntity data2 = new JsonEntity(&quot;{\&quot;key\&quot;:\&quot;value\&quot;}&quot;);
 * entity.addFilePart(&quot;image1.jpg&quot;, data1);
 * entity.addPart(&quot;content1&quot;, data2);
 *
 * JsonElement response = client.post(&quot;api/v1/&quot;, params, entity, headers, new JsonProcessor());
 * </pre>
 *
 * <h1>Example PUT</h1>
 *
 * <pre>
 * SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>(&quot;http://example.com&quot;);
 * List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
 * params.add(new BasicNameValuePair(&quot;key&quot;, &quot;value&quot;));
 *
 * List&lt;Header&gt; headers = new ArrayList&lt;Header&gt;();
 * headers.add(new BasicHeader(&quot;1&quot;, &quot;2&quot;));
 *
 * JsonEntity data = new JsonEntity(&quot;{\&quot;key\&quot;:\&quot;value\&quot;}&quot;);
 * GzippedEntity entity = new GzippedEntity(data);
 *
 * JsonElement response = client.post(&quot;api/v1/&quot;, params, entity, headers, new JsonProcessor());
 * </pre>
 *
 * <h1>Example custom processor</h1>
 *
 * <pre>
 * SyncHttpClient<String> client = new SyncHttpClient<String>(&quot;http://example.com&quot;);
 * List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
 * params.add(new BasicNameValuePair(&quot;key&quot;, &quot;value&quot;));
 *
 * List&lt;Header&gt; headers = new ArrayList&lt;Header&gt;();
 * headers.add(new BasicHeader(&quot;1&quot;, &quot;2&quot;));
 *
 * String encodedResponse = client.get(&quot;api/v1/&quot;, params, headers, new Processor()
 * {
 * 	private StringBuffer stringBuffer;
 *
 * 	@Override public void onPublishedDownloadProgress(byte[] chunk, int chunkLength, long totalProcessed, long totalLength)
 * 	{
 *  	if (stringBuffer == null)
 *  	{
 * 			int total = (int)(totalLength > Integer.MAX_VALUE ? Integer.MAX_VALUE : totalLength);
 * 			stringBuffer = new StringBuffer(Math.max(8192, total));
 * 		}
 *
 * 		if (chunk != null)
 *		{
 * 			try
 * 			{
 *				// Shift all the bytes right
 * 				byte tmp = chunk[chunk.length - 1];
 *				for (int index = chunk.length - 2; index >= 0; index--)
 *				{
 *					chunk[index + 1] = chunk[index];
 *				}
 *
 *				chunk[0] = tmp;
 * 				stringBuffer.append(new String(chunk, 0, chunkLength, "UTF-8").);
 * 			}
 * 			catch (Exception e)
 * 			{
 * 				e.printStackTrace();
 * 			}
 *  	}
 * 	}
 *
 * 	@Override public String getContent()
 * 	{
 *  	return stringBuffer.toString();
 * 	}
 * });
 * </pre>
 *
 * Because of the nature of REST, GET and DELETE requests behave in the same
 * way, POST and PUT requests also behave in the same way.
 *
 * @author Callum Taylor &lt;callumtaylor.net&gt; &#064;scruffyfox
 */
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
	 * @param baseUri The base connection uri
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
	 * @param baseUri The base connection uri
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
	 * @param params The request params for the connection
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
	 * Performs a OPTIONS request on the baseUri
	 * @param response The response handler for the request
	 */
	public E options(Processor<?> response)
	{
		return options("", null, null, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param path The path extended from the baseUri
	 */
	public E options(String path)
	{
		return options(path, null, null, new ByteArrayProcessor());
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public E options(String path, Processor<?> response)
	{
		return options(path, null, null, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E options(List<Header> headers, Processor<?> response)
	{
		return options("", null, headers, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E options(List<NameValuePair> params, List<Header> headers, Processor<?> response)
	{
		return options("", params, headers, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The request params for the connection
	 * @param response The response handler for the request
	 */
	public E options(String path, List<NameValuePair> params, Processor<?> response)
	{
		return options(path, params, null, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E options(String path, List<NameValuePair> params, List<Header> headers, Processor<?> response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		return executeTask(RequestMode.OPTIONS, requestUri, headers, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param response The response handler for the request
	 */
	public E head(Processor<?> response)
	{
		return head("", null, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param path The path extended from the baseUri
	 */
	public E head(String path)
	{
		return head(path, null, null, new ByteArrayProcessor());
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public E head(String path, Processor<?> response)
	{
		return head(path, null, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E head(List<Header> headers, Processor<?> response)
	{
		return head("", null, headers, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E head(List<NameValuePair> params, List<Header> headers, Processor<?> response)
	{
		return head("", params, headers, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The request params for the connection
	 * @param response The response handler for the request
	 */
	public E head(String path, List<NameValuePair> params, Processor<?> response)
	{
		return head(path, params, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E head(String path, List<NameValuePair> params, List<Header> headers, Processor<?> response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		return executeTask(RequestMode.HEAD, requestUri, headers, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param response The response handler for the request
	 */
	public E delete(Processor<?> response)
	{
		return delete("", null, null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 */
	public E delete(String path)
	{
		return delete(path, null, null, null, new ByteArrayProcessor());
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public E delete(String path, Processor<?> response)
	{
		return delete(path, null, null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E delete(List<Header> headers, Processor<?> response)
	{
		return delete("", null, null, headers, response);
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
	 * @param params The request params for the connection
	 * @param response The response handler for the request
	 */
	public E delete(String path, List<NameValuePair> params, Processor<?> response)
	{
		return delete(path, params, null, null, response);
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
	 * Performs a DELETE request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param deleteData The delete data entity to delete to the server
	 * @param response The response handler for the request
	 */
	public E delete(List<NameValuePair> params, HttpEntity deleteData, Processor<?> response)
	{
		return delete("", params, deleteData, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param deleteData The delete data entity to delete to the server
	 * @param response The response handler for the request
	 */
	public E delete(String path, HttpEntity deleteData, Processor<?> response)
	{
		return delete(path, null, deleteData, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param deleteData The delete data entity to delete to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E delete(String path, HttpEntity deleteData, List<Header> headers, Processor<?> response)
	{
		return delete(path, null, deleteData, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param deleteData The delete data entity to delete to the server
	 * @param response The response handler for the request
	 */
	public E delete(String path, List<NameValuePair> params, HttpEntity deleteData, Processor<?> response)
	{
		return delete(path, params, deleteData, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param deleteData The delete data entity to delete to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E delete(String path, List<NameValuePair> params, HttpEntity deleteData, List<Header> headers, Processor<?> response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		return executeTask(RequestMode.DELETE, requestUri, headers, deleteData, response);
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

	/**
	 * Performs a PATCH request on the baseUr
	 * @param response The response handler for the request
	 */
	public E patch(Processor<?> response)
	{
		return patch("", null, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUr
	 * @param path The path extended from the baseUri
	 */
	public E patch(String path)
	{
		return patch(path, null, null, null, new ByteArrayProcessor());
	}

	/**
	 * Performs a PATCH request on the baseUr
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public E patch(String path, Processor<?> response)
	{
		return patch(path, null, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public E patch(List<NameValuePair> params, Processor<?> response)
	{
		return patch("", params, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E patch(List<NameValuePair> params, List<Header> headers, Processor<?> response)
	{
		return patch("", params, null, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E patch(HttpEntity postData, Processor<?> response)
	{
		return patch("", null, postData, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E patch(HttpEntity postData, List<Header> headers, Processor<?> response)
	{
		return patch("", null, postData, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E patch(List<NameValuePair> params, HttpEntity postData, Processor<?> response)
	{
		return patch("", params, postData, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public E patch(String path, List<NameValuePair> params, Processor<?> response)
	{
		return patch(path, params, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E patch(String path, List<NameValuePair> params, List<Header> headers, Processor<?> response)
	{
		return patch(path, params, null, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E patch(String path, HttpEntity postData, Processor<?> response)
	{
		return patch(path, null, postData, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E patch(String path, HttpEntity postData, List<Header> headers, Processor<?> response)
	{
		return patch(path, null, postData, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E patch(String path, List<NameValuePair> params, HttpEntity postData, Processor<?> response)
	{
		return patch(path, params, postData, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E patch(String path, List<NameValuePair> params, HttpEntity postData, List<Header> headers, Processor<?> response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		return executeTask(RequestMode.PATCH, requestUri, headers, postData, response);
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

	/**
	 * Gets the connection info <b>after</b> a connection request has been made
	 * @return The connection info, or null
	 */
	public ConnectionInfo getConnectionInfo()
	{
		if (executor != null && executor.response != null)
		{
			return executor.response.getConnectionInfo();
		}

		return null;
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
					request = new HttpDeleteWithBody(requestUri.toString());
				}
				else if (requestMode == RequestMode.HEAD)
				{
					request = new HttpHead(requestUri.toString());
				}
				else if (requestMode == RequestMode.PATCH)
				{
					request = new HttpPatch(requestUri.toString());
				}
				else if (requestMode == RequestMode.OPTIONS)
				{
					request = new HttpOptions(requestUri.toString());
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

				if ((requestMode == RequestMode.POST || requestMode == RequestMode.PUT || requestMode == RequestMode.DELETE || requestMode == RequestMode.PATCH) && postData != null)
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