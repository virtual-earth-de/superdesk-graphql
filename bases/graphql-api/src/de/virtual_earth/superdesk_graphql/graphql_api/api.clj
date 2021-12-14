(ns de.virtual-earth.superdesk-graphql.graphql-api.api
  (:gen-class)
  (:require
   [clojure.core :refer [apply]]
   [de.virtual-earth.superdesk-graphql.graphql-api.core :as core]))

(defn -main [& argv]
  (apply core/main argv) )
