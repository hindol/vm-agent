(ns vm-agent.besu
  (:require [io.pedestal.interceptor.chain :as chain]
            [vm-agent.json-rpc :as json-rpc]))

(def connection
  "HTTP[S] URL of the Besu client."
  "http://localhost:8545")

(def read-block-number
  "Returns the index corresponding to the block number of the current chain head."
  {:name ::read-block-number
   :enter (fn [context]
            (chain/enqueue* context (json-rpc/interceptor "eth_blockNumber")))})

(def read-accounts
  "Returns a list of account addresses that the client owns."
  {:name ::read-accounts
   :enter (fn [context]
            (chain/enqueue* context (json-rpc/interceptor "eth_accounts")))})

(def read-peers
  "Returns networking information about connected remote nodes."
  {:name ::read-peers
   :enter (fn [context]
            (chain/enqueue* context (json-rpc/interceptor "admin_peers")))})

(def add-peer
  "Adds a static node.

  Example:
  ```shell
  curl -i http://localhost:8890/besu/peers/ \
  -X POST \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{\"enode-url\":\"enode://ea26ccaf0867771ba1fec32b3589c0169910cb4917017dba940efbef1d2515ce864f93a9abc846696ebad40c81de7c74d7b2b46794a71de8f95a0d019f494ff3@127.0.0.1:30303\"}'
  ```"
  {:name ::add-peer
   :enter (fn [context]
            (let [enode-url (get-in context [:request :json-params :enode-url])]
              (chain/enqueue* context (json-rpc/interceptor "admin_addPeer" enode-url))))})

(def remove-peer
  "Removes a static node.

  Example:
  ```shell
  curl -i http://localhost:8890/besu/peers/ \
  -X DELETE \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{\"enode-url\":\"enode://ea26ccaf0867771ba1fec32b3589c0169910cb4917017dba940efbef1d2515ce864f93a9abc846696ebad40c81de7c74d7b2b46794a71de8f95a0d019f494ff3@127.0.0.1:30303\"}'
  ```"
  {:name ::remove-peer
   :enter (fn [context]
            (let [enode-url (get-in context [:request :json-params :enode-url])]
              (chain/enqueue* context (json-rpc/interceptor "admin_removePeer" enode-url))))})

(def read-validators
  "Lists the validators defined in the latest block."
  {:name ::read-validators
   :enter (fn [context]
            (chain/enqueue* context
                            (json-rpc/interceptor "ibft_getValidatorsByBlockNumber" "latest")))})

(def add-validator
  "Proposes adding a validator with the specified address."
  {:name ::add-validator
   :enter (fn [context]
            (let [enode-address (get-in context [:request :json-params :enode-address])]
              (chain/enqueue* context (json-rpc/interceptor "ibft_proposeValidatorVote" enode-address true))))})

(def remove-validator
  "Proposes removing a validator with the specified address."
  {:name ::remove-validator
   :enter (fn [context]
            (let [enode-address (get-in context [:request :json-params :enode-address])]
              (chain/enqueue* context (json-rpc/interceptor "ibft_proposeValidatorVote" enode-address false))))})
