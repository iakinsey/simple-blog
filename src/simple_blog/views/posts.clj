(ns simple-blog.views.posts
  (:require [simple-blog.views.common :as common])
  (:use [noir.core :only [defpage defpartial render]]
        [noir.response :as resp]
        [hiccup.core]
        [hiccup.page]
        [hiccup.form]
        [simple-blog.models.post]))

(defpage [:get "/user/new/failure"] []
         (html5
           [:p "Failed to create a new user."]))

(defpage [:get "/user/new/success"] []
         (html5
           [:p "User created successfully."]))

(defpage [:get "/user/login/success"] []
         (html5
           [:p "Login success."]))

(defpage [:get "/user/login/failure"] []
         (html5
           [:p "Login failure."]))

(defpage [:post "/user/login"] {:keys [username password]}
         (login! username password)
         (resp/redirect "/user/login/success"))

(defpage [:post "/user/new"] {:keys [username password verify]}
         (if (and (= verify password)
                  (user-available? username))
           (do
            (add-user? username password)
            (resp/redirect "/user/new/success"))
           (resp/redirect "/user/new/failure")))

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
