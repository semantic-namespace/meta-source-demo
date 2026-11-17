(ns shop.schema
  "The shapes of the shop's data keys, as a malli registry.")

(def registry
  {:user/email      [:re #".+@.+"]
   :line/sku        :string
   :line/qty        pos-int?
   :order/lines     [:vector [:map :line/sku :line/qty]]
   :order/id        :string
   :order/state     :keyword
   :payment/receipt :string})
