This is the repository for the EAI Coding Challenge.

This project is coded in Java.
This project was made in Eclipse, and should be able to be imported to Eclipse. However, it is not necessary.
The main files of interest are Main.java located in src/main/java, containing all the main code to make the JavaSpark RESTful API work,
and UnitTests.java in src/test/java, containing the main JUnit tests (in JUnit 4). 
These files can be used to run in various environments, not necessarily depending on Eclipse.

When run, the program will try to connect to a Elasticsearch server in order to complete its tasks. 
By default, it tries to connect to localhost:9200, which is the default Elasticsearch server, 
but this can be changed by running the program with arguments.

The main method can take up to two arguments, which represent the host and port in order. 
The port must be parseable to an int.

For example, running the program with "127.0.0.1, 1234"
will make the program attempt to connect to an Elasticsearch server at 127.0.0.1:1234

NOTE: The GET query method is not functioning properly in the current state. 
No results are returned/displayed from the query.
This also means that its unit test fails.

Also, the POST and PUT methods require specific syntaxes to work properly

To use the POST method, up to three strings must be provided, separated by commas, 
representing the name, phone number, and address of the contact
For example:
    POST /contact John,5555555,Main St
Will create a contact at the id John with the following fields {name:John,number:5555555,address:Main St}

To use the PUT method, the additional information that is to be changed must follow param1:value1,param2:value2,...
Where param1, param2, etc. are the names of the fields to be changed 
and value1, value2, etc. are the values that the corresponding parameters are to be changed to
For example
    PUT /contact/John name:Joe,number:777777
Will update John's name to Joe, also changing the id of the contact to Joe, and number to 777777, without changing the address.
