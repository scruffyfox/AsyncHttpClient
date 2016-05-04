package net.callumtaylor.asynchttp.obj;

/**
 * Simple data structure used to describe a connection packet transfer
 */
public class Packet
{
	/**
	 * The size of the data that was transferred in bytes
	 */
	public long length;

	/**
	 * The total size to be transferred in bytes
	 */
	public long total;

	/**
	 * If the request was via a downstream, if false, the request was via an upstream
	 */
	public boolean isDownload;

	public Packet(long length, long total, boolean isDownload)
	{
		this.length = length;
		this.total = total;
		this.isDownload = isDownload;
	}
}
