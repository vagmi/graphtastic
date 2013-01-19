(ns graphtastic.graph
  (:use graphtastic.commons)
  (:require [graphtastic.index :as index])
  (:import org.neo4j.graphdb.factory.GraphDatabaseFactory
           org.neo4j.cypher.javacompat.ExecutionEngine))

(defonce graphdb (ref nil))

(defn set-db [db]
  (dosync (ref-set graphdb db)))

(defn stop! []
  (.shutdown @graphdb))

(defn add-shutdown-hook []
  (.addShutdownHook (Runtime/getRuntime) (Thread. #(stop!))))

(defn start! 
  ([db] (let [newdb (.newEmbeddedDatabase (GraphDatabaseFactory.) db)]
          (set-db newdb)
          (index/hook-events newdb)
          (add-shutdown-hook)))
  ([db options] (let [db-builder (.newEmbeddedDatabaseBuilder (GraphDatabaseFactory.) db)]
                  (.setConfig db-builder options)
                  (set-db (.newGraphDatabase db-builder))
                  (add-shutdown-hook))))

(defmacro with-tx [& body]
  `(let [tx# (.beginTx @graphdb)]
     (try
       (let [val# (do ~@body)]
         (.success tx#)
         val#)
       (finally (.finish tx#)))))

(defn create-node [props]
  (with-tx
    (let [node (.createNode @graphdb)]
      (doseq [[k v] props] (.setProperty node (name k) v))
      node)))

(defn relate 
  ([node1 relname node2] (relate node1 relname node2 {}))
  ([node1 relname node2 props]
     (with-tx
       (let [rel (.createRelationshipTo node1 node2 (rel-type relname))]
         (doseq [[k v] props] (.setProperty rel (name k) v))
         rel))))

(defn query [cypher-query]
  (let [ee (ExecutionEngine. @graphdb)]
    (.execute ee cypher-query)))

(defn find-nodes [props]
  (index/find-nodes @graphdb props))