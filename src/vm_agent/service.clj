(ns vm-agent.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [vm-agent.content-negotiation :as conneg]
            [vm-agent.besu :as besu]))

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
  [conneg/coerce-body conneg/negotiate (body-params/body-params)])

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
