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

(ns de.virtual-earth.superdesk-graphql.production-api-to-graphql.auth
  (:require [hato.client :as http]
            [tick.core :as time])
  (:import (java.util Base64)
           (java.time Instant)))

;; der nutzer übergibt immer url/clientname/clientpass,
;; wir machen daraus das JWT und die Abfrage per JWT
;; außerdem speichern wir das JWT
;; in einem Atom unter dem Schlüssel URL#clientname#clientpass
;; um es wiederzufinden. Da wird auch der Timeout gespeichert,
;; und wenn der am ablaufen ist wird ein neues token geholt
;; multithreading: wie stelle ich sicher, dass nicht zwei
;; threads ein neues token holen?
;; -> ein promise anstoßen oder wie oder was?
;; auch das könnten zwei threads gleichzeitig machen....
;; fuck, brauche ich da ein lock?

(defonce connections (atom {}))


(defn renew_time
  "calculate preferred expiration time in unix time, 5 min before actual expiration.
  Should this be configurable?"
  [expires_in]

  (let [five_min (* 5 60)
        renew_in (- expires_in five_min)
        current_time (.getEpochSecond (Instant/now))]

    (+ current_time
       renew_in)))

(defn renew? [renew_at]
  (< renew_at (.getEpochSecond (Instant/now))))


;; typischer resolver bzw jeder Zugriff auf superdesk:
;; (hato/get ... {:oauth-token (oauth-token-for connInfo})
;; mit connInfo eine map {:tokenrequest-url "https://someserver.net/api" :clientId "id" :pass "pass"}
;; und das oauth-token-for macht all das token holen und auffrischen.
;; das ist also im Grunde alles, was ich hier definieren muß

(defn get-token
  "simply get the token response from endpoint"
  [token-endpoint]
  (let [tokenresponse (http/post
                       (:token-url token-endpoint)
                       {:form-params {:grant_type "client_credentials"}
                        :as :json :coerce :always
                        :basic-auth {:user (:user token-endpoint)
                                     :pass (:pass token-endpoint)}})]
    ;; should *really* add some error processing here ;)
    (:body tokenresponse)))

(defn get-and-store-token

  [api-endpoint endpointhash]

  nil
  )

(defn fresh?
  [time]
  (> time (Instant/now)))

(defn stored-token
  "return oauth token if we have it cached and it is still fresh"
  [endpointhash]
  (if-let [tokenmap (get @connections endpointhash false)]
    (if (renew? (:expires_at tokenmap))
      false
      (:token tokenmap))))

(defn make-hash
  [something]
  "blablubb FIXME")

(defn oauth-token-for
  "Get oauth token from token endpoint"
  [api-endpoint]
  (let [endpointhash (make-hash api-endpoint)]
    (if-let [token (stored-token endpointhash)]
      token
      (get-and-store-token api-endpoint endpointhash))))

(comment

  (def testatom (atom {}))
  @testatom
  (swap! testatom assoc :ttl 430)

  (let [now (Instant/now)]
    (.after? (.plusSeconds now 5) now)) 

  (.plusSeconds (Instant/now) 5)
  
  (atom 0)

  (def result (:body
               (http/post "https://superdesk.literatur.review/api/auth_server/token"
                          {:form-params {:grant_type "client_credentials"}
                           :as :json :coerce :always
                           :basic-auth {:user "61a2666419b8361ae639d5f9"
                                        :pass "5t9O24UTTtT656cYfmNXXBr06fNIxSlVbZ0ajO3o"}}))) 

  ;; die Gültigkeit des Tokens in Sekunden auf die aktuelle Zeit in Sekunden aufschlagen (minus X sekunden)
  ;; und das ans result dranhängen.
  ;; darüber kann dann jeder neue Zugriff prüfen, ob wir ein neues Ticket brauchen.

  )
