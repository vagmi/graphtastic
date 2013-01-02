(ns graphtastic.test-helpers
  (:use clojure.java.io)
  (:require [graphtastic.graph :as graph])
  (:import org.neo4j.test.TestGraphDatabaseFactory
           java.io.File))

(defmacro with-test-db [& body]
  `(do
     (graph/set-db (.newGraphDatabase (.newImpermanentDatabaseBuilder (TestGraphDatabaseFactory.))))
     (try
       ~@body
       (finally (graph/stop!)))))

(defn get-temp-db-path []
  (let [temp-file (File/createTempFile "temp" (str (System/nanoTime)))
        temp-path (str (.getPath temp-file) ".d")]
    (.mkdir (file temp-path))
    (.delete temp-file)
    temp-path))