(ns cache.agent
  (:require
    [predis.core :as redis]
    [predis.mock :as mock]
    [clojure.data.json :as json]
    [clojure.core.cache.wrapped :as cw]))


(def local-cache (cw/fifo-cache-factory {}))
(def redis-cache (mock/->redis))

(defn get-contact-id
  "Get contact id from db mock"
  [_ _]
  "test1")

(defn load-activities
  "API call for activities mock"
  [_]
  (Thread/sleep 2000)
  [1 2 3 4 5 6 7 8 9 10])


(defn lookup-activities
  "Check remote cache for activities, if empty, call API and set result in remote cache"
  [remote-cache contact-id]
  (if-let [activities (redis/get remote-cache contact-id)]
    (json/read-str activities)
    (let [activities (load-activities contact-id)]
      (redis/set remote-cache contact-id (json/write-str activities))
      activities)))

(defn get-activities
  "Get activities from local-cache, remote-cache or from API.
  Set found activities if missing in cache"
  [contact-id]
  (if-let [result (cw/lookup-or-miss local-cache contact-id (partial lookup-activities redis-cache))]
    [true result]
    [false []]))

(comment
  (lookup-activities redis-cache 1)
  (get-activities 1)
  (get @local-cache 1)
  (cw/evict local-cache 1)
  (get @local-cache 1)
  (get-activities 1)

  )

