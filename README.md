# VM Agent

VM Agent is an agent that runs in an Azure VM and selectively exposes some APIs of the blockchain client running alongside it.

This project features,

- A minimalistic JSON-RPC 2.0 [client](src/vm_agent/json_rpc.clj) implementation in Clojure. Check [this](src/vm_agent/besu.clj) file for example usage.

- Pedestal [conditional interceptors](src/vm_agent/besu.clj) and [content negotiation](src/vm_agent/main.clj).

## Getting Started

- Install the [Clojure CLI tools](https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools), preferably in a \*nix environment ([Windows Subsystem for Linux](https://docs.microsoft.com/en-us/windows/wsl/install-win10) works fine.) Outside WSL, [Windows support](https://clojure.org/guides/getting_started#_installation_on_windows) is experimental at this point.

- Build the project and create a Docker image in one step,

        > clojure -A:pack mach.pack.alpha.jib --image-name $DOCKER_REGISTRY/vm-agent:0.1.0 --image-type docker -m vm-agent.server

    `$DOCKER_REGISTRY` is any registry where you want to host the image. For [Azure Container Registry](https://azure.microsoft.com/en-in/services/container-registry/), use `<registry>.azurecr.io`.

- Start the server,

        > docker run $DOCKER_REGISTRY/vm-agent:0.1.0

- Try one of the examples.

## Examples

```shell
curl -i \
     -X POST \
     -H 'Content-Type: application/json' \
     -H 'Accept: application/json' \
     http://localhost:8890/besu/block-number
```

**Note:** There are known [networking limitations](https://docs.docker.com/docker-for-windows/networking/) when running Docker on Windows. Try these workarounds,

1. Run Docker in a [Vagrant](https://www.vagrantup.com/) VM.
1. Run cURL in a container that is attached to the same network.
