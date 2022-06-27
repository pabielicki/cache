(ns cache.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [org.senatehouse.expect-call :as ec]
            [cache.core :refer [activities]]
            [predis.core :as redis]
            [clojure.data.json :as json]
            [clojure.core.cache.wrapped :as cw]
            [cache.agent :as agent]))

(deftest activities-cache
  (testing "get activities"
    (let [req {:auth-user {:username "test1"}}]
      (testing "no data in cache"
        (is (= 0 (count (get @agent/local-cache "test1"))))
        (is (= nil (redis/get agent/redis-cache "test1")))
        (let [activities (activities req)]
          (is (= 10 (count (:body activities))))
          (is (= (:body activities) (get @agent/local-cache "test1")))))
      (testing "data in local cache"
        (is (= 10 (count (get @agent/local-cache "test1"))))
        (ec/expect-call (:never agent/lookup-activities)
                        (let [activities (activities req)]
                          (is (= (:body activities) (get @agent/local-cache "test1"))))))
      (testing "data only in remote cache"
        (cw/evict agent/local-cache "test1")
        (is (= 0 (count (get @agent/local-cache "test1"))))
        (is (= 10 (count (json/read-str (redis/get agent/redis-cache "test1")))))
        (ec/expect-call (:never agent/load-activities)
                        (let [activities (activities req)]
                          (is (= (:body activities) (json/read-str (redis/get agent/redis-cache "test1"))))
                          (is (= (:body activities) (get @agent/local-cache "test1")))))))))
