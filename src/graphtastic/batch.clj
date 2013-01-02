(ns graphtastic.batch
  (:use graphtastic.commons)
  (:require [graphtastic.index :as index]
            [clojure.walk :as walk])
  (:import (org.neo4j.unsafe.batchinsert BatchInserter BatchInserters LuceneBatchInserterIndexProvider)))

(defonce graphdb (ref nil))
(defonce batch-node-index (ref nil))
(defonce batch-rel-index (ref nil))
(defonce batch-index-provider (ref nil))

(defn stop! []
  (.shutdown @batch-index-provider)
  (.shutdown @graphdb))

(defn start! [path]
  (let [inserter (BatchInserters/inserter path)
        provider (LuceneBatchInserterIndexProvider. inserter)
        node-index (.nodeIndex provider "default_node" {"type" "exact"})
        rel-index (.relationshipIndex provider "default_relationship" {"type" "exact"})]
    (dosync
     (ref-set graphdb inserter)
     (ref-set batch-index-provider provider)
     (ref-set batch-node-index node-index)
     (ref-set batch-rel-index rel-index)))
    (.addShutdownHook (Runtime/getRuntime) (Thread. #(stop!))))

(defn index-node [node props]
  (if (empty? (index/node-keys))
    (.add @batch-node-index node (walk/stringify-keys props))
    (.add @batch-node-index node (walk/stringify-keys (select-keys props (index/node-keys))))))

(defn index-rel [rel props]
  (if (empty? (index/relationship-keys))
    (.add @batch-rel-index rel (walk/stringify-keys props))
    (.add @batch-rel-index rel (walk/stringify-keys (select-keys props (index/relationship-keys))))))

(defn create-node [props]
  (let [node (.createNode @graphdb (walk/stringify-keys props))]
    (index-node node props)
    node))

(defn relate
  ([node1 relname node2] (relate node1 relname node2 {}))
  ([node1 relname node2 props] 
     (let [rel (.createRelationship @graphdb node1 node2 (rel-type relname) (walk/stringify-keys props))]
       (if-not (empty? props)
         (index-rel rel props))
       rel)))

(defn find-nodes [props]
  (.query @batch-node-index (index/lucene-query props)))

(defn flush-indices! []
  (.flush @batch-node-index)
  (.flush @batch-rel-index))