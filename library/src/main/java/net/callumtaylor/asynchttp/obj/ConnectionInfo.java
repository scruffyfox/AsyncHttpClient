package net.callumtaylor.asynchttp.obj;

import okhttp3.Headers;

public class ConnectionInfo
{
	public String connectionUrl = "";
	public RequestMode requestMethod = RequestMode.GET;
	public long connectionTime = 0L;
	public long connectionLength = 0L;

	public long responseLength = 0L;
	public int responseCode = 0;
	public long responseTime = 0L;

	public Headers requestHeaders;
	public Headers responseHeaders;

	@Override public String toString()
	{
		return "ConnectionInfo [connectionUrl=" + connectionUrl + ", requestMethod=" + requestMethod + ", requestHeaders=" + requestHeaders + ", connectionTime=" + connectionTime + ", connectionLength=" + connectionLength + ", responseLength=" + responseLength + ", responseCode=" + responseCode + ", responseTime=" + responseTime + ", responseHeaders=" + responseHeaders + "]";
	}
}
