package net.callumtaylor.asynchttp.obj;

public class Packet
{
	public long length;
	public long total;
	public boolean isDownload;

	public Packet(long length, long total, boolean isDownload)
	{
		this.length = length;
		this.total = total;
		this.isDownload = isDownload;
	}
}