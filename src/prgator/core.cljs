(ns prgator.core
  (:require
    [clojure.pprint :refer [pprint]]
    [promesa.core :as p]
    [framework.middleware :as mw]
    [framework.server :refer [server]]
    [framework.env :as env]
    [prgator.routes.root :refer [status-pages]]
    ["express$default" :as express]))

(defn wrap-slack-challenge
  [handler]
  (fn [req]
    (pprint req)
    (if (and (= (get-in req [:headers "content-type"]) "application/json")
             (= (get-in req [:body :type] "url_verification"))
             (get-in req [:body :challenge]))
      {:status 200
       :headers {:content-type "text/plain"}
       :body (get-in req [:body :challenge])}
      (handler req))))

(defn wrap-logger
  [handler]
  (fn [req]
    (pprint req)
    (handler req)))

(defn middleware
  []
  (p/-> (#'mw/wrap-default-view)
        (#'mw/wrap-router)
        (#'wrap-logger)
        (#'wrap-slack-challenge)
        (#'mw/wrap-static "public")
        (#'mw/wrap-json)
        (#'mw/wrap-error-view)
        (#'mw/wrap-render-page status-pages)))


(defonce app-ref (atom nil))

(defn create-server
  [request-handler]
  (let [app (express)
        port (env/optional :PORT 3000)]
    (doto app (server request-handler))
    (reset! app-ref (.listen app
                             port
                             (fn [] (println "Server started on port" port))))
    nil))
