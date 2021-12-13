;; Copyright [2021] [virtual earth Gesellschaft für Wissens re/prä sentation mbH]

;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at

;; http://www.apache.org/licenses/LICENSE-2.0

;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns de.virtual-earth.superdesk-graphql.production-api-to-graphql.core
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.pprint :refer [pprint]]
   [com.walmartlabs.lacinia.schema :as schema]
   [com.walmartlabs.lacinia.util :as util]
   [de.virtual-earth.superdesk-graphql.production-api.core :as sd]))

(defn ^:private item-by-guid
  [conn context args value]
  (sd/item-by-guid conn (:guid args)))

(defn ^:private user-by-id
  [conn context args value]
  (sd/user-by-id conn (:_id args)))

(defn resolver-map
  "Establish connection to superdesk, set up resolver map"
  [endpoint]
  (let [conn (sd/init endpoint)]
    {:query/item-by-guid (partial item-by-guid conn)
     :query/user-by-id (partial user-by-id conn)}))

(defn superdesk-schema
  "create GraphQL schema for superdesk data"
  [config]
  (-> (io/resource (:schema-file-name config))
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map (:endpoint config)))
      schema/compile)) 

