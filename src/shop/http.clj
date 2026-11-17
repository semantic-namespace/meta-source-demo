(ns shop.http
  "The routes, each handed only the component it uses. A handler is built by
   a named fn, so what a route calls can be followed."
  (:require [shop.orders :as orders]))

(defn health [_] {:status 200 :body "ok"})

(defn create-order [svc]
  (fn [{:keys [body-params]}] {:status 201 :body (orders/create svc body-params)}))

(defn fetch-order [svc]
  (fn [{{:keys [id]} :path-params}] {:status 200 :body (orders/fetch svc {:order/id id})}))

(defn pay-order [svc]
  (fn [{{:keys [id]} :path-params}] {:status 200 :body (orders/pay svc {:order/id id})}))

(defn public-routes []
  [["/public/health" {:name :public/health :get {:handler health}}]])

(defn order-routes [svc]
  [["/api/orders" {:name :api.orders/create
                   :post {:handler (create-order svc)
                          :parameters {:body [:map :user/email :order/lines]}
                          :responses {201 {:body [:map :order/id :order/state]}}}}]
   ["/api/orders/:id" {:name :api.orders/fetch
                       :get {:handler (fetch-order svc)
                             :responses {200 {:body [:map :order/id :order/state]}}}}]
   ["/api/orders/:id/pay" {:name :api.orders/pay
                           :post {:handler (pay-order svc)
                                  :responses {200 {:body [:map :payment/receipt :order/state]}}}}]])
