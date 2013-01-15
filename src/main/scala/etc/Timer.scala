package etc

/**
 * Created with IntelliJ IDEA.
 * User: arne
 * Date: 15.01.13
 * Time: 11:41
 * To change this template use File | Settings | File Templates.
 */
object Timer {

  case class TimerTreeNode(name:String, var time:Long = 0, var subNodes:List[TimerTreeNode] = Nil)
  var timerCallStack:List[TimerTreeNode] = Nil

  def time(name:String)(program: => Unit) {

    val newNode = TimerTreeNode(name)
    if (!timerCallStack.isEmpty){
      timerCallStack.head.subNodes ::= newNode
    }
    timerCallStack ::= newNode
    val start = System.nanoTime()
    program
    val end = System.nanoTime()
    assert(timerCallStack.head == newNode)
    newNode.time = end-start

    if (timerCallStack.tail.isEmpty){
      println(timerCallStack.head)
    }

    timerCallStack = timerCallStack.tail
  }
}

