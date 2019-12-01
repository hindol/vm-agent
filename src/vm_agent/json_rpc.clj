(ns vm-agent.json-rpc
  (:require [clojure.core.async :as async]
            [io.pedestal.interceptor :as intc]
            [clj-http.client :as client]))

(def version "2.0")

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

(defn call
  [connection method & params]
  (->> {:form-params  (wrap-request method params)
        :content-type :json
        :as           :json}
       (client/post connection)
       (unwrap-response)))

(def interceptor
  (intc/interceptor
   {:name ::interceptor
    :enter (fn [context]
             (async/go
               (let [request    (:request context)
                     connection (:besu-connection request)
                     method     (:json-rpc-method request)
                     params     (:json-rpc-params request)
                     response   (apply call connection method params)]
                 (assoc context :response response))))}))