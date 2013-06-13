(ns leiningen.prism
  (:require [leiningen.core.eval :as eval]))

(defn prism
  "Automatically run tests when files change.

   Watches for file changes in your :source-paths and runs tests
   in your :test-paths when they change."
  [project]
  (let [[test-paths source-paths] (map project [:test-paths :source-paths])]
    (eval/eval-in-project project
                          `(p/autotest! '~source-paths '~test-paths)
                          '(require '[com.aphyr.prism :as p]))))
