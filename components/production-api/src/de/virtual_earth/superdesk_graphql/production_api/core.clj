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

(ns de.virtual-earth.superdesk-graphql.production-api.core
  (:require [clojure.data.json :as json]
            [clojure.pprint :refer [pprint]]
            [hato.client :as http]
            [tick.core :as time]
            [clojure.string :as str])
  (:import (java.util Base64)
           (java.time Instant)))

(defn epochTimeInc
  "Add arg seconds to epoch time and return result"
  [seconds] (+ (.getEpochSecond (Instant/now)) seconds))

(defn refresh-inc
  "calculate preferred expiration time, 5 min before actual expiration, which should be in seconds.
  Should this be configurable?"
  [expires-in]
  ;; bit overkill here :-D
  (let [five-min (* 5 60)] 
    (- expires-in five-min)))

(defn refresh-now? [refresh-at]
  "Return true if arg (epoch time) is older than 'now' or if it's nil"
  (if refresh-at
    (< refresh-at (.getEpochSecond (Instant/now)))
    true))

(defn get-token
  "Get the token response from endpoint"
  [client endpoint]
  (let [tokenresponse (http/post
                       (:token-url endpoint)
                       {:form-params {:grant_type "client_credentials"}
                        :as :json :coerce :always
                        :http-client client
                        :basic-auth {:user (:client-id endpoint)
                                     :pass (:password  endpoint)}})]
    ;; FIXME should *really* add some error processing here ;)
    (:body tokenresponse)))

(defn refresh-token!
  "Create or update token and store it in conn"
  [conn]
  (let [token (get-token (:httpc @conn) (:endpoint @conn))] 
    (swap! conn
           (fn [conn token] (->
                            conn
                            (assoc :token token)
                            (assoc :refresh-at (epochTimeInc
                                                (refresh-inc (:expires_in token))))))
           token )))

(defn ensure-authenticated-conn!
  "Update or create token if necessary, return connection value"
  [conn]
  "Refresh conn if it's stale, return value of conn"
  (if (refresh-now? (:refresh-at @conn))
    (refresh-token! conn))
  @conn)

