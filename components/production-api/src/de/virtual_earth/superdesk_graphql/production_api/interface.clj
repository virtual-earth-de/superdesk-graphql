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

(ns de.virtual-earth.superdesk-graphql.production-api.interface
  (:require [de.virtual-earth.superdesk-graphql.production-api.core :as core]))

(defn init
  "Get oauth token from Superdesk, store it in an atom, create http-client and put
  it in the same atom, as well as all the endpoint info (so we can regenerate
  the token"
  [endpoint]
  (core/init endpoint))

(defn item-by-guid
  "Get item by guid"
  [conn guid]
  (core/item-by-guid conn guid))

(defn items-by-query
  "Send query for items, args are conn and query as edn which will be turned into json"
  [conn query]
  (core/items-by-query conn query))

(defn users-list
  "List all users"
  [conn]
  (core/users-list conn))

(defn user-by-id
  "Return user for id"
  [conn id]
  (core user-by-id conn id))
