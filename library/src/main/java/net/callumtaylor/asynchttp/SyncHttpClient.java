package net.callumtaylor.asynchttp;

import android.net.Uri;
import android.text.TextUtils;

import net.callumtaylor.asynchttp.obj.ConnectionInfo;
import net.callumtaylor.asynchttp.obj.NameValuePair;
import net.callumtaylor.asynchttp.obj.RequestMode;
import net.callumtaylor.asynchttp.obj.RequestUtil;
import net.callumtaylor.asynchttp.response.ByteArrayResponseHandler;
import net.callumtaylor.asynchttp.response.ResponseHandler;

import java.util.List;

import okhttp3.Headers;
import okhttp3.RequestBody;

/**
 * @mainpage
 *
 * This class is a synchronous class which runs on any thread that the code was created
 * on. This will throw a {@link android.os.NetworkOnMainThreadException} if ran on the UI thread.
 *
 * <b>Note:</b> Because of the way SyncHttpClient works, only one instance can be created
 * at a time. If one client makes 2 requests, the first request is canceled for the new
 * request. You can either wait for the first to finish before making the second, or you
 * can create two separate instances.
 *
 * <b>Depends on</b>
 * <ul>
 * <li>{@link net.callumtaylor.asynchttp.response.ResponseHandler}</li>
 * <li>{@link RequestBody}</li>
 * <li>{@link NameValuePair}</li>
 * <li>{@link ConnectionInfo}</li>
 * <li>{@link net.callumtaylor.asynchttp.obj.Packet}</li>
 * <li>{@link RequestMode}</li>
 * <li>{@link RequestUtil}</li>
 * <li>{@link net.callumtaylor.asynchttp.obj.HttpsFactory}</li>
 * </ul>
 * <h1>Example GET</h1>
 *
 * SyncHttpClient is a paramitized class which means the type you infer to it, is the type
 * that gets returned when calling the method. When supplying a {@link net.callumtaylor.asynchttp.response.ResponseHandler}, that
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
 * JsonElement response = client.get(&quot;api/v1/&quot;, params, headers, new JsonResponseHandler());
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
 * JsonElement response = client.delete(&quot;api/v1/&quot;, params, headers, new JsonResponseHandler());
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
 * JsonElement response = client.post(&quot;api/v1/&quot;, params, entity, headers, new JsonResponseHandler());
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
 * JsonElement response = client.post(&quot;api/v1/&quot;, params, entity, headers, new JsonResponseHandler());
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
 * JsonElement response = client.post(&quot;api/v1/&quot;, params, entity, headers, new JsonResponseHandler());
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
 * String encodedResponse = client.get(&quot;api/v1/&quot;, params, headers, new ResponseHandler&lt;String&gt;()
 * {
 * 	private StringBuffer stringBuffer;
 *
 * 	@Override public void onByteChunkReceived(byte[] chunk, int chunkLength, long totalProcessed, long totalLength)
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
	/**
	 * User agent to send with every request. Defaults to {@link RequestUtil#getDefaultUserAgent()}
	 */
	public static String userAgent = RequestUtil.getDefaultUserAgent();

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
	 * Cancels the current executor task
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
	public E get(ResponseHandler<?> response)
	{
		return get("", null, null, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param path The path extended from the baseUri
	 */
	public E get(String path)
	{
		return get(path, null, null, new ByteArrayResponseHandler());
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public E get(String path, ResponseHandler<?> response)
	{
		return get(path, null, null, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E get(Headers headers, ResponseHandler<?> response)
	{
		return get("", null, headers, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E get(List<NameValuePair> params, Headers headers, ResponseHandler<?> response)
	{
		return get("", params, headers, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The request params for the connection
	 * @param response The response handler for the request
	 */
	public E get(String path, List<NameValuePair> params, ResponseHandler<?> response)
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
	public E get(String path, List<NameValuePair> params, Headers headers, ResponseHandler<?> response)
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
	public E options(ResponseHandler<?> response)
	{
		return options("", null, null, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param path The path extended from the baseUri
	 */
	public E options(String path)
	{
		return options(path, null, null, new ByteArrayResponseHandler());
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public E options(String path, ResponseHandler<?> response)
	{
		return options(path, null, null, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E options(Headers headers, ResponseHandler<?> response)
	{
		return options("", null, headers, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E options(List<NameValuePair> params, Headers headers, ResponseHandler<?> response)
	{
		return options("", params, headers, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The request params for the connection
	 * @param response The response handler for the request
	 */
	public E options(String path, List<NameValuePair> params, ResponseHandler<?> response)
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
	public E options(String path, List<NameValuePair> params, Headers headers, ResponseHandler<?> response)
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
	public E head(ResponseHandler<?> response)
	{
		return head("", null, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param path The path extended from the baseUri
	 */
	public E head(String path)
	{
		return head(path, null, null, new ByteArrayResponseHandler());
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public E head(String path, ResponseHandler<?> response)
	{
		return head(path, null, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E head(Headers headers, ResponseHandler<?> response)
	{
		return head("", null, headers, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E head(List<NameValuePair> params, Headers headers, ResponseHandler<?> response)
	{
		return head("", params, headers, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The request params for the connection
	 * @param response The response handler for the request
	 */
	public E head(String path, List<NameValuePair> params, ResponseHandler<?> response)
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
	public E head(String path, List<NameValuePair> params, Headers headers, ResponseHandler<?> response)
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
	public E delete(ResponseHandler<?> response)
	{
		return delete("", null, null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 */
	public E delete(String path)
	{
		return delete(path, null, null, null, new ByteArrayResponseHandler());
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public E delete(String path, ResponseHandler<?> response)
	{
		return delete(path, null, null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E delete(Headers headers, ResponseHandler<?> response)
	{
		return delete("", null, null, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E delete(List<NameValuePair> params, Headers headers, ResponseHandler<?> response)
	{
		return delete("", params, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The request params for the connection
	 * @param response The response handler for the request
	 */
	public E delete(String path, List<NameValuePair> params, ResponseHandler<?> response)
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
	public E delete(String path, List<NameValuePair> params, Headers headers, ResponseHandler<?> response)
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
	public E delete(List<NameValuePair> params, RequestBody deleteData, ResponseHandler<?> response)
	{
		return delete("", params, deleteData, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param deleteData The delete data entity to delete to the server
	 * @param response The response handler for the request
	 */
	public E delete(String path, RequestBody deleteData, ResponseHandler<?> response)
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
	public E delete(String path, RequestBody deleteData, Headers headers, ResponseHandler<?> response)
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
	public E delete(String path, List<NameValuePair> params, RequestBody deleteData, ResponseHandler<?> response)
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
	public E delete(String path, List<NameValuePair> params, RequestBody deleteData, Headers headers, ResponseHandler<?> response)
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
	public E post(ResponseHandler<?> response)
	{
		return post("", null, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUr
	 * @param path The path extended from the baseUri
	 */
	public E post(String path)
	{
		return post(path, null, null, null, new ByteArrayResponseHandler());
	}

	/**
	 * Performs a POST request on the baseUr
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public E post(String path, ResponseHandler<?> response)
	{
		return post(path, null, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public E post(List<NameValuePair> params, ResponseHandler<?> response)
	{
		return post("", params, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E post(List<NameValuePair> params, Headers headers, ResponseHandler<?> response)
	{
		return post("", params, null, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E post(RequestBody postData, ResponseHandler<?> response)
	{
		return post("", null, postData, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E post(RequestBody postData, Headers headers, ResponseHandler<?> response)
	{
		return post("", null, postData, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E post(List<NameValuePair> params, RequestBody postData, ResponseHandler<?> response)
	{
		return post("", params, postData, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public E post(String path, List<NameValuePair> params, ResponseHandler<?> response)
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
	public E post(String path, List<NameValuePair> params, Headers headers, ResponseHandler<?> response)
	{
		return post(path, params, null, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E post(String path, RequestBody postData, ResponseHandler<?> response)
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
	public E post(String path, RequestBody postData, Headers headers, ResponseHandler<?> response)
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
	public E post(String path, List<NameValuePair> params, RequestBody postData, ResponseHandler<?> response)
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
	public E post(String path, List<NameValuePair> params, RequestBody postData, Headers headers, ResponseHandler<?> response)
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
	public E put(ResponseHandler<?> response)
	{
		return put("", null, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUr
	 * @param path The path extended from the baseUri
	 */
	public E put(String path)
	{
		return put(path, null, null, null, new ByteArrayResponseHandler());
	}

	/**
	 * Performs a PUT request on the baseUr
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public E put(String path, ResponseHandler<?> response)
	{
		return put(path, null, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public E put(List<NameValuePair> params, ResponseHandler<?> response)
	{
		return put("", params, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E put(List<NameValuePair> params, Headers headers, ResponseHandler<?> response)
	{
		return put("", params, null, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E put(RequestBody postData, ResponseHandler<?> response)
	{
		return put("", null, postData, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E put(RequestBody postData, Headers headers, ResponseHandler<?> response)
	{
		return put("", null, postData, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E put(List<NameValuePair> params, RequestBody postData, ResponseHandler<?> response)
	{
		return put("", params, postData, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public E put(String path, List<NameValuePair> params, ResponseHandler<?> response)
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
	public E put(String path, List<NameValuePair> params, Headers headers, ResponseHandler<?> response)
	{
		return put(path, params, null, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E put(String path, RequestBody postData, ResponseHandler<?> response)
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
	public E put(String path, RequestBody postData, Headers headers, ResponseHandler<?> response)
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
	public E put(String path, List<NameValuePair> params, RequestBody postData, ResponseHandler<?> response)
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
	public E put(String path, List<NameValuePair> params, RequestBody postData, Headers headers, ResponseHandler<?> response)
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
	public E patch(ResponseHandler<?> response)
	{
		return patch("", null, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUr
	 * @param path The path extended from the baseUri
	 */
	public E patch(String path)
	{
		return patch(path, null, null, null, new ByteArrayResponseHandler());
	}

	/**
	 * Performs a PATCH request on the baseUr
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public E patch(String path, ResponseHandler<?> response)
	{
		return patch(path, null, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public E patch(List<NameValuePair> params, ResponseHandler<?> response)
	{
		return patch("", params, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E patch(List<NameValuePair> params, Headers headers, ResponseHandler<?> response)
	{
		return patch("", params, null, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E patch(RequestBody postData, ResponseHandler<?> response)
	{
		return patch("", null, postData, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public E patch(RequestBody postData, Headers headers, ResponseHandler<?> response)
	{
		return patch("", null, postData, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E patch(List<NameValuePair> params, RequestBody postData, ResponseHandler<?> response)
	{
		return patch("", params, postData, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public E patch(String path, List<NameValuePair> params, ResponseHandler<?> response)
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
	public E patch(String path, List<NameValuePair> params, Headers headers, ResponseHandler<?> response)
	{
		return patch(path, params, null, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public E patch(String path, RequestBody postData, ResponseHandler<?> response)
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
	public E patch(String path, RequestBody postData, Headers headers, ResponseHandler<?> response)
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
	public E patch(String path, List<NameValuePair> params, RequestBody postData, ResponseHandler<?> response)
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
	public E patch(String path, List<NameValuePair> params, RequestBody postData, Headers headers, ResponseHandler<?> response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		return executeTask(RequestMode.PATCH, requestUri, headers, postData, response);
	}

	private E executeTask(RequestMode mode, Uri uri, Headers headers, RequestBody sendData, ResponseHandler<?> requestProcessor)
	{
		executor = new ClientExecutorTask<E>(mode, uri, headers, sendData, requestProcessor, true, allowAllSsl, requestTimeout);
		executor.preExecute();
		E response = executor.executeTask();
		executor.postExecute();

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
}
