(ns vm-agent.main
  (:require [clojure.core.async :as async]
            [clojure.data.json :as json]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.content-negotiation :as conneg]
            [io.pedestal.test :as test]
            [vm-agent.web3j :as web3j]
            [vm-agent.json-rpc :as json-rpc]
            [vm-agent.besu :as besu])
  (:gen-class))

(def supported-types ["text/html" "application/edn" "application/json" "text/plain"])

(def content-neg-intc (conneg/negotiate-content supported-types))

(defn accepted-type
  [context]
  (get-in context [:request :accept :field] "text/plain"))

(defn transform-content
  [body content-type]
  (case content-type
    "text/html" body
    "text/plain" body
    "application/edn" (pr-str body)
    "application/json" (json/write-str body)))

(defn coerce-to
  [response content-type]
  (-> response
      (update :body transform-content content-type)
      (assoc-in [:headers "Content-Type"] content-type)))

(def coerce-body
  {:name  ::coerce-body
   :leave (fn [context]
            (cond-> context
              (nil? (get-in context [:response :headers "Content-Type"]))
              (update-in [:response] coerce-to (accepted-type context))))})

(defn response
  [status body & {:as headers}]
  {:status  status
   :body    body
   :headers headers})

(def ok       (partial response 200))
(def created  (partial response 201))
(def accepted (partial response 202))

(def echo
  {:name ::echo
   :enter (fn [context]
            (let [response (ok context)]
              (assoc context :response response)))})

(def read-block-number
  {:name ::read-block-number
   :enter (fn [context]
            (async/go
              (assoc context
                     :response
                     (ok (str (web3j/get-block-number @web3j/connection))))))})

(def read-accounts
  {:name ::read-accounts
   :enter (fn [context]
            (async/go
              (assoc context
                     :response
                     (ok (web3j/get-accounts @web3j/connection)))))})

(def read-validators
  {:name ::read-validators
   :enter (fn [context]
            (async/go
              (update-in context [:request]
                         assoc
                         :json-rpc-method "ibft_getSignerMetrics"
                         :json-rpc-params ["1" "100"])))})

(def routes
  (route/expand-routes
   #{["/block-number"   :get    [coerce-body content-neg-intc besu/read-block-number]]
     ["/public-key"     :get    echo :route-name :read-public-key]
     ["/accounts"       :get    [read-accounts]]
     ["/peers"          :get    echo :route-name :read-peers]
     ["/peers/:id"      :put    echo :route-name :add-peer]
     ["/validators"     :get    echo :route-name :read-validators]
     ["/validators/:id" :put    echo :route-name :add-validator]
     ["/validators/:id" :delete echo :route-name :remove-validator]}))

(def service-map
  {::http/routes routes
   ::http/type   :jetty
   ::http/port   8890})

(defn start
  []
  (web3j/connect)
  (http/start (http/create-server service-map)))

;; For interactive development
(defonce server (atom nil))

(defn start-dev
  []
  (web3j/connect)
  (reset! server
          (http/start (http/create-server
                       (assoc service-map
                              ::http/join? false)))))

(defn stop-dev
  []
  (http/stop @server)
  (web3j/disconnect))

(defn restart
  []
  (stop-dev)
  (start-dev))

(defn test-request
  [verb url]
  (io.pedestal.test/response-for (::http/service-fn @server) verb url))

(defn -main
  [& _]
  (start))