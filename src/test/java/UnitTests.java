

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;




public class UnitTests {
	//------------------------------------------------
	//	WARNING:
	//This will delete the "contact" index in the elasticsearch server to conduct tests on an empty index
	//or whatever is specified in the Main.java file as INDEX
	static RestHighLevelClient client;

	@BeforeClass
	public static void init() throws IOException{
		//Specify the host and port here
		String host = "localhost";
		int port = 9200;

		//Client to interact with elasticsearch
		client = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http")));
		
		//delete the "contact" index, or whatever is specified in the Main.java file as INDEX
		DeleteByQueryRequest request = new DeleteByQueryRequest(Main.INDEX);
		request.setQuery(QueryBuilders.matchAllQuery());
		BulkByScrollResponse response = client.deleteByQuery(request, RequestOptions.DEFAULT);
	}

	@AfterClass
	public static void reset() throws IOException{
		//delete the "contact" index, or whatever is specified in the Main.java file as INDEX
		DeleteByQueryRequest request = new DeleteByQueryRequest(Main.INDEX);
		request.setQuery(QueryBuilders.matchAllQuery());
		BulkByScrollResponse response = client.deleteByQuery(request, RequestOptions.DEFAULT);
	}

	@Test
	//Tests the output when using GET for a contact that does not yet exist
	public void getNonExistantContactTest() throws IOException{
		String name = "test";
		String[] output = Main.performGet(client, name);
		assertEquals("404", output[0]);
	}
	
	//Manually posts contacts and tests the output when using GET
	@Test
	public void getContactTest() throws IOException{
		String name1 = "gettest1";
		String name2 = "gettest2";
		String name3 = "gettest3";
		String number = "555";
		String address = "add";
		//create a contact with only a name
		Map<String, Object> jsonMap1 = new HashMap<>();
		jsonMap1.put("name", name1);
		//create a contact with only a name and number
		Map<String, Object> jsonMap2 = new HashMap<>();
		jsonMap2.put("name", name2);
		jsonMap2.put("number", number);
		//create a contact with name, number, and address
		Map<String, Object> jsonMap3 = new HashMap<>();
		jsonMap3.put("name", name3);
		jsonMap3.put("number", number);
		jsonMap3.put("address", address);
		
    	//create an index request with the id being the username
    	IndexRequest request1 = new IndexRequest(Main.INDEX).id(name1).source(jsonMap1);
    	IndexResponse indexResponse1 = client.index(request1, RequestOptions.DEFAULT);
    	IndexRequest request2 = new IndexRequest(Main.INDEX).id(name2).source(jsonMap2);
    	IndexResponse indexResponse2 = client.index(request2, RequestOptions.DEFAULT);
    	IndexRequest request3 = new IndexRequest(Main.INDEX).id(name3).source(jsonMap3);
    	IndexResponse indexResponse3 = client.index(request3, RequestOptions.DEFAULT);
    	
		//test the get method
    	String[] output;
    	output = Main.performGet(client, name1);
    	assertEquals("200", output[0]);
    	assertNotEquals("", output[1]);
    	assertNotEquals(null, output[1]);
    	output = Main.performGet(client, name2);
    	assertEquals("200", output[0]);
    	assertNotEquals("", output[1]);
    	assertNotEquals(null, output[1]);
    	output = Main.performGet(client, name3);
    	assertEquals("200", output[0]);
    	assertNotEquals("", output[1]);
    	assertNotEquals(null, output[1]);
	}
	
	//Tests the output when using DELETE for a contact that does not yet exist
	@Test
	public void deleteNonExistantContactTest() throws IOException{
		String name = "test";
		String[] output = Main.performDelete(client, name);
		assertEquals("404", output[0]);
	}
	
