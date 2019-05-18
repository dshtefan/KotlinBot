import java.net.URL
import org.json.JSONObject
import java.util.Date

fun main() {
    val full = "https://api.telegram.org/bot817626634:AAFMUYI89iv06Ql3S0NsQ4-tzmZWg2yb-cM/getupdates"
    val token = "817626634:AAFMUYI89iv06Ql3S0NsQ4-tzmZWg2yb-cM"
    val url = "https://api.telegram.org/bot"
    val response = URL(url + token + "/getupdates").readText()
    val resJSON = parse(response)
    if (resJSON == null || !resJSON.getBoolean("ok")){
        println("No response received")
        return
    }
    val arr = resJSON.getJSONArray("result")
    for(i in 0 until arr.length()){
        val message = arr.getJSONObject(i).getJSONObject("message")
        val message_id = message.getInt("message_id")
        val message_text = message.getString("text")
        val chat_id = message.getJSONObject("chat").getLong("id")
        val date = message.getLong("date")
        val da = Date(date)
        println("TIME:" + da.toString() + " ID:$message_id TEXT:$message_text CHAT:$chat_id")
    }
}

fun parse(json: String): JSONObject? = JSONObject(json)
