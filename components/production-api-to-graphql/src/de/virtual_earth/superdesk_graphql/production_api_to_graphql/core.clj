(ns de.virtual-earth.superdesk-graphql.production-api-to-graphql.core
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [io.pedestal.http :as http]
            [com.walmartlabs.lacinia.pedestal2 :as lp]
            [com.walmartlabs.lacinia.util :as util]
            [com.walmartlabs.lacinia.schema :as schema])
  )

(defn ^:private resolve-hello
  [context args value]
  "eello, Clojurians!")

(defn superdesk-schema
  "create GraphQL schema for superdesk data"
  [schema-file-name]
  (-> (io/resource schema-file-name)
      slurp
      edn/read-string
      (util/inject-resolvers {:queries/hello resolve-hello})
      schema/compile)) 

