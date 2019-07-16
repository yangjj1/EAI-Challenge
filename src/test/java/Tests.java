/*
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;


//------------------------------------------------
//					WARNING:
//This will delete the "contact" index in the elasticsearch server to conduct tests on an empty index
//or whatever is specified in the Main.java file as INDEX
public class Tests {
	
	RestHighLevelClient client;

	@BeforeAll
	public void init(){
		//Specify the host and port here
		String host = "localhost";
		int port = 9200;
    	
    	//Client to interact with elasticsearch
    	client = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http")));
    }
	
	@BeforeEach
	public void reset() throws IOException{
		//delete the "contact" index, or whatever is specified in the Main.java file as INDEX
    	DeleteIndexRequest request = new DeleteIndexRequest(Main.INDEX);
    	AcknowledgedResponse deleteIndexResponse = client.indices().delete(request, RequestOptions.DEFAULT);
	
	}
	
	@Test
	//Tests the output when using the GET /contact/{name} of a contact that does not yet exist
	public void getNonExistantContactTest() throws IOException{
		Main tester = new Main();
		
		String name = "test";
		String[] output = tester.performGet(client, name);
		assertEquals(output[0], "404");
	}
}
*/