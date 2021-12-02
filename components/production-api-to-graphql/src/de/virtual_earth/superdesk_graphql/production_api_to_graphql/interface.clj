(ns de.virtual-earth.superdesk-graphql.production-api-to-graphql.interface
  (:require [de.virtual-earth.superdesk-graphql.production-api-to-graphql.auth :as auth]
            [de.virtual-earth.superdesk-graphql.graphql-api.core :as core]))


(defn superdesk-schema
  "create GraphQL schema for superdesk data"
  [schema-file-name]
  core/superdesk-schema schema-file-name) 
