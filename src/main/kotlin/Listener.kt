import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import java.util.Date
import java.util.Scanner
import java.util.concurrent.PriorityBlockingQueue
import java.util.regex.MatchResult

class Listener(private val storage: PriorityBlockingQueue<Notification>,
               private val token: String): Thread(){

    private var lastUpdateId = 0L
    private val url = "https://api.telegram.org/bot"
    override fun run(){
        while(true){
            try {
                val response = getUpdates()
                if (response != null) {
                    saveNewMessages(response)
                }
            } catch (e: Exception){}
        }
    }
    private fun getUpdates():JSONObject?{
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
    @Throws(Exception::class)
    private fun saveNewMessages(json: JSONObject){
        val arr = json.getJSONArray("result")
        for(i in 0 until arr.length()){
            val update_id = arr.getJSONObject(i).getLong("update_id")
            if(lastUpdateId >= update_id)
                continue
            lastUpdateId = update_id
            val message = arr.getJSONObject(i).getJSONObject("message")
            val sec = getSecondsFromString(message.getString("text").trim().toLowerCase())
            val chat = message.getJSONObject("chat").getLong("id")
            var date = message.getLong("date")
            val text: String
            if(message.getString("text") == "/start") {
                text = URLEncoder.encode("Чтобы я вам написал через определенное время, " +
                                            "используйте следующее форматирование:\n" +
                                            "_s\n" +
                                            "_m\n" +
                                            "_m_s\n" +
                                            "_h\n" +
                                            "_h_s\n" +
                                            "_h_m\n" +
                                            "_h_m_s\n" +
                                            "где h,m,s - часы, минуты и секунды, " +
                                            "а \"_\" - количество", "UTF-8")
            } else{
                if(sec == null){
                    text = URLEncoder.encode("Неверный формат", "UTF-8")
                } else{
                    text = URLEncoder.encode("Вы просили написать ${Date((date + sec) * 1000)}", "UTF-8")
                    storage.put(Notification(date, chat, "Ок, напишу через ${sec}с"))
                    date += sec
                }
            }
            storage.put(Notification(date, chat, text))
        }
    }
    private fun getSecondsFromString(text: String) :Long?{
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