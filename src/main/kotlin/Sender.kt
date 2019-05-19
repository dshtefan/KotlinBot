import java.lang.Exception
import java.net.URL
import java.util.concurrent.PriorityBlockingQueue

class Sender(private val storage: PriorityBlockingQueue<Notification>,
             private val token: String): Thread(){

    private val url = "https://api.telegram.org/bot"
    override fun run() {
        while(true){
            try {
                if (storage.peek().date * 1000 + 999 <= System.currentTimeMillis()) {
                    val el = storage.take()
                    sendResponse(el)
                }
            } catch(e: Exception){}
        }
    }
    private fun sendResponse(el: Notification){
        URL(url + token + "/sendmessage?text=${el.text} &" +
                "chat_id=${el.chat}").readText()
    }
}