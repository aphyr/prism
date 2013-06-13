(ns leiningen.prism
  (:require [leiningen.core.eval :as eval]
            [leiningen.test :as test]))

(defn prism
  "Automatically run tests when files change.

   Watches for file changes in your :source-paths and runs tests
   in your :test-paths when they change."
  [project]
  ; Run tests
  (try
    (test/test project)
    (catch Throwable t nil))
  
  ; Watch for changes
  (let [[test-paths source-paths] (map project [:test-paths :source-paths])]
    (eval/eval-in-project project
                          `(p/autotest! '~source-paths '~test-paths)
                          '(require '[com.aphyr.prism :as p])))

  (deref (promise)))
