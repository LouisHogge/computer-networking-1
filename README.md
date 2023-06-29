# Introduction to Computer Networking: First part of the assignment

## Project Description
In this first part of the project, you are going to implement a client able to perform queries to a DNS server, then parse the response from the server. 
Your DNS client will rely on the TCP protocol, which is stream-oriented.

## How to Use the Project
The client can be launched using the following command: *java Client \<name server IP\> \<domain name to query\> [question type]*, where the first 2 arguments are mandatory and the last one is optional (if not mentioned the question type is A). 

For example, these commands are valid and you should use these values to test your program:
```bash
java Client 139.165.99.199 ddi.uliege.be
```
```bash
java Client 139.165.214.214 ulg.ac.be TXT
```
