(ns prgator.routes.root
  (:require
    [clojure.pprint :refer [pprint]]
    [framework.utils :refer [pprint-str]]
    [framework.server.router :as router]))

(defn base
  [req {:keys [pages]} & children]
  [:html
   [:head
    [:meta {:name "viewport", :content "width=device-width, initial-scale=1.0"}]
    [:title "PR Gator"]]
   [:body.bg-primary.text-white
    (-> [:div.page]
        (into children))]])

(defn error-404
  [req data]
  [base req data
   [:section [:h1 "404 Not Found"]
    [:p
     "No route handler for this URL"]]])

(defn error-505
  [req {:keys [error], :as data}]
  [base req data
   [:section [:h1 "Hey it's a.. uh... \"oh no\""]
    [:pre (str (ex-message error) "\n" (pprint-str (ex-data error)))]]])

(def status-pages {404 #'error-404, 500 #'error-505})

(router/def-route
  "/webhooks/slack"
  (fn [req]
    (pprint req)
    {:status "ok"
     :message "Processed slack webhook"}))

(comment
  (require '[framework.server.router :as router]))
