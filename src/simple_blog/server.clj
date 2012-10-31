(ns simple-blog.server
  (:use [monger.core :only [connect! set-db! get-db]])
  (:require [noir.server :as server]))

(def DATABASE_NAME "test")

(server/load-views-ns 'simple-blog.views)

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'simple-blog})

    ;Connect to specified mongo database under DATABASE_NAME
    (if (do
          (connect!)
          (set-db! (get-db DATABASE_NAME)))
      (printf (str "Running mongodb on database " DATABASE_NAME ".")))))
