;==============================================================================
; Namespace and imports
;==============================================================================

(ns simple-blog.models.post
  (:refer-clojure :exclude [sort find])
  (:use [monger.collection :only [find-maps insert update] :as mgcol]
        [monger.query :only [with-collection find fields paginate] :as mq]
        [clojure.string :only [lower-case trim join replace] :as cljstr])
  (:require [noir.session :as session]))

;==============================================================================
; Data types
;==============================================================================

(def
  ^{:doc "Current domain."}
   PAGE_ROOT "http://127.0.0.1")


(defn date 
  [datetime]
  ^{:doc "Takes a java.util.Date. object and returns a more Clojuric data
         structure. This works nicely with any sort of mongo query."}
  (zipmap [:year :month :day :hour :minute :second]
          ; Maps the format of a Java date time object over a list of strings
          ; that represent Java date and time patterns.
          (map (fn
                 [indicator]
                 (.format (java.text.SimpleDateFormat. indicator) datetime))
               ["yyyy" "M" "d" "H" "m" "s"])))

(defn now []
  ^{:doc "Current time."}
  (date (java.util.Date.)))

(defn gen-slug
  [title]
  ^{:doc "Generates seo-friendly slug."}
  (-> title
      ; Remove non-whitespace special characters.
      (cljstr/replace  #"[^a-zA-Z0-9\ ]+" "")
      ; Replace whitespace with dashes.
      (cljstr/replace  #"\s+" "-")
      ; Make all characters lowercase.
      (cljstr/lower-case)
      ; Trim anything that was missed.
      (cljstr/trim)))

;==============================================================================
; Queries
;==============================================================================

(defn get-user
  [username]
  ^{:doc "Retrieves user information from the database."}
  (first (mgcol/find-maps "users" {:username username})))

(defn user-available?
  [username]
  ^{:doc "Checks if user can be added. Effectively the opposite of
         (user-exists?)."}
  (= (get-user username) nil))

(defn user-exists?
  [username]
  ^{:doc "Checks if user exists in the database"}
  (not (user-available? username)))

(defn valid-credentials?
  [username password]
  ^{:doc "Checks to see if user credentials are valid."}
  (if (and
        ;Does the user exist?
        (user-exists? username)
        ;Is the password valid?
        ;TODO encrypt passwords
        (= ((get-user username) :password) password))
    true
    false))

(defn post-permalink
  [year month slug]
  ^{:doc "Generates permalink for current page."}
  (join "/" year month slug))

(defn get-pages
  [page_num length]
  ^{:doc "Returns a blog listing."}
  (mq/with-collection "posts"
                     (mq/find {})
                     (mq/fields [:title :body :username :timestamp :slug])
                     (mq/paginate :page page_num :per-page length)))

(defn get-post
  [year month slug]
  ^{:doc "Gets post from query items."}
  (first (mgcol/find-maps "posts" {"timestamp.year" year 
                                   "timestamp.month" month 
                                   "slug" slug})))

(defn get-comments
  [id]
  (mgcol/find-maps "posts" {:_id id} [:comments]))

;==============================================================================
; Sessions
;==============================================================================

(defn current-user []
  ^{:doc "Gets the user currently logged in."}
  (session/get :username))

(defn login!
  [username password]
  ^{:doc "Logs user into session"}
  ; Redundant valid-credential checking.
  (if (valid-credentials? username password)
      (session/put! :username username)))

;==============================================================================
; Inserts
;==============================================================================

(defn add-user?
  [username password]
  ^{:doc "Adds new user to the database"}
  ; TODO encrypt passwords
    (mgcol/insert "users" {:username username :password password})
    true)

(defn add-post!
  [title body]
  ^{:doc "Adds a new post to the database."}
  (let [now (now)]
    (mgcol/insert "posts" {:title title
                           :body body 
                           :username (current-user)
                           :timestamp now
                           :slug (gen-slug title)})))

(defn add-comment!
  [id title name email body]
  ^{:doc "Adds comment to a post."}
  (mgcol/update "posts" {:_id id} {"$push" {:comments {:title title
                                                        :name name
                                                        :email email
                                                        :body body
                                                        :timestamp (now)}}}))
