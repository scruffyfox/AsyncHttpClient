package net.callumtaylor.asynchttp.obj;

import net.callumtaylor.asynchttp.AsyncHttpClient.RequestMode;

public class ConnectionInfo
{
	public String connectionUrl = "";
	public RequestMode requestMethod = RequestMode.GET;
	public long connectionTime = 0L;
	public long connectionLength = 0L;

	public long responseLength = 0L;
	public int responseCode = 0;
	public long responseTime = 0L;

	@Override public String toString()
	{
		return "ConnectionInfo " + hashCode() + "\n[\n    connectionUrl=" + connectionUrl + ", \n    requestMethod=" + requestMethod.getCanonical() + ", \n    connectionTime=" + connectionTime + ", \n    connectionLength=" + connectionLength + ", \n    responseLength=" + responseLength + ", \n    responseCode=" + responseCode + ", \n    responseTime=" + responseTime + "\n]";
	}
}