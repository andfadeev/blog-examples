(ns com.andreyfadeev.test-helper.wiremock
  (:require [org.httpkit.client :as http]
            [freeport.core :refer [get-free-port!]]
            [clojure.string :as str]
            [jsonista.core :as json])
  (:import (com.github.tomakehurst.wiremock WireMockServer)
           (com.github.tomakehurst.wiremock.core WireMockConfiguration)))

(def object-mapper (json/object-mapper {:decode-key-fn true}))

;; Server helpers
(defn wire-mock-config
  ([] (wire-mock-config (get-free-port!)))
  ([port] (.port (WireMockConfiguration/options) port)))

(defn wire-mock-server
  ([] (wire-mock-server (wire-mock-config)))
  ([config] (atom (new WireMockServer config))))

(defn start
  [^WireMockServer wire-mock-server]
  (.start wire-mock-server))

(defn stop
  [^WireMockServer wire-mock-server]
  (.stop wire-mock-server))

(defn service-mock-base-url
  [wire-mock-server-atom service-name]
  (str/join ["http://localhost:"
             (.portNumber (.getOptions @wire-mock-server-atom))
             "/"
             (name service-name)]))

;; WireMock API
(defn url [wire-mock-server path]
  (.url wire-mock-server path))

(defn reset-url [wire-mock-server]
  (url wire-mock-server "/__admin/reset"))

(defn mappings-url [wire-mock-server]
  (url wire-mock-server "/__admin/mappings"))

(defn requests-url [wire-mock-server]
  (url wire-mock-server "/__admin/requests"))

(defn unmatched-url [wire-mock-server]
  (url wire-mock-server "/__admin/requests/unmatched"))

(defn reset [wire-mock-server]
  @(http/post (reset-url wire-mock-server)))

(defn configure-mocks-on
  [wire-mock-server-atom mocks]
  (doseq [mock mocks]
    (let [response (-> (mappings-url @wire-mock-server-atom)
                       (http/post {:body (json/write-value-as-string mock)})
                       (deref))]
      (when-not (= (:status response) 201)
        (-> "Error while adding stub to WireMock server"
            (ex-info response)
            (throw))))))

(defn get-mappings-from
  [wire-mock-server-atom]
  (-> (mappings-url @wire-mock-server-atom)
      (http/get)
      (deref)
      :body
      (json/read-value object-mapper)))

(defn get-requests-from
  [wire-mock-server-atom]
  (-> (requests-url @wire-mock-server-atom)
      (http/get)
      (deref)
      :body
      (json/read-value object-mapper)))

(defn verify-no-unmatched
  [wire-mock-server-atom]
  (let [body (-> (unmatched-url @wire-mock-server-atom)
                 (http/get)
                 (deref)
                 :body
                 (json/read-value object-mapper))]
    (when (seq (:requests body))
      (throw (ex-info "There were unmatched requests" body)))))

;; Fixtures
(defn with-wire-mock-server
  [wire-mock-server-atom]
  (fn [f]
    (try
      (start @wire-mock-server-atom)
      (f)
      (finally
        (stop @wire-mock-server-atom)))))

(defn with-empty-wire-mock-server [wire-mock-server-atom]
  (fn [f]
    (reset @wire-mock-server-atom)
    (f)))

(defn with-verify-no-unmatched
  [wire-mock-server-atom]
  (fn [f]
    (f)
    (verify-no-unmatched wire-mock-server-atom)))