(ns dev.mathiasp
  (:require
   [clojure.core :refer [instance?]]
   [clojure.data.json :as json]
   [clojure.walk :as walk]
   [clojure.string :as string]
   [com.walmartlabs.lacinia :as lacinia]
   [de.virtual-earth.superdesk-graphql.graphql-api.core :as apicore]
   [de.virtual-earth.superdesk-graphql.production-api.core :as papicore]
   [de.virtual-earth.superdesk-graphql.production-api-to-graphql.interface
    :as sd2gql]
   [de.virtual-earth.superdesk-graphql.production-api-to-graphql.core
    :as sd2gqlc]
   [hato.client :as http])
  (:import
   (clojure.lang IPersistentMap)
   (java.net URLEncoder URLDecoder)))

;; start server
(apicore/main nil)

;; query superdesk api
(def conn (papicore/init (get-in apicore/config [:superdesk-production-api :endpoint] )))

(papicore/items-by-query
 conn
 {:query {:filtered {:filter {:term {:type :composite}}}}})

(papicore/items-by-query
 conn
 {:query {:filtered {:filter {:term {:anpa_category.qcode :sachbuch-rezension}}}}})

(papicore/items-by-query
 conn
 {:query {:term {:unique_name (URLEncoder/encode "#75" "UTF-8") }}})

(papicore/items-by-query
 conn
 {:query {:multi_match
          {:query "venedig"
           :fields [:headline :abstract :body_html]}}})

(papicore/items-by-query
 conn
 {:query {:match {:headline {:query "venedig"} }}})

(papicore/items-by-query
 conn
 {:query {:query_string {:query "venedig"} }})

(papicore/item-by-path
 conn
 "75-blabla" ) 

(papicore/items-by-query
 conn
 {:query {:bool {:must [{:term {:anpa_category.qcode :home}}
                        {:term {:anpa_category.qcode :index}}
                        {:term {:type :composite}}
                        {:terms {:state [:published :corrected]}}]}}})

(sd2gqlc/index-by-category conn nil {:category "sachbuch-rezension"} nil)

;; walk the item tree and add path
(walk/postwalk-demo [[1 2] [3 4 [5 6]] [7 8]]) 


(defn document-path
  "add document path for URL to item"
  [{:keys [unique_name slugline] :or {unique_name "#XXXXXXXXX" slugline "missing slugline!"} :as item}]
  (let [id (subs unique_name 1)
        slug (URLEncoder/encode slugline "UTF-8")]
    (str id "-" slug)))

