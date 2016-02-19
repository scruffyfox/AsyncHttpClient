package net.callumtaylor.asynchttp.obj;

import org.apache.http.Header;

import java.util.List;
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

	public List<Header> requestHeaders;
	public Map<String, String> responseHeaders;

	@Override public String toString()
	{
		return "ConnectionInfo [connectionUrl=" + connectionUrl + ", requestMethod=" + requestMethod + ", requestHeaders=" + requestHeaders + ", connectionTime=" + connectionTime + ", connectionLength=" + connectionLength + ", responseLength=" + responseLength + ", responseCode=" + responseCode + ", responseTime=" + responseTime + ", responseHeaders=" + responseHeaders + "]";
	}
}