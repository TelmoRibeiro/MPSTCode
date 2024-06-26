package mpst.syntax

import mpst.syntax.Type.{Agent,Message,Sort,Variable}

/* IDEA:
    the basics...

  @ telmo -
    should Interaction(_,_,_,_) be considered an action as well?
    toString is hiding sort
*/

enum Protocol:
  override def toString:String =
    this match
      case Interaction(agentA,agentB,message,_) => s"$agentA>$agentB:$message"
      case Send   (agentA,agentB,message,_) => s"$agentA$agentB!$message"
      case Receive(agentA,agentB,message,_) => s"$agentA$agentB?$message"
      case RecursionCall(variable) => s"$variable"
      case Skip => s"skip"
      case Sequence(protocolA,protocolB) => s"$protocolA ; $protocolB"
      case Parallel(protocolA,protocolB) => s"($protocolA || $protocolB)"
      case Choice  (protocolA,protocolB) => s"($protocolA + $protocolB)"
      case RecursionFixedPoint(variable,protocolB) => s"def $variable in ($protocolB)"
  end toString

  case Interaction(agentA:Agent,agentB:Agent,message:Message,sort:Sort)
  case Send       (agentA:Agent,agentB:Agent,message:Message,sort:Sort)
  case Receive    (agentA:Agent,agentB:Agent,message:Message,sort:Sort)
  case RecursionCall(variable:Variable)
  case Skip
  case Sequence(protocolA:Protocol,protocolB:Protocol)
  case Parallel(protocolA:Protocol,protocolB:Protocol)
  case Choice  (protocolA:Protocol,protocolB:Protocol)
  case RecursionFixedPoint(variable:Variable,protocolB:Protocol)
end Protocol

object Protocol:
  def isGlobal(protocol:Protocol):Boolean =
    protocol match
      case Interaction(_,_,_,_) => true
      case Send   (_,_,_,_) => false
      case Receive(_,_,_,_) => false
      case RecursionCall(_) => true
      case Skip => true
      case Sequence(protocolA,protocolB) => isGlobal(protocolA) && isGlobal(protocolB)
      case Parallel(protocolA,protocolB) => isGlobal(protocolA) && isGlobal(protocolB)
      case Choice  (protocolA,protocolB) => isGlobal(protocolA) && isGlobal(protocolB)
      case RecursionFixedPoint(_,protocolB) => isGlobal(protocolB)
  end isGlobal

  def isLocal(protocol:Protocol):Boolean =
    protocol match
      case Interaction(_,_,_,_) => false
      case Send   (_,_,_,_) => true
      case Receive(_,_,_,_) => true
      case RecursionCall(_) => true
      case Skip => true
      case Sequence(protocolA,protocolB) => isLocal(protocolA) && isLocal(protocolB)
      case Parallel(protocolA,protocolB) => isLocal(protocolA) && isLocal(protocolB)
      case Choice  (protocolA,protocolB) => isLocal(protocolA) && isLocal(protocolB)
      case RecursionFixedPoint(_,protocolB) => isLocal(protocolB)
  end isLocal

  def isAction(protocol:Protocol):Boolean =
    protocol match
      case Send   (_,_,_,_) => true
      case Receive(_,_,_,_) => true
      case _ => false
  end isAction

  def getAgents(protocol:Protocol):Set[Agent] =
    protocol match
      case Interaction(agentA,agentB,_,_) => (Set() + agentA) + agentB
      case Send   (agentA,agentB,_,_) => (Set() + agentA) + agentB
      case Receive(agentA,agentB,_,_) => (Set() + agentA) + agentB
      case RecursionCall(_) => Set()
      case Skip => Set()
      case Sequence(protocolA,protocolB) => getAgents(protocolA) ++ getAgents(protocolB)
      case Parallel(protocolA,protocolB) => getAgents(protocolA) ++ getAgents(protocolB)
      case Choice  (protocolA,protocolB) => getAgents(protocolA) ++ getAgents(protocolB)
      case RecursionFixedPoint(_,protocolB) => getAgents(protocolB)
  end getAgents

  def hasParallel(protocol:Protocol):Boolean =
    protocol match
      case Interaction(_,_,_,_) => false
      case Send   (_,_,_,_) => false
      case Receive(_,_,_,_) => false
      case RecursionCall(_) => false
      case Skip => false
      case Sequence(protocolA,protocolB) => hasParallel(protocolA) && hasParallel(protocolB)
      case Parallel(protocolA,protocolB) => true
      case Choice  (protocolA,protocolB) => hasParallel(protocolA) && hasParallel(protocolB)
      case RecursionFixedPoint(_,protocolB) => hasParallel(protocolB)
  end hasParallel
end Protocol