(ns com.aphyr.prism-test
  (:use clojure.test
        com.aphyr.prism))

(deftest slur-test
  (let [a (atom [])
           dt 10
           f (fn [_] (swap! a #(conj % (System/currentTimeMillis))))
           g (slur dt f)]
       (doseq [x (range 1 10000)] (g 1))
       (is (= true (if (> (count @a) 1)
                (let [diffs (reduce #(and %1 %2) (map (partial < dt)(map - (subvec @a 1) @a)))]
                  diffs)
                true)))))
