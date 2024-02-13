# java-client
Java client to connect to the queueing system

To connect to the server, just add the JAR file to your project dependencies and run the following code:

```
Client client = ClientFactory.INSTANCE.createClient("SERVER ADDRESS");
client.push(new MessageDto(key, value)); // push message
MessageDto message = client.pull(); // pull message
String key = message.key();
byte[] value = message.value();
client.subscribe(this::function); // subscribe a function 
```
