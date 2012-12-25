(defproject graphtastic "0.1.0-SNAPSHOT"
  :description "Clojure Neo4j Library"
  :url "http://github.com/vagmi/graphtastic"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[commons-io/commons-io "2.4"]
                 [org.clojure/clojure "1.4.0"]
                 [org.neo4j/neo4j "1.8.1"]]
  :profiles {:dev {:dependencies [[midje "1.2.0"]
                                  [org.neo4j/neo4j-kernel "1.8.1" :classifier "tests"]]
                   :plugins [[lein-midje "2.0.3"]]}})
