(ns vm-agent.error
  (:require [io.pedestal.interceptor.error :as error]))

(def interceptor
  (error/error-dispatch
   [context ex]

   [{:exception-type :java.net.ConnectException}]
   (assoc context :response {:status 503
                             :body   {:error "Besu not up yet!"}})

   [{:exception-type :java.io.FileNotFoundException}]
   (assoc context :response {:status 404
                             :body   {:error (.getMessage ex)}})

   :else
   (assoc context :io.pedestal.interceptor.chain/error ex)))