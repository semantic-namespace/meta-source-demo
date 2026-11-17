(ns shop.main
  (:require [clojure.java.io :as io]
            [integrant.core :as ig]
            [shop.system]))

(defn -main [& _]
  (let [config (ig/read-string (slurp (io/resource "system.edn")))
        system (ig/init config)]
    (.addShutdownHook (Runtime/getRuntime) (Thread. #(ig/halt! system)))
    (println "shop on http://localhost:3000  (Ctrl-C to stop)")))
