(ns cache.core
  (:require
    [ring.util.http-response :refer [ok internal-server-error]]
    [cache.agent :as agent]))

(defn handle [status response]
  (case status
    200 (ok response)
    500 (internal-server-error response)))

(defn activities [req]
  (let [agent-id (get-in req [:auth-user :username])
        contact-id (agent/get-contact-id nil agent-id)
        [ok? res] (agent/get-activities contact-id)]
    (handle (if ok? 200 500) res)))

(comment
  (activities {:auth-user {:username 2}})
  (agent/get-activities 1)
  )