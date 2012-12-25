(ns graphtastic.commons-test
  (:use midje.sweet
        graphtastic.commons))

(fact (rel-type :knows) => truthy)
(fact (.name (rel-type :knows)) => "knows")