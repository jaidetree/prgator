(ns prgator.develop
  (:require
    [promesa.core :as p]
    [prgator.core :as pr-gator :refer [app-ref]]))

(defn handler
  [req]
  (p/let [f (pr-gator/middleware)
          res (f req)]
    res))

(defn -main
  []
  (pr-gator/create-server handler))

(defn restart
  []
  (let [app @app-ref]
    (p/-> (p/do! (new js/Promise
                      (fn [resolve _reject]
                        (if app
                          (do (println "Gracefully shutting down server")
                              (.close app resolve))
                          (resolve))))
                 (println "Restarting server")
                 (-main))
          (p/catch (fn [err] (js/console.error err))))))


(comment
  (+ 1 2)
  (let [app @app-ref] (.close app (fn [] (println "Server closed"))))
  (println "Starting up")
  (-main)
  (restart))
