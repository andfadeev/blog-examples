(ns com.andreyfadeev.wiremock-testing-test
  (:require [clojure.test :refer :all]
            [com.andreyfadeev.test-helper.wiremock :as wiremock]
            [org.httpkit.client :as http]
            [jsonista.core :as json]))

(def object-mapper (json/object-mapper {:decode-key-fn true}))

(let [wire-mock-server (wiremock/wire-mock-server)]
  (use-fixtures
    :once
    (wiremock/with-wire-mock-server wire-mock-server))
  (use-fixtures
    :each
    (wiremock/with-empty-wire-mock-server wire-mock-server)
    ;; Uncomment to see in action: It will throw an exception as
    ;; mock-service-B-unexpected-call-example test has an unexpected call to Wiremock server.
    #_(wiremock/with-verify-no-unmatched wire-mock-server))

  (deftest empty-wiremock-server-test
    (testing "Wiremock server is working and 0 requests received"
      (is (= {:meta {:total 0}
              :requestJournalDisabled false
              :requests []}
             (wiremock/get-requests-from wire-mock-server)))))

  (deftest mock-service-A-direct-call-example-test
    (wiremock/configure-mocks-on
     wire-mock-server
     [{:request {:url "/service-a/health"
                 :method "GET"}
       :response {:status 200
                  :body (json/write-value-as-string {:message "A response OK"})}}])

    (testing "One mapping is registered on Wiremock server"
      (is (= 1 (-> (wiremock/get-mappings-from wire-mock-server)
                   :mappings
                   (count)))))

    (testing "When calling /service-a/health on Wiremock server correct result is returned"
      (is (= {:message "A response OK"}
             (-> (wiremock/service-mock-base-url wire-mock-server :service-a)
                 (str "/health")
                 (http/get)
                 (deref)
                 :body
                 (json/read-value object-mapper))))))

  (deftest mock-service-B-unexpected-call-example
    (testing "Wiremock responses 404 as there is no mapping registered for the service B call"
      (is (= 404 (-> (wiremock/service-mock-base-url wire-mock-server :service-b)
                     (str "/health")
                     (http/get)
                     (deref)
                     :status))))))
