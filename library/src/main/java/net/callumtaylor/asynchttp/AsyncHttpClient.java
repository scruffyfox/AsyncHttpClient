package net.callumtaylor.asynchttp;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.text.TextUtils;

import net.callumtaylor.asynchttp.obj.ClientTaskImpl;
import net.callumtaylor.asynchttp.obj.ConnectionInfo;
import net.callumtaylor.asynchttp.obj.NameValuePair;
import net.callumtaylor.asynchttp.obj.Packet;
import net.callumtaylor.asynchttp.obj.RequestMode;
import net.callumtaylor.asynchttp.obj.RequestUtil;
import net.callumtaylor.asynchttp.response.ResponseHandler;

import java.util.List;

import okhttp3.Headers;
import okhttp3.RequestBody;

/**
 * @mainpage
 *
 * The client class used for initiating HTTP requests using an AsyncTask. It
 * follows a RESTful paradigm for the connections with the 4 possible methods,
 * GET, POST, PUT and DELETE.
 *
 * <b>Note:</b> Because AsyncHttpClient uses
 * AsyncTask, only one instance can be created at a time. If one client makes 2
 * requests, the first request is canceled for the new request. You can either
 * wait for the first to finish before making the second, or you can create two
 * seperate instances.
 *
 * <b>Depends on</b>
 * <ul>
 * <li>{@link net.callumtaylor.asynchttp.response.ResponseHandler}</li>
 * <li>{@link RequestBody}</li>
 * <li>{@link NameValuePair}</li>
 * <li>{@link ConnectionInfo}</li>
 * <li>{@link Packet}</li>
 * <li>{@link RequestMode}</li>
 * <li>{@link RequestUtil}</li>
 * </ul>
 * <h1>Example GET</h1>
 *
 * <pre>
 * AsyncHttpClient client = new AsyncHttpClient(&quot;http://example.com&quot;);
 * List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
 * params.add(new BasicNameValuePair(&quot;key&quot;, &quot;value&quot;));
 *
 * List&lt;Header&gt; headers = new ArrayList&lt;Header&gt;();
 * headers.add(new BasicHeader(&quot;1&quot;, &quot;2&quot;));
 *
 * client.get(&quot;api/v1/&quot;, params, headers, new JsonResponseHandler()
 * {
 * 	&#064;Override public void onSuccess()
 * 	{
 * 		JsonElement result = getContent();
 * 	}
 * });
 * </pre>
 *
 * <h1>Example DELETE</h1>
 *
 * <pre>
 * AsyncHttpClient client = new AsyncHttpClient(&quot;http://example.com&quot;);
 * List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
 * params.add(new BasicNameValuePair(&quot;key&quot;, &quot;value&quot;));
 *
 * List&lt;Header&gt; headers = new ArrayList&lt;Header&gt;();
 * headers.add(new BasicHeader(&quot;1&quot;, &quot;2&quot;));
 *
 * client.delete(&quot;api/v1/&quot;, params, headers, new JsonResponseHandler()
 * {
 * 	&#064;Override public void onSuccess()
 * 	{
 * 		JsonElement result = getContent();
 * 	}
 * });
 * </pre>
 *
 * <h1>Example POST - Single Entity</h1>
 *
 * <pre>
 * AsyncHttpClient client = new AsyncHttpClient(&quot;http://example.com&quot;);
 * List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
 * params.add(new BasicNameValuePair(&quot;key&quot;, &quot;value&quot;));
 *
 * List&lt;Header&gt; headers = new ArrayList&lt;Header&gt;();
 * headers.add(new BasicHeader(&quot;1&quot;, &quot;2&quot;));
 *
 * JsonEntity data = new JsonEntity(&quot;{\&quot;key\&quot;:\&quot;value\&quot;}&quot;);
 * GzippedEntity entity = new GzippedEntity(data);
 *
 * client.post(&quot;api/v1/&quot;, params, entity, headers, new JsonResponseHandler()
 * {
 * 	&#064;Override public void onSuccess()
 * 	{
 * 		JsonElement result = getContent();
 * 	}
 * });
 * </pre>
 *
 * <h1>Example POST - Multiple Entity + file</h1>
 *
 * <pre>
 * AsyncHttpClient client = new AsyncHttpClient(&quot;http://example.com&quot;);
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
 * client.post(&quot;api/v1/&quot;, params, entity, headers, new JsonResponseHandler()
 * {
 * 	&#064;Override public void onSuccess()
 * 	{
 * 		JsonElement result = getContent();
 * 	}
 * });
 * </pre>
 *
 * <h1>Example PUT</h1>
 *
 * <pre>
 * AsyncHttpClient client = new AsyncHttpClient(&quot;http://example.com&quot;);
 * List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
 * params.add(new BasicNameValuePair(&quot;key&quot;, &quot;value&quot;));
 *
 * List&lt;Header&gt; headers = new ArrayList&lt;Header&gt;();
 * headers.add(new BasicHeader(&quot;1&quot;, &quot;2&quot;));
 *
 * JsonEntity data = new JsonEntity(&quot;{\&quot;key\&quot;:\&quot;value\&quot;}&quot;);
 * GzippedEntity entity = new GzippedEntity(data);
 *
 * client.post(&quot;api/v1/&quot;, params, entity, headers, new JsonResponseHandler()
 * {
 * 	&#064;Override public void onSuccess()
 * 	{
 * 		JsonElement result = getContent();
 * 	}
 * });
 * </pre>
 *
 * Because of the nature of REST, GET and DELETE requests behave in the same
 * way, POST and PUT requests also behave in the same way.
 *
 * @author Callum Taylor &lt;callumtaylor.net&gt; &#064;scruffyfox
 */
