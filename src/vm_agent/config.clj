(ns vm-agent.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]))

(defn- str->int
  "Wrapper around Java's [[Integer/parseInt]]."
  [s]
  (Integer/parseInt s))

(defn- try-parse-int
  "Unlike [[Integer/parseInt]], returns `nil` instead of throwing [[NumberFormatException]]
  if parsing fails."
  [s]
  (try
    (str->int s)
    (catch NumberFormatException _))) ;; Swallow exception

(defn from-env
  []
  {:besu-host (env :besu-host)
   :besu-http-port (try-parse-int (env :besu-http-port))
   :besu-ws-port (try-parse-int (env :besu-ws-port))
   :besu-address-file (env ::besu-address-file)
   :besu-validators-file (env :besu-validators-file)
   :besu-genesis-file (env :besu-genesis-file)})

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
  (log/spyf "config => %s"
            (merge-with discard-nils
                        (from-edn "dev.edn")
                        (from-env))))

(def prod
  (log/spyf "config => %s"
            (from-env)))
