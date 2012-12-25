(ns graphtastic.graph-test
  (:use midje.sweet
        graphtastic.test-helpers
        clojure.java.io
        clojure.test)
  (:require [graphtastic.graph :as graph]
            [graphtastic.index :as index])
  (:import java.io.File 
           org.apache.commons.io.FileUtils 
           org.neo4j.tooling.GlobalGraphOperations
           org.neo4j.helpers.collection.IteratorUtil))

(defn get-temp-db-path []
  (let [temp-file (File/createTempFile "temp" (str (System/nanoTime)))
        temp-path (str (.getPath temp-file) ".d")]
    (.mkdir (file temp-path))
    (.delete temp-file)
    temp-path))

(deftest graph-tests
  
  (testing "Basic lifecycle"
    (let [db-path (get-temp-db-path)]
      (try
        (graph/start! db-path)
        (fact @graph/graphdb => truthy)
        (finally
          (FileUtils/deleteQuietly (file db-path))))))

  (testing "Node creation and relationships"
    (with-test-db
      (let [morpheus (graph/create-node {:type "person" :name "morpheus"})
            trinity (graph/create-node {:type "person" :name "trinity"})
            knows (graph/relate morpheus :knows trinity)
            ggo (GlobalGraphOperations/at @graph/graphdb)]
        (fact morpheus => truthy)
        (fact trinity => truthy)
        (fact knows => truthy)
        (fact (IteratorUtil/count (.getAllNodes ggo)) => 3) ; including the root node
        (fact (IteratorUtil/count (.getAllRelationships ggo)) => 1)
        (fact (IteratorUtil/count (.getAllRelationshipTypes ggo)) => 1)))))

(deftest test-index
  (with-test-db
    (index/reset-indices!)
    (index/hook-events @graph/graphdb)
    (let [frodo (graph/create-node {:name "frodo" :type "hobbit"})
          samwise (graph/create-node {:name "samwise" :type "hobbit"})]
      (fact (count (into [] (graph/find-nodes {:type "hobbit"}))) => 2))))