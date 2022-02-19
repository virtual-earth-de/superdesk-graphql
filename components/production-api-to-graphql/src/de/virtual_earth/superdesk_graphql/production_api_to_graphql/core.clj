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
   [clojure.data.json :as json]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.pprint :refer [pprint]]
   [clojure.string :as str]
   [com.walmartlabs.lacinia.schema :as schema]
   [com.walmartlabs.lacinia.util :as util]
   [de.virtual-earth.superdesk-graphql.production-api.core :as sd]))

(defn item-by-guid
  [conn context args value]
  (sd/item-by-guid conn (:guid args)))

(defn items-by-query
  [conn context args value]
  (sd/items-by-query conn (:query args)))

(defn all-items
  [conn context args value]
  (sd/items-by-query
   conn
   {:query {:not {:term {:state :spiked}}}}))

(defn items-by-category
  [conn context args value]
  (sd/items-by-query
   conn
   {:query {:bool {:must [{:term {:anpa_category.qcode (:category args)}}
                          {:term {:type :text}}
                          {:terms {:state [:published :corrected]}}]}}}))

(defn items-by-search
  [conn context args value]
  (pprint (:search args))
  (sd/items-by-query
   conn
   {:query {:bool {:must [{:terms {:state [:published :corrected]}}
                          {:query_string {:query (:search args) }}]}}}))

(defn index-by-category
  [conn context args value]
  (sd/items-by-query
   conn
   {:query {:bool {:must [{:term {:anpa_category.qcode (:category args)}}
                          {:term {:anpa_category.qcode :index}}
                          {:term {:type :composite}}
                          {:terms {:state [:published :corrected]}}]}}}))

(defn user-by-id
  [conn context args value]
  (sd/user-by-id conn (:_id args)))

(defn author-role-author
  [conn context args author-role]
  (let [author-id (:parent author-role)]
    (sd/user-by-id conn author-id)))

(defn item-for-ref
  [conn context args ref]
  (sd/item-by-guid conn (:guid ref)))

(defn complete-route
  [route]
  (let [croute
        {:id (name (:id route))
         :name (name (or (:name route) (str/capitalize (name (:id route)))))
         :anpa_category (name (or (:anpa_category route) (:id route)))
         }]
    (if-not (:subroutes route)
      croute
      (assoc croute :subroutes
             (map (fn [route] (complete-route route))
                  (:subroutes route))))))


(defn generate-routes
  "Generate full routes/navigation info from config info."
  [routes _context _args _ref]
  (let [completed-routes (map (fn [route] (complete-route route))
                              routes)]
    completed-routes))

(defn resolver-map
  "Establish connection to superdesk, set up resolver map"
  [routes superdesk-production-api]
  (let [conn (sd/init (:endpoint superdesk-production-api))]
    {:author_role/author (partial author-role-author conn)
     :ref/item-for-ref (partial item-for-ref conn)
     ;; :item/menu-for-item (partial menu-for-item conn) ; depreciated
     :query/routes (partial generate-routes routes)
     :query/item-by-guid (partial item-by-guid conn)
     :query/items-by-query (partial items-by-query conn)
     :query/all-items (partial all-items conn)
     :query/items-by-category (partial items-by-category conn)
     :query/items-by-search (partial items-by-search conn)
     :query/index-by-category (partial index-by-category conn)
     :query/user-by-id (partial user-by-id conn)}))

(defn superdesk-schema
  "create GraphQL schema for superdesk data"
  [routes superdesk-production-api]
  (-> (io/resource (:schema-file-name superdesk-production-api))
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map routes superdesk-production-api))
      schema/compile)) 

