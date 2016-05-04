package net.callumtaylor.asynchttp.obj;

import okhttp3.Headers;

/**
 * Data wrapper for details about the request that was made
 */
public class ConnectionInfo
{
	/**
	 * The URL of the request that was made
	 */
	public String connectionUrl = "";

	/**
	 * The request method
	 */
	public RequestMode requestMethod = RequestMode.GET;

	/**
	 * The time in milliseconds when the connection was made
	 */
	public long connectionTime = 0L;

	/**
	 * The size of the connection in bytes
	 */
	public long connectionLength = 0L;

	/**
	 * The size of the response in bytes
	 */
	public long responseLength = 0L;

	/**
	 * The response code from the server
	 */
	public int responseCode = 0;

	/**
	 * The time the server responded in milliseconds
	 */
	public long responseTime = 0L;

	/**
	 * The request headers that were sent
	 */
	public Headers requestHeaders;

	/**
	 * The headers that were received from the server
	 */
	public Headers responseHeaders;

	@Override public String toString()
	{
		return "ConnectionInfo [connectionUrl=" + connectionUrl + ", requestMethod=" + requestMethod + ", requestHeaders=" + requestHeaders + ", connectionTime=" + connectionTime + ", connectionLength=" + connectionLength + ", responseLength=" + responseLength + ", responseCode=" + responseCode + ", responseTime=" + responseTime + ", responseHeaders=" + responseHeaders + "]";
	}
}
