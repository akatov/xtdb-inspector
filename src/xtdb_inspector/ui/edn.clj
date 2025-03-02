(ns xtdb-inspector.ui.edn
  "Pretty HTML rendering of arbitrary EDN."
  (:require [ripley.html :as h]))

(defmulti render (fn [_ctx item] (type item)))

(defmethod render :default [_ item]
  (let [str (pr-str item)]
    (h/html [:span str])))

(defmethod render java.lang.String [_ctx str]
  (h/html [:span.text-lime-500 "\"" str "\""]))

(defmethod render java.lang.Number [_ctx num]
  (let [str (pr-str num)]
    (h/html [:span.text-red-300 str])))

(defmethod render clojure.lang.Keyword [_ctx kw]
  (let [str (pr-str kw)]
    (h/html [:span.text-emerald-700 str])))

(defmethod render clojure.lang.PersistentVector [ctx vec]
  (let [dir (if (every? map? vec)
              "flex-col"
              "flex-row")]
    (h/html
     [:div.flex
      "["
      [:div.inline-flex.space-x-2 {:class dir}
       [::h/for [v vec]
        [:div.inline-block (render ctx v)]]]
      "]"])))

;; Need to style table cells by hand, otherwise this will inherit
;; styling from table that contains it
(def key-cell-style "padding: 0 1em 0 0;")
(def val-cell-style "padding: 0;")

(defmethod render clojure.lang.IPersistentMap [ctx m]
  (let [entries (seq m)
        normal-entries (butlast entries)
        last-entry (last entries)]
    (h/html
     [:div.inline-block.flex
      "{"
      [:table
       [::h/for [[key val] normal-entries]
        [:tr.whitespace-pre
         [:td.align-top {:style key-cell-style}
          (render ctx key)]
         [:td.align-top {:style val-cell-style} (render ctx val)]]]
       [:tr.whitespace-pre
        [:td.align-top {:style key-cell-style} (render ctx (key last-entry))]
        [:td.align-top {:style val-cell-style}
         [:div.inline-block (render ctx (val last-entry))] "}"]]]])))

(defn edn [thing]
  (render {} thing))
