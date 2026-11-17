(ns shop.orders
  "The order service: each function is an entity of the registry
   (registry/shop.edn), linked by :atlas.source/id."
  (:require [shop.order :as order]))

(defn ^{:atlas.source/id :fn.orders/create} create
  [{:keys [db]} {:user/keys [email] :order/keys [lines]}]
  (let [id (str (random-uuid))]
    (swap! db assoc id {:order/id id :user/email email :order/lines lines :order/state ::order/cart})
    {:order/id id :order/state ::order/cart}))

(defn ^{:atlas.source/id :fn.orders/fetch} fetch
  [{:keys [db]} {:order/keys [id]}]
  (select-keys (get @db id) [:order/id :order/lines :order/state]))

(defn ^{:atlas.source/id :fn.orders/notify} notify
  [{:keys [mailer]} {:user/keys [email] :order/keys [id]}]
  (swap! (:outbox mailer) conj {:to email :subject (str "Order " id " paid")}))

(defn ^{:atlas.source/id :fn.orders/pay} pay
  [{:keys [db payments] :as orders} {:order/keys [id]}]
  (let [{:order/keys [state] :as o} (get @db id)
        paid (-> state (order/advance ::order/checkout) (order/advance ::order/paid))]
    (swap! db assoc id (assoc o :order/state paid))
    (notify orders o)
    {:payment/receipt (str (:api-key payments) "/" id) :order/state paid}))
