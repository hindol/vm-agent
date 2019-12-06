(ns vm-agent.content-negotiation
  (:require [cheshire.core :as json]
            [io.pedestal.http.content-negotiation :as conneg]))

(def supported-types ["text/html" "application/edn" "application/json" "text/plain"])

(def negotiate
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