(defn id-from-path
  "creates the id/unique_name (numbers until dash w. prepended '#') from document path"
  [path]
  (let [id (first (string/split path #"-" 2))]
    (str "#" id)))

(defn item-with-path
  "add document path to item"
  [item]
  (if (= (type item) clojure.lang.PersistentArrayMap)
    (assoc item :path (document-path item))
    item))

(def itemtree {:slugline "erste Ebene" :unique_name "#1" :refs [{:slugline "erstes doc" :unique_name "#11"}
                                                                {:slugline "zweites doc" :unique_name "#12"}]}) 
(type itemtree) 

(= (type itemtree) clojure.lang.PersistentArrayMap) 

(def itemtree-with-path (walk/postwalk item-with-path itemtree))
itemtree-with-path
(:path itemtree-with-path)  
(def query "aggregations=0&es_highlight=1&projections=%5B%22highlights%22%2C%22_created%22%2C%22_updated%22%2C%22_etag%22%2C%22_type%22%2C%22state%22%2C%22embargo%22%2C%22publish_schedule%22%2C%22broadcast%22%2C%22flags%22%2C%22rewrite_of%22%2C%22rewritten_by%22%2C%22expiry%22%2C%22task%22%2C%22type%22%2C%22linked_in_packages%22%2C%22renditions%22%2C%22item_id%22%2C%22guid%22%2C%22_current_version%22%2C%22lock_action%22%2C%22lock_user%22%2C%22lock_session%22%2C%22genre%22%2C%22source%22%2C%22language%22%2C%22last_published_version%22%2C%22archived%22%2C%22associations%22%2C%22queue_state%22%2C%22alt_text%22%2C%22description_text%22%2C%22rewrite_sequence%22%2C%22correction_sequence%22%2C%22byline%22%2C%22copyrightholder%22%2C%22copyrightnotice%22%2C%22usageterms%22%2C%22groups%22%2C%22deleted_groups%22%2C%22assignment_id%22%2C%22marked_for_user%22%2C%22translated_from%22%2C%22translations%22%2C%22schedule_settings%22%2C%22urgency%22%2C%22headline%22%2C%22marked_desks%22%2C%22associatedItems%22%2C%22versioncreated%22%2C%22scheduledDateTime%22%2C%22updated%22%2C%22ingest_provider%22%2C%22desk%22%2C%22fetchedDesk%22%2C%22used%22%2C%22used_updated%22%2C%22used_count%22%2C%22nestedlink%22%5D&source=%7B%22query%22%3A%7B%22filtered%22%3A%7B%22filter%22%3A%7B%22and%22%3A%5B%7B%22not%22%3A%7B%22term%22%3A%7B%22state%22%3A%22spiked%22%7D%7D%7D%2C%7B%22or%22%3A%5B%7B%22and%22%3A%5B%7B%22term%22%3A%7B%22state%22%3A%22draft%22%7D%7D%2C%7B%22term%22%3A%7B%22original_creator%22%3A%226107ad122f8f94b095279b1b%22%7D%7D%5D%7D%2C%7B%22not%22%3A%7B%22terms%22%3A%7B%22state%22%3A%5B%22draft%22%5D%7D%7D%7D%5D%7D%2C%7B%22not%22%3A%7B%22term%22%3A%7B%22package_type%22%3A%22takes%22%7D%7D%7D%2C%7B%22bool%22%3A%7B%22must_not%22%3A%5B%7B%22term%22%3A%7B%22last_published_version%22%3Afalse%7D%7D%2C%7B%22exists%22%3A%7B%22field%22%3A%22rewritten_by%22%7D%7D%2C%7B%22term%22%3A%7B%22rewritten_by%22%3A%22%22%7D%7D%5D%7D%7D%5D%7D%2C%22query%22%3A%7B%22query_string%22%3A%7B%22query%22%3A%22venedig%22%2C%22lenient%22%3Atrue%2C%22default_operator%22%3A%22AND%22%7D%7D%7D%7D%2C%22sort%22%3A%5B%7B%22versioncreated%22%3A%22desc%22%7D%5D%2C%22size%22%3A50%2C%22from%22%3A0%7D")

(URLDecoder/decode (:path itemtree-with-path)) 
(URLDecoder/decode query ) 

(def p (document-path {:unique_name "#75" :sluline "My nice document öäüß"}))

p 
(URLDecoder/decode (:path p) "UTF-8") 
(string/split (:path p) #"-" 2) 
(id-from-path (:path p))

;;menu code

(sd2gqlc/generate-routes [{:id :test :anpa_category :my-test
                           :submenu
                           [{:id :subtest1}
                            {:id :subtest2}
                            ]} ] true true true )

(def routes (:routes apicore/config)) 

(sd2gqlc/generate-routes routes true true true )

(name :what)

(defn simplify
  "Converts all ordered maps nested within the map into standard hash maps, and
   sequences into vectors, which makes for easier constants in the tests, and eliminates ordering problems."
  [m]
  (walk/postwalk
   (fn [node]
     (cond
       (instance? IPersistentMap node)
       (into {} node)

       (seq? node)
       (vec node)

       :else
       node))
   m))
apicore/config

(def schema (sd2gql/superdesk-schema (:superdesk-production-api apicore/config )))

schema 

(defn q
  [query-string]
  (-> (lacinia/execute schema query-string nil nil)
      simplify))


papicore/schachnovelle

(q "{ item_by_guid(guid: \"90bf03f7-75cf-4404-b3b4-fc4b01c7b272\") { unique_name refs { guid key source type item {slugline guid headline type renditions { original { href width height mimetype} }}} authors { role author {_id display_name byline} } poi {x y} genre {qcode name} headline slugline abstract priority flags {marked_for_sms marked_for_not_publication} }}")

(q "{ user_by_id(_id: \"6123a1ce774a67acc29baac4\") { first_name last_name display_name byline email job_title sign_off } }}")

;; IDEE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
;; kann ich Menüeinträge aus tags simulieren, indem ich in clojure die menuroot/first_level/second_level in einen Baum
;; oder Graphen überführe?
;; und dann (q "{ menuentries () {pages children {pages { guid slug name abstract }  children { pages { guid slug name abstract } children {pages { guid slug name abstract } } }}}" für einen 3-ebenen Menübaum, in dem die Seiten und Untermenüs sind?
;; Die Felder für die Seiten habe ich im Beispiel eingeschränkt, damit es nicht so viele Daten werden...



;; get token from superdesk
(def result (http/post "https://superdesk.literatur.review/api/auth_server/token"
                       {:form-params {:grant_type "client_credentials"}
                        :as :json
                        :basic-auth {:user "61a2666419b8361ae639d5f9"
                                     :pass "5t9O24UTTtT656cYfmNXXBr06fNIxSlVbZ0ajO3o"}})) 

result

(def test (atom {:nix "nix"}))

(swap! test (fn [test was] (-> test
                              (assoc :was was)))
       "was")
test


(json/write-str {:test "hey"})
(json/write-str {:query {:filteres {:filter {:terms {:type [:composite]}}}}})


(def myo
  {:objects
   {:item
    {:description "Superdesk Item"
     :enums {:itemtype
             {:values [:text :composite :picture :audio :video]
              :description "Item type. One of text|composite|picture|audio|video."}}
     :fields {;; identifiers
              :guid {:type (non-null ID)
                     :description "Globally unique id. Using external id for ingested content."}
              :unique_id {:type (non-null ID)
                          :description "Internally unique id."}
              :unique_name {:type (non-null String)
                            :description "Internally unique name. By default same as unique_id."}
              :family_id {:type String
                          :description "Id for all items derived from single item via fetch or copy actions. For ingested items equals to ingest_id."}
              :related_to {:type String
                           :description "Original item id when doing associate metadata action."}
              ;; content metadata
              :headline {:type String}
              :slugline {:type String}
              :byline {:type String}
              :abstract {:type String
                         :description "Perex or lead."}
              :keywords {:type (list String)
                         :description "List of keywords."}
              :word_count {:type Int
                           :description "Wordcount in body_html field."}
              :priority {:type Int}
              :urgency {:type Int}
              :description_text {:type String
                                 :description "Text description of the item. Used for media types."}
              :body_html {:type String}
              :body_text {:type String
                          :description "Text content of the item. Used for preformatted text."}
              :body_footer {:type String
                            :description "Content footer, used for additional information."}
              ;; :dateline {:type FIXME-dict
              ;;            :description "Info about when/where story was written."
              ;; :groups {:type FIXME-dict
              ;;          :description "Package contents in NewsML like format."}
              :media {:type String
                      :description "Binary file reference for media type items."}
              :mediatype {:type String
                          :description "Binary file mime type."}
              ;;             :poi {:type FIXME-dict
              ;;                   :description "Point of Interest on a picture.
              ;; param x:	horizontal offset
              ;; param y:	vertical offset"}
              ;;             :renditions {:type FIXME-dict
              ;;                          :description "

              ;;     Renditions of a media type item:

              ;;     'renditions': {
              ;;         'original': {
              ;;             'href': '...',
              ;;             'width': 1280,
              ;;             'height': 800,
              ;;         },
              ;;         ...
              ;;     }

              ;; "}
              :filemeta_json {:type String
                              :description "JSON encoded filemeta. Avoids storage issues."}
              ;;             :associations {:type FIXME-dict
              ;;                            :description "

              ;;     Embedded items within body text or predefined relations:

              ;;     'associations': {
              ;;         'featured_image': {
              ;;             'type': 'picture',
              ;;             'guid': 'urn:localhost:123',
              ;;             ...
              ;;         }
              ;;     }thx!

              ;; "}
              :alt_text {:type String
                         :description "Alternate text for picture type items."}
              :sms_message {:type String
                            :description "Short summary of an item, can be used for sms/twitter subscribers."}
              ;; item metadata
              :type {:type String
                     :description "Item type. One of text|composite|picture|audio|video."}
              :language {:type String
                         :description "Item language code."}
              :anpa_take_key {:type String}
              :profile {:type String
                        :description "Content profile id"}
              :state {:type String
                      :description "Workflow state.

ContentStates = <class 'superdesk.metadata.item.ContentStates'>
    ContentStates(DRAFT, INGESTED, ROUTED, FETCHED, SUBMITTED, PROGRESS, SPIKED, PUBLISHED, KILLED, CORRECTED, SCHEDULED, RECALLED, UNPUBLISHED, CORRECTION, BEING_CORRECTED)"}
              :revert_state {:type String
                             :description "Previous item state, is updated on every state change."}
              :pubstatus {:type String
                          :description " Publication state.
    PubStatuses = <class 'superdesk.metadata.item.PubStatuses'>
        PubStatuses(USABLE, HOLD, CANCELED)"}
              :signal {:type String
                       :description "Signal information sent to subscriber."} 
              :ednote {:type String
                       :description "Editorial comment."}
              ;; :flags {:type FIXME-dict
              ;;         :description "Various flags."}
              :expiry {:type String ; really a datetime
                       :description "When this item will expire. It updates on every save/send action."}
              ;; Copyright information
              :usageterm {:type String}
              :copyrightnotice {:type String}
              :copyrightholder {:type String}
              :creditline {:type String}
              ;; CVs - These attributes are populated using values from controlled vocabularies.
              :anpa_category {:type (list String)
                              :description "Values from category cv."}
              :subject {:type (list String)
                        :description "Values from IPTC subjectcodes plus from custom cvs."}
              :genre {:type (list String)
                      :description "Values from genre cv."}
              :company_codes {:type (list String)
                              :description "Values from company codes cv."}
              :place {:type (list String)
                      :description "Place where story happened."}
              ;; System - Set/updated by system mostly.
              :_current_version {:type Int
                                 :description "Version of an item, gets incremented on save or publish."}
              :version {:type Int
                        :description "Set by client - used to create items with version 0 which are used as drafts."}
              :firstcreated {:type String
                             :description "When the item was created."}
              :versioncreated {:type String
                               :description "When current version was created."}
              :original_creator {:type ID
                                 :description "User who created/fetched item."}
              :version_creator {:type ID
                                :description "User who created the current version."}
              :lock_user {:type ID
                          :description "User who has current lock on this item."}
              :lock_time {:type String
                          :description "When was the item locked?"}
              :lock_session {:type ID
                             :description "Session id where item was locked. This way it can detect items locked by same user but in different sessions."}
              :template {:type ID
                         :description "Template id if item was created using a template."}
              :published_in_package {:type ID
                                     :description "If item was published as part of a package for the first time this will be set to package id." }
              ;; Ingest - Set on ingest, might be empty for items created in house.

              :ingest_id
              {:type String
               :description "Ingest item id from which item was fetched. For ingested items same as family_id."}
              :ingest_provider
              { :type ID
               :description "Ingest provider id."}
              :source
              {:type String
               :description "Ingest provider source value. Using DEFAULT_SOURCE_VALUE_FOR_MANUAL_ARTICLES config for items created locally."}
              :original_source
              { :type String
               :description "Source value from ingested item." }
              :ingest_provider_sequence
              { :type Int
               :description "Counter for ingest items." }

              
              }}}
   :queries
   {:item_by_guid
    {:type :item
     :description "Access Superdesk item by ID"
     :args {:guid {:type ID}}
     :resolve :query/page-by-id}}})





;; Local Variables:
;; cider-clojure-cli-aliases: "dev:test"
;; End:
