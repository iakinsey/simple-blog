(ns simple-blog.views.posts
  (:require [simple-blog.views.common :as common]
            [noir.session :as session])
  (:use [noir.core :only [defpage defpartial render]]
        [noir.response :as resp :exclude [empty]]
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

;------------------------------------------------------------------------------
; User Auth
;------------------------------------------------------------------------------

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

(defpartial postAdd
            [{:keys [title body]}]
            (label "title" "Title: ")
            (text-field "title" title)
            (label "body" "Body: ")
            (text-area "body" body))

;==============================================================================
; Partials
;==============================================================================

;------------------------------------------------------------------------------
; Blog posts
;------------------------------------------------------------------------------

(defpartial blogPosts
            [title body username timestamp]
            (html5
              [:p title]
              [:p username]
              [:p timestamp]
              [:p body]))

;------------------------------------------------------------------------------
;   User Login
;   User Creation
;------------------------------------------------------------------------------

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

(defpartial postAddForm
            [user]
            (form-to [:post "/post/new"]
                     (postAdd user)
                     (submit-button "Add post")))

;==============================================================================
;Pages
;==============================================================================

;------------------------------------------------------------------------------
; Success/failure messages
;
;   Generic notifications.
;------------------------------------------------------------------------------

(defn notify
  "Gives the user a notification"
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

(defpage [:get "/post/new"] {:as user}
         (postAddForm user))

;------------------------------------------------------------------------------
; Listings
;
;   Blog Articles
;   Comments
;------------------------------------------------------------------------------

(defpage [:get "/"] [])

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
         (if (valid-credentials? username password)
           (do
            (login! username password)
            (notify "Login success."))
           (notify "Login failed!")))

(defpage [:post "/user/new"] {:keys [username password verify]}
         (if (and (= verify password)
                  (user-available? username))
           (do
            (add-user? username password)
            (notify "User created successfully."))
          (notify "Failed to create a new user.")))

(defpage [:post "/post/new"] {:keys [title body]}
         (do
          (add-post! title body)
           (notify "New post created successfully.")))