public class AsyncHttpClient
{
	/**
	 * User agent to send with every request. Defaults to {@link RequestUtil#getDefaultUserAgent()}
	 */
	public static String userAgent = RequestUtil.getDefaultUserAgent();

	private AsyncClientExecutorTask executorTask;
	private Uri requestUri;
	private long requestTimeout = 0L;
	private boolean allowAllSsl = false;
	private boolean allowRedirect = true;

	/**
	 * Creates a new client using a base Url without a timeout
	 * @param baseUrl The base connection url
	 */
	public AsyncHttpClient(String baseUrl)
	{
		this(baseUrl, 0);
	}

	/**
	 * Creates a new client using a base Uri without a timeout
	 * @param baseUri The base connection uri
	 */
	public AsyncHttpClient(Uri baseUri)
	{
		this(baseUri, 0);
	}

	/**
	 * Creates a new client using a base Url with a timeout in MS
	 * @param baseUrl The base connection url
	 * @param timeout The timeout in MS
	 */
	public AsyncHttpClient(String baseUrl, long timeout)
	{
		this(Uri.parse(baseUrl), timeout);
	}

	/**
	 * Creates a new client using a base Uri with a timeout in MS
	 * @param baseUri The base connection uri
	 * @param timeout The timeout in MS
	 */
	public AsyncHttpClient(Uri baseUri, long timeout)
	{
		requestUri = baseUri;
		requestTimeout = timeout;
	}

