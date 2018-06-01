package parser

//target platform: MySQL;
/*todos:
 * 1. should we add checks on what data column is provided; 
 * 2. should we check whether the constrains are valid?
 */
object DS_AST{  
  trait DeviceSelectionRule
  case object MUST extends DeviceSelectionRule
  case object NEED extends DeviceSelectionRule
  
  //val dbColumnNames = List("TemperatureSensor", "GPSSensor", "InternetACCESS","Internet_SPEED", "CPU", "Battery", "Memory")
  
  case class Rule(ruleType:Option[DeviceSelectionRule], In:String){
    override def toString:String={
      In
    }
  }
  
  case class Rules(lis:Seq[Rule]){
    override def toString:String={
      val where = lis.filter(_.ruleType.get == MUST).mkString(" AND ")
      val orderby = lis.filter(_.ruleType.get == NEED).mkString(" , ")

      if (orderby.length == 0) {
        "select device from devices where " + where + " limit 1"
      }
      else {
        "select device from devices where " + where + " order by " + orderby + " limit 1"
      }
    }
  }
}


