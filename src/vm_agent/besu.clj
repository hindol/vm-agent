(ns vm-agent.besu
  (:require [io.pedestal.interceptor :as intc]
            [io.pedestal.interceptor.chain :as chain]
            [vm-agent.json-rpc :as json-rpc]))

(def connection "http://localhost:8545")

(def interceptor
  (intc/interceptor
   {:name ::interceptor
    :enter (fn [context]
             (-> context
                 (assoc-in [:request :besu-connection] connection)
                 (chain/enqueue* json-rpc/interceptor)))
    :leave (fn [context]
             (update-in context [:request] dissoc :besu-connection))}))

(def read-block-number
  {:name ::read-block-number
   :enter (fn [context]
            (-> context
                (update-in [:request]
                           assoc
                           :json-rpc-method "eth_blockNumber"
                           :json-rpc-params [])
                (chain/enqueue* interceptor)))})