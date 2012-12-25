(ns graphtastic.commons
  (:import org.neo4j.graphdb.DynamicRelationshipType))

(defn rel-type [reltype]
  "Return a relaitonship for clojure keyword e.g., (rel-type :name)"
  (DynamicRelationshipType/withName (name reltype)))