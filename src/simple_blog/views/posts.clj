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
; Globals
;==============================================================================

(def
  ^{:doc "Number of blog posts to display per each page"}
  PER_PAGE 10)

;==============================================================================
; Form structures
;
;   User Login
;   User Creation
;==============================================================================

;------------------------------------------------------------------------------
; User Auth
;------------------------------------------------------------------------------

(defpartial userLogin [{:keys [username password]}]
            ^{:doc "Structure for a user login form."}
            (label "username" "Username: ")
            (text-field "username" username)
            (label "password" "Password: ")
            (text-field "password" password))

(defpartial userAdd [{:keys [username password verify]}]
            ^{:doc "Structure for an add user form."}
            (userLogin [username password])
            (label "verify" "Verify Password: ")
            (text-field "verify" password))

(defpartial postAdd [{:keys [title body]}]
            ^{:doc "Structure for an add post form."}
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


(defpartial blogPost
            [object]
            ^{:doc "User login page"}
            (html5
              [:p (object :title)]
              [:p (object :body)]
              [:p (object :username)]
              [:p (object :timestamp)]
              [:br]))

;------------------------------------------------------------------------------
;   User Login
;   User Creation
;------------------------------------------------------------------------------

(defpartial userLoginForm
            ^{:doc "Complete html user login form."}
            [user]
            (form-to [:post "/user/login"]
                     (userLogin user)
                     (submit-button "Login")))

(defpartial userAddForm
            ^{:doc "Complete html user add form."}
            [user]
            (form-to [:post "/user/new"]
                     (userAdd user)
                     (submit-button "Create account")))

(defpartial postAddForm
            ^{:doc "Complete html post add form."}
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

(defn paginated_view
  ^{:doc "Paginated list of blog posts"}
  [page_num page_length]
  (map blogPost (get-pages page_num page_length)))

(defn notify
  ^{:doc "Gives the user a notification."}
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
         ^{:doc "User login page"}
         (userLoginForm user))

(defpage [:get "/user/new"] {:as user}
         ^{:doc "New user page"}
         (userAddForm user))

(defpage [:get "/post/new"] {:as user}
         ^{:doc "New post page"}
         (postAddForm user))

;------------------------------------------------------------------------------
; Listings
;
;   Blog Articles
;   Comments
;------------------------------------------------------------------------------


(defpage [:get "/"] []
         ^{:doc "Main page."}
         (paginated_view 1 PER_PAGE))

(defpage [:get "/page/:page_num"] {:keys [page_num]}
         ^{:doc "Blog post listing."}
         (paginated_view page_num PER_PAGE))

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
         ^{:doc "Logs the user in."}
           (do
            (login! username password)
            (notify "Login success."))
           (notify "Login failed!")))

(defpage [:post "/user/new"] {:keys [username password verify]}
         (if (and (= verify password)
         ^{:doc "Adds a new user."}
                  (user-available? username))
           (do
            (add-user? username password)
            (notify "User created successfully."))
          (notify "Failed to create a new user.")))

(defpage [:post "/post/new"] {:keys [title body]}
         ^{:doc "Adds a new post."}
         (do
          (add-post! title body)
           (notify "New post created successfully.")))