	//Manually posts contacts and tests the output when using DELETE
	@Test
	public void deleteContactTest() throws IOException{
		String name1 = "deltest1";
		String name2 = "deltest2";
		String name3 = "deltest3";
		String number = "555";
		String address = "add";
		//create a contact with only a name
		Map<String, Object> jsonMap1 = new HashMap<>();
		jsonMap1.put("name", name1);
		//create a contact with only a name and number
		Map<String, Object> jsonMap2 = new HashMap<>();
		jsonMap2.put("name", name2);
		jsonMap2.put("number", number);
		//create a contact with name, number, and address
		Map<String, Object> jsonMap3 = new HashMap<>();
		jsonMap3.put("name", name3);
		jsonMap3.put("number", number);
		jsonMap3.put("address", address);
		
    	//create an index request with the id being the username
    	IndexRequest request1 = new IndexRequest(Main.INDEX).id(name1).source(jsonMap1);
    	IndexResponse indexResponse1 = client.index(request1, RequestOptions.DEFAULT);
    	IndexRequest request2 = new IndexRequest(Main.INDEX).id(name2).source(jsonMap2);
    	IndexResponse indexResponse2 = client.index(request2, RequestOptions.DEFAULT);
    	IndexRequest request3 = new IndexRequest(Main.INDEX).id(name3).source(jsonMap3);
    	IndexResponse indexResponse3 = client.index(request3, RequestOptions.DEFAULT);
    	
		//test the get method
    	String[] output;
    	output = Main.performDelete(client, name1);
    	assertEquals("200", output[0]);
    	output = Main.performDelete(client, name2);
    	assertEquals("200", output[0]);
    	output = Main.performDelete(client, name3);
    	assertEquals("200", output[0]);
	}
	
	//Tests the output when using PUT for a contact that does not yet exist
	@Test
	public void putNonExistantContactTest() throws IOException{
		String name = "test";
		String[] output = Main.performPut(client, name, "name:tester");
		assertEquals("404", output[0]);
	}
	//Manually posts contacts and tests the output when using PUT
	@Test
	public void putContactInvalidNumberTest() throws IOException{
		String name = "puttestinv";
		String number = "555";
		String address = "add";
		//create a contact with only a name
		//create a contact with name, number, and address
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("name", name);
		jsonMap.put("number", number);
		jsonMap.put("address", address);
		
    	//create an index request with the id being the username
    	IndexRequest request = new IndexRequest(Main.INDEX).id(name).source(jsonMap);
    	IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
    	
		//test the get method
    	String[] output;
    	output = Main.performPut(client, name, "number:abc");
    	assertEquals("400", output[0]);
    	output = Main.performPut(client, name, "name:puttestinvnew,number:abc");
    	assertEquals("400", output[0]);
    	output = Main.performPut(client, name, "number:abc,address:Main Street");
    	assertEquals("400", output[0]);
    	output = Main.performPut(client, name, "number:888-888-8888");
    	assertEquals("400", output[0]);
    	output = Main.performPut(client, name, "number:888-888-8888,address:Main St");
    	assertEquals("400", output[0]);
    	output = Main.performPut(client, name, "number:111222333444555666");
    	assertEquals("400", output[0]);
    	output = Main.performPut(client, name, "number:111222333444555666,address:Main St");
    	assertEquals("400", output[0]);
    	output = Main.performPut(client, name, "number:111222333444555666,name:puttestinvnew");
    	assertEquals("400", output[0]);
	}
	
