;==============================================================================
; Namespace and imports
;==============================================================================

(ns simple-blog.views.posts
  (:require [simple-blog.views.common :as common]
            [noir.session :as session])
  (:use [noir.core :only [defpage defpartial render]]
        [noir.response :as resp :exclude [empty]]
        [hiccup.core]
        [hiccup.page]
        [hiccup.form]
        [hiccup.element]
        [simple-blog.models.post]))

;==============================================================================
; Globals
;==============================================================================

(def
  ^{:doc "Number of blog posts to display per each page"}
  PER_PAGE 10)

;==============================================================================
; Form structures
;==============================================================================

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

(defpartial commentAdd [{:keys [id title name email body]}]
            ^{:doc "Structure for a newcomment form."}
            (label "title" "Title :")
            (text-field "title" title)
            (label "name" "Name: ")
            (text-field "name" name)
            (label "email" "Email: ")
            (text-field "email" email)
            (label "body" "Body: ")
            (text-area "body" body)
            (hidden-field "id" id))

;==============================================================================
; Partials
;==============================================================================

;------------------------------------------------------------------------------
; Pages
;------------------------------------------------------------------------------

(defpartial render-post
            [object]
            ^{:doc "Renders blog post."}
            (html5
              [:p ((object :slug) :year)
                  ((object :slug) :month)
                  ((object :slug) :slug)]
              [:p (object :title)]
              [:p (object :body)]
              [:p (object :username)]
              [:p (object :timestamp)]
              [:p (str object)]
              [:br]))

(defpartial render-comment
            [object]
            ^{:doc "Renders a single comment"}
            (html5
              [:p  (object :title)]
              [:p "By: " (object :name)]
              [:p "At: " (object :timestamp)]
              [:p (object :body)]))

(defpartial render-comments
            [object]
            ^{:doc "Renders comments for blog post."}
            (map render-comment (object :comments)))

;------------------------------------------------------------------------------
; Listings
;------------------------------------------------------------------------------

(defn paginated_view
  [page_num page_length]
  ^{:doc "Paginated list of blog posts"}
  (map render-post (get-pages page_num page_length)))

;------------------------------------------------------------------------------
; Forms
;------------------------------------------------------------------------------

(defpartial userLoginForm
            [user]
            ^{:doc "Complete html user login form."}
            (form-to [:post "/user/login"]
                     (userLogin user)
                     (submit-button "Login")))

(defpartial userAddForm
            [user]
            ^{:doc "Complete html user add form."}
            (form-to [:post "/user/new"]
                     (userAdd user)
                     (submit-button "Create account")))

(defpartial postAddForm
            [user]
            ^{:doc "Complete html post add form."}
            (form-to [:post "/post/new"]
                     (postAdd user)
                     (submit-button "Add post")))

(defpartial commentAddForm 
            [object id]
            ^{:doc "Complete new comment form."}
            (form-to [:post "/comment/new"]
                     (commentAdd object)
                     (submit-button "Add post")))

;==============================================================================
; Pages
;==============================================================================

;------------------------------------------------------------------------------
; Notifications (deprecate soon)
;------------------------------------------------------------------------------

(defn notify
  [message]
  ^{:doc "Gives the user a notification."}
  (html5
    [:p message]))

;------------------------------------------------------------------------------
; Forms
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
;------------------------------------------------------------------------------


(defpage [:get "/"] []
         ^{:doc "Main page."}
         (paginated_view 1 PER_PAGE))

(defpage [:get "/page/:page_num"] {:keys [page_num]}
         ^{:doc "Blog post listing."}
         (paginated_view page_num PER_PAGE))

(defpage [:get "/:year/:month/:slug"] {:keys [year month slug]}
         ^{:doc "Individual blog post"}
         (let [post_object (get-post year month slug)]
           ((juxt (render-post)
                  (commentAddForm)
                  (render-comments))
              post_object)))
         
;==============================================================================
; POST functions
;==============================================================================

;------------------------------------------------------------------------------
; Auth
;------------------------------------------------------------------------------

(defpage [:post "/user/login"] {:keys [username password]}
         ^{:doc "Logs user in."}
         (if (valid-credentials? username password)
         ^{:doc "Logs the user in."}
           (do
            (login! username password)
            (notify "Login success."))
           (notify "Login failed!")))

(defpage [:post "/user/new"] {:keys [username password verify]}
         ^{:doc "Adds a new user."}
         (if (and (= verify password)
                  (user-available? username))
           (do
            (add-user? username password)
            (notify "User created successfully."))
          (notify "Failed to create a new user.")))

;------------------------------------------------------------------------------
; Posts
;------------------------------------------------------------------------------

(defpage [:post "/post/new"] {:keys [title body]}
         ^{:doc "Adds a new post."}
         (do
          (add-post! title body)
          (notify "New post created successfully.")))

;------------------------------------------------------------------------------
; Comments
;------------------------------------------------------------------------------

(defpage [:post "/:year/:month/:slug/comment/add"]
         {:keys [id title name email body]}
         (do
           (add-comment! id title name email body)
           (notify "Comment created successfully.")))
