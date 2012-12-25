(ns graphtastic.index
  (require [clojure.string :as str]))

(defn lucene-query [props]
  (str/join " AND " (map (fn [entry]
                           (let [[k v] entry
                                 key (name k)
                                 value (str "\"" v "\"")]
                             (str/join ":" [key value]))) props)))