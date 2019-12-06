(ns vm-agent.http
  (:require [clj-http.client :as client]))

(defprotocol HttpClient
  "An HTTP client."
  (post [this url request] "Makes an HTTP POST request."))

(defrecord CljHttpClient [options]
  HttpClient
  (post [this url body]
    (client/post url (merge options {:form-params body}))))

; An implementation of [[HttpClient]] that uses `clj-http` to make
; the actual requests. 
(def clj-http
  (->CljHttpClient {:content-type     :json
                    :as               :json
                    :coerce           :always
                    :throw-exceptions false}))
