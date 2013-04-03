package net.callumtaylor.asynchttp.obj;

import java.util.ArrayList;
import java.util.List;

import net.callumtaylor.asynchttp.AsyncHttpClient.RequestMode;

import org.apache.http.Header;

public class ConnectionInfo
{
	public String connectionUrl = "";
	public RequestMode requestMethod = RequestMode.GET;
	public long connectionTime = 0L;
	public long connectionLength = 0L;

	public long responseLength = 0L;
	public int responseCode = 0;
	public long responseTime = 0L;

	public List<Header> responseHeaders = new ArrayList<Header>();

	@Override public String toString()
	{
		return "ConnectionInfo [connectionUrl=" + connectionUrl + ", requestMethod=" + requestMethod + ", connectionTime=" + connectionTime + ", connectionLength=" + connectionLength + ", responseLength=" + responseLength + ", responseCode=" + responseCode + ", responseTime=" + responseTime + ", responseHeaders=" + responseHeaders + "]";
	}
}