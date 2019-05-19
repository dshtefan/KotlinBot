import org.json.JSONObject
import java.net.URL
import java.util.*
import java.util.concurrent.PriorityBlockingQueue
import java.util.regex.MatchResult

class Listener( val storage: PriorityBlockingQueue<LongArray>,
                val url: String,
                val token: String): Thread(){
    private var last_update_id = 0L;

    override fun run() {
        while(true){
            val response = getUpdates()
            if(response != null){
                saveNewMessages(response)
            }
        }
    }
    fun getUpdates():JSONObject?{
        try {
            val res =  JSONObject(URL(url + token + "/getupdates").readText())
            if(res.getBoolean("ok"))
                return res
            else
                return null
        } catch (e: Exception){
            return null
        }
    }
    fun saveNewMessages(json: JSONObject){
        val arr = json.getJSONArray("result")
        for(i in 0 until arr.length()){
            val update_id = arr.getJSONObject(i).getLong("update_id")
            if(last_update_id >= update_id)
                continue
            last_update_id = update_id
            val message = arr.getJSONObject(i).getJSONObject("message")
            val sec = getSecondsFromString(message.getString("text"))
            if(sec == null){
                continue
            }
            val chat_id = message.getJSONObject("chat").getLong("id")
            val date = message.getLong("date")
            storage.add(longArrayOf(date + sec, chat_id))
        }
    }
    fun getSecondsFromString(text: String) :Long? {
        val patterns: Array<String> = arrayOf(
            "^(\\d+)s\$",
            "^(\\d+)m\$",
            "^(\\d+)m(\\d+)s\$",
            "^(\\d+)h\$",
            "^(\\d+)h(\\d+)s\$",
            "^(\\d+)h(\\d+)m\$",
            "^(\\d+)h(\\d+)m(\\d+)s\$"
        )
        var match_result: MatchResult? = null
        var i = 1
        for(pattern in patterns){
            val s = Scanner(text)
            s.findInLine(pattern)
            try {
                match_result = s.match()
                break
            } catch (e: IllegalStateException){
                println("--$pattern")
            }
            s.close()
            i++
        }
        if(match_result == null)
            return null
        val smh = arrayOf(i%2, i%4/2, i/4)
        i = match_result.groupCount()
        var sec = 0L
        for(j in 0..2 ) {
            if (smh[j] == 1) {
                sec += match_result.group(i).toLong() * Math.pow(60.0, j.toDouble()).toLong()
                i--
            }
        }
        return sec
    }
}