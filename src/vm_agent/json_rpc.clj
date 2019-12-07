(ns vm-agent.json-rpc
  (:require [io.pedestal.interceptor :as intc]
            [vm-agent.http :as http]))

(def ^:private ^:const version
  "JSON-RPC protocol version."
  "2.0")

(defn- wrap-request
  [method params]
  {:jsonrpc version
   :method  method
   :params  params
   :id      1})

(defn- unwrap-response
  [response]
  (-> response
      (select-keys [:status :body])
      (update-in [:body] dissoc :jsonrpc :id)))

(defn- call
  [connection method & params]
  (->> (wrap-request method params)
       (http/post http/clj-http connection)
       (unwrap-response)))

(defn interceptor
  "Generates and returns a Pedestal interceptor for the given JSON-RPC method and its parameters."
  [connection method & params]
  (intc/interceptor
   {:name ::interceptor
    :enter (fn [context]
             (let [response (apply call connection method params)]
               (assoc context :response response)))}))
