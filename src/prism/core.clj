(ns prism.core
  (:use [clojure.java.io :only [file]])
  (:require [filevents.core :as fe]
            [clojure.string :as s]))

(defn not-deleted
  [kind file]
  (condp = kind
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

(defn reload
  "Reloads a namespace symbol and returns that symbol."
  [sym]
  (when sym
    (require sym :reload)
    sym))

(defn ensure-trailing-slash
  [s]
  (s/replace s #"([^/])$" "$1/"))

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
                  ensure-trailing-slash)]
     (fe/watch-dir dir
                   (comp #(when % (f %))
                         reload
                         (partial file->ns root)
                         not-deleted)))))
