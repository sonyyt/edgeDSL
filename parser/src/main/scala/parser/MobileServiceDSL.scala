package parser
import scala.util.parsing.combinator._
import scala.util.parsing.combinator.syntactical._

object MobileServiceDSL extends StandardTokenParsers{
  lexical.reserved += ("ExecutionParameter", "Service", "MS", "Given", "output", "EQUALS","Global", "input", "device", "to", "invoke", "wait", "select", "sort", "set", "as", "on", "Execution", "SUCCESS", "as", "result", "FAILURE", "null", "return", "exit")
  
  lexical.delimiters += ("{","}",".","->",":","$",";","(",")",">","<","==",">=","<=",",","|","=","&" ,"_")
  
  import MS_AST._
  //lv top: mobileService
  lazy val MobileService: Parser[MobileServiceObj] = service_id ~ service_description ^^ {case i ~ a => MobileServiceObj(i,a)}
  lazy val service_id: Parser[MobileServiceIdentity] = "Service" ~> ident ^^ MobileServiceIdentity
  lazy val service_description: Parser[MobileServiceDetails] = "{"~> opt(service_params) ~ microServices<~"}" ^^ {
    case Some(input) ~ microservices => MobileServiceDetails(input,microservices)
    case None ~ microservices => MobileServiceDetails(null,microservices)
  
  }
  lazy val inputKey = ident
  lazy val inputValue = ident|numericLit 
  lazy val service_params: Parser[ServiceInputParameters] = "Global" ~> "input" ~> ":"~>rep1sep("$"~>inputKey,",")<~";" ^^ ServiceInputParameters
  
  //lv 2: microservices
  lazy val microServices: Parser[MicroServices] = rep(MicroService) ^^ MicroServices
  
  //lv3: microservice
  lazy val MicroService: Parser[MicroServiceObj] = "MS" ~>(":" ~> ident) ~ ("{"~> MS_specifics <~"}") ^^ {case i ~ d => MicroServiceObj(i, d)}
  //lazy val MicroService: Parser[MicroServiceObj] = "MicroService" ~>  numericLit ~ (":" ~> ident) ~ ("{"~> MS_specifics <~"}") ^^ {case s ~ i ~ d => MicroServiceObj(s.toInt, i, d)}
  
  lazy val MS_specifics: Parser[MsDetails] = rep(microservice_detail) ^^ MsDetails
  
  lazy val microservice_detail : Parser[MsDetail]= (deviceSelectionDetail|inputDetail|onConditionDetail)  ^^ { case microservice_detail => microservice_detail }
    
  //("device" ~ ":" ~ deviceRes | "set" ~ ":" ~ setRes | onCondition ~ ":" ~ onRes) ~ ";"

  lazy val deviceSelectionDetail: Parser[MsDetail] = "device" ~> (":" ~> deviceRes)  ^^ DeviceDetail 
  //device selection rules
  lazy val deviceRes: Parser[RequiredDevice] = rep1sep(rule,".") ^^ RequiredDevice
  lazy val rule: Parser[SpecificRule] = (select_sort?) ~ ("(" ~> stringLit <~ ")") ^^ {case r ~ s => SpecificRule(r,s)}
  lazy val select_sort: Parser[DeviceSelectionRule] = "select"^^^MUST|"sort"^^^NEED
 
  lazy val inputDetail: Parser[MsDetail] = ("set" ~> ":" ~> "$" ~> inputKey <~ "=") ~ (inputValue|"_")  ^^ {case k ~ v => new InputDetail(k,v)}
  
  lazy val onConditionDetail: Parser[MsDetail] = ("on"~>"."~> condition) ~ ( ":" ~> outputs) ~ opt( redirection)  ^^ {
    case c ~ o ~ Some(r) => new OnConditionDetail(c,o,r)
    case c ~ o ~ None => new OnConditionDetail(c,o,new redirToNull())
  } 
  
  lazy val condition: Parser[Condition] = (conditionExecution | conditionResult)^^ { case condition => condition }
  
  lazy val conditionExecution: Parser[Condition] = "Execution"~>"."~>(fail_succ?) ^^  {case i  => new ConditionExecution(i)}
  
  lazy val conditionResult: Parser[Condition] = ("result"~>"."~> ident) ~ operation ~ (ident|numericLit)  ^^  {case i ~ o ~ t => new ConditionResult(i, o, t.toString)}

