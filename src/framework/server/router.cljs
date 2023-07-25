(ns framework.server.router
  (:require [clojure.string :as s]))

(defonce routes-ref (atom {}))

(defn path-str->seq
  [path-str]
  (->> (s/split path-str #"/")
       (drop 1)
       (vec)))


(defn def-route
  [route-path handler]
  (swap! routes-ref assoc route-path
         [(path-str->seq route-path)
          handler]))

(defn routes
  []
  @routes-ref)

(defn match-paths
  [paths route-paths]
  (if (not= (count paths) (count route-paths))
    nil
    (loop [i 0
           ret {:params {}}]
      (if (> i (dec (count route-paths)))
        ret
        (let [expected (nth route-paths i)
              actual   (nth paths i)]
          (cond
            ;; Dynamic var
            (s/starts-with? expected ":")
            (recur
              (inc i)
              (assoc-in ret
                        [:params (keyword (subs expected 1))]
                        actual))

            ;; Literal match
            (= expected actual)
            (recur
              (inc i)
              ret)

            ;; No match
            :else
            nil))))))


(defn path-matcher
  [current-path]
  (let [paths (path-str->seq current-path)]
    (fn [[route-paths handler :as route]]
      (let [result (match-paths paths route-paths)]
        (when result
          {:handler handler
           :params (:params result)})))))


(defn render-route
  [req {:keys [handler params]}]
  (let [req (assoc req :params params)]
    (handler req)))


(defn route-url
  [f]
  (fn [req]
    (->> (routes)
         (vals)
         (keep (path-matcher (:path req)))
         (first)
         (f req))))

(comment
  @routes-ref
  (reset! routes-ref {})
  (match-paths
    ["/"]
    ["/"])

  ((path-matcher "/webhooks/slack")
   [[] (fn [] nil)])

  (->> (routes)
       (vals)
       (keep (path-matcher "/webhooks/slack"))
       (first)))

