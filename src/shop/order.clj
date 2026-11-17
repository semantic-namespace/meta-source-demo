(ns shop.order
  "An order's life, as a tilakone machine: an order is paid before it ships."
  (:require [tilakone.core :as tk]))

(def fsm
  {::tk/states [{::tk/name ::cart
                 ::tk/transitions [{::tk/on ::checkout ::tk/to ::paying}
                                   {::tk/on ::cancel ::tk/to ::cancelled}]}
                {::tk/name ::paying
                 ::tk/transitions [{::tk/on ::paid ::tk/to ::paid}
                                   {::tk/on ::declined ::tk/to ::cart}]}
                {::tk/name ::paid
                 ::tk/transitions [{::tk/on ::ship ::tk/to ::shiped}]}
                {::tk/name ::shipped}
                {::tk/name ::cancelled}]
   ::tk/state ::cart})

(defn advance
  "The order's next state on `signal`; throws when the machine has no such
   transition."
  [state signal]
  (::tk/state (tk/apply-signal (assoc fsm ::tk/state state) signal)))
