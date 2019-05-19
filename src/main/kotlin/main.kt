import java.util.concurrent.PriorityBlockingQueue

fun main() {
    val storage: PriorityBlockingQueue<LongArray> = PriorityBlockingQueue<LongArray>(11) { x, y -> x[0].toInt() - y[0].toInt() }
    //val fullurl = "https://api.telegram.org/bot817626634:AAFMUYI89iv06Ql3S0NsQ4-tzmZWg2yb-cM/getupdates"
    val token = "817626634:AAFMUYI89iv06Ql3S0NsQ4-tzmZWg2yb-cM"
    val url = "https://api.telegram.org/bot"
    val listener = Listener(storage, url, token)
    val sender = Sender(storage, url, token)
    listener.start()
    sender.start()
}
