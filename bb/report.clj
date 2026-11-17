(ns report
  "What meta-source makes of the shop, beyond check: each rule and the
   entities it applies to, and what each entity reaches when every key is
   a plain dependency and when the packs say what their keys mean.

     bb report"
  (:require [meta-source.cli :as cli]
            [meta-source.index :as index]
            [meta-source.rules :as rules]))

(defn- reach-of
  "{id #{reached}} over `idx`'s dependencies (deps, calls, what provides it)."
  [idx]
  (let [view (#'index/relations-view idx)
        view (assoc view :reverse-edges (memoize #(rules/reverse-edges view %)))]
    (into (sorted-map) (for [id (keys (:entities idx))]
                         [id (set (keys (rules/reachable view [:deps :calls :provided-by] id)))]))))

(defn -main [& _]
  (let [idx (index/build (#'cli/config {:root "." :no-cache true}))
        view (#'index/relations-view idx)
        ;; the same index with no key typed: every edge a dependency
        untyped (assoc idx :relations {} :depending #{} :untyped-relations {} :dataflow {:consumes [] :produces []})
        typed-reach (reach-of idx)
        untyped-reach (reach-of untyped)
        total #(reduce + (map count (vals %)))]
    (println "rules, and the entities each applies to:")
    (doseq [{:keys [id rule]} (:rules idx)]
      (println (format "  %-28s %s" id (vec (filter #(rules/holds? view (:when rule) %) (sort (keys (:entities idx))))))))
    (println)
    (println "reach (entity → what it depends on, transitively):")
    (println (format "  every key a dependency: %3d pairs" (total untyped-reach)))
    (println (format "  keys typed by the packs: %3d pairs" (total typed-reach)))
    (println)
    (println (format "  %-24s %8s %8s" "entity" "untyped" "typed"))
    (doseq [[id r] (sort-by (comp - count val) untyped-reach)
            :let [t (get typed-reach id)]
            :when (not= (count r) (count t))]
      (println (format "  %-24s %8d %8d" id (count r) (count t))))))
