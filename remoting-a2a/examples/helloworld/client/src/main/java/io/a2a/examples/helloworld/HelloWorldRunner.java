///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.a2a.sdk:a2a-java-sdk-core:0.2.4-SNAPSHOT
//SOURCES HelloWorldClient.java

/**
 * JBang script to run the A2A HelloWorldClient example.
 * This script automatically handles the dependencies and runs the client.
 * 
 * Prerequisites:
 * - JBang installed (see https://www.jbang.dev/documentation/guide/latest/installation.html)
 * - A running A2A server (see README.md for instructions on setting up the Python server)
 * 
 * Usage: 
 * $ jbang HelloWorldRunner.java
 * 
 * The script will communicate with the A2A server at http://localhost:9999
 */
public class HelloWorldRunner {
    public static void main(String[] args) {
        io.a2a.examples.helloworld.HelloWorldClient.main(args);
    }
} 