	//Manually posts contacts and tests the output when using PUT
	@Test
	public void putContactTest() throws IOException{
		String name1 = "puttest1";
		String name2 = "puttest2";
		String name3 = "puttest3";
		String number = "555";
		String address = "add";
		//create a contact with only a name
		Map<String, Object> jsonMap1 = new HashMap<>();
		jsonMap1.put("name", name1);
		//create a contact with only a name and number
		Map<String, Object> jsonMap2 = new HashMap<>();
		jsonMap2.put("name", name2);
		jsonMap2.put("number", number);
		//create a contact with name, number, and address
		Map<String, Object> jsonMap3 = new HashMap<>();
		jsonMap3.put("name", name3);
		jsonMap3.put("number", number);
		jsonMap3.put("address", address);
		
    	//create an index request with the id being the username
    	IndexRequest request1 = new IndexRequest(Main.INDEX).id(name1).source(jsonMap1);
    	IndexResponse indexResponse1 = client.index(request1, RequestOptions.DEFAULT);
    	IndexRequest request2 = new IndexRequest(Main.INDEX).id(name2).source(jsonMap2);
    	IndexResponse indexResponse2 = client.index(request2, RequestOptions.DEFAULT);
    	IndexRequest request3 = new IndexRequest(Main.INDEX).id(name3).source(jsonMap3);
    	IndexResponse indexResponse3 = client.index(request3, RequestOptions.DEFAULT);
    	
		//test the get method
    	String[] output;
    	output = Main.performPut(client, name1, "number:555");
    	assertEquals("200", output[0]);
    	output = Main.performPut(client, name1, "address:Main Street");
    	assertEquals("200", output[0]);
    	output = Main.performPut(client, name2, "number:777,address:Main Street");
    	assertEquals("200", output[0]);
    	output = Main.performPut(client, name2, "address:Main St,number:123");
    	assertEquals("200", output[0]);
    	output = Main.performPut(client, name2, "address:Main St,name:puttest2new");
    	assertEquals("200", output[0]);
    	output = Main.performPut(client, name3, "name:puttest3new");
    	assertEquals("200", output[0]);
	}
	
	//Test the output when using PUT to change to name that already exists
	//Manually creates contacts to test with
	@Test
	public void putExistingContactTest() throws IOException{
		String name1 = "posttestexists1";
		String name2 = "posttestexists2";
		String number = "555";
		String address = "add";
		//create a contact with name, number, and address
		Map<String, Object> jsonMap1 = new HashMap<>();
		jsonMap1.put("name", name1);
		jsonMap1.put("number", number);
		jsonMap1.put("address", address);
		//create a contact with name, number, and address
		Map<String, Object> jsonMap2 = new HashMap<>();
		jsonMap2.put("name", name2);
		jsonMap2.put("number", number);
		jsonMap2.put("address", address);
		
		
    	//create an index request with the id being the username
    	IndexRequest request1 = new IndexRequest(Main.INDEX).id(name1).source(jsonMap1);
    	IndexResponse indexResponse1 = client.index(request1, RequestOptions.DEFAULT);
    	IndexRequest request2 = new IndexRequest(Main.INDEX).id(name2).source(jsonMap2);
    	IndexResponse indexResponse2 = client.index(request2, RequestOptions.DEFAULT);
    	
    	String output[];
    	output = Main.performPut(client, name2, "name:"+name1);
    	assertEquals("400", output[0]);
    	output = Main.performPut(client, name2, "name:"+name1+",number:5555");
    	assertEquals("400", output[0]);
    	output = Main.performPost(client, "address:Main St,number:555,name:"+name1);
    	assertEquals("400", output[0]);
    	output = Main.performPost(client, "address:Main St,name:"+name1);
    	assertEquals("400", output[0]);
	}
	
	//Tests the output when using PUT with no additional information
	@Test
	public void putNoAdditionalInformationTest() throws IOException{
		String name = "test";
		String[] output = Main.performPut(client, name, "");
		assertEquals("400", output[0]);
		output = Main.performPut(client, name, null);
		assertEquals("400", output[0]);
	}
	
	//Tests the output when using POST with no additional information
	@Test
	public void postNoAdditionalInformationTest() throws IOException{
		String name = "test";
		String[] output = Main.performPost(client, "");
		assertEquals("400", output[0]);
		output = Main.performPost(client, null);
		assertEquals("400", output[0]);
	}
	
	//Tests the output when using POST with invalid phone numbers
	@Test
	public void postInvalidNumberTest() throws IOException{
		String name = "test";
		String num1 = "abc";
		//Note, ONLY numeric characters are accepted
		String num2 = "888-888-8888";
		String num3 = "111222333444555666777";
		String[] output = Main.performPost(client, name+","+num1);
		assertEquals("400", output[0]);
		output = Main.performPost(client, name+","+num2);
		assertEquals("400", output[0]);
		output = Main.performPost(client, name+","+num3);
		assertEquals("400", output[0]);
	}
	
