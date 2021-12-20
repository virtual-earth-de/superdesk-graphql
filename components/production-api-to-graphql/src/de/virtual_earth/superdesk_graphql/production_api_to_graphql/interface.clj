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

(ns de.virtual-earth.superdesk-graphql.production-api-to-graphql.interface
  (:require [de.virtual-earth.superdesk-graphql.production-api-to-graphql.core :as core]))

(defn superdesk-schema
  "create GraphQL schema for superdesk data"
  [routes superdesk-production-api]
  (core/superdesk-schema routes superdesk-production-api)) 
