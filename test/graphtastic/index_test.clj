(ns graphtastic.index-test
  (:use midje.sweet
        graphtastic.index))

(fact (lucene-query {:name "morpheus" :type "person"}) => "name:\"morpheus\" AND type:\"person\"")