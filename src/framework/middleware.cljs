(ns framework.middleware
  (:require
    [nbb.core :as nbb]
    [cljs.pprint :refer [pprint]]
    [clojure.string :as s]
    [promesa.core :as p]
    ["fs/promises" :as fs]
    ["path" :as path]
    [framework.server.router :as router]
    [framework.utils :refer [urlpath->filepath file-exists?]]
    [reagent.dom.server :as rdom]
    [framework.server.mime-types :refer [mime-types]]))

(defn wrap-default-view
  []
  (fn [req]
    {:status 404}))

(defn wrap-render-page
  [handler status-pages]
  (fn [req]
    (p/let [res (handler req)]
      (let [status (get res :status 404)]
        (cond (and (<= 200 status) (< status 300) (vector? (:body res)))
              {:headers {:Content-Type "text/html"},
               :status status,
               :body (rdom/render-to-string (:body res))}
              (and (<= 200 status) (< status 300)) res
              (and (<= 300 status) (< status 400)) res
              :else (let [view (get status-pages status)]
                      {:headers {:Content-Type "text/html"},
                       :status status,
                       :body (rdom/render-to-string (view res
                                                          (:data res)))}))))))

(defn wrap-error-view
  [handler]
  (fn [req]
    (-> (p/promise (handler req))
        (p/catch (fn [error] {:status 500, :data {:error error}})))))

(defn wrap-logging
  [handler]
  (fn [req]
    (pprint req)
    (let [res (handler req)]
      (pprint res)
      res)))

(defn wrap-static
  [handler root]
  (fn [req]
    (let [filepath (urlpath->filepath root (:path req))
          ext (subs (.extname path filepath) 1)]
      (p/let [file-exists (file-exists? filepath)]
        (if file-exists
          (p/let [contents (.readFile fs filepath)
                  content-type (get mime-types ext)]
            {:status 200,
             :headers {"Content-Type" content-type},
             :body contents})
          (handler req))))))

(defn json-req-header?
  [req]
  (= (get-in req [:headers "content-type"]) "application/json"))

(defn json-res-header?
  [req]
  (= (get-in req [:headers "Content-Type"]) "application/json"))

(defn json->clj
  [body]
  (-> body
      (js/JSON.parse)
      (js->clj :keywordize-keys true)))

(defn clj->json
  [body]
  (-> body
      (clj->js)
      (js/JSON.stringify nil 2)))

(defn wrap-json
  [handler]
  (fn [req]
    (p/let [req (if (json-req-header? req) (update req :body json->clj) req)
            res (handler req)]
      (if (json-res-header? res) (update res :body clj->json) res))))

(defn wrap-router
  [handler]
  (router/route-url
    (fn [req route]
      (if route
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body (router/render-route req route)}
        (handler req)))))




