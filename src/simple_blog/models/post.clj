  (ns simple-blog.models.post
    (:refer-clojure :exclude [sort find])
    (:use [monger.collection :only [find-maps insert update] :as mgcol]
          [monger.query :only [with-collection find fields paginate] :as mq]
          [clojure.string :only [lower-case trim join] :as cljstr])
    (:require [noir.session :as session]))

(def
  ^{:doc "Current domain."}
   PAGE_ROOT "http://127.0.0.1")

(defn now []
  ^{:doc "Current time."}
  (java.util.Date.))

(defn gen-slug
  [title]
  ^{:doc "Generates seo-friendly slug."}
  (-> title
      ; Remove non-whitespace special characters.
      (replace  #"[^a-zA-Z0-9\ ]+" "")
      ; Replace whitespace with dashes.
      (replace  #"\s+" "-")
      ; Make all characters lowercase.
      (cljstr/lower-case)
      ; Trim anything that was missed.
      (cljstr/trim)))

(defn current-user []
  ^{:doc "Gets the user currently logged in."}
  (session/get :username))

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

(defn login!
  [username password]
  ^{:doc "Logs user into session"}
  ; Redundant valid-credential checking.
  (if (valid-credentials? username password)
      (session/put! :username username)))

(defn add-user?
  [username password]
  ^{:doc "Adds new user to the database"}
  ; TODO encrypt passwords
    (mgcol/insert "users" {:username username :password password})
    true)

(defn add-post!
  [title body]
  ^{:doc "Adds a new post to the database."}
  (def year (.format (java.text.SimpleDateFormat. "yyyy") (now)))
  (def month (.format (java.text.SimpleDateFormat. "M") (now)))

  (mgcol/insert "posts" {:title title
                         :body body 
                         :username (current-user)
                         :timestamp (now)
                         :year year
                         :month month 
                         :slug (gen-slug title)}))

(defn get-pages
  [page_num length]
  ^{:doc "Returns a blog listing."}
  (mq/with-collection "posts"
                   (mq/find {})
                   (mq/fields [:title :body :username :timestamp])
                   (mq/paginate :page page_num :per-page length)))

(defn get-post
  [year month slug]
  (mgcol/find "posts" {:year year :month month :slug slug}))


(defn post-permalink
  [year month slug]
  ^{:doc "Generates permalink for current page."}
  (cljstr/join "/" [PAGE_ROOT year month slug]))

(defn add-comment!
  [id title name email body]
  (mgcol/update "posts" {:_id id} 
                {:comments {:title title
                            :name name
                            :email email
                            :body body
                            :timestamp (now)}}))
