(ns de.virtual-earth.superdesk-graphql.graphql-api.core
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [aero.core :as aero :refer (read-config)]
            [io.pedestal.http :as http]
            [com.walmartlabs.lacinia.pedestal2 :as lp]
            [com.walmartlabs.lacinia.util :as util]
            [com.walmartlabs.lacinia.schema :as schema]
            [de.virtual-earth.superdesk-graphql.production-api-to-graphql.interface :as sdapi])
  (:gen-class))


(defn aero-config [profile]
  ;; this should probably be configurable ;)
  (read-config  "/usr/local/etc/superdesk-to-graphqld.edn" {:profile profile}))

;; hmmm, dies sollte unten im main stehen, aber jetzt für die repl ist es besser hier draußen...
;; da hätte ich gerne noch eine bessere Lösung...

(def config (aero-config :dev))

(def service (lp/default-service
              (sdapi/superdesk-schema "superdesk-graphql-schema.edn")
              {:port (get-in config [:graphql-api :port])
               :host (get-in config [:graphql-api :host])}))

(def runnable-service (http/create-server service))

(defn -main [& argv]
  (http/start runnable-service))


(comment

  config

  (get-in config [:graphql-api :port])

  (read-string (slurp (io/resource "superdesk-graphql-schema.edn")))

  (def schema (superdesk-schema "superdesk-graphql-schema.edn"))

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
