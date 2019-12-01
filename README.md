# VM Agent

VM Agent is an agent that runs in an Azure VM and selectively exposes some APIs of the blockchain client running alongside it.

This project features,

- A minimalistic JSON-RPC 2.0 [client](src/vm_agent/json_rpc.clj) implementation in Clojure. Check [this](src/vm_agent/besu.clj) file for example usage.

- Pedestal [conditional interceptors](src/vm_agent/besu.clj) and [content negotiation](src/vm_agent/main.clj).
