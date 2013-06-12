(ns prism.core
  (:use [ojo.impl :only [create-watch]])
  (:require ojo.watch))

(defn watch!
  "Watches directory and reloads clojure files, invoking (f
  list-of-namespaces) after reloading"
  ([dir f]
   (watch! dir {} f))
  ([dir opts f]
   (let [dir [dir [["*.clj"]]]
         resp (fn [x]
                (prn x))
         opts (merge {:worker-poll-ms 500
                      :worker-count 1
                      :respond resp}
                     opts)
         watch (apply create-watch dir [:create :modify]
                      (mapcat identity opts))]
;     (future (ojo.watch/start-watch watch))
     watch)))

(def cease-watch ojo.watch/cease-watch)
