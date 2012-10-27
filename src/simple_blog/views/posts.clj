(ns simple-blog.views.posts
  (:require [simple-blog.views.common :as common])
  (:use [noir.core :only [defpage defpartial render]]
        [noir.response :as resp]
        [hiccup.core]
        [hiccup.page]
        [hiccup.form]
        [simple-blog.models.post]))

;==============================================================================
; Form structures
;
;   User Login
;   User Creation
;==============================================================================
;
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

;==============================================================================
; Partial forms
; 
;   User Login
;   User Creation
;==============================================================================

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

;==============================================================================
;Pages
;==============================================================================

;------------------------------------------------------------------------------
; Success/failure messages
;
;   User Login
;   User Creation
;------------------------------------------------------------------------------

(defn notify
  [message]
  (html5
    [:p message]))

;------------------------------------------------------------------------------
; Interactive pages (forms)
; 
;   User Login
;   User Creation
;------------------------------------------------------------------------------

(defpage [:get "/user/login"] {:as user}
         (userLoginForm user))

(defpage [:get "/user/new"] {:as user}
         (userAddForm user))

;==============================================================================
; POST functions
;==============================================================================

;------------------------------------------------------------------------------
; Auth
;
;   User Login
;   User Creation
;------------------------------------------------------------------------------

(defpage [:post "/user/login"] {:keys [username password]}
         (login! username password)
         (notify "Login success."))

(defpage [:post "/user/new"] {:keys [username password verify]}
         (if (and (= verify password)
                  (user-available? username))
           (do
            (add-user? username password)
            (notify "User created successfully."))
          (notify "Failed to create a new user.")))
