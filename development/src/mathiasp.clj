(ns dev.mathiasp
  (:require [de.virtual-earth.superdesk-graphql.graphql-api.core :as base ]
            [de.virtual-earth.superdesk-graphql.production-api-to-graphql.core :as gcore]
            [de.virtual-earth.superdesk-graphql.production-api-to-graphql.interface :as gintf]
            [hato.client :as http])) 

(def schema (gintf/superdesk-schema "superdesk-graphql-schema.edn")) 

schema 


;; get token from superdesk
(def result (http/post "https://superdesk.literatur.review/api/auth_server/token"
                       {:form-params {:grant_type "client_credentials"}
                        :as :json
                        :basic-auth {:user "61a2666419b8361ae639d5f9"
                                     :pass "5t9O24UTTtT656cYfmNXXBr06fNIxSlVbZ0ajO3o"}})) 

result

;; Local Variables:
;; cider-clojure-cli-aliases: "dev:test"
;; End:
