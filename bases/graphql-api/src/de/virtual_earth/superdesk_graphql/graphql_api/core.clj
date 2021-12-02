(ns de.virtual-earth.superdesk-graphql.graphql-api.core
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [aero.core :as aero :refer (read-config)]
            [io.pedestal.http :as http]
            [com.walmartlabs.lacinia.pedestal2 :as lp]
            [com.walmartlabs.lacinia.util :as util]
            [com.walmartlabs.lacinia.schema :as schema])
  (:gen-class))


(defn aero-config [profile]
  ;; this should probably be configurable ;)
  (read-config  "/usr/local/etc/superdesk-to-graphqld.edn" {:profile profile}))

(def config (aero-config :dev))

(defn ^:private resolve-hello
  [context args value]
  "Hello, Clojurians!")

(def superdesk-schema
  (-> (io/resource "superdesk-graphql-schema.edn")
      slurp
      edn/read-string
      (util/inject-resolvers {:queries/hello resolve-hello})
      schema/compile)) 

(def service (lp/default-service
              superdesk-schema
              {:port (get-in config [:graphql-api :port])
               :host (get-in config [:graphql-api :host])}))

(defonce runnable-service (http/create-server service))

(defn -main [& argv]
  (http/start runnable-service))


(comment

  config

  service

  (-main) 
  (def server (start-server false))

  (.stop server) 


  (q1/handler "bla") 
  (aero-config :dev) 

  (testmuu)

  )

