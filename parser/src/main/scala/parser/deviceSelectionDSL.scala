package parser
import scala.util.parsing.combinator._
import scala.util.parsing.combinator.syntactical._

object deviceSelectionDSL extends StandardTokenParsers{
  lexical.reserved += ("MUST","NEED")
  
  lexical.delimiters += (":","&")
  
  import DS_AST._
  
  lazy val deviceRequest: Parser[Rules] = rep1sep(rule,"&") ^^ Rules
  
  lazy val rule: Parser[Rule] = (select_sort?) ~ (":" ~> stringLit  ) ^^ {case a ~ b => Rule(a,b)}
  
  lazy val select_sort: Parser[DeviceSelectionRule] = "MUST"^^^MUST|"NEED"^^^NEED
  
}
  
  