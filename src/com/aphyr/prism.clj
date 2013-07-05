(ns com.aphyr.prism
  (:refer-clojure :exclude [test])
  (:require [clojure.java.io :refer [file as-relative-path]]
            [filevents.core :as fe]
            [clojure.string :as s]
            [clojure.stacktrace :as stacktrace]
            [clojure.test :as test]))

(defonce mutex (Object.))

(defn not-deleted
  [kind file]
  (case kind
    :created  file
    :modified file
    :deleted  nil))

(defn file->ns
  "Returns a namespace symbol corresponding to the file."
  [root f]
  (when f
    (let [path (.getCanonicalPath (file f))]
      (when (re-find #"\.clj$" path)
        (-> path
            str
            ; Strip root prefix
            (s/replace root "")
            ; Drop clj
            (s/replace #".clj$" "")
            ; Underscores to dashes
            (s/replace "_" "-")
            ; Slashes to dots
            (s/replace "/" ".")
            symbol)))))

(defn reload!
  "Reloads a namespace symbol and returns that symbol. If complain? is true,
  prints debugging info on error."
  ([sym] (reload! sym true))
  ([sym complain?]
   (when sym
     (try
       (print "Reloading" sym "...") (flush)
       (require sym :reload)
       (println " done.")
       sym
       (catch Exception e
         (println " failed.")
         (when complain?
           (println "Failed to reload" sym)
           (if (and (.getMessage e) 
                    (re-find #"\.clj\:\d+" (.getMessage e)))
             ; Looks like a line number in a file.
             (println (.getMessage e))
             ; Dunno
             (stacktrace/print-cause-trace e)))
         nil)))))

(defn ensure-trailing-slash
  [s]
  (s/replace s #"([^/])$" "$1/"))

(defn slur
  "Takes a unary function f, and a time in ms. Returns a function g such that
  (g x) causes (f x) to be invoked unless (g x) had been invoked less than dt
  ms ago."
  [dt f]
  ; Maintain a map of args to invocation times.
  (let [ts (atom {})]
    (fn g [x]
      (let [now (System/currentTimeMillis)
            ts  (swap! ts (fn [ts]
                            (let [prev (get ts x)]
                              (if (and prev (< (- now prev) dt))
                                ts
                                (assoc ts x now)))))
            prev (get ts x)]

        ; If we're the first to call, or it's been longer than dt ms, call f.
        (when (or (= now prev)
                  (<= dt (- now prev)))
          (f x))))))

(defn namespace-changed
  "Actually reload a namespace n and call our callback."
  [n f]
  (locking mutex
    (try
      (when-let [n (reload! n)]
        (f n))
      (catch Throwable e

        (println "Failed handling change in" file)
        (.printStackTrace e)))
    (flush)))

(defn watch!
  "Watches directory and reloads clojure files, invoking (f namespace) after
  reloading. Returns a future."
  ([dir f]
   (watch! dir {} f))
  ([dir opts f]
   (let [root (-> opts
                  (get :root dir)
                  file
                  .getCanonicalPath
                  ensure-trailing-slash)
         slurred-handler (slur 500 (fn [n]
                                     (future
                                       (Thread/sleep 100)
                                       (namespace-changed n f))))]

     (fe/watch-dir dir
                   (fn [kind file]
                     (when-let [n (->> (not-deleted kind file)
                                       (file->ns root))]
                       (slurred-handler n)))))))


(defn autotest!
  "Watches directories and re-runs tests after reloading."
  ([]
   (autotest! ["src"] ["test"]))
  ([src-dirs test-dirs]
   ; Source dirs
   (doseq [dir src-dirs]
     (watch! dir (fn [n]
                   (let [test-ns (symbol (str n "-test"))]
                   (if (reload! test-ns false)
                     (test/run-tests test-ns)
                     (println "No test namespace" test-ns
                              "- doing nothing."))))))
   ; Test dirs
   (doseq [dir test-dirs]
     (watch! dir (fn [n]
                     (test/run-tests n))))))
