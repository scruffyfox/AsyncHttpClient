package net.callumtaylor.asynchttp.obj

/**
 * // TODO: Add class description
 */
data class Response<T>(
	var body: T? = null,
	var code: Int = 0,
	var time: Long = 0,
	var length: Long = 0,
	var headers: ArrayList<Pair<String, String>> = arrayListOf(),
	var request: Request
)
