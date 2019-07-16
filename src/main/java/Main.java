import static spark.Spark.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

public class Main {
	
	//Array of parameters available to store in the address book. This implementation assumes the first will be the name.
	//we also assume that the second parameter is the phone number in this implementation
	private static String[] PARAMETERS = {"name", "number", "address"};
	static String INDEX = "contact";
	
    public static void main(String[] args) {
    	
    	//These are the default elasticsearch settings. If no host or port are specified as arguments, these are the values used.
    	//When running with arguments, the first argument is assumed to be the host, and the second to be the port.
    	String url = "localhost";
    	int port = 9200;
    	
    	//The first argument is assumed to be the host, so if it is provided, change the host
    	if(args.length > 0){
    		url = args[0];
    	}
    	//The second argument is assumed to be the port, so if it is provided, change the port
    	if(args.length > 1){
    		port = Integer.parseInt(args[1]);
    	}
    	
    	//This is how we interact with elasticsearch
    	RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(url, port, "http")));
    	
    	//----------------------------------------------
    	//			POST METHOD
    	//	This method requires an arbitrary format to parse the information:
    	//	POST .../contact name,number,address
    	//	with the number and address fields being optional. The name must not be currently in use.
    	post("/contact", "application/json", (req, res) -> {
    		//extract the additional information
        	String data = req.body();
        	//perform the POST operation
        	String[] results = performPost(client, data);
        	//set the status and message
        	res.status(Integer.parseInt(results[0]));
        	return results[1];
        	
        });
    	
    	//----------------------------------------------
    	//			GET QUERY METHOD
    	//	NOTE: This method is not working properly in the current implementation. No hits are being retrieved.
    	get("/contact", (req, res) -> {
    		//grab the relevant query information, setting defaults if they are not specified
    		//get the pageSize parameter, if it does not exist, set it to 0.
    		String pageSizeStr = req.queryParams("pageSize");
    		if(pageSizeStr == null || pageSizeStr.equals("")){
    			pageSizeStr = "0";
    		}
    		//get the page parameter, if it does not exist, set it to 1.
    		String pageStr = req.queryParams("page");
    		if(pageStr == null || pageStr.equals("")){
    			pageStr = "1";
    		}
    		//parse the pageSize and page parameters into ints, and calculate the offset
    		int pageSize = Integer.parseInt(pageSizeStr);
    		int page = Integer.parseInt(pageStr);
    		String query = req.queryParams("q");
    		//perform the operation
    		String[] results = performQuery(client, pageSize, page, query);
    		
    		//set the status and message
    		res.status(Integer.parseInt(results[0]));
    		return results[1];
    	});
    	
    	//----------------------------------------------
    	//			GET METHOD
    	//	This method returns the contact specified by name.
    	get("/contact/:name", (req, res) -> {
    		//extract the name
    		String name = req.params(":name");
    		//perform the GET operation
    		String[] results = performGet(client, name);
    		//if the status code is OK = 200, then also set the response type to json
    		if(results[0].equals("200")){
    			res.type("application/json");
    		}
    		res.status(Integer.parseInt(results[0]));
    		return results[1];
    	});
    	
    	//----------------------------------------------
    	//			DELETE METHOD
    	//	This method removes the contact specified by name
    	delete("/contact/:name", (req, res) -> {
    		//extract the name
    		String name = req.params(":name");
    		//perform the DELETE operation
    		String[] results = performDelete(client, name);
    		//set the status code and message
    		res.status(Integer.parseInt(results[0]));
    		return results[1];
    	});
    	
    	//----------------------------------------------
    	//			PUT METHOD
    	//	This method updates information specified by name.
    	//	The input must be in the format: param1:value1,param2:value2,...
    	//	Where the params must match those specified. In the current implementation, that includes "name", "number", and "address".
    	//	If a name is updated, then this method will take additional steps to create a new contact with that name and delete the old one.
    	put("/contact/:name", (req, res) -> {
    		//extract the name and additional input
    		String name = req.params(":name");
        	String data = req.body();
        	//perform the PUT operation
        	String[] results = performPut(client, name, data);
        	//set the status code and message
        	res.status(Integer.parseInt(results[0]));
        	return results[1];
    	});
    }
    
    
    //------------------------------------------------
    //			POST METHOD
    //
    //	This method takes 2 arguments, a REST client for the elasticsearch server
    //	and the information to create the contact
    //
	//	This method requires an arbitrary format to parse the information:
	//	POST .../contact name,number,address
	//	with the number and address fields being optional. The name must not be currently in use.
    //
    //	This method returns 2 Strings in an array: a status code and a message
    public static String[] performPost(RestHighLevelClient client, String body) throws IOException{
    	//prepare the output
    	String[] output = new String[2];
    	//check if the additional information exists
    	//if there is none, print an error message
    	if(body == null || body.equals("")){
    		//set the status to 400 = Bad Request
    		output[0] = "400";
    		//output the error message
    		output[1] = "Please input up to " +PARAMETERS.length+" values, separated by commas representing " +PARAMETERS[0]+","+PARAMETERS[1]+","+PARAMETERS[2];
    		return output;
    	}
    	
    	//parse the data
    	String[] fields = body.split(",");
    	//if there is no additional information sent, put an error message
    	//indicating that we need a name to create a contact
    	if(fields.length < 1){
    		//set the status to 400 = Bad Request
    		output[0] = "400";
    		//output the error message
    		output[1] = "Please input up to " +PARAMETERS.length+" values, separated by commas representing " +PARAMETERS[0]+","+PARAMETERS[1]+","+PARAMETERS[2];
    		return output;
    	}
    	
    	//check if the username is already in use
    	GetRequest getRequest = new GetRequest(INDEX, fields[0]);
    	getRequest.fetchSourceContext(new FetchSourceContext(false));
    	getRequest.storedFields("_none_");
    	if(client.exists(getRequest, RequestOptions.DEFAULT)){
    		//set the status to 400 = Bad Request
    		output[0] = "400";
    		//output the error message
    		output[1] = "This name is already in use. Try PUT to update an existing contact";
    		return output;
    	}
    	
    	//we enforce limitations on what can be inputted as a number, it must be purely numeric and be less than 15 digits:
    	if(fields.length > 1){
    		String numStr = fields[1];
    		//try to parse the integer
    		try{
    			int num = Integer.parseInt(numStr);
    		}
    		//if it is not an int, output an error
    		catch(NumberFormatException e){
    			output[0] = "400";
    			output[1] = "That is not a valid phone number. Phone numbers can only include numbers and must be at most 15 digits.";
    			return output;
    		}
    		//if the number is more than 15 characters, output an error
    		if(numStr.length() > 15){
    			output[0] = "400";
    			output[1] = "That is not a valid phone number. Phone numbers can only include numbers and must be at most 15 digits.";
    			return output;
    		}
    	}
    	//if additional information has been provided, get ready to add it
    	//create the document source
    	Map<String, Object> jsonMap = new HashMap<>();
    	jsonMap.put(PARAMETERS[0], fields[0]);
    	if(fields.length > 1){
    		jsonMap.put(PARAMETERS[1], fields[1]);
    	}
    	if(fields.length > 2){
    		jsonMap.put(PARAMETERS[2], fields[2]);
    	}
    	//create an index request with the id being the username
    	IndexRequest request = new IndexRequest(INDEX).id(fields[0]).source(jsonMap);
    	IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
    	//set the status to the status of the post request
    	output[0] = Integer.toString(indexResponse.status().getStatus());
    	output[1] = indexResponse.status().toString();
    	return output;
    }

    //----------------------------------------------
	//			GET QUERY METHOD
	//	NOTE: This method is not working properly in the current implementation. No hits are being retrieved.
    
    //	This method takes 4 arguments, a REST client for the elasticsearch server, a page size, 
    //	a page number, and a query
    //
    //	Searches with the query and displays a page of hits based on the page size and current page
    //	Returns 2 Strings in an array: a status code (as a String) and a status message
    public static String[] performQuery(RestHighLevelClient client, int pageSize, int page, String query) throws IOException{
    	//prepare the output
    	String[] output = new String[2];

    	//calculate the offset based on the page size and current page
		int offset = pageSize * (page - 1);
    	//if the starting page is 0 or less, set it to 1 and set the offset to 0.
		if(page <= 0){
			page = 1;
			offset = 0;
		}
		
		//build the search query
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		//we want a queryStringQuery
		searchSourceBuilder.query(QueryBuilders.queryStringQuery(query));
		//set the offset that we calculated earlier
		searchSourceBuilder.from(offset);
		//if the pageSize is a valid number, set the number of hits we want returned, otherwise ignore it and use the default value
		if(pageSize > 0){
			searchSourceBuilder.size(pageSize);
		}
		
		//build the searchRequest based on the query we construted
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(INDEX);
		searchRequest.source(searchSourceBuilder);
		
		//Execute the searchRequest and get the status.
		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		output[0] = Integer.toString(searchResponse.status().getStatus());
		
		//if the search timed out, send out an error message.
		if(searchResponse.isTimedOut()){
			output[1] = "Search timed out.";
			return output;
		}
		
		//get the hits from the searchResponse as an array of SearchHits
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHits = hits.getHits();
		//prepare the output
		String results = "";
		//loop over each hit and add its Source to the output
		for(int i=0; i<searchHits.length; i++){
			results = results + searchHits[i].getSourceAsString();
		}
		//if the output would return nothing, return a message instead.
		if(results == null || results.equals("")){
			output[1] = "No results found.";
			return output;
		}
		output[1] = results;
		return output;
    }

    //------------------------------------------------
    //			GET METHOD
    //
    //	This method takes 2 arguments, a REST client for the elasticsearch server, and a contact name to get
    //
    //	Looks up the contact with the specified name
    //	If it exists, return 2 Strings in an array: an OK status code (as a String) and the relevant information
    //	Otherwise, return 2 Strings in an array: an error status code (as a String) and an error message
    public static String[] performGet(RestHighLevelClient client, String name) throws IOException{
    	//prepare the output
    	String[] output = new String[2];
    	
    	//check if the username exists
    	GetRequest getRequest = new GetRequest(INDEX, name);
    	GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
    	//if it exists, print out the relevant information
    	if(getResponse.isExists()){
    		//set the status to 200 = Success
    		output[0] = "200";
    		//set the message to the contact information
    		output[1] = getResponse.getSourceAsString();
    		return output;
    	}
    	//else, return an error
    	else{
    		//set the status to 404 = information not found
    		output[0] = "404";
    		//set the error message
    		output[1] = "User not found";
    		return output;
    	}
    }
    
	//----------------------------------------------
	//			DELETE METHOD
    //
    //	This method takes 2 arguments, a REST client for the elasticsearch server, and a contact name to delete
    //
    //	Attempts to delete the contact with the specified name
    //	Returns 2 Strings in an array: a status code for the status of the operation, and a status message
    public static String[] performDelete(RestHighLevelClient client, String name) throws IOException{
    	//prepare the output
    	String[] output = new String[2];
    	
    	//attempt to delete the username
		DeleteRequest request = new DeleteRequest(INDEX, name);
		DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);
		//return the status
		output[0] = Integer.toString(deleteResponse.status().getStatus());
		output[1] = deleteResponse.status().toString();
		return output;
    }
    
	//----------------------------------------------
	//			PUT METHOD
    //
    //	This method takes 3 arguments, a REST client for the elasticsearch server, a contact name to update,
    //	and a String containing parameters to update and their new values
    //
    //	This method updates information of the contact with the specified name
	//	The input must be in the format: param1:value1,param2:value2,...
	//	Where the params must match those specified. In the current implementation, that includes "name", "number", and "address".
	//	If a name is updated, then this method will take additional steps to create a new contact with that name and delete the old one.
    //
    //	This method returns 2 Strings in an array: a status code (as a String), and a status message
    public static String[] performPut(RestHighLevelClient client, String name, String body) throws IOException{
    	//prepare the output
    	String[] output = new String[2];
    	
    	//check if there is additional information
    	//if there is none, print an error message
    	if(body == null || body.equals("")){
    		//set the status to 400 = Bad Request
    		output[0] = "400";
    		//output the error message
    		output[1] = "Please input up to " +PARAMETERS.length+" values, separated by commas representing " +PARAMETERS[0]+","+PARAMETERS[1]+","+PARAMETERS[2];
    		return output;
    	}
    	//parse the additional information, splitting by comma so each String is a key:value pair
    	String[] fields = body.split(",");
    	//if there is no additional information sent, put an error message
    	//indicating that we need a name to create a contact
    	if(fields.length < 1){
    		//set the status to 400 = Bad Request
    		output[0] = "400";
    		//set the error message
    		output[1] = "Please input a list of parameters and values as 'param1:value1,param2:value2,...'.";
    		return output;
    	}
    	
		//create a getRequest to check if a contact with the specified name exists
    	GetRequest getRequest = new GetRequest(INDEX, name);
    	getRequest.fetchSourceContext(new FetchSourceContext(false));
    	getRequest.storedFields("_none_");
    	//if the contact could not be found, send an error
    	if(!client.exists(getRequest, RequestOptions.DEFAULT)){
    		//set the status to 404 = Not Found
    		output[0] = "404";
    		//set the error message
    		output[1] = "There is no contact with that name.";
    		return output;
    	}
    	
    	//for each parameter, check that it is in a valid syntax and if it is, add it to the map
    	Map<String, Object> jsonMap = new HashMap<>();
    	//create a list from the array of available parameters
    	List<String> validParams = Arrays.asList(PARAMETERS);
    	//also keep a boolean to keep track of if the name field is being updated, 
    	//as we will need to do additional work if this is the case
    	boolean nameUpdated = false;
    	String newName = "";
    	//loop over each key:value pair, checking to see if it is a valid parameter and adding it to the map if it is.
    	for(int i=0; i<fields.length; i++){
    		String curr = fields[i];
    		String[] values = curr.split(":");
    		//check if this is a valid parameter, and if not, send an error.
    		if(!validParams.contains(values[0])){
    			output[0] = "400";
    			output[1] = "Invalid parameter";
    			return output;
    		}
    		else{
    			//we enforce limitations on what can be inputted as a number, it must be purely numeric and be less than 15 digits:
    			//we assume that the second parameter is the phone number in this implementation
    			if(values[0].equals(PARAMETERS[1])){
    				String numStr = values[1];
    	    		//try to parse the integer
    	    		try{
    	    			int num = Integer.parseInt(numStr);
    	    		}
    	    		//if it is not an int, output an error
    	    		catch(NumberFormatException e){
    	    			output[0] = "400";
    	    			output[1] = "That is not a valid phone number. Phone numbers can only include numbers and must be at most 15 digits.";
    	    			return output;
    	    		}
    	    		//if the number is more than 15 characters, output an error
    	    		if(numStr.length() > 15){
    	    			output[0] = "400";
    	    			output[1] = "That is not a valid phone number. Phone numbers can only include numbers and must be at most 15 digits.";
    	    			return output;
    	    		}
    			}
    			jsonMap.put(values[0], values[1]);
    		}
    		//if this is the name field, set the boolean to true and check if the new name is in use
    		if(values[0].equals(validParams.get(0))){
    			newName = values[1];
    			nameUpdated = true;
            	//check if the username is already in use
            	GetRequest getRequest2 = new GetRequest(INDEX, newName);
            	getRequest2.fetchSourceContext(new FetchSourceContext(false));
            	getRequest2.storedFields("_none_");
            	//if the new name is already in use, throw an error.
            	if(client.exists(getRequest2, RequestOptions.DEFAULT)){
            		//set the status to 400 = Bad Request
            		output[0] = "400";
            		//set the error message
            		output[1] = "The requested name is already in use. Please try another name.";
            		return output;
            	}
    		}
    	}
    	//update the requested information
		UpdateRequest request = new UpdateRequest(INDEX, name).doc(jsonMap);
		UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);
		//if the name was updated, we need to create a new contact with the new name as the id, and delete the old one
		if(nameUpdated){
			//check if the update ended successfully and if not, output the updateResponse status
			if(updateResponse.status().getStatus() != 200){
				output[0] = Integer.toString(updateResponse.status().getStatus());
				output[1] = updateResponse.status().toString();
				return output;
			}
			//check to make sure newName is valid
			if(newName == null || newName.equals("")){
				output[0] = "400";
				output[1] = "Invalid new name, please try again.";
				return output;
			}
			//we need to use a new getRequest to get all the information, including the updated ones
			GetRequest getRequestOld = new GetRequest(INDEX, name);
        	GetResponse getResponseOld = client.get(getRequestOld, RequestOptions.DEFAULT);
        	//if it does not exist, print out an error
        	if(!getResponseOld.isExists()){
        		output[0] = "400";
        		//set the error message
        		output[1] = "Something went wrong when trying to grab the information of the current contact.";
        		return output;
        	}
			//create an index request with the id being the new name
        	IndexRequest indexRequest = new IndexRequest(INDEX).id(newName).source(getResponseOld.getSource());
        	IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        	//set the status to the status of the post request
        	//if the status is not success, output the status
        	if(indexResponse.status().getStatus() != 201){
        		output[0] = Integer.toString(indexResponse.status().getStatus());
        		output[1] = indexResponse.status().toString();
        		return output;
        	}
        	//finally, delete the contact with the old name
    		//attempt to delete the username
    		DeleteRequest deleteRequest = new DeleteRequest(INDEX, name);
    		DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
    		output[0] = Integer.toString(deleteResponse.status().getStatus());
    		output[1] = deleteResponse.status().toString();
    		return output;
		}
		//if we didn't update the name, send the status as normal
		output[0] = Integer.toString(updateResponse.status().getStatus());
		output[1] = updateResponse.status().toString();
		return output;
    }
}