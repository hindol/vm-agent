(ns vm-agent.server
  (:require [io.pedestal.http :as http]
            [io.pedestal.test :as test]
            [vm-agent.service :as service]))

(defn start
  []
  (http/start (http/create-server service/service-map)))

;; For interactive development
(defonce server (atom nil))

(defn start-dev
  []
  (reset! server
          (http/start (http/create-server
                       (assoc service/service-map
                              ::http/join? false)))))

(defn stop-dev
  []
  (http/stop @server))

(defn restart
  []
  (stop-dev)
  (start-dev))

(defn test-request
  [verb url]
  (test/response-for (::http/service-fn @server) verb url))

(defn -main
  [& _]
  (start))
