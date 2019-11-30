(ns vm-agent.web3j
  (:import (org.web3j.protocol Web3j)
           (org.web3j.protocol.http HttpService)))

(defn get-block-number
  [conn]
  (.. conn
      (ethBlockNumber)
      (send)
      (getBlockNumber)))

(defn get-accounts
  [conn]
  (.. conn
      (ethAccounts)
      (send)
      (getAccounts)))

(defonce connection (atom nil))

(defn connect
  []
  (when (nil? @connection)
    (reset! connection
            (Web3j/build (HttpService. "http://localhost:8545"))))
  nil)

(defn disconnect
  []
  (reset! connection nil))