  lazy val operation: Parser[OperationType] = ">="^^^NoLessThan|">"^^^MoreThan|"<="^^^NoMoreThan|"<"^^^LessThan|"=="^^^EqualTo|"EQUALS"^^^StringEqual
    
  lazy val fail_succ: Parser[ExecutionStatus] = "FAILURE"^^^Fail|"SUCCESS"^^^SUCCESS
  
  lazy val outputs: Parser[Outputs] = rep(output) ^^ Outputs
  lazy val output: Parser[Output] = (outputToMS|outputToReturn) <~ ";" ^^ {case output => output }
  lazy val outputToMS:Parser[Output] = ("output" ~> ident) ~ opt("as" ~> ident) ^^  {
    case execP~Some(outputAs) => new OutputToMS(execP,outputAs)
    case execP~None => new OutputToMS(execP,execP) 
  }
  lazy val outputToReturn:Parser[Output] = ("return" ~> ident) ~ opt("as" ~> ident) ^^  {
    case execP~Some(outputAs) => new OutputToReturn(execP,outputAs)
    case execP~None => new OutputToReturn(execP,execP)
  }
  
  lazy val redirection: Parser[Redirection] = redirToMS|redirToReturn ^^ {case redirection => redirection }
  lazy val redirToMS:Parser[RedirToMS] = "invoke" ~> ident ^^ {case i=> new RedirToMS(i)} 
  lazy val redirToReturn:Parser[RedirToReturn] = "Service" ~> "." ~> "exit" ^^ {case i=>new RedirToReturn()}
//  lazy val redirToNull:Parser[redirToNull] = "wait" ^^ {case i => new redirToNull()} 
  
  
//  lazy val responses: Parser[Responses] =  rep1sep(response, ";") ^^ Responses
//  
//  //processing here: not in parser. 
//  
//  lazy val response: Parser[Response] = (responseReturn|responseExit|responseInvoke|responseRenameInvoke)^^ {case  response => response }
//
//  lazy val responseExit: Parser[Response] = "Service"~>"."~>"exit" ^^ {case i=>new ResponseExit(i)}
//  
//  lazy val responseReturn:Parser[Response] = ("return" ~> ident <~ "as") ~ ident ^^{case retVal~retAs => new ResponseReturn(retVal,retAs)}
//  
//  lazy val responseInvoke:Parser[Response] = ("input" ~> ident <~ "to") ~ ("MicroService"~>numericLit) ^^{case retVal~retAs => new ResponseInvoke(retVal,retAs.toInt)}
//
//  lazy val responseRenameInvoke:Parser[Response] = ("input" ~> ident <~ "as") ~ ident ~ ("to" ~> "MicroService"~>numericLit) ^^{case retVal~inputVal~retAs => new ResponseRenameInvoke(retVal, inputVal,retAs.toInt)}

  
  /*
  // set execution parameters and inputs
  lazy val setRes = executionParameter|inputParameter
  lazy val inputParameter = input ~ "AS" ~ taskInputkey
  lazy val input = numericLit|ident|"$"~ident
  lazy val taskInputkey = ident;
  lazy val executionParameter = "ExecutionParameter" ~ "." ~ inputKey ~ "=" ~ inputValue
 
  //on execution result conditions
  lazy val onCondition = "on" ~ "." ~ condition
  lazy val condition = execution_status|result_status
  lazy val execution_status = "Execution" ~ "." ~ "FAILURE"|"Execution" ~ "." ~ "SUCCESS"
  lazy val result_status = "("~ rep1sep(result_conditions,"&") ~")"
  lazy val result_conditions = resultKey ~ operators ~ constraint
  lazy val resultKey = "result" ~ "." ~ ident
  lazy val operators = ">"|"<"|"=="|">="|"<="
  lazy val constraint = ident|numericLit;
  
  // on execution result reactions
  lazy val onRes = rep1sep(onResponses,"|")
  lazy val onResponses =  serviceExit | outputToReturn | outputToTask
  lazy val serviceExit = "Service" ~ "." ~ "exit" 
  lazy val outputToReturn = resultKey ~ "AS" ~ returnKey
  lazy val outputToTask = inputToTask ~ "->" ~ "task" ~ numericLit
  lazy val inputToTask = ident|numericLit|resultKey|"null"
  lazy val returnKey = "return" ~ "." ~ ident; 
 
 */
  
}