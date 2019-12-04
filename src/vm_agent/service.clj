(ns vm-agent.service
  (:require [cheshire.core :as json]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.content-negotiation :as conneg]
            [vm-agent.besu :as besu]))

(def supported-types ["text/html" "application/edn" "application/json" "text/plain"])

(def content-neg-intc
  "An interceptor that does content negotiation with the client.
  Attaches the most acceptable response format to the key :accept in the request map."
  (conneg/negotiate-content supported-types))

(defn accepted-type
  [context]
  (get-in context [:request :accept :field] "text/plain"))

(defn transform-content
  [body content-type]
  (case content-type
    "text/html" body
    "text/plain" body
    "application/edn" (pr-str body)
    "application/json" (json/generate-string body)))

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

(def common-interceptors
  "Common interceptors for every routes."
  [coerce-body content-neg-intc (body-params/body-params)])

(def routes
  (route/expand-routes
   #{["/besu/block-number"         :get    (conj common-interceptors besu/read-block-number)]
     ["/besu/syncing"              :get    (conj common-interceptors besu/syncing)]
     ["/besu/public-key"           :get    (conj common-interceptors besu/read-public-key)]
     ["/besu/enode-url"            :get    (conj common-interceptors besu/read-enode-url)]
     ["/besu/accounts/"            :get    (conj common-interceptors besu/read-accounts)]
     ["/besu/peers/"               :get    (conj common-interceptors besu/read-peers)]
     ["/besu/peers/"               :post   (conj common-interceptors besu/add-peer)]
     ["/besu/peers/"               :delete (conj common-interceptors besu/remove-peer)]
     ["/besu/validators/"          :get    (conj common-interceptors besu/read-validators)]
     ["/besu/validators/"          :post   (conj common-interceptors besu/add-validator)]
     ["/besu/validators/"          :delete (conj common-interceptors besu/remove-validator)]
     ["/besu/send-raw-transaction" :post   (conj common-interceptors besu/send-raw-transaction)]}))

(def service-map
  {::http/routes routes
   ::http/type   :jetty
   ::http/host   "0.0.0.0"
   ::http/port   8890})
