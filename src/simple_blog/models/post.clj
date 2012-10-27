(ns simple-blog.models.post
  (:use [monger.collection :as mgcol])
  (:require [noir.session :as session]))

(defn get-user!
  [username]
  (mgcol/find-maps "users" {:username username} ()))

(defn user-exists?
  [username]
  "Checks if user exists in the database"
  (not (= (get-user! username) ())))

(defn valid-credentials?
  [username password]
  (if (and
        ;Does the user exist?
        (user-exists? username)
        ;Is the password valid?
        ;TODO encrypt passwords
        (= ((get-user! username) :password) password))
    (true)))

(defn login! 
  "Logs user into session"
  [username password]
  ; This represents the user
  (if (valid-credentials? username password)
    (do
      (session/put! username))
    ;else
    (str password username)))
