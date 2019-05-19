import java.lang.Exception
import java.net.URL
import java.net.URLEncoder
import java.util.Date
import java.util.concurrent.PriorityBlockingQueue

class Sender(private val storage: PriorityBlockingQueue<LongArray>,
             private val url: String,
             private val token: String): Thread(){

    override fun run() {
        while(true){
            try {
                if (storage.peek()[0] * 1000 + 999 <= System.currentTimeMillis()) {
                    val el = storage.take()
                    sendRequest(el)
                }
            } catch(e: Exception){}
        }
    }
    fun sendRequest(el: LongArray){
        val text = URLEncoder.encode("Вы просили написать", "UTF-8")
        URL(url + token + "/sendmessage?text=$text " +
                "${Date(el[0]*1000).toString()}&" +
                "chat_id=${el[1]}").readText()
    }
}