(ns xtdb-inspector.core
  "Core XTDB inspector ns, start new inspector server or
  get routes to integrate into an existing ring app."
  (:require [org.httpkit.server :as http-kit]
            [compojure.core :refer [context routes GET]]
            [compojure.route :as route]
            [xtdb-inspector.page :as page]
            [xtdb-inspector.page.doc :as page.doc]
            [xtdb-inspector.page.query :as page.query]
            [xtdb-inspector.page.attr :as page.attr]
            [xtdb-inspector.page.tx :as page.tx]
            [xtdb-inspector.page.dashboard :as page.dashboard]
            [ripley.live.context :as context]))


(defn- page [prefix ctx req page-fn]
  (let [ctx (assoc ctx :request req)]
    (page/page-response
     prefix
     ctx
     #(page-fn prefix ctx))))

(defn inspector-handler [xtdb-node prefix]
  (let [ctx {:xtdb-node xtdb-node}]
    (routes
     (context/connection-handler (str prefix "/__ripley-live") :ping-interval 45)
     (context prefix []
              (GET "/doc" req
                   (page prefix ctx req #'page.doc/render-form))
              (GET "/doc/:doc-id" req
                   (page prefix ctx req #'page.doc/render))
              (GET "/query" req
                   (page prefix ctx req #'page.query/render))
              (GET "/query/:query" req
                   (page prefix ctx req #'page.query/render))
              (GET "/attr" req
                   (page prefix ctx req #'page.attr/render))
              (GET "/attr/:keyword" req
                   (page prefix ctx req #'page.attr/render))
              (GET "/attr/:namespace/:keyword" req
                   (page prefix ctx req #'page.attr/render))
              (GET "/tx" req
                   (page prefix ctx req #'page.tx/render))
              (GET "/dashboard" req
                   (page prefix ctx req #'page.dashboard/render-listing))
              (GET "/dashboard/:dashboard" req
                   (page prefix ctx req #'page.dashboard/render))
              (route/resources "/")))))


(defn start [{:keys [port xtdb-node prefix]
              :or {port 3000
                   prefix ""}}]
  {:pre [(some? xtdb-node)]}
  (http-kit/run-server
   (inspector-handler xtdb-node prefix)
   {:port port}))
