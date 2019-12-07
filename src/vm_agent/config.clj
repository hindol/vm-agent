(ns vm-agent.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [environ.core :refer [env]]))

(defn- parse-int
  "Wrapper around Java's [[Integer/parseInt]]."
  [s]
  (Integer/parseInt s))

(defn- try-parse-int
  "Unlike [[Integer/parseInt]], returns `nil` instead of throwing [[NumberFormatException]]
  if parsing fails."
  [s]
  (try
    (parse-int s)
    (catch NumberFormatException _))) ;; Swallow exception

(defn from-env
  []
  {:besu-host (env :besu-host)
   :besu-port (try-parse-int (env :besu-port))})

(defn from-edn
  [path]
  (-> path
      (io/resource)
      (slurp)
      (edn/read-string)))

(defn- discard-nils
  [a b]
  (if (nil? b) a b))

(def dev
  (merge-with discard-nils
              (from-edn "dev.edn")
              (from-env)))
