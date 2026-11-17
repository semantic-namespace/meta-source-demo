(ns shop.system
  "Each component's init-key destructures its config and hands each call
   only what it needs, so meta-source reads the system whole."
  (:require [integrant.core :as ig]
            [muuntaja.core :as m]
            [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [ring.adapter.jetty :as jetty]
            [shop.http :as http]))

(defmethod ig/init-key :shop/db [_ {:keys [url]}] (atom {}))
(defmethod ig/halt-key! :shop/db [_ db] (reset! db {}))

(defmethod ig/init-key :shop/payments [_ {:keys [api-key]}] {:api-key api-key})

(defmethod ig/init-key :shop/mailer [_ {:keys [from]}] {:from from :outbox (atom [])})
(defmethod ig/halt-key! :shop/mailer [_ mailer] (reset! (:outbox mailer) []))

(defmethod ig/init-key :shop/orders [_ {:keys [db payments mailer]}]
  {:db db :payments payments :mailer mailer})

(defmethod ig/init-key :shop/router [_ {:keys [orders]}]
  (ring/ring-handler (ring/router [(http/public-routes) (http/status-routes orders) (http/order-routes orders)]
                                  {:data {:muuntaja m/instance :middleware [muuntaja/format-middleware]}})))

(defmethod ig/init-key :shop/server [_ {:keys [port handler]}]
  (jetty/run-jetty handler {:port port :join? false}))
(defmethod ig/halt-key! :shop/server [_ server] (.stop server))
