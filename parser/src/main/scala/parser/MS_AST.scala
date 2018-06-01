package parser

import deviceSelectionDSL._

object MS_AST{
  //top level： mobile service
  case class MobileServiceObj(ServiceID:MobileServiceIdentity, details:MobileServiceDetails)

  case class MobileServiceIdentity(ServiceID:String)
  case class ServiceInputParameters(params:List[String]){
    override def toString:String = {
       params.mkString("|")
      // : is the separater between rule type and detailed string. 
    }
  }
  case class MobileServiceDetails(inputParams:ServiceInputParameters, microservices: MicroServices){
    def getInputParameters:String = {
      if(inputParams==null){
        //println("global input null")
        return "";
      }else{
        //println("global input"+ inputParams.toString())
        return inputParams.toString()
      }
    }
  }

  //level 1: microservices
  case class MicroServices(lis:Seq[MicroServiceObj])

  case class MicroServiceObj(identity:String,msDetails:MsDetails)
  //level 3: ms details
  
  case class MsDetails(details:Seq[MsDetail])
  
  abstract class MsDetail(option:String){
     val MsType:String = option
  }
  
  trait DeviceSelectionRule
  case object MUST extends DeviceSelectionRule
  case object NEED extends DeviceSelectionRule
  case class SpecificRule(ruleType:Option[DeviceSelectionRule], In:String){
    override def toString:String = {
      return ruleType.get+":\""+In+"\"";
      // : is the separater between rule type and detailed string. 
    }
  }
  case class RequiredDevice(lis:Seq[SpecificRule]){
    def outputToString:String={
      return lis.mkString("&")
      // & is the separater between rules
    }
  }
  
  case class DeviceDetail(device:RequiredDevice) extends MsDetail("deviceSelection"){
    //override protected val MsType:String = "msDevice"
    //parse device selection rules
    def parseRules:String={
      var flag = true;
      var deviceSelectionRules = this.device.outputToString;
      println(deviceSelectionRules);
      deviceRequest(new lexical.Scanner(deviceSelectionRules)) match {
        case Success(dsRule, _) => {
          println(dsRule)
          dsRule.asInstanceOf[DS_AST.Rules];
          return dsRule.toString();
        }
        case Failure(msg, _) => {
          println("Failure:"+msg)
          return null;
        }
        case Error(msg, _) => {
          println("Error"+msg)
          return null;
        }
      }
    }
  }
  
  case class InputDetail(ParaName:String, ParaValue:String) extends MsDetail("microservice input") {
    val ParameterName = ParaName;
    val ParameterValue = ParaValue;
  }
  
  case class OnConditionDetail(condition:Condition, ops: Outputs, redir:Redirection ) extends MsDetail("On Condition"){
  }
   
  abstract class Condition(ct:String) {
	  protected val ConditionType:String = ct
	  def getConditionString():String
	  /*design: conditionString: 
	   * 1: success;
	   * 2: fail;
	   * 3: > result|target
	   * 4: >=
	   * 5: == 
	   * 6: <=
	   * 7: < 
	  */
  }

  // here we can change the conditionString to whatever easy to parse: e.g.: 1| 2| 3|???
  class ConditionExecution (status:Option[ExecutionStatus]) extends Condition("execution") {
    // condition is   execution status;
	  override def getConditionString():String={
	    if(status.get.eq(Fail)){
	      "2";
	    }else{
	      "1"
	    }
	  }
  }
  
  
  trait ExecutionStatus
  case object Fail extends ExecutionStatus
  case object SUCCESS extends ExecutionStatus
  
  
  class ConditionResult (ResultParamName:String, operation:OperationType, target:String) extends Condition("result") {
	  override def getConditionString():String={
	      val conditionString = operation match {
            case MoreThan  => "3"+ResultParamName+"|"+target
            case NoLessThan  => "4"+ResultParamName+"|"+target
            case EqualTo  => "5"+ResultParamName+"|"+target
            case NoMoreThan  => "6"+ResultParamName+"|"+target
            case LessThan  => "7"+ResultParamName+"|"+target
            case StringEqual=> "8" + ResultParamName+"|"+target
	      }
	      return conditionString
     }
  }
  
  trait OperationType
  case object NoLessThan extends OperationType
  case object MoreThan extends OperationType
  case object NoMoreThan extends OperationType
  case object LessThan extends OperationType
  case object EqualTo extends OperationType
  case object StringEqual extends OperationType 
  //-----------------------------------Responses on Condition--------------------
  case class Outputs(lis:Seq[Output])
   
  abstract class Output(var outputType:String, var executionResult:String, var outputAs:String)
  
  class OutputToMS (executionResult:String, outputAs:String) extends Output("toMS", executionResult,outputAs  )
  
  class OutputToReturn(executionResult:String, outputAs:String) extends Output("toReturn", executionResult, outputAs)
  

  
  abstract class Redirection(redirectionType:String){
    var Type = redirectionType;
  }
  
  class RedirToMS(val MsToInvoke: String) extends Redirection("toMS"){
    val targetMSID = MsToInvoke
  }
  
  class RedirToReturn extends Redirection("return")
  
  class redirToNull extends Redirection("null")
  
  // one rule to specify here: output to one ms, redirect to another? can that happen?

//  
//  
//  class ResponseReturn (val ReturnValue:String, val ReturnAs: String) extends Response("return value") {
//	  override def print(){
//      println("ResponseType: add return value")
//    }
//  }
//   
//  class ResponseInvoke (val output:String, val MsToInvoke: Int) extends Response("invoke") {
//	  override def print(){
//      println("ResponseType:Continue Invoke")
//    }
//  }
//  
//  class ResponseRenameInvoke (val output:String, val rename:String, val MsToInvoke: Int) extends Response("invoke rename") {
//	  override def print(){
//      println("ResponseType:Continue Invoke")
//    }
//  }
//  
//  case class Responses(lis:Seq[Response]){
//    var rs = lis;
//    def getRS():Seq[Response]=(
//        rs
//    )
//  }
//-----------------------------------End Responses on Condition--------------------
  
}