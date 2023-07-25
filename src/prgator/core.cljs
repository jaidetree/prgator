(ns prgator.core
  (:require
    [promesa.core :as p]
    [framework.middleware :as mw]
    [framework.server :refer [server]]
    [framework.env :as env]
    [prgator.routes.root :refer [status-pages]]
    ["express$default" :as express]))

(defn middleware
  []
  (p/-> (#'mw/wrap-default-view)
        (#'mw/wrap-router)
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