(defn papi-get!
  "Send a get with current bearer token"
  [conn args]
  (ensure-authenticated-conn! conn) 
  (let [{:keys [endpoint token]} @conn] 
    ;;this connection can fail for various reasons even if our token is fresh. FIXME: add error handling
    (json/read-str
     (:body (http/get (str (:production-url endpoint) args) {:oauth-token (:access_token token)}))
     :key-fn keyword)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn init
  "Get oauth token from Superdesk, store it in an atom, create http-client and put
  it in the same atom, as well as all the endpoint info (so we can regenerate
  the token later on"
  [endpoint]
  (let [httpc (http/build-http-client {:connect-timeout 10000})
        token (get-token httpc endpoint)
        conn (atom {:endpoint endpoint
                    :httpc httpc} )]
    (ensure-authenticated-conn! conn)
    ;; I need to return the wohl conn/atom,
    ;; so functions using this can hand us back the (stateful) connection atom for updates
    conn))

(defn users-list [conn]
  (papi-get! conn "/users"))

(defn user-by-id [conn id]
  (papi-get! conn (str "/users/" id)))

(defn item-by-guid [conn guid]
  (papi-get! conn (str "/items/" guid)))

(defn items-by-query [conn query]
  "Send query for items, args are conn and query as edn which will be turned into json"
  (:_items (papi-get! conn (str "/items?source=" (json/write-str query)))))

(comment


  (def token-r (:body
                (http/post "https://superdesk.literatur.review/api/auth_server/token"
                           {:form-params {:grant_type "client_credentials"}
                            :as :json :coerce :always
                            :basic-auth {:user "61a2666419b8361ae639d5f9"
                                         :pass "5t9O24UTTtT656cYfmNXXBr06fNIxSlVbZ0ajO3o"}}))) 

  token-r

  (def endpoint {:production-url "https://superdesk.literatur.review/prodapi/v1"
                 :token-url "https://superdesk.literatur.review/api/auth_server/token"
                 :client-id "61a2666419b8361ae639d5f9"
                 :password "5t9O24UTTtT656cYfmNXXBr06fNIxSlVbZ0ajO3o"} )

  (def httpc (http/build-http-client {:connect-timeout 10000}))

  httpc 

  (def wrapped-token (get-token httpc endpoint))

  (def conn (atom {:endpoint endpoint
                   :httpc    httpc}))

  conn

  (ensure-authenticated-conn! conn) 

  conn

  (def realconn (init endpoint))

  realconn

  (def schachnovelle  (item-by-guid conn "90bf03f7-75cf-4404-b3b4-fc4b01c7b272"))
  
  schachnovelle
  
  (:authors schachnovelle)

  (item-authors conn (:authors schachnovelle))
  
  (def einbild (item-by-guid conn "dcd55151-7f97-42e9-a066-07f46cc21694"))

  (user-by-id conn (:parent (first (:authors schachnovelle))))


  
  (def ffox_query  (json/read-str "{\"query\":{\"filtered\":{\"filter\":{\"and\":[{\"not\":{\"term\":{\"state\":\"spiked\"}}},{\"term\":{\"family_id\":\"90bf03f7-75cf-4404-b3b4-fc4b01c7b272\"}},{\"not\":{\"term\":{\"unique_id\":35}}}]}}},\"size\":200,\"from\":0,\"sort\":{\"versioncreated\":\"desc\"}}" :key-fn keyword))

  (def testquery
    { :query
     { :filtered
      { :filter
       { :and [{ :not { :term { :state "spiked" } } }
               { :term { :family_id "90bf03f7-75cf-4404-b3b4-fc4b01c7b272" } }
               { :not { :term { :unique_id 35 } } } ] }
       }
      }, :size 200, :from 0, :sort { :versioncreated "desc" } }
    )

  (def testquery2
    { :query
     { :filtered
      { :filter
       { :and [{ :not { :term { :state "spiked" } } }
               { :not { :term { :unique_id 35 } } } ] }
       }
      }, :size 200, :from 0, :sort { :versioncreated "desc" } }
    )



  (def ffox-items (items-by-query conn ffox_query))

  (def composite-items (items-by-query conn (json/write-str {:query {:filtered {:filter {:not {:term {:state :spiked} }}}}})))

  (def composite-items (items-by-query conn testquery2))

  (def items-not-spiked-query  {:query {:filtered {:filter {:not {:term {:state :spiked}}}}}} )

  (def items-not-spiked-string-query "{\"query\":{\"filtered\":{\"filter\":{\"not\":{\"term\":{\"state\":\"spiked\"}}}}}}")

  (= (json/write-str items-not-spiked-query) items-not-spiked-string-query)

  (def items-not-spiked (items-by-query conn items-not-spiked-string-query))

  (def testq {:query {:term {:type {:value "picture"}}} })

  (def testitems (items-by-query conn (json/write-str testq)))

  composite-items
  
  (:_items (json/read-str (:body composite-items) :key-fn keyword))

  (json/read-str (:body ffox-items))

  (json/read-str (:body (users-list conn)) :key-fn keyword)
  
  (swap! conn (fn [myconn] (-> myconn
                              (assoc :refresh-at (- (.getEpochSecond (Instant/now)) (* 5 60)))
                              )))

  (def query-r (:body
                (http/post "https://superdesk.literatur.review/prodapi/v1/items?source=")) )

  (defn items-query [{:keys [endpoint token]} query]
    (http/get (str "https://superdesk.literatur.review/prodapi/v1/items?source=" (json/write-str {:query {:filtered {:filter {:not {:term {:state :spiked}}}}}})) {:oauth-token (:access_token token) }))

  (items-query @conn nil)
  
  )
