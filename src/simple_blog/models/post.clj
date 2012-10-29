  (ns simple-blog.models.post
    (:refer-clojure :exclude [sort find])
    (:use [monger.collection :only [find-maps insert] :as mgcol]
          [monger.query :only [with-collection find fields paginate] :as mq])
    (:require [noir.session :as session]))

(defn current-user []
  ^{:doc "Gets the user currently logged in."}
  (session/get :username))

(defn get-user
  ^{:doc "Retrieves user information from the database."}
  [username]
  (first (mgcol/find-maps "users" {:username username})))

(defn user-available?
  ^{:doc "Checks if user can be added. Effectively the opposite of
         (user-exists?)."}
  [username]
  (= (get-user username) nil))

(defn user-exists?
  ^{:doc "Checks if user exists in the database"}
  [username]
  (not (user-available? username)))

(defn valid-credentials?
  ^{:doc "Checks to see if user credentials are valid."}
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
  ^{:doc "Logs user into session"}
  [username password]
  ; Redundant valid-credential checking.
  (if (valid-credentials? username password)
      (session/put! :username username)))

(defn add-user?
  ^{:doc "Adds new user to the database"}
  [username password]
  ; TODO encrypt passwords
    (mgcol/insert "users" {:username username :password password})
    true)

(defn add-post!
  ^{:doc "Adds a new post to the database."}
  [title body]
  (mgcol/insert "posts" {:title title
                         :body body 
                         :username (current-user)
                         :timestamp (java.util.Date.)}))

(defn get-pages
  ^{:doc "Returns a blog listing depending on the pagination settings."}
  [page_num length]
  (mq/with-collection "posts"
                   (mq/find {})
                   (mq/fields [:title :body :username :timestamp])
                   (mq/paginate :page page_num :per-page length)))
