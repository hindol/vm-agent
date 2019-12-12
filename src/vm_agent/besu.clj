(ns vm-agent.besu
  (:require
   [cheshire.core :as json]
   [clojure.string :as str]
   [io.pedestal.interceptor.chain :as chain]
   [json-rpc.pedestal :as pedestal]
   [vm-agent.config :as config]))

(def ^:private ^:const url
  "HTTP[S] URL of the Besu client."
  (str "http://" (:besu-host config/dev) ":" (:besu-port config/dev)))

(def ^:private ^:const connection
  "A JSON-RPC connection."
  (json-rpc/connect url))

(defn- enode-url->public-key
  [enode-url]
  (let [[_ public-key] (re-matches #"enode://(.*)@\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}:\d{1,5}" enode-url)]
    (str "0x" public-key)))

(defn- trim-0x-prefix
  [address]
  (if (str/starts-with? address "0x")
    (subs address 2)
    address))

(defn- emplace
  [context connection method & params]
  (update context :request
          assoc
          :json-rpc-connection connection
          :json-rpc-method     method
          :json-rpc-params     params))

(defn- cleanup
  [context]
  (update context :request
          dissoc
          :json-rpc-connection
          :json-rpc-method
          :json-rpc-params))

(def read-genesis
  "Returns the genesis.json file.

  Example:
  ```shell
  curl -i \\
       -H 'Accept: application/json' \\
       http://localhost:8890/besu/genesis'
  ```"
  {:name ::read-genesis
   :enter (fn [context]
            (assoc context :response {:status 200
                                      :body {:result (slurp (:besu-genesis-file config/dev))}}))})

(def create-genesis
  "Seeds the genesis.json with initial set of validators.

  Example:
  ```shell
  curl -i \\
       -X PUT \\
       -H 'Content-Type: application/json' \\
       -H 'Accept: application/json' \\
       -d '{\"validators\":[\"0x7838e914b7d5c67b69bc29853b36ba8d86f463bc\"]}' \\
       http://localhost:8890/besu/genesis
  ```"
  {:name ::create-genesis
   :enter (fn [context]
            (let [{{{validators :validators} :json-params} :request} context
                  validators (map trim-0x-prefix validators)]
              (spit (:besu-validators-file config/dev) (json/generate-string validators))))})

(def read-block-number
  "Returns the index corresponding to the block number of the current chain head.

  Example:
  ```shell
  curl -i \\
       -H 'Accept: application/json' \\
       http://localhost:8890/besu/block-number'
  ```"
  {:name ::read-block-number
   :enter (fn [context]
            (let [context (emplace context connection "eth_blockNumber")]
              (chain/enqueue* context pedestal/json-rpc)))
   :leave cleanup})

(def syncing
  "Returns an object with data about the synchronization status, or false if not synchronizing.

  Example:
  ```shell
  curl -i \\
       -H 'Accept: application/json' \\
       http://localhost:8890/besu/syncing'
  ```"
  {:name ::syncing
   :enter (fn [context]
            (let [context (emplace context connection "eth_syncing")]
              (chain/enqueue* context pedestal/json-rpc)))
   :leave cleanup})

(def read-public-key
  "Returns the node public key.
  Internally, this function calls the net_enode method which returns the enode-url.
  We then extract the public key from the enode URL.

  Example:
  ```shell
  curl -i \\
       -H 'Accept: application/json' \\
       http://localhost:8890/besu/public-key'
  ```"
  {:name ::read-public-key
   :enter (fn [context]
            (let [context (emplace context connection "net_enode")]
              (chain/enqueue* context pedestal/json-rpc)))
   :leave (fn [context]
            (let [enode-url  (get-in context [:response :body :result])
                  public-key (enode-url->public-key enode-url)]
              (-> context
                  (assoc-in [:response :body :result] public-key)
                  (cleanup))))})

(def read-address
  "Returns the node address.

  Example:
  ```shell
  curl -i \\
       -H 'Accept: application/json' \\
       http://localhost:8890/besu/address'
  ```"
  {:name ::read-address
   :enter (fn [context]
            (assoc context :response {:status 200
                                      :body {:result (slurp (:besu-address-file config/dev))}}))})

(def read-enode-url
  "Returns the enode URL.

  Example:
  ```shell
  curl -i -H 'Accept: application/json' http://localhost:8890/besu/enode-url
  ```"
  {:name ::read-enode-url
   :enter (fn [context]
            (let [context (emplace context connection "net_enode")]
              (chain/enqueue* context pedestal/json-rpc)))
   :leave cleanup})

(def read-accounts
  "Returns a list of account addresses that the client owns.

  Example:
  ```shell
  curl -i -H 'Accept: application/json' http://localhost:8890/besu/accounts/
  ```"
  {:name ::read-accounts
   :enter (fn [context]
            (let [context (emplace context connection "eth_accounts")]
              (chain/enqueue* context pedestal/json-rpc)))
   :leave cleanup})

(def read-peers
  "Returns networking information about connected remote nodes.

  Example:
  ```shell
  curl -i -H 'Accept: application/json' http://localhost:8890/besu/peers/
  ```"
  {:name ::read-peers
   :enter (fn [context]
            (let [context (emplace context connection "admin_peers")]
              (chain/enqueue* context pedestal/json-rpc)))
   :leave cleanup})

(def add-peer
  "Adds a static node.

  Example:
  ```shell
  curl -i \\
       -X POST \\
       -H 'Content-Type: application/json' \\
       -H 'Accept: application/json' \\
       -d '{\"enode-url\":\"enode://ea26ccaf0867771ba1fec32b3589c0169910cb4917017dba940efbef1d2515ce864f93a9abc846696ebad40c81de7c74d7b2b46794a71de8f95a0d019f494ff3@127.0.0.1:30303\"}' \\
       http://localhost:8890/besu/peers/
  ```"
  {:name ::add-peer
   :enter (fn [context]
            (let [{{{enode-url :enode-url} :json-params} :request} context
                  context (emplace context connection "admin_addPeer" enode-url)]
              (chain/enqueue* context pedestal/json-rpc)))
   :leave cleanup})

(def remove-peer
  "Removes a static node.

  Example:
  ```shell
  curl -i \\
       -X DELETE \\
       -H 'Content-Type: application/json' \\
       -H 'Accept: application/json' \\
       -d '{\"enode-url\":\"enode://ea26ccaf0867771ba1fec32b3589c0169910cb4917017dba940efbef1d2515ce864f93a9abc846696ebad40c81de7c74d7b2b46794a71de8f95a0d019f494ff3@127.0.0.1:30303\"}' \\
       http://localhost:8890/besu/peers/
  ```"
  {:name ::remove-peer
   :enter (fn [context]
            (let [{{{enode-url :enode-url} :json-params} :request} context
                  context (emplace context connection "admin_removePeer" enode-url)]
              (chain/enqueue* context pedestal/json-rpc)))
   :leave cleanup})

(def read-validators
  "Lists the validators defined in the latest block.

  Example:
  ```shell
  curl -i \\
       -H 'Accept: application/json' \\
       http://localhost:8890/besu/validators/'
  ```"
  {:name ::read-validators
   :enter (fn [context]
            (let [context (emplace context connection "ibft_getValidatorsByBlockNumber" "latest")]
              (chain/enqueue* context pedestal/json-rpc)))
   :leave cleanup})

(def add-validator
  "Proposes adding a validator with the specified address."
  {:name ::add-validator
   :enter (fn [context]
            (let [{{{enode-address :enode-url} :json-params} :request} context
                  context (emplace context connection "ibft_proposeValidatorVote" enode-address true)]
              (chain/enqueue* context pedestal/json-rpc)))
   :leave cleanup})

(def remove-validator
  "Proposes removing a validator with the specified address."
  {:name ::remove-validator
   :enter (fn [context]
            (let [{{{enode-address :enode-url} :json-params} :request} context
                  context (emplace context connection "ibft_proposeValidatorVote" enode-address false)]
              (chain/enqueue* context pedestal/json-rpc)))
   :leave cleanup})

(def send-raw-transaction
  "Sends a signed transaction. A transaction can send ether, deploy a contract, or interact with a contract."
  {:name ::send-raw-transaction
   :enter (fn [context]
            (let [{{{transaction :enode-url} :json-params} :request} context
                  context (emplace context connection "eth_sendRawTransaction" transaction)]
              (chain/enqueue* context pedestal/json-rpc)))
   :leave cleanup})
