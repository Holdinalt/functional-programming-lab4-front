(ns front
  (:require
   [reagent.core :as reagent]
   [reagent.dom :as rd]
   [clojure.edn :as edn]
   [ajax.core :refer [GET POST]]
   [markdown.core :refer [md->html]]))

(enable-console-print!)

(def serverUrl "http://localhost:3000/")
(def postNumber 0)

(def comms (reagent/atom []))
(def post (reagent/atom []))

(def log (.-log js/console))

(defn handle-comments-change [new-comm]
  (let [sorted (sort #(< (:id %1) (:id %2)) (edn/read-string new-comm))]
    (reset! comms sorted))
  (log (str "fetched comms " @comms)))

(defn get-comments []
  (GET (str serverUrl "api/comments/get/" postNumber)
    {:handler handle-comments-change}
    {:error println}))

(defn handle-post-change [new-post]
  (reset! post (edn/read-string new-post))
  (log (str "fetched post " @post)))

(defn get-post []
  (GET (str serverUrl "api/posts/get/" postNumber)
    {:handler handle-post-change}
    {:error println}))

(defn update-data []
  (get-post)
  (get-comments))

(defn sendForm [event]
  (.preventDefault event)
  (.persist event)
  (log event)
  (let [target (.-target event)
        name (.-value (first target))
        text (.-value (second target))
        image (aget (.-files (aget target 2)) 0)]
    (.reset target)

    (let [form-data (doto
                     (js/FormData.)
                      (.append "name" name)
                      (.append "text" text))
          form-data (if image (doto form-data (.append "image" image "image.jpg"))  form-data)]
      (POST
        (str serverUrl "api/comments/add")
        ;{:body (doto form-data (.append "image" image "image.jpg"))
        {:body form-data
         :handler update-data
         :error println
         :timeout 100}))

    (.-reset target)))

(defn format-date [time]
  (.toLocaleString (js/Date. time)))

(defn format-id [id]
  (when id (str ">> " (.toLocaleString id "en-US" #js {:minimumIntegerDigits 8 :useGrouping false}))))

(defn thread-button [text onclick]
  [:p.threadButton {:onClick onclick}
   "["
   [:a.threadButtonLink text]
   "]"])

(defn thread-controls []
  [:div.threadControls
   (thread-button "Bottom" #())
   (thread-button "Update" update-data)])

(defn comment-title [title author date id]
  [:div.commentTitle
   (when title [:span.commentTitleTitle title])
   [:span.commentTitleAuthor author]
   [:span.commentTitleDate (format-date date)]
   [:span.commentTitleId (format-id id)]])

(defn thread-post-main [imageUrl title author date id text]
  [:div.threadPost
   (when imageUrl [:img.threadPostImg {:src (str serverUrl "api/file/" imageUrl)}])
   [:div.threadPostBody
    (comment-title title author date id)
    [:p {:dangerouslySetInnerHTML {:__html (md->html text)}}]]])

(defn thread-post-comment [imageUrl author date id text]
  [:div.threadComment {:style {:background-color "#d6daf0" :margin-left "10px"}}
   [:div.threadPostBody
    (comment-title nil author date id)
    [:p {:dangerouslySetInnerHTML {:__html (md->html text)}}]]
   (when imageUrl [:img.threadCommentImg {:src (str serverUrl "api/file/" imageUrl)}])])

(defn add-comment []
  [:div
   ;[:form.addComment {:method "post" :id "form1" :action (str serverUrl "api/comments/add") :enc-type "multipart/form-data"}
   [:form.addComment {:id "form1" :enc-type "multipart/form-data" :onSubmit sendForm}
    [:input#name {:name "name"}]
    [:textarea#text {:name "text"}]
    [:input#image {:type "file" :accept "image/png, image/jpeg" :name "image"}]
    [:button {:type "submit" :form "form1"} "Отправить"]]])

(defn thread []
  [:div
   [thread-controls]

   (let
    [author-name (:author-name @post)
     id (:id @post)
     image (:image @post)
     text (:text @post)
     time (:time @post)
     title (:title @post)]
     [thread-post-main image title author-name time id text])

   (for [comment @comms]
     (let [author-name (:author-name comment)
           image (:image comment)
           time (:time comment)
           text (:text comment)
           id (:id comment)]
       [thread-post-comment image author-name time id text]))

   [add-comment]])

(defn header []
  [:div.header
   [:div "9Chan Logo"]
   [:div.topTittle "/a/ - Anime & Manga"]
   ;[:img {:src (str serverUrl "api/file/screen.jpg") :style {:width 300 :height 200}}]
   ])
(rd/render
 [:div#main-container.main-container
  [header]
  [thread]]

 (. js/document (getElementById "app")))

(update-data)

(defn on-js-reload [])