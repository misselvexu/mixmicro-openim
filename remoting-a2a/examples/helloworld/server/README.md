# A2A Hello World Example

This example demonstrates how to use the A2A Java SDK to communicate with an A2A client. The example includes a Java server that receives both regular and streaming messages from a Python A2A client.

## Prerequisites

- Java 11 or higher
- Python 3.8 or higher
- [uv](https://github.com/astral-sh/uv)
- Git

## Run the Java A2A Server

The Java server can be started using `mvn` as follows:

```bash
cd examples/helloworld/server
mvn quarkus:dev
```

## Setup and Run the Python A2A Client

The Python A2A client is part of the [a2a-samples](https://github.com/google-a2a/a2a-samples) project. To set it up and run it:

1. Clone the a2a-samples repository:
   ```bash
   git clone https://github.com/google-a2a/a2a-samples.git
   cd a2a-samples/samples/python/agents/helloworld
   ```

2. **Recommended method**: Install dependencies using uv (much faster Python package installer):
   ```bash
   # Install uv if you don't have it already
   # On macOS and Linux
   curl -LsSf https://astral.sh/uv/install.sh | sh
   # On Windows
   powershell -c "irm https://astral.sh/uv/install.ps1 | iex"

   # Install the package using uv
   uv venv
   source .venv/bin/activate  # On Windows: .venv\Scripts\activate
   uv pip install -e .
   ```

4. Run the client with uv (recommended):
   ```bash
   uv run test_client.py
   ```

The client will connect to the Java server running on `http://localhost:9999`.

## What the Example Does

The Python A2A client (`test_client.py`) performs the following actions:

1. Fetches the server's public agent card
2. Fetches the server's extended agent card if supported by the server (see https://github.com/a2aproject/a2a-java/issues/81)
3. Creates an A2A client using the extended agent card that connects to the Python server at `http://localhost:9999`.
4. Sends a regular message asking "how much is 10 USD in INR?".
5. Prints the server's response.
6. Sends the same message as a streaming request.
7. Prints each chunk of the server's streaming response as it arrives.

## Notes

- Make sure the Java server is running before starting the Python client.
- The client will wait for 10 seconds to collect streaming responses before exiting.
- You can modify the server's response in `AgentExecutorProducer.java` if needed.
- You can modify the server's agent card in `AgentCardProducer.java` if needed.
- You can modify the server's URL in `application.properties` and `AgentCardProducer.java` if needed.