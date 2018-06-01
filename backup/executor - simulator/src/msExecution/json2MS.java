package msExecution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class json2MS {
	String msJson;
	ObjectMapper mapper;
	MicroService ms;
	public json2MS(){
		mapper = new ObjectMapper();
		ms  = null;
	}
	
	public json2MS setJsonFile(String fileName){
		this.msJson = readAllBytes(fileName);
		return this;
	}
	
	public json2MS readDefaultFile(){
		String defaultFileName = "C://SoftwareInnovationsLab/edgeDSL/parser/jsonOutput.txt";
		return this.setJsonFile(defaultFileName);	
	}
	
	public json2MS setJsonString(String json){
		this.msJson = json;
		return this;
	}
	
	public MicroService convert(){
		if(this.msJson==null){ 
			throw new Error("json2MS error: require string as input");
		}
		try {
			//JSON from file to Object
			ms = mapper.readValue(msJson, MicroService.class);
			return ms;
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public MicroService convertWithoutGraph(){	
		MicroService ms = this.convert();
		return ms;
	}
	
	public MicroService convertWithGraph(){	
		MicroService ms = this.convert();
		drawGraph dg = new drawGraph(ms.controlFlowGraph);
		dg.parseExecutionGraph();
		return ms;
	}
	
    private static String readAllBytes(String filePath){
        String content = "";
        try{
            content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        }catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
}
