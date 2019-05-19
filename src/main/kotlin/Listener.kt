import org.json.JSONObject
import java.net.URL
import java.util.Scanner
import java.util.concurrent.PriorityBlockingQueue
import java.util.regex.MatchResult

class Listener(private val storage: PriorityBlockingQueue<LongArray>,
               private val url: String,
               private val token: String): Thread(){
    private var lastUpdateId = 0L

    override fun run(){
        while(true){
            val response = getUpdates()
            if(response != null){
                saveNewMessages(response)
            }
        }
    }
    fun getUpdates():JSONObject?{
        try {
            val res =  JSONObject(URL(url + token + "/getupdates?offset=$lastUpdateId").readText())
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
            if(lastUpdateId >= update_id)
                continue
            lastUpdateId = update_id
            val message = arr.getJSONObject(i).getJSONObject("message")
            val sec = getSecondsFromString(message.getString("text"))
            if(sec == null){
                continue
            }
            val chat_id = message.getJSONObject("chat").getLong("id")
            val date = message.getLong("date")
            storage.put(longArrayOf(date + sec, chat_id))
        }
    }
    fun getSecondsFromString(text: String) :Long?{
        val patterns: Array<String> = arrayOf(
            "^(\\d+)s\$",
            "^(\\d+)m\$",
            "^(\\d+)m(\\d+)s\$",
            "^(\\d+)h\$",
            "^(\\d+)h(\\d+)s\$",
            "^(\\d+)h(\\d+)m\$",
            "^(\\d+)h(\\d+)m(\\d+)s\$"
        )
        var matchResult: MatchResult? = null
        var i = 1
        for(pattern in patterns){
            val s = Scanner(text)
            s.findInLine(pattern)
            try {
                matchResult = s.match()
                s.close()
                break
            } catch (e: IllegalStateException){
                s.close()
                i++
            }
        }
        if(matchResult == null)
            return null
        val smh = arrayOf(i%2, i%4/2, i/4)
        i = matchResult.groupCount()
        var sec = 0L
        for(j in 0..2 ) {
            if (smh[j] == 1) {
                sec += matchResult.group(i).toLong() * Math.pow(60.0, j.toDouble()).toLong()
                i--
            }
        }
        return sec
    }
}