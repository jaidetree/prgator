(ns prgator.prod
  (:require
    [promesa.core :as p]
    [prgator.core :as pr-gator :refer [app-ref]]))

(defn -main
  []
  (p/let [handler (pr-gator/middleware)]
    (pr-gator/create-server handler)))


(comment
  (-main))
