(ns graphtastic.test-helpers
  (:require [graphtastic.graph :as graph])
  (:import org.neo4j.test.TestGraphDatabaseFactory))

(defmacro with-test-db [& body]
  `(do
     (graph/set-db (.newGraphDatabase (.newImpermanentDatabaseBuilder (TestGraphDatabaseFactory.))))
     (try
       ~@body
       (finally (graph/stop!)))))