	/**
	 * Cancels a request if it's running
	 */
	public void cancel()
	{
		if (executorTask != null && executorTask.getStatus() == Status.RUNNING)
		{
			executorTask.cancel(true);
		}
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param response The response handler for the request
	 */
	public void get(ResponseHandler response)
	{
		get("", null, null, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void get(String path, ResponseHandler response)
	{
		get(path, null, null, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void get(Headers headers, ResponseHandler response)
	{
		get("", null, headers, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void get(List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		get("", params, headers, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void get(String path, List<NameValuePair> params, ResponseHandler response)
	{
		get(path, params, null, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void get(String path, List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		executeTask(RequestMode.GET, requestUri, headers, null, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param response The response handler for the request
	 */
	public void options(ResponseHandler response)
	{
		options("", null, null, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void options(String path, ResponseHandler response)
	{
		options(path, null, null, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void options(Headers headers, ResponseHandler response)
	{
		options("", null, headers, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void options(List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		options("", params, headers, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void options(String path, List<NameValuePair> params, ResponseHandler response)
	{
		options(path, params, null, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void options(String path, List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		executeTask(RequestMode.OPTIONS, requestUri, headers, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param response The response handler for the request
	 */
	public void head(ResponseHandler response)
	{
		head("", null, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void head(String path, ResponseHandler response)
	{
		head(path, null, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void head(Headers headers, ResponseHandler response)
	{
		head("", null, headers, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void head(List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		head("", params, headers, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void head(String path, List<NameValuePair> params, ResponseHandler response)
	{
		head(path, params, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void head(String path, List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		executeTask(RequestMode.HEAD, requestUri, headers, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param response The response handler for the request
	 */
	public void delete(ResponseHandler response)
	{
		delete("", null, null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void delete(String path, ResponseHandler response)
	{
		delete(path, null, null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void delete(List<NameValuePair> params, ResponseHandler response)
	{
		delete("", params, null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void delete(List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		delete("", params, null, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void delete(RequestBody postData, ResponseHandler response)
	{
		delete("", null, postData, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void delete(RequestBody postData, Headers headers, ResponseHandler response)
	{
		delete("", null, postData, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void delete(List<NameValuePair> params, RequestBody postData, ResponseHandler response)
	{
		delete("", params, postData, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void delete(String path, List<NameValuePair> params, ResponseHandler response)
	{
		delete(path, params, null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void delete(String path, List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		delete(path, params, null, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void delete(String path, RequestBody postData, ResponseHandler response)
	{
		delete(path, null, postData, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void delete(String path, RequestBody postData, Headers headers, ResponseHandler response)
	{
		delete(path, null, postData, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void delete(String path, List<NameValuePair> params, RequestBody postData, ResponseHandler response)
	{
		delete(path, params, postData, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void delete(String path, List<NameValuePair> params, RequestBody postData, Headers headers, ResponseHandler response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		executeTask(RequestMode.DELETE, requestUri, headers, postData, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param response The response handler for the request
	 */
	public void post(ResponseHandler response)
	{
		post("", null, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUr
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void post(String path, ResponseHandler response)
	{
		post(path, null, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void post(List<NameValuePair> params, ResponseHandler response)
	{
		post("", params, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void post(List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		post("", params, null, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void post(RequestBody postData, ResponseHandler response)
	{
		post("", null, postData, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void post(RequestBody postData, Headers headers, ResponseHandler response)
	{
		post("", null, postData, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void post(List<NameValuePair> params, RequestBody postData, ResponseHandler response)
	{
		post("", params, postData, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void post(String path, List<NameValuePair> params, ResponseHandler response)
	{
		post(path, params, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void post(String path, List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		post(path, params, null, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void post(String path, RequestBody postData, ResponseHandler response)
	{
		post(path, null, postData, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void post(String path, RequestBody postData, Headers headers, ResponseHandler response)
	{
		post(path, null, postData, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void post(String path, List<NameValuePair> params, RequestBody postData, ResponseHandler response)
	{
		post(path, params, postData, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void post(String path, List<NameValuePair> params, RequestBody postData, Headers headers, ResponseHandler response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		executeTask(RequestMode.POST, requestUri, headers, postData, response);
	}

	/**
	 * Performs a PUT request on the baseUr
	 * @param response The response handler for the request
	 */
	public void put(ResponseHandler response)
	{
		put("", null, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUr
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void put(String path, ResponseHandler response)
	{
		put(path, null, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void put(List<NameValuePair> params, ResponseHandler response)
	{
		put("", params, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void put(List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		put("", params, null, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void put(RequestBody postData, ResponseHandler response)
	{
		put("", null, postData, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void put(RequestBody postData, Headers headers, ResponseHandler response)
	{
		put("", null, postData, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void put(List<NameValuePair> params, RequestBody postData, ResponseHandler response)
	{
		put("", params, postData, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void put(String path, List<NameValuePair> params, ResponseHandler response)
	{
		put(path, params, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void put(String path, List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		put(path, params, null, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void put(String path, RequestBody postData, ResponseHandler response)
	{
		put(path, null, postData, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void put(String path, RequestBody postData, Headers headers, ResponseHandler response)
	{
		put(path, null, postData, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void put(String path, List<NameValuePair> params, RequestBody postData, ResponseHandler response)
	{
		put(path, params, postData, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void put(String path, List<NameValuePair> params, RequestBody postData, Headers headers, ResponseHandler response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		executeTask(RequestMode.PUT, requestUri, headers, postData, response);
	}

	/**
	 * Performs a PATCH request on the baseUr
	 * @param response The response handler for the request
	 */
	public void patch(ResponseHandler response)
	{
		patch("", null, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUr
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void patch(String path, ResponseHandler response)
	{
		patch(path, null, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void patch(List<NameValuePair> params, ResponseHandler response)
	{
		patch("", params, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void patch(List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		patch("", params, null, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void patch(RequestBody postData, ResponseHandler response)
	{
		patch("", null, postData, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void patch(RequestBody postData, Headers headers, ResponseHandler response)
	{
		patch("", null, postData, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void patch(List<NameValuePair> params, RequestBody postData, ResponseHandler response)
	{
		patch("", params, postData, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void patch(String path, List<NameValuePair> params, ResponseHandler response)
	{
		patch(path, params, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void patch(String path, List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		patch(path, params, null, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void patch(String path, RequestBody postData, ResponseHandler response)
	{
		patch(path, null, postData, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void patch(String path, RequestBody postData, Headers headers, ResponseHandler response)
	{
		patch(path, null, postData, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void patch(String path, List<NameValuePair> params, RequestBody postData, ResponseHandler response)
	{
		patch(path, params, postData, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void patch(String path, List<NameValuePair> params, RequestBody postData, Headers headers, ResponseHandler response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		executeTask(RequestMode.PATCH, requestUri, headers, postData, response);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void executeTask(RequestMode mode, Uri uri, Headers headers, RequestBody sendData, ResponseHandler response)
	{
		if (executorTask != null || (executorTask != null && (executorTask.getStatus() == Status.RUNNING || executorTask.getStatus() == Status.PENDING)))
		{
			executorTask.cancel(true);
			executorTask = null;
		}

		executorTask = new AsyncClientExecutorTask(mode, uri, headers, sendData, response, allowRedirect);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			executorTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		else
		{
			executorTask.execute();
		}
	}

	protected static class AsyncClientExecutorTask extends AsyncTask<Void, Packet, Void> implements ClientTaskImpl
	{
		private static final int BUFFER_SIZE = 1 * 1024 * 8;

		private ClientExecutorTask clientTask;

		public AsyncClientExecutorTask(RequestMode mode, Uri request, Headers headers, RequestBody postData, ResponseHandler response, boolean allowRedirect)
		{
			clientTask = new ClientExecutorTask(mode, request, headers, postData, response, allowRedirect);
		}

		@Override public void cancel()
		{
			cancel(true);
		}

		@Override public void onPreExecute()
		{
			clientTask.onPreExecute();
		}

		@Override public Void doInBackground()
		{
			clientTask.doInBackground();
			return null;
		}

		@Override public void onPostExecute()
		{
			clientTask.onPostExecute();
		}

		@Override public void onProgressUpdate(Packet... values)
		{
			if (clientTask.response != null && !isCancelled())
			{
				if (values[0].isDownload)
				{
					clientTask.response.onPublishedDownloadProgressUI(values[0].length, values[0].total);
				}
				else
				{
					clientTask.response.onPublishedUploadProgressUI(values[0].length, values[0].total);
				}
			}
		}

		@Override protected Void doInBackground(Void... params)
		{
			return doInBackground();
		}

		@Override protected void onPostExecute(Void result)
		{
			onPostExecute();
		}

		public void postPublishProgress(Packet... values)
		{
			publishProgress(values);
		}
	}

	public void setAllowAllSsl(boolean allow)
	{
		this.allowAllSsl = allow;
	}

	public void setAllowRedirect(boolean allow)
	{
		this.allowRedirect = allow;
	}
}
