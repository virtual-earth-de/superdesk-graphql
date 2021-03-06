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


(ns de.virtual-earth.superdesk-graphql.graphql-api.core
  (:require
   [aero.core :as aero :refer [read-config]]
   [clojure.pprint :refer [pprint]]
   [com.walmartlabs.lacinia.pedestal2 :as lp]
   [de.virtual-earth.superdesk-graphql.production-api-to-graphql.interface
    :as sd2gql]
   [integrant.core :as ig]
   [io.pedestal.http :as http]))

;; interface integrant with aero: just add integrants readers 
(defmethod aero/reader 'ig/ref
  [opts tag value]
  (ig/ref value))

(defmethod aero/reader 'ig/refset
  [opts tag value]
  (ig/refset value))

(defn aero-config [profile]
  ;; this should probably be configurable ;)
  (read-config  "/usr/local/etc/superdesk-to-graphqld.edn" {:profile profile}))

;; hmmm, dies sollte unten im main stehen, aber jetzt für die repl ist es besser hier draußen...
;; de hätte ich gerne noch eine bessere Lösung...

(def config (aero-config :dev))

(let [path-prefix  (get-in config [:graphql-api :path-prefix] "/")
      api-path     (str path-prefix "api")
      ide-path     (str path-prefix "ide")
      ws-path      (str path-prefix "ws")
      asset-path   (str path-prefix "assets/graphiql")
      graphql-port (get-in config [:graphql-api :port])
      graphql-host (get-in config [:graphql-api :host])
      show-ide     (get-in config [:graphql-api :ide])]
  (def service (lp/default-service (sd2gql/superdesk-schema (:routes config)
                                                            (:superdesk-production-api config))
                                   {:api-path   api-path
                                    :ide-path   ide-path
                                    :ws-path    ws-path
                                    :asset-path asset-path
                                    :port       graphql-port
                                    :host       graphql-host
                                    :graphiql   show-ide})))

(def runnable-service (http/create-server service))

(defn main [& argv]
  (http/start runnable-service))


(comment

  config

  (def schema (sd2gql/superdesk-schema (:superdesk-production-api config)))

  schema

  service

  runnable-service

  (def server (http/start runnable-service))

  server

  (http/stop server)

  (-main) 

  (aero-config :dev) 

  (def service (lp/default-service
                (superdesk-schema "superdesk-graphql-schema.edn")
                nil ))

  )


;; Local Variables:
;; cider-clojure-cli-aliases: "dev:test"
;; End:
