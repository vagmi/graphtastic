(ns graphtastic.batch-test
  (:use midje.sweet
        graphtastic.test-helpers
        clojure.java.io
        clojure.test)
  (:import java.io.File
           org.apache.commons.io.FileUtils
           org.neo4j.tooling.GlobalGraphOperations
           org.neo4j.helpers.collection.IteratorUtil)
  (:require [graphtastic.batch :as batch]
            [graphtastic.graph :as graph]
            [graphtastic.index :as index]))


(defmacro with-batch-db [path & body]
  `(try
     (batch/start! ~path)
     ~@body
     (finally
       (batch/stop!))))

(defmacro with-graph-db [path & body]
  `(try
     (graph/start! ~path)
     ~@body
     (finally
       (graph/stop!))))


(defn insert-million-nodes []
  (doseq [num (range 1000000)]
    (batch/create-node {:num num :type "number"})))

(deftest batch-tests
  (testing "Basic Lifecycle"
    (let [db-path (get-temp-db-path)]
      (with-batch-db db-path
        (fact @batch/graphdb => truthy)
        (batch/flush-indices!))
      (FileUtils/deleteQuietly (file db-path))))

  (testing "Node creation and relationships"
    (let [db-path (get-temp-db-path)]
      (with-batch-db db-path
        (let [morpheus (batch/create-node {:type "person" :name "morpheus"})
              trinity (batch/create-node {:type "person" :name "trinity"})
              knows (batch/relate morpheus :knows trinity)]
          (batch/flush-indices!)
          (fact morpheus => truthy)
          (fact trinity => truthy)
          (fact knows => truthy)))
      (with-graph-db db-path
        (let [ggo (GlobalGraphOperations/at @graph/graphdb)]
          (fact (IteratorUtil/count (.getAllNodes ggo)) => 3) ; including the root node
          (fact (IteratorUtil/count (.getAllRelationships ggo)) => 1)
          (fact (IteratorUtil/count (.getAllRelationshipTypes ggo)) => 1)))))

  (testing "Batch insertion"
    (let [db-path (get-temp-db-path)]
      (with-batch-db db-path
        (insert-million-nodes)
        (batch/flush-indices!))
      (with-graph-db db-path
        (let [ggo (GlobalGraphOperations/at @graph/graphdb)]
          (fact (IteratorUtil/count (.getAllNodes ggo)) => 1000001)))
      (FileUtils/deleteQuietly (file db-path))))

  (testing "indices"
    (let [db-path (get-temp-db-path)]
      (with-batch-db db-path
        (let [frodo (batch/create-node {:name "frodo" :type "hobbit"})
              samwise (batch/create-node {:name "samwise" :type "hobbit"})]
          (batch/flush-indices!)
          (fact (count (into [] (batch/find-nodes {:type "hobbit"}))) => 2)))
      (FileUtils/deleteQuietly (file db-path)))))