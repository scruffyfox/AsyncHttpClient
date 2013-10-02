package net.callumtaylor.asynchttp.obj;

import java.util.Map;

public class ConnectionInfo
{
	public String connectionUrl = "";
	public RequestMode requestMethod = RequestMode.GET;
	public long connectionTime = 0L;
	public long connectionLength = 0L;

	public long responseLength = 0L;
	public int responseCode = 0;
	public long responseTime = 0L;

	public Map<String, String> responseHeaders;

	@Override public String toString()
	{
		return "ConnectionInfo [connectionUrl=" + connectionUrl + ", requestMethod=" + requestMethod + ", connectionTime=" + connectionTime + ", connectionLength=" + connectionLength + ", responseLength=" + responseLength + ", responseCode=" + responseCode + ", responseTime=" + responseTime + ", responseHeaders=" + responseHeaders + "]";
	}
}