	//Test the output when using POST with a name that already exists
	//Manually creates contacts to test with
	@Test
	public void postExistingContactTest() throws IOException{
		String name = "posttestexists";
		String number = "555";
		String address = "add";
		//create a contact with name, number, and address
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("name", name);
		jsonMap.put("number", number);
		jsonMap.put("address", address);
		
    	//create an index request with the id being the username
    	IndexRequest request = new IndexRequest(Main.INDEX).id(name).source(jsonMap);
    	IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
    	
    	String cont1 = name;
    	String cont2 = name+","+number;
    	String cont3 = name+","+number+","+address;
    	String output[];
    	output = Main.performPost(client, cont1);
    	assertEquals("400", output[0]);
    	output = Main.performPost(client, cont2);
    	assertEquals("400", output[0]);
    	output = Main.performPost(client, cont3);
    	assertEquals("400", output[0]);
	}
	
	//Test the output when using POST
	@Test
	public void postContactTest() throws IOException{
		String cont1 = "posttest1";
		String cont2 = "posttest2,555";
		String cont3 = "posttest3,123,Main St";
		String[] output;
		output = Main.performPost(client, cont1);
		assertEquals("201", output[0]);
		output = Main.performPost(client, cont2);
		assertEquals("201", output[0]);
		output = Main.performPost(client, cont3);
		assertEquals("201", output[0]);
	}
	
	//Manually creates contacts and tests Querying
	@Test
	public void queryContactTest() throws IOException{
		String name1 = "qtest1";
		String name2 = "qtest2";
		String name3 = "qtest3";
		String number = "555";
		String address = "add";
		//create a contact with only a name
		Map<String, Object> jsonMap1 = new HashMap<>();
		jsonMap1.put("name", name1);
		//create a contact with only a name and number
		Map<String, Object> jsonMap2 = new HashMap<>();
		jsonMap2.put("name", name2);
		jsonMap2.put("number", number);
		//create a contact with name, number, and address
		Map<String, Object> jsonMap3 = new HashMap<>();
		jsonMap3.put("name", name3);
		jsonMap3.put("number", number);
		jsonMap3.put("address", address);
		
    	//create an index request with the id being the username
    	IndexRequest request1 = new IndexRequest(Main.INDEX).id(name1).source(jsonMap1);
    	IndexResponse indexResponse1 = client.index(request1, RequestOptions.DEFAULT);
    	IndexRequest request2 = new IndexRequest(Main.INDEX).id(name2).source(jsonMap2);
    	IndexResponse indexResponse2 = client.index(request2, RequestOptions.DEFAULT);
    	IndexRequest request3 = new IndexRequest(Main.INDEX).id(name3).source(jsonMap3);
    	IndexResponse indexResponse3 = client.index(request3, RequestOptions.DEFAULT);
    	
    	String[] output;
    	output = Main.performQuery(client, 0, 0, "");
    	assertEquals("200", output[0]);
    	assertEquals("No results found.", output[1]);
    	output = Main.performQuery(client, 1, 1, "qtest1");
    	assertEquals("200", output[0]);
    	assertNotEquals("", output[1]);
    	assertNotEquals("No results found.", output[1]);
    	output = Main.performQuery(client, 1, 1, "qtest2");
    	assertEquals("200", output[0]);
    	assertNotEquals("", output[1]);
    	assertNotEquals("No results found.", output[1]);
    	output = Main.performQuery(client, 1, 1, "qtest3");
    	assertEquals("200", output[0]);
    	assertNotEquals("", output[1]);
    	assertNotEquals("No results found.", output[1]);
    	output = Main.performQuery(client, 1, 1, "qtest");
    	assertEquals("200", output[0]);
    	assertNotEquals("", output[1]);
    	assertNotEquals("No results found.", output[1]);
	}
	
}
