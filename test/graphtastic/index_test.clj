(ns graphtastic.index-test
  (:require [graphtastic.graph :as graph])
  (:use midje.sweet
        clojure.test
        graphtastic.test-helpers
        graphtastic.index))

(fact (lucene-query {:name "morpheus" :type "person"}) => "name:\"morpheus\" AND type:\"person\"")

(defn setup-indices! []
  (index-nodes-on [:name :type]) 
  (index-nodes-on [:from :type :to])
  (index-relationships-on [:from :to])
  (index-relationships-on [:prev :from :next]))



(against-background 
  [(before :contents (setup-indices!))]
  (fact (node-keys) => #{:name :type :from :to})
  (fact (relationship-keys) => #{:from :to :prev :next}))

(deftest index-provision
  (testing "that relationship and node indices are created"
    (with-test-db
      (fact (-> @graph/graphdb .index (.existsForNodes "default_node")) => falsey)
      (fact (-> @graph/graphdb .index (.existsForRelationships "default_node")) => falsey)
      (let [node-index (default-node-index @graph/graphdb)
            rel-index (default-relationship-index @graph/graphdb)]
        (fact (-> @graph/graphdb .index (.existsForNodes "default_node")) => truthy)
        (fact (-> @graph/graphdb .index (.existsForRelationships "default_relationship")) => truthy)))))

(deftest test-hook
  (testing "that indexing hooks should be setup properly and return results"
    (with-test-db
      (reset-indices!)
      (index-nodes-on [:name :type])
      (index-relationships-on [:since :until])
      (hook-events @graph/graphdb)
      (let [gandalf (graph/create-node {:name "gandalf" :type "wizard"})
            frodo (graph/create-node {:name "frodo" :type "hobbit"})
            samwise (graph/create-node {:name "samwise" :type "hobbit"})
            aragorn (graph/create-node {:name "aragorn" :type "human"})
            arwen (graph/create-node {:name "arwen" :type "elf"})
            legolas (graph/create-node {:name "legolas" :type "elf"})]
        
        (graph/relate gandalf :knows frodo)
        (graph/relate gandalf :knows aragorn)
        (graph/relate frodo :friends_with samwise)
        (graph/relate frodo :knows aragorn)
        (graph/relate aragorn :loves arwen)
        (graph/relate aragorn :friends_with legolas)
        (fact (count (into [] (find-nodes @graph/graphdb  {:type "hobbit"}))) => 2)
        (fact (count (into [] (find-nodes @graph/graphdb {:type "elf"}))) => 2)
        (fact (.getId (first (find-nodes @graph/graphdb {:name "aragorn"}))) => (.getId aragorn))))))
            
            
            
      