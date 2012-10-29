(ns simple-blog.models.post
  (:refer-clojure :exclude [sort find])
  (:use [monger.collection :only [find-maps insert] :as mgcol]
        [monger.query :only [with-collection find fields paginate] :as mq])
  (:require [noir.session :as session]))

(defn current-user []
  (session/get :username))

(defn get-user
  [username]
  (first (mgcol/find-maps "users" {:username username})))

(defn user-available?
  "Checks if user can be added. Effectively the opposite of (user-exists?)."
  [username]
  (= (get-user username) nil))

(defn user-exists?
  [username]
  "Checks if user exists in the database"
  (not (user-available? username)))

(defn valid-credentials?
  [username password]
  (if (and
        ;Does the user exist?
        (user-exists? username)
        ;Is the password valid?
        ;TODO encrypt passwords
        (= ((get-user username) :password) password))
    true
    false))

(defn login!
  "Logs user into session"
  [username password]
  ; Redundant valid-credential checking.
  (if (valid-credentials? username password)
      (session/put! :username username)))

(defn add-user?
  "Adds user to the database"
  [username password]
  ; TODO encrypt passwords
    (mgcol/insert "users" {:username username :password password})
    true)

(defn add-post!
  [title body]
  (mgcol/insert "posts" {:title title
                         :body body 
                         :username (current-user)
                         :timestamp (java.util.Date.)}))

(defn get-page
  [page_num length]
  (mq/with-collection "posts"
                   (mq/find {})
                   (mq/fields [:title :body :username :timestamp])
                   (mq/paginate :page page_num :per-page length)))
