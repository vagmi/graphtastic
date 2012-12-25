# Graphtastic

[![Build Status](https://travis-ci.org/vagmi/graphtastic.png?branch=master)](https://travis-ci.org/vagmi/graphtastic)

Neo4j is a NoSQL Graph Database designed for high peformance transactional graphs. It is orders of magnitude more performant than relational databases for highly connected datasets where the primary usecase is navigation and path selection between them. Although, social network is the first thing that springs to one's mind when thinking of Neo4j, there are several problems that are amenable to graph solutions. For example, transitive authorizations and permissions can be modelled using graphs.

Graphtastic tries to bring in the awesomeness of Neo4j with the awesomeness of clojure. The traversal framework in Neo4j makes clojure a natural choice for writing expanders, predicate filters and evaluators.

## Usage

Require `graphtastic.graph` into your namespace.

    (ns your.namespace
      (:require [graphtastic.graph :as graph]))

Start your embedded database with the following.
  
    (graph/start! "path/to/db")
  
Create and relate nodes as below.

    (let [gandalf (graph/create-node {:name "gandalf" :type "wizard"})
          frodo (graph/create-node {:name "frodo" :type "hobbit"})
          samwise (graph/create-node {:name "samwise" :type "hobbit"})
          aragorn (graph/create-node {:name "aragorn" :type "human"})
          arwen (graph/create-node {:name "arwen" :type "elf"})
          legolas (graph/create-node {:name "legolas" :type "elf"})]
      (graph/relate gandalf :knows frodo)
      (graph/relate gandalf :knows aragorn)
      (graph/relate frodo :friends samwise)
      (graph/relate frodo :knows aragorn)
      (graph/relate aragorn :loves arwen)
      (graph/relate aragorn :friends legolas)
	  (into [] (graph/find-nodes {:type "hobbit"})))

Graphtastic automatically sets up a node and relationship index. If you do not specify the keys you wish to index on, it would by default index all the node and relationship properties. If you wish to index only certain properties, you can specify them as below. Graphtastic achieves this by setting up a TransactionEventHandler and indexes the nodes and relationships on the beforeCommit hook.

    (require [graphtastic.index :as index])
  
    (index/index-nodes-on [:type])
    (index/index-relationships-on [:from :to])

Neo4j requires you to shutdown the database instance to avoid database corruption. Graphtastic handles this automatically by adding a JVM shutdown hook to gracefully shutdown the database even in case of uncaught exceptions.

## License

Copyright © 2012 Vagmi Mudumbai

Distributed under the Eclipse Public License, the same as Clojure.
