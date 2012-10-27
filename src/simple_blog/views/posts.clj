(ns simple-blog.views.posts
  (:require [simple-blog.views.common :as common])
  (:use [noir.core :only [defpage defpartial]]
        [hiccup.core]
        [hiccup.page]
        [hiccup.form]
        [simple-blog.models.post]))

(defpage [:post "/user/login"] {:keys [username password]}
         (login! username password)
         (html5
           (str "login success")))

(defpage [:post "/user/new" ] {:keys [username password verify]}
         (if (and (= verify password)
                  (add-user? username password))
           (str "Hooray!")
           (str "Invalid password.")))

(defpartial userLogin
            [{:keys [username password]}]
            (label "username" "Username: ")
            (text-field "username" username)
            (label "password" "Password: ")
            (text-field "password" password))

(defpartial userAdd
            [{:keys [username password verify]}]
            (userLogin [username password])
            (label "verify" "Verify Password: ")
            (text-field "verify" password))

(defpartial userLoginForm
            [user]
            (form-to [:post "/user/login"]
                     (userLogin user)
                     (submit-button "Login")))

(defpartial userAddForm
            [user]
            (form-to [:post "/user/new"]
                     (userAdd user)
                     (submit-button "Create account")))

(defpage [:get "/user/login"] {:as user}
         (userLoginForm user))

(defpage [:get "/user/new"] {:as user}
         (userAddForm user))
