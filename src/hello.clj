(ns hello
  (:require [clojure.data.json :as json]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.content-negotiation :as conneg]))

(def supported-types ["text/html" "application/edn" "application/json" "text/plain"])

(def content-neg-intc (conneg/negotiate-content supported-types))

(defn ok
  [body]
  {:status 200
   :body   body})

(defn not-found
  []
  {:status 404
   :body   "Not found!"})

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

(defn greeting-for
  [name]
  (if (empty? name)
    "Hello, world!"
    (str "Hello, " name "!")))

(defn greet
  [request]
  (let [name (get-in request [:query-params :name])]
    {:status 200
     :body (greeting-for name)}))

(def echo
  {:name ::echo
   :enter (fn [context]
            (let [request  (:request context)
                  response (ok request)]
              (assoc context :response response)))})

(def routes
  (route/expand-routes
   #{["/greet" :get [coerce-body content-neg-intc greet] :route-name :greet]
     ["/echo"  :get echo]}))

(def server (atom nil))

(defn create-server []
  (http/create-server
   {::http/routes routes
    ::http/type   :jetty
    ::http/port   8890
    ::http/join?  false}))                                                            ;; <2>

(defn start []
  (swap! server
         (constantly (http/start (create-server))))                                   ;; <3>
  nil)

(defn stop []
  (swap! server http/stop)                                                            ;; <4>
  nil)