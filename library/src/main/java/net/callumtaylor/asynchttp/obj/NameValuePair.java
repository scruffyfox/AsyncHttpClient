package net.callumtaylor.asynchttp.obj;

/**
 * // TODO: Add class description
 *
 * @author Callum Taylor
 * @documentation // TODO Reference flow doc
 * @project AsyncHttpClient
 */
public class NameValuePair
{
	private String name, value;

	public NameValuePair(String name, String value)
	{
		this.name = name;
		this.value = value;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}
}
