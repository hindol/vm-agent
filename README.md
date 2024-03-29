# VM Agent [![Clojars Project](https://img.shields.io/clojars/v/com.github.hindol/vm-agent.svg)](https://clojars.org/com.github.hindol/vm-agent) [![cljdoc badge](https://cljdoc.org/badge/com.github.hindol/vm-agent)](https://cljdoc.org/d/com.github.hindol/vm-agent/CURRENT)

VM Agent is an agent that runs in an Azure VM and selectively exposes some APIs of the blockchain client running alongside it.

This project features,

- A minimalistic JSON-RPC 2.0 [client](https://github.com/Hindol/json-rpc) implementation in Clojure. Check [this](src/vm_agent/besu.clj) file for example usage.

- Pedestal [conditional interceptors](src/vm_agent/besu.clj) and [content negotiation](src/vm_agent/content_negotiation.clj).

## Getting Started

- Install the [Clojure CLI tools](https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools), preferably in a \*nix environment ([Windows Subsystem for Linux](https://docs.microsoft.com/en-us/windows/wsl/install-win10) works fine.) Outside WSL, [Windows support](https://clojure.org/guides/getting_started#_installation_on_windows) is experimental at this point.

- Build the project and create a Docker image in one step,

        > clojure -A:pack mach.pack.alpha.jib \
                  --image-name $DOCKER_REGISTRY/vm-agent:0.1.0 \
                  --image-type docker \
                  -m vm-agent.server

    `$DOCKER_REGISTRY` is any registry where you want to host the image. For [Azure Container Registry](https://azure.microsoft.com/en-in/services/container-registry/), use `<registry>.azurecr.io`.

- Start the server,

        > docker run $DOCKER_REGISTRY/vm-agent:0.1.0

- Try one of the examples.

## Examples

```shell
curl -i \
     -H 'Accept: application/json' \
     http://localhost:8890/besu/block-number
```

**Note:** There are known [networking limitations](https://docs.docker.com/docker-for-windows/networking/) when running Docker on Windows. Try these workarounds,

1. Run Docker in a [Vagrant](https://www.vagrantup.com/) VM.
1. Run cURL in a container that is attached to the same network.

## [API Documentation](https://cljdoc.org/d/com.github.hindol/vm-agent/CURRENT/api/vm-agent.besu)

Currently, these APIs are exposed,

```clojure
#{["/besu/genesis"              :get    (conj common-interceptors besu/read-genesis)]
  ["/besu/genesis"              :put    (conj common-interceptors besu/create-genesis)]
  ["/besu/block-number"         :get    (conj common-interceptors besu/read-block-number)]
  ["/besu/syncing"              :get    (conj common-interceptors besu/syncing)]
  ["/besu/public-key"           :get    (conj common-interceptors besu/read-public-key)]
  ["/besu/address"              :get    (conj common-interceptors besu/read-address)]
  ["/besu/enode-url"            :get    (conj common-interceptors besu/read-enode-url)]
  ["/besu/accounts/"            :get    (conj common-interceptors besu/read-accounts)]
  ["/besu/peers/"               :get    (conj common-interceptors besu/read-peers)]
  ["/besu/peers/"               :post   (conj common-interceptors besu/add-peer)]
  ["/besu/peers/"               :delete (conj common-interceptors besu/remove-peer)]
  ["/besu/validators/"          :get    (conj common-interceptors besu/read-validators)]
  ["/besu/validators/"          :post   (conj common-interceptors besu/add-validator)]
  ["/besu/validators/"          :delete (conj common-interceptors besu/remove-validator)]
  ["/besu/send-raw-transaction" :post   (conj common-interceptors besu/send-raw-transaction)]
```

Take a look at the [handlers](src/vm_agent/besu.clj). The code is well documented.

## Clojure Crash Course

Check out [Clojure in 15 Minutes](https://gist.github.com/Hindol/727eb69e9943b371e66902c19960fd0c).
