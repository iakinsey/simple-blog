(ns simple-blog.views.posts
  (:require [simple-blog.views.common :as common])
  (:use [noir.core :only [defpage defpartial]]
        [hiccup.core]
        [hiccup.page]
        [hiccup.form]
        [simple-blog.models.post]))

(defpage [:post "/login"] {:keys [username password]}
         (html5
           (login! username password)))

(defpartial userLogin
            [{:keys [username password]}]
            (label "username" "Username: ")
            (text-field "username" username)
            (label "password" "Password: ")
            (text-field "password" password))

(defpartial userAddForm
            [{:keys [username password verify]}]
            (userLogin)
            (label "verify" "Verify Password: ")
            (text-field "verify" password))

(defpartial userLoginForm
            [user]
            (form-to [:post "/login"]
                     (userLogin user)
                     (submit-button "Login")))

(defpage [:get "/user/login"] {:as user}
         (userLoginForm user))

(defpage [:get "/user/new"] {:as user}
         (userAddForm user))
