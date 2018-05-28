# edgeDSL
## What is edgeDSL
edgeDSL is a domain sepcific language used for edge computing environments. It defines how to execute edge-based services. 

Each edge-based services is identified by a unique ID. A mobile application can invoke an edge-based service via it's unique ID. The invocation is sent to be processed by the router connected to the mobile device. The router works as a gateway, and coordinates its connected nearby devices to provide the service. 

Each edge-based service is accomplished by a serial of microservices, each of which is provided by an edge device. The DSL defines how such microservices can be combined to  provide a service. 

## what is included in the package
Parser: parsese a service script written in the DSL to a JSON file which can be used to instruct how to execute microservices. 
Executor: executes a required microservice on the gateway. 
Microservices: are executable packages on devices that provides microservices. 

## How to run this package. 
