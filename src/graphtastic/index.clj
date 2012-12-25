(ns graphtastic.index
  (:require [clojure.string :as str]
           [clojure.set :as set]
           [graphtastic.graph :as graph])
  (:import (org.neo4j.graphdb.event TransactionEventHandler TransactionData)))

(defonce default-node-keys (ref (sorted-set)))
(defonce default-relationship-keys (ref (sorted-set)))
(defonce transaction-event-handler (ref nil))

(defn index-nodes-on [keys]
  "Define the keys on which you would like to index nodes on. Can be called multiple times
   (index-nodes-on [:name :type])"
  (dosync
   (ref-set default-node-keys (set/union @default-node-keys (into (sorted-set) keys)))))

(defn index-relationships-on [keys]
  "Define the keys on which you would like to index relationships on. Can be called multiple times
   (index-relationships-on [:name :type])"
  (dosync
   (ref-set default-relationship-keys (set/union @default-relationship-keys (into (sorted-set) keys)))))

(defn node-keys [] @default-node-keys)

(defn relationship-keys [] @default-relationship-keys)

(defn index-prop-entry [index prop-entry]
  (.add index (.entity prop-entry) (.key prop-entry) (.value prop-entry)))

(defn default-node-index [db]
  (-> db .index (.forNodes "default_node" {"type" "exact"})))

(defn default-relationship-index [db]
  (-> db .index (.forRelationships "default_node" {"type" "exact"})))


(defn indexing-handler-for [db]
  (reify TransactionEventHandler
    (beforeCommit [this data]
      (let [node-index (default-node-index db)
            rel-index (default-relationship-index db)]
        (doseq [nodeprop (.assignedNodeProperties data)] 
          (if (contains? @default-node-keys (keyword (.key nodeprop)))
            (index-prop-entry node-index nodeprop)))
        (doseq [relprop (.assignedRelationshipProperties data)] 
          (if (contains? @default-relationship-keys (keyword (.key relprop)))
            (index-prop-entry rel-index relprop)))
        (doseq [node (.deletedNodes data)] (.remove node-index node))
        (doseq [rel (.deletedRelationships data)] (.remove rel-index rel))))
    (afterCommit [this data state] nil)
    (afterRollback [this data state] nil)))

(defn hook-events [db]
  (.registerTransactionEventHandler db (indexing-handler-for db)))

(defn lucene-query [props]
  (str/join " AND " (map (fn [entry]
                           (let [[k v] entry
                                 key (name k)
                                 value (str "\"" v "\"")]
                             (str/join ":" [key value]))) props)))

(defn find-nodes [db props]
  (.query (default-node-index db) (lucene-query props)))