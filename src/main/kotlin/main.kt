import java.util.concurrent.PriorityBlockingQueue

fun main() {
    val storage = PriorityBlockingQueue<Notification>(11) { x, y -> x.date.toInt() - y.date.toInt() }
    val token = "817626634:AAFMUYI89iv06Ql3S0NsQ4-tzmZWg2yb-cM"
    Listener(storage, token).start()
    Sender(storage, token).start